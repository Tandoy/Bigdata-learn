package com.flink.source;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;
import java.util.regex.Pattern;

public class SourceKafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // kafka 配置项
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "dxbigdata103:9092");
        // topic 和 partition 动态发现
        // 自动发现消费的partition变化
        properties.setProperty("flink.partition-discovery.interval-millis", String.valueOf((10 * 1000)));
        Pattern topicPattern = Pattern.compile("topic[0-9]");
        DataStreamSource<String> streamSource = env.addSource(new FlinkKafkaConsumer011<String>(topicPattern, new SimpleStringSchema(), properties));
        streamSource.print();
        env.execute("test-kafka");
    }
}

