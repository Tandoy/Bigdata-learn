package com.flink.Watermark;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.flink.bean.SensorReading;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;


/**
 * flink处理数据延迟方式：
 * allowedLateness
 * sideOutputLateData
 * Watermark 周期性插入、非周期性插入
 */
/**
 * VM最小指的是，多个输入分区的数据进入同一个任务时候，以最小的wm为准；在同一个数据流中（就是图片里面的情况）是最新的wm为主
 */

/**
 * Watermark 是一种衡量 Event Time 进展的机制。
 * Watermark 是用于处理乱序事件的，而正确的处理乱序事件，通常用
 * Watermark 机制结合 window 来实现。
 * 数据流中的 Watermark 用于表示 timestamp 小于 Watermark 的数据，都已经
 * 到达了，因此，window 的执行也是由 Watermark 触发的。
 * Watermark 可以理解成一个延迟触发机制，我们可以设置 Watermark 的延时
 * 时长 t，每次系统会校验已经到达的数据中最大的 maxEventTime，然后认定 eventTime
 * 小于 maxEventTime - t 的所有数据都已经到达，如果有窗口的停止时间等于
 * maxEventTime – t（实际上就是watermark），那么这个窗口被触发执行。
 */
public class Watermark {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        // 生成watermark的周期间隔时长
        streamExecutionEnvironment.getConfig().setAutoWatermarkInterval(100);
        // kafka 配置项
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        DataStreamSource<String> streamSource = streamExecutionEnvironment.addSource(new FlinkKafkaConsumer011<String>("GMALL_STARTUP", new SimpleStringSchema(), properties));

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
            // 乱序数据流式处理，生成watermark
//        }).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<SensorReading>(Time.milliseconds(2)) {
//            @Override
//            public long extractTimestamp(SensorReading sensorReading) {
//                return Long.getLong(sensorReading.getTs());
//            }
//        });
            // 数据有序流式处理，生成watermark
        }).assignTimestampsAndWatermarks(new AscendingTimestampExtractor<SensorReading>() {
            @Override
            public long extractAscendingTimestamp(SensorReading sensorReading) {
                return Long.getLong(sensorReading.getTs());
            }
        });

        mapOperator.keyBy("uid")
                .timeWindow(Time.seconds(15))
                .maxBy("uid").print();
        streamExecutionEnvironment.execute();
    }
}
