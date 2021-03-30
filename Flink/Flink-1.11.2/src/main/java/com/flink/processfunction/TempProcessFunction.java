package com.flink.processfunction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Collector;

import java.util.Properties;

/**
 * 监控温度传感器的温度值，如果温度值在 10 秒钟之内(processing time)
 * 连续上升，则报警
 */
public class TempProcessFunction {
    public static void main(String[] args) throws Exception {
        // 从kafka中读取topic数据
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // kafka 配置项
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        DataStreamSource<String> streamSource = env.addSource(new FlinkKafkaConsumer011<String>("GMALL_STARTUP", new SimpleStringSchema(), properties));
        // 进行相关ETL
        // 将kafka数据转换成SensorReading类型
        // 先过滤到不符规范的json数据
        SingleOutputStreamOperator<String> filter = streamSource.filter(new FilterFunction<String>() {
            public boolean filter(String s) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(s);
                    return  true;
                }catch (Exception e){
                    System.out.println("此次json不符合规范");
                }
                return  false;
            }
        });
        // 这里使用fastjson解析flink流式处理拿到的json数据
        DataStream<SensorReading> mapOperator = filter.map(new MapFunction<String, SensorReading>() {
            public SensorReading map(String s) throws Exception {
                JSONObject parseObject = JSON.parseObject(s);
                return  new SensorReading(parseObject.getString("area"),parseObject.getString("uid"),
                        parseObject.getString("os"),parseObject.getString("ch"),parseObject.getString("appid")
                        ,parseObject.getString("mid"),parseObject.getString("type"),parseObject.getString("vs")
                        ,parseObject.getString("ts"));
            }
        });

        //10s内
        mapOperator.keyBy("uid")
                .process(new TempProcess(10))
                .print();
        env.execute();
    }
    public static class TempProcess extends KeyedProcessFunction<Tuple,SensorReading,String>{
        // 定义检测时间间隔
        private Integer interval;

        public TempProcess(Integer interval) {
            this.interval = interval;
        }

        // 定义上次登陆时间状态
        ValueState<Long> tsState;
        // 定义定时器当前时间戳状态
        ValueState<Long> timerTsState;

        @Override
        public void open(Configuration parameters) throws Exception {
            tsState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("ts-State",Long.class,Long.MIN_VALUE));
            timerTsState = getRuntimeContext().getState(new ValueStateDescriptor<Long>("timer-TsState",Long.class));
        }

        @Override
        public void processElement(SensorReading sensorReading, Context context, Collector<String> collector) throws Exception {
            // 1.取出状态
            Long ts = tsState.value();
            Long timer = timerTsState.value();

            // 2.更新登陆时间状态
            tsState.update(Long.getLong(sensorReading.getTs()));

            // 3.判断是否登陆时间上升
            if (Long.getLong(sensorReading.getTs()) > ts && timer == null){
                // 注册定时器、定时器状态进行更新
                context.timerService().registerProcessingTimeTimer(context.timerService().currentProcessingTime() + interval * 10000L);
                timerTsState.update(context.timerService().currentProcessingTime() + interval * 10000L);
            }else if (Long.getLong(sensorReading.getTs()) < ts && timer != null){
                context.timerService().deleteProcessingTimeTimer(timer);
                // 登陆时间没有上升，删除定时器以及当前定时器时间戳状态清除
                timerTsState.clear();
            }
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            // 定时器触发处理逻辑
            out.collect("uid" + ctx.getCurrentKey().<String>getField(0));

            // 清理timer状态
            timerTsState.clear();
        }

        @Override
        public void close() throws Exception {
            tsState.clear();
        }
    }
}
