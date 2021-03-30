package com.flink.state;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 算子状态 对key无影响
 */
public class OperatorState {
    public static void main(String[] args) throws Exception {
        // 从kafka中读取topic数据
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置状态后端    其实就是checkpoint的容错方式
        env.setStateBackend(new MemoryStateBackend());
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

        // 设置算子状态
        SingleOutputStreamOperator<Integer> map = mapOperator.map(new MyCountMapFunction());
        map.print();
        env.execute();
    }
    public static class MyCountMapFunction implements MapFunction<SensorReading,Integer>, ListCheckpointed<Integer> {
        // 定义本地变量，进行count统计
        private Integer count = 0;
        @Override
        public Integer map(SensorReading sensorReading) throws Exception {
//            count ++;
//            return count;
            return ++ count;
        }

        //发生故障时对状态的处理
        @Override
        public List<Integer> snapshotState(long l, long l1) throws Exception {
            return Collections.singletonList(count);
        }
        //恢复后对状态的处理操作
        @Override
        public void restoreState(List<Integer> list) throws Exception {
            // 由于是列表状态，所以对其进行遍历求和
            for (Integer num:list){
                count += num;
            }
        }
    }
}
