package com.flink.state;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Collector;

import java.util.Properties;

/**
 * 状态编程_阈值
 */
public class KeyState_Case {
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

        // 当前后两条数据时间差超过一定阈值即触发报警机制
        SingleOutputStreamOperator<Tuple3<String, Double, Double>> tuple3SingleOutputStreamOperator = mapOperator.keyBy("uid")
                .flatMap(new MythredTemputer(10.0));
        tuple3SingleOutputStreamOperator.print();
        env.execute();
    }
    public static class MythredTemputer extends RichFlatMapFunction<SensorReading, Tuple3<String,Double,Double>>{
        // 定义阈值
        private Double re = 10.0;
        // 定义状态
        private ValueState<Double> valueState;
        public MythredTemputer(Double re) {
            this.re = re;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            valueState = getRuntimeContext().getState(new ValueStateDescriptor<Double>("re", Double.class));
        }

        @Override
        public void flatMap(SensorReading sensorReading, Collector<Tuple3<String, Double, Double>> collector) throws Exception {
         // 由于在创建状态时没有设置初始值,需要进null判断
            Double value = valueState.value();
            if (value != null){
                // 计算差值
                double tempuer = Math.abs(value - Double.valueOf(sensorReading.getTs()));
                if (tempuer > 10){
                    collector.collect(new Tuple3<String, Double, Double>(sensorReading.getUid(),value,Double.valueOf(sensorReading.getTs())));
                }
            }
            // 状态更新
            valueState.update(Double.valueOf(sensorReading.getTs()));
        }

        @Override
        public void close() throws Exception {
            // 清空状态
            valueState.clear();
        }
    }
}
