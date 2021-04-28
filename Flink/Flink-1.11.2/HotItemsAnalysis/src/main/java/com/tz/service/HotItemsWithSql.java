package com.tz.service;

import com.tz.beans.UserBehavior;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Slide;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.Properties;

public class HotItemsWithSql {
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

        // 4.创建table执行环境
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(environment, settings);

        // 5.将流转换成表
        Table table = tableEnvironment.fromDataStream(userBehaviorSingleOutputStreamOperator,"itemId,behavior,timestamp.rowtime as ts");

        // 6.tableAPI 计算操作
        Table table1 = table.filter("behavior = 'pv'") // 过滤出pv操作records
                .window(Slide.over("1.hours").every("5.minutes").on("ts").as("w"))
                .groupBy("itemId,w")
                .select("itemId, w.end as windowEnd, itemId.count as cnt");

        // 7.利用SQL开窗函数进行Top N
        DataStream<Row> rowDataStream = tableEnvironment.toAppendStream(table, Row.class);
        tableEnvironment.createTemporaryView("agg",rowDataStream,"itemId,behavior,ts");
                // 纯 sql 实现
        tableEnvironment.createTemporaryView("data_table", userBehaviorSingleOutputStreamOperator, "itemId, behavior, timestamp.rowtime as ts");
        Table resultSqlTable = tableEnvironment.sqlQuery("select * from " +
                "  ( select *, ROW_NUMBER() over (partition by windowEnd order by cnt desc) as row_num " +
                "  from ( " +
                "    select itemId, count(itemId) as cnt, HOP_END(ts, interval '5' minute, interval '1' hour) as windowEnd " +
                "    from data_table " +
                "    where behavior = 'pv' " +
                "    group by itemId, HOP(ts, interval '5' minute, interval '1' hour)" +
                "    )" +
                "  ) " +
                " where row_num <= 5 ");
//        Table resultSqlTable = tableEnvironment.sqlQuery("select * from (select *,ROW_NUMBER() over (partition by windowEnd order by cnt desc) as rn from agg)" +
//                "where rn <= 5");
        tableEnvironment.toRetractStream(resultSqlTable,Row.class).print();

        environment.execute("hot items with sql");
    }
}
