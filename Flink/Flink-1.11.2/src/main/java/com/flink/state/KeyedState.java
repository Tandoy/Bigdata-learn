package com.flink.state;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;

/**
 * 键控状态，针对每个key，状态之间互相隔离
 */
public class KeyedState {
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

        // 键控状态 valueState ListState MapState ReduceStete
        SingleOutputStreamOperator<Integer> outputStreamOperator = mapOperator.keyBy("uid")
                .map(new MyKeyCountMapFunction());
        outputStreamOperator.print();
        env.execute();
    }
    public static class MyKeyCountMapFunction extends RichMapFunction<SensorReading,Integer>{
        private ValueState<Integer> valueState;

        // other keyedState
        private ListState<Integer> listState;
        private MapState<String,Double> mapState;
        @Override
        public void open(Configuration parameters) throws Exception {
            valueState = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("value-state",Integer.class,0));
            listState = getRuntimeContext().getListState(new ListStateDescriptor<Integer>("list-state", Integer.class));
            mapState = getRuntimeContext().getMapState(new MapStateDescriptor<String, Double>("map-state",String.class,Double.class));
        }

        @Override
        public Integer map(SensorReading sensorReading) throws Exception {
            // get the current value
            Integer count = valueState.value();
            // do the update value
            count ++;
            // do the state value
            valueState.update(count);
            return count;
        }
    }
}
