//package com.flink.source;
//
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.EnvironmentSettings;
//import org.apache.flink.table.api.Table;
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
//import org.apache.flink.table.catalog.hive.HiveCatalog;
//import org.apache.flink.types.Row;
//
//
//public class SourceHive {
//    public static void main(String[] args) throws Exception{
//        System.setProperty("HADOOP_USER_NAME","hive");
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        EnvironmentSettings settings = EnvironmentSettings.newInstance()
//                .inStreamingMode()
//                .useBlinkPlanner()
//                .build();
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);
//
////        EnvironmentSettings settings = EnvironmentSettings
////            .newInstance()
////            .useBlinkPlanner()
////            .inBatchMode()
////            .build();
////
////        TableEnvironment tableEnv = TableEnvironment.create(settings);
//        String name = "myhive";      // Catalog名称，定义一个唯一的名称表示
//        String defaultDatabase = "test";  // 默认数据库名称
//        String hiveConfDir = "D:\Downloads\github\Bigdata-learn\Flink\src\main\resources\hive-site.xml";  // hive-site.xml路径
//        String version = "1.1.0";       // Hive版本号
//
//        HiveCatalog hive = new HiveCatalog(name, defaultDatabase, hiveConfDir, hiveConfDir,version);
//        //StatementSet statementSet = tableEnv.createStatementSet();
//
//        tableEnv.registerCatalog(name, hive);
//        tableEnv.useCatalog(name);
//
//        Table sqlResult = tableEnv.sqlQuery("select * from test");
//
//        DataStream<Row> rowDataStream = tableEnv.toAppendStream(sqlResult, Row.class);
//
//
//        rowDataStream.print();
//
//        env.execute();
//
//
////        String sql =
////            "create table testOut ( " +
////                "name varchar(20) not null, "+
////                "age varchar(20) not null "+
////                ") with ( "+
////                "'connector.type' = 'jdbc',"+
////                "'connector.url' = 'jdbc:mysql://192.168.1.101:3306/jeecg_boot?characterEncoding=UTF-8',"+
////                "'connector.table' = 'test_stu',"+
////                "'connector.driver' = 'com.mysql.jdbc.Driver',"+
////                "'connector.username' = 'root',"+
////                "'connector.password' = '123456')";
////        tableEnv.executeSql(sql);
////        statementSet.addInsert("testOut",sqlResult);
//
////       statementSet.execute();
//    }
//}
//
