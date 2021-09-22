package com.tz.flink.tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class CanalFlinkCDC {
    public static void main(String[] args) {
        // 1.创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 2.source：canal-json 订单表
        tableEnv.executeSql("CREATE TABLE mysqlbinlog1 (\n" +
                "  id BIGINT,\n" +
                "  a STRING,\n" +
                "  b STRING,\n" +
                "  c STRING\n" +
                ") WITH (\n" +
                " 'connector' = 'kafka',\n" +
                " 'topic' = 'mysqlbinlog1',\n" +
                " 'properties.bootstrap.servers' = 'dxbigdata102:9092',\n" +
                " 'properties.group.id' = 'testGroup',\n" +
                " 'scan.startup.mode' = 'earliest-offset',\n" +
                " 'format' = 'canal-json'\n" +
                ")");

        // 3.
        tableEnv.executeSql("select * from mysqlbinlog1").print();
    }
}
