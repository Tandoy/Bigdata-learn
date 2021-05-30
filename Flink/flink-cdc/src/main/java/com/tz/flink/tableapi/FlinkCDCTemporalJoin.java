package com.tz.flink.tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * 使用flink sql来实现：
 * 1.flink-cdc捕获mysql upsert 订单表数据
 * 2.kafka-connectors捕获topic 产品维度版本表数据
 * 3.实现TemporalJoin
 */
public class FlinkCDCTemporalJoin {
    public static void main(String[] args){
        // 1.创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 2.source：mysql-cdc 订单表
        tableEnv.executeSql("CREATE TABLE demoOrders (\n" +
                "         `order_id` INTEGER ,\n" +
                "          `order_date` DATE ,\n" +
                "          `order_time` TIMESTAMP(3),\n" +
                "          `quantity` INT ,\n" +
                "          `product_id` INT ,\n" +
                "          `purchaser` STRING,\n" +
                "           WATERMARK FOR order_time AS order_time \n" +
                "         ) WITH (\n" +
                "          'connector' = 'mysql-cdc',\n" +
                "          'hostname' = '172.16.0.23',\n" +
                "          'port' = '3306',\n" +
                "          'username' = 'root',\n" +
                "          'password' = 'xysh1234',\n" +
                "          'database-name' = 'davincidb',\n" +
                "          'debezium.snapshot.locking.mode' = 'none',\n" +
                "          'table-name' = 'demo_orders')\n");
        tableEnv.executeSql("select * from demoOrders").print();
        // TODO 3.从kafka中获取产品维度版本表数据
    }
}
