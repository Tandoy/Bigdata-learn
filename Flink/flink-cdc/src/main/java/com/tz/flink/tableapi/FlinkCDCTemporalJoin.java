package com.tz.flink.tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * 使用flink sql来实现：
 * 1.flink-cdc捕获mysql upsert 订单表数据
 * 2.kafka捕获topic 产品维度版本表数据
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

        // 3.从kafka中获取产品维度版本表数据
        tableEnv.executeSql("CREATE TABLE `demoProducts` (\n" +
                "          `product_id` STRING, \n" +
                "          `product_name` STRING,\n" +
                "          `price` DECIMAL(10, 4),\n" +
                "          `currency` STRING, \n" +
                "           update_time TIMESTAMP(3) METADATA FROM 'timestamp', \n" +
                "           PRIMARY KEY(product_id) NOT ENFORCED, \n" +
                "          WATERMARK FOR update_time AS update_time \n" +
                "        ) WITH (\n" +
                "          'connector' = 'kafka',\n" +
                "          'topic' = 'demo_products',\n" +
                "          'properties.bootstrap.servers' = 'dxbigdata103:9092',\n" +
                "          'scan.startup.mode' = 'earliest-offset',\n" +
                "          'format' = 'debezium-json',\n" +
                "          'debezium-json.ignore-parse-errors' = 'true')\n");
        // 4.TemporalJoin
        // TemporalJoin中version table必须要有主键
        tableEnv.executeSql("SELECT o.order_id, o.order_date, o.order_time, o.quantity, o.purchaser, p.product_id,\n" +
                "               p.product_name, p.update_time, p.price, p.currency, p.price * o.quantity as total_price  \n" +
                "           FROM demoOrders as o \n" +
                "           LEFT JOIN demoProducts FOR SYSTEM_TIME AS OF o.order_time p \n" +
                "           ON o.product_id = p.product_id").print();
    }
}
