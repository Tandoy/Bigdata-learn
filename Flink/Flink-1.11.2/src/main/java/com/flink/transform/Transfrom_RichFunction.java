package com.flink.transform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import com.flink.bean.SensorReading;

import java.util.Properties;

/**
 * 富函数
 */
public class Transfrom_RichFunction {
    public static void main(String[] args) throws Exception {
        //从kafka读取数据然后进行计算
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        // kafka 配置项
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        DataStreamSource<String> streamSource = streamExecutionEnvironment.addSource(new FlinkKafkaConsumer011<String>("GMALL_STARTUP", new SimpleStringSchema(), properties));

        // 将kafka数据转换成SensorReading类型
        // 先过滤到不符规范的json数据
        SingleOutputStreamOperator<String> filter = streamSource.filter(new FilterFunction<String>() {
            public boolean filter(String s) throws Exception {
                try {
                    JSONObject jsonObject = JSON.parseObject(s);
                    return  true;
                }catch (Exception e){
                    System.out.println("此json不符合规范");
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

        //rich_function
        SingleOutputStreamOperator<Tuple2<Integer,String>> richOperator = mapOperator.map(new MyMapfunction());
        richOperator.print();
        streamExecutionEnvironment.execute();
    }
    //富函数继承RichMapFunction抽象类
    public static class MyMapfunction extends RichMapFunction<SensorReading,Tuple2<Integer, String>>{

        @Override
        public Tuple2<Integer, String> map(SensorReading sensorReading) throws Exception {
            return new Tuple2<>(getRuntimeContext().getIndexOfThisSubtask(),
                    sensorReading.getArea());
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            // 初始化，此处一般可用于数据库连接。
            System.out.println("open");
        }

        @Override
        public void close() throws Exception {
            // 连接关闭，状态清理等操作
            System.out.println("close");
        }
    }
}
