package com.flink.transform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import com.flink.bean.SensorReading;

import java.util.Collections;
import java.util.Properties;

/**
 * 1.对于要充当key的POJO类，必须满足以下条件：
 *          字段名必须声明为public的
 *          必须有默认的无参构造器
 *          所有构造器必须声明为public的
 * 2.max()与maxBy()的区别：max只会针对单个字段进行聚合比较，其他字段不正确
 *                       maxBy()取到对应字段最大值以及整条记录
 */
public class RollingAgg {
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

        // 分组，进行滚动聚合
        KeyedStream<SensorReading, Tuple> keyedStream = mapOperator.keyBy("uid");
        SingleOutputStreamOperator<SensorReading> max = keyedStream.max("ts");

        //reduce聚合
        final SingleOutputStreamOperator<SensorReading> reduce = keyedStream.reduce(new ReduceFunction<SensorReading>() {
            public SensorReading reduce(SensorReading v1, SensorReading v2) throws Exception {
                return new SensorReading(v2.getArea(),v2.getUid(),v2.getOs(),v2.getCh(),v2.getAppid(),v2.getMid(),v2.getType(),
                        v2.getVs(),String.valueOf(Math.min(Integer.getInteger(v1.getTs()), Integer.getInteger(v2.getTs()))));
            }
        });
        SingleOutputStreamOperator<String> resOperator = reduce.map(new MapFunction<SensorReading, String>() {
            public String map(SensorReading sensorReading) throws Exception {
                return sensorReading.toString();
            }
        });
        resOperator.print();

        // 分流
        SplitStream<SensorReading> splitStream = mapOperator.split(new OutputSelector<SensorReading>() {
            public Iterable<String> select(SensorReading sensorReading) {
                return (Integer.getInteger(sensorReading.getVs()) > 2) ? Collections.singletonList("high") :
                        Collections.singletonList("low");
            }
        });
        DataStream<SensorReading> high = splitStream.select("high");
        DataStream<SensorReading> low = splitStream.select("low");
        high.print("hign");

        //合流 connect 先将hign流转换成二元组，然后合流操作
        SingleOutputStreamOperator<Tuple2<String, String>> tupeStream = high.map(new MapFunction<SensorReading, Tuple2<String, String>>() {
            public Tuple2<String, String> map(SensorReading sensorReading) throws Exception {
                return new Tuple2<String, String>(sensorReading.getAppid(),sensorReading.getArea());
            }
        });
        ConnectedStreams<Tuple2<String, String>, SensorReading> connectedStreams = tupeStream.connect(low);
        SingleOutputStreamOperator<Object> outputStreamOperator = connectedStreams.map(new CoMapFunction<Tuple2<String, String>, SensorReading, Object>() {
            public Object map1(Tuple2<String, String> stringStringTuple2) throws Exception {
                return new Tuple3<>(stringStringTuple2.f0, stringStringTuple2.f1, "warning");
            }

            public Object map2(SensorReading sensorReading) throws Exception {
                return new Tuple2<>(sensorReading.getAppid(), "healthy");
            }
        });
        outputStreamOperator.print();

        streamExecutionEnvironment.execute();
    }
}
