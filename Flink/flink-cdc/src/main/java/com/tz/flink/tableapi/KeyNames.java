package com.tz.flink.tableapi;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class KeyNames {
    public static void main(String[] args) {
        // 1.创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 2.source：mysql-cdc 任务参与记录表
        tableEnv.executeSql("CREATE TABLE activity_records (\n" +
                "  record_id STRING NOT NULL\n" +
                "  ,created_at STRING\n" +
                "  ,updated_at STRING\n" +
                "  ,activity_id STRING\n" +
                "  ,activity_name STRING \n" +
                "  ,cust_id STRING\n" +
                "  ,reward_id STRING\n" +
                "  ,activity_type_id STRING\n" +
                "  ,activity_type_name STRING\n" +
                "  ,current_progress STRING\n" +
                "  ,status STRING\n" +
                "  ,attribute STRING\n" +
                "  ,repeat_time STRING\n" +
                "  ,repeat_begin_time STRING\n" +
                "  ,repeat_end_time STRING\n" +
                "  ,meet_date STRING\n" +
                "  ,meet_date_str STRING\n" +
                "  ,done_date STRING\n" +
                "  ,give_date STRING\n" +
                "  ,meet_timeout_date STRING\n" +
                "  ,last_give_date STRING\n" +
                "  ,real_give_date STRING\n" +
                "  ,web_show STRING\n" +
                "  ,txn_seq STRING\n" +
                "  ,activity_timeout_date STRING\n" +
                "  ,active_receive STRING\n" +
                "  ,is_delete STRING\n" +
                ") WITH (\n" +
                " 'connector' = 'mysql-cdc',\n" +
                " 'hostname' = '172.16.0.23',\n" +
                " 'port' = '3306',\n" +
                " 'username' = 'root',\n" +
                " 'password' = 'xysh1234',\n" +
                " 'database-name' = 'flinkcdc',\n" +
                " 'table-name' = 'activity_records',\n" +
                " 'debezium.snapshot.locking.mode' = 'none'\n" +
                ")");
        // 3.source：mysql-cdc 用户在交互平台行为日志记录表
        tableEnv.executeSql("CREATE TABLE interactive_events (\n" +
                "  weixin_event_id  STRING NOT NULL\n" +
                "  ,created_at STRING \n" +
                "  ,updated_at STRING \n" +
                "  ,cust_id STRING \n" +
                "  ,event_key STRING \n" +
                "  ,content STRING  \n" +
                "  ,record_date STRING \n" +
                ") WITH (\n" +
                " 'connector' = 'mysql-cdc',\n" +
                " 'hostname' = '172.16.0.23',\n" +
                " 'port' = '3306',\n" +
                " 'username' = 'root',\n" +
                " 'password' = 'xysh1234',\n" +
                " 'database-name' = 'flinkcdc',\n" +
                " 'table-name' = 'interactive_events',\n" +
                " 'debezium.snapshot.locking.mode' = 'none'\n" +
                ")");
        // 4.sink：mysql 聚合结果表
        tableEnv.executeSql("CREATE TABLE reslut (\n" +
                "  cust_id STRING NOT NULL\n" +
                "  ,setquery_cnt BIGINT \n" +
                "  ,cipher_cnt BIGINT \n" +
                "  ,openapp_day BIGINT \n" +
                "  ,openapp_cnt BIGINT \n" +
                "  ,thumbsup_day BIGINT \n" +
                "  ,thumbsup_cnt BIGINT \n" +
                "  ,save_cnt BIGINT \n" +
                "  ,save_day BIGINT \n" +
                "  ,setup_cnt BIGINT \n" +
                "  ,novicedone_cnt BIGINT \n" +
                "  ,dailydone_cnt BIGINT \n" +
                "  ,flashdone_cnt BIGINT \n" +
                "  ,passivedone_cnt BIGINT \n" +
                "  ,elsedone_cnt BIGINT \n" +
                "  ,done_cnt BIGINT \n" +
                "  ,app_cnt BIGINT \n" +
                "  ,PRIMARY KEY (cust_id) NOT ENFORCED\n" +
                ") WITH (\n" +
                "  'connector' = 'jdbc',\n" +
                "  'url' = 'jdbc:mysql://172.16.0.23:3306/flinkcdc?useSSL=false&autoReconnect=true',\n" +
                "  'driver' = 'com.mysql.cj.jdbc.Driver',\n" +
                "  'table-name' = 'reslut',\n" +
                "  'username' = 'root',\n" +
                "  'password' = 'xysh1234',\n" +
                "  'lookup.cache.max-rows' = '3000',\n" +
                "  'lookup.cache.ttl' = '10s',\n" +
                "  'lookup.max-retries' = '3'\n" +
                ")");
        // 进行聚合操作并插入聚合结果表
        tableEnv.executeSql("INSERT INTO reslut\n" +
                "select \n" +
                "t1.cust_id\n" +
                ",sum(case when t1.event_key = '6' then 1 else 0 end) as setquery_cnt\n" +
                ",sum(case when t1.event_key = '7' then 1 else 0 end) as cipher_cnt\n" +
                ",count(DISTINCT case when t1.event_key = '2' then t1.record_date else null end) as openapp_day\n" +
                ",sum(case when t1.event_key = '2' then 1 else 0 end) as openapp_cnt\n" +
                ",count(DISTINCT case when t1.event_key = '3' then t1.record_date else null end) as thumbsup_day\n" +
                ",sum(case when t1.event_key = '3' then 1 else 0 end) as thumbsup_cnt\n" +
                ",sum(case when t1.event_key = '4' then 1 else 0 end) as save_cnt\n" +
                ",count(DISTINCT case when t1.event_key = '4' then t1.record_date else null end) as save_day\n" +
                ",sum(case when t1.event_key = '5' then 1 else 0 end) as setup_cnt\n" +
                ",COALESCE(max(t2.novicedone_cnt),0) as novicedone_cnt\n" +
                ",COALESCE(max(t2.dailydone_cnt),0) as dailydone_cnt\n" +
                ",COALESCE(max(t2.flashdone_cnt),0) as flashdone_cnt\n" +
                ",COALESCE(max(t2.passivedone_cnt),0) as passivedone_cnt\n" +
                ",COALESCE(max(t2.elsedone_cnt),0) as elsedone_cnt\n" +
                ",COALESCE(max(t2.done_cnt),0) as done_cnt\n" +
                ",sum(case when t1.event_key = 0 then 1 else 0 end) as app_cnt\n" +
                "from interactive_events t1\n" +
                "left join (\n" +
                "SELECT\n" +
                "tmp.cust_id\n" +
                ",sum(case when tmp.attribute='0' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as novicedone_cnt\n" +
                ",sum(case when tmp.attribute='1' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as dailydone_cnt\n" +
                ",sum(case when tmp.attribute='2' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as flashdone_cnt\n" +
                ",sum(case when tmp.attribute='3' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as passivedone_cnt\n" +
                ",sum(case when tmp.attribute='4' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as elsedone_cnt\n" +
                ",sum(case when tmp.status in ('1','2') then 1 else 0 end) as done_cnt\n" +
                "from activity_records tmp\n" +
                "group by tmp.cust_id\n" +
                ") t2\n" +
                "on t2.cust_id = t1.cust_id\n" +
                "group by t1.cust_id");
    }
}
