package com.flink.tableApi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Csv;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Schema;

/**
 * 使用table api  测试kafka数据管道
 */
public class KafkaPipeline {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(executionEnvironment, settings);
        // 构建kafka读取
        tableEnvironment.connect(new Kafka()
                .version("0.11")
                .topic("GMALL_STARTUP")
                .property("zookeeper.connect", "dxbigdata103:2181")
                .property("bootstrap.servers", "dxbigdata103:9092"))
                .withFormat(new Csv())
                .withSchema(new Schema()
                .field("id", DataTypes.STRING())
                .field("timestamp",DataTypes.BIGINT())
                .field("temp",DataTypes.DOUBLE()))
                .createTemporaryTable("inputKafka");

        // 进行简单转换操作
        Table inputKafka = tableEnvironment.from("inputKafka");

        Table kafkaTable = inputKafka.select("id,temp").filter("id === 'sensor_6'");

        // 建立kafka输出管道
        tableEnvironment.connect(new Kafka()
                .version("0.11")
                .topic("sinktest")
                .property("zookeeper.connect", "dxbigdata103:2181")
                .property("bootstrap.servers", "dxbigdata103:9092"))
                .withFormat(new Csv())   //此处须是新CSV依赖
                .withSchema(new Schema()
                        .field("id", DataTypes.STRING())
//                        .field("timestamp",DataTypes.BIGINT())
                        .field("temp",DataTypes.DOUBLE()))
                .createTemporaryTable("outputKafka");

        kafkaTable.insertInto("outputKafka");

        // 执行
        executionEnvironment.execute();

    }
}
