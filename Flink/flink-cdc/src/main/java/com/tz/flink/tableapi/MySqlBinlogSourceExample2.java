package com.tz.flink.tableapi;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class MySqlBinlogSourceExample2 {
    public static void main(String[] args) throws Exception {
        // 1.创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // 2.源表一
        tableEnv.executeSql(
                "CREATE TABLE tb1 (" +
                        "id bigint NOT NULL," +
                        "username varchar(50)," +
                        "password varchar(255)," +
                        "phone varchar(20)," +
                        "email varchar(255)," +
                        "create_time varchar(255)" +
                        ") WITH (" +
                        "'connector' = 'mysql-cdc'," +
                        "'hostname' = '172.16.0.23'," +
                        "'port' = '3306'," +
                        "'username' = 'root'," +
                        "'password' = 'xysh1234'," +
                        "'database-name' = 'davincidb'," +
                        "'table-name' = 'users'" +
                        ")"
        );

        // 3.源表二
        tableEnv.executeSql(
                "CREATE TABLE tb2 (" +
                        "id bigint NOT NULL," +
                        "username varchar(50)," +
                        "password varchar(255)," +
                        "phone varchar(20)," +
                        "email varchar(255)," +
                        "create_time varchar(255)" +
                        ") WITH (" +
                        "'connector' = 'mysql-cdc'," +
                        "'hostname' = '172.16.0.23'," +
                        "'port' = '3306'," +
                        "'username' = 'root'," +
                        "'password' = 'xysh1234'," +
                        "'database-name' = 'davincidb'," +
                        "'table-name' = 'users2'" +
                        ")"
        );

        // 4.目标表 提前在mysql创建对应的表
        tableEnv.executeSql(
                "CREATE TABLE users_res (" +
                        "id bigint NOT NULL," +
                        "username varchar(50)," +
                        "PRIMARY KEY(id) NOT ENFORCED " +
                        ") WITH (" +
                        "'connector' = 'jdbc'," +
                        "'url' = 'jdbc:mysql://172.16.0.23:3306/davincidb', " +
                        "'username' = 'root', " +
                        "'password' = 'xysh1234', " +
                        "'table-name' = 'users_res' " +
                        ")"
        );

        // 5.Flink job
        tableEnv.executeSql(
                        "select " +
                        "t1.id " +
                        ",t2.username " +
                        "from tb1 t1 " +
                        "left join tb2 t2 " +
                        "on t2.id = t1.id "
        ).print();
    }
}