package com.tz.service;


import com.tz.beans.ItemViewCount;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import com.tz.beans.UserBehavior;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;
import java.util.regex.Pattern;


/**
 * 实时热门商品”的需求:
 * 每隔 5 分钟输出最近一小时内点击量最多的前 N 个商品
 */
public class HotItems {
    public static void main(String[] args) throws Exception {
        // 1.创建流执行环境
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setParallelism(1); //并行度为1
        environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime); // flink默认为处理时间，这里设置为事件时间
        // 2.读取csv文件
//        DataStreamSource<String> streamSource = environment.readTextFile("D:\\Downloads\\github\\Bigdata-learn\\Flink\\Flink-1.11.2\\HotItemsAnalysis\\src\\main\\resources\\UserBehavior.csv");
        // 2.读取kafka数据源
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        properties.setProperty("auto.offset.reset", "earliest");
        // topic 和 partition 动态发现
        // 自动发现消费的partition变化
//        properties.setProperty("flink.partition-discovery.interval-millis", String.valueOf((10 * 1000)));
//        Pattern topicPattern = Pattern.compile("hotitems[0-9]");
        DataStreamSource<String> streamSource = environment.addSource(new FlinkKafkaConsumer<String>("hotitems", new SimpleStringSchema(), properties));
        // 3.计算逻辑处理：
        UserBehavior userBehavior = new UserBehavior();
            // 3.1 csv数据封装成POJO类型
        SingleOutputStreamOperator<UserBehavior> mapOperator = streamSource.map(new MapFunction<String, UserBehavior>() {
            @Override
            public UserBehavior map(String record) throws Exception {
                String[] fields = record.split(",");
                userBehavior.setUserId(new Long(fields[0]));
                userBehavior.setItemId(new Long(fields[1]));
                userBehavior.setCategoryId(new Integer(fields[2]));
                userBehavior.setBehavior(fields[3]);
                userBehavior.setTimestamp(new Long(fields[4]));
                return userBehavior;
            }
        });
            // 3.2 业务时间为事件时间（设置watermark）
        SingleOutputStreamOperator<UserBehavior> userBehaviorSingleOutputStreamOperator = mapOperator.assignTimestampsAndWatermarks(new AscendingTimestampExtractor<UserBehavior>() {
            @Override
            public long extractAscendingTimestamp(UserBehavior userBehavior) {
                return userBehavior.getTimestamp() * 1000L;
            }
        });
            // 3.3 过滤出点击行为数据
        SingleOutputStreamOperator<UserBehavior> filterOperator = userBehaviorSingleOutputStreamOperator.filter(new FilterFunction<UserBehavior>() {
            @Override
            public boolean filter(UserBehavior userBehavior) throws Exception {
                if ("pv".equals(userBehavior.getBehavior())) {
                    return true;
                } else {
                    return false;
                }
            }
        });
             // 3.4 创建窗口1小时，固定步长为5min的滑动窗口
        SingleOutputStreamOperator<ItemViewCount> aggregateWindowOperator= filterOperator.keyBy("itemId")
                .timeWindow(Time.hours(1), Time.minutes(5))
                .aggregate(new CountAgg(), new WindowResultFunction());//使用增量聚合函数，缓解state的压力/apply
            // 3.5 按每个窗口聚合，输出每个窗口中点击量前 N 名的商品
        aggregateWindowOperator.keyBy("windowEnd")
                .process(new TopNHotItems(5))
                .print();
        // 4.关闭执行环境
        environment.execute("hot items");
    }

    //  自定义预聚合函数类，每来一个数据就 count 加 1
    public static class CountAgg implements AggregateFunction<UserBehavior,Long,Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(UserBehavior userBehavior, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long accumulator1, Long accumulator2) {
            return accumulator1 + accumulator2;
        }
    }

    // 自定义窗口函数，结合窗口信息，输出当前 count 结果
    public static class WindowResultFunction implements WindowFunction<Long, ItemViewCount, Tuple, TimeWindow> {

        @Override
        public void apply(Tuple tuple, TimeWindow timeWindow, Iterable<Long> iterable, Collector<ItemViewCount> collector) throws Exception {
            Long itemId = tuple.getField(0);
            Long windowEnd = timeWindow.getEnd();
            Long count = iterable.iterator().next();
            collector.collect( new ItemViewCount(itemId, windowEnd, count) );
        }
    }

    // 自定义求TopN
    public static class TopNHotItems extends KeyedProcessFunction<Tuple,ItemViewCount,String> {
            private Integer topSize; // Top N
            public TopNHotItems(Integer topSize) {
                this.topSize = topSize;
            }

            // 定义ListState
            ListState<ItemViewCount> itemViewCountListState;

        @Override
        public void open(Configuration parameters) throws Exception {
           itemViewCountListState = getRuntimeContext().getListState(new ListStateDescriptor<ItemViewCount>("item-count-list", ItemViewCount.class));
        }
        @Override
        public void processElement(ItemViewCount itemViewCount, Context context, Collector<String> collector) throws Exception {
            // 将统计值放入状态后端
            itemViewCountListState.add(itemViewCount);
            context.timerService().registerEventTimeTimer(itemViewCount.getWindowEnd() + 1);
        }

        // 定时器
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            // 获取状态后端的值
            ArrayList<ItemViewCount> itemViewCounts = Lists.newArrayList(itemViewCountListState.get().iterator());
            // 排序
            itemViewCounts.sort(new Comparator<ItemViewCount>() {
                @Override
                public int compare(ItemViewCount o1, ItemViewCount o2) {
                    return o2.getCount().intValue() - o1.getCount().intValue();
                }
            });
            // 将排名信息格式化成 String
            StringBuilder result = new StringBuilder();
            result.append("====================================\n");
            result.append(" 窗口结束时间: ").append(new Timestamp(timestamp - 1)).append("\n");

            for (int i = 0; i < Math.min(topSize,itemViewCounts.size()) ; i++) {
                ItemViewCount currentItemViewCount = itemViewCounts.get(i);
                result.append("No").append(i+1).append(":")
                        .append(" 商品 ID=")
                        .append(currentItemViewCount.getItemId())
                        .append(" 浏览量=")
                        .append(currentItemViewCount.getCount())
                        .append("\n");
            }
            result.append("====================================\n\n");
            // 控制输出频率，模拟实时滚动结果
            Thread.sleep(1000);
            out.collect(result.toString());
        }
    }
}
