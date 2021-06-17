## Flink-CDC-SQL生产实践
### 背景
```text
由于之前业务系统的实时计算某个用户查询系统密码次数类似这种指标是用主动任务触发进行查询。
但随着数据量的日益增长和实时分析的需求越来越大，急需对系统进行流式计算、实时化改造。正是在这个背景下，开始了我们与Flink-CDC-SQL的故事。
```

### 技术方案选型
```text
1.Canal-->Kafka->Flink-->Mysql
2.Flink-CDC-->Mysql
分析：
    第一种是针对采集Mysql的binlog日志进行实时计算额传统架构，优点：技术架构成熟&生产经验丰富；缺点：维护组件过多。
    第二种是随着Flink兴起而开始逐渐落地的新型架构，优点：组件依赖少&端到端延迟减少；缺点：生产经验少。
```

### 解决方案
```text
实时计算具体思路是：使用Flink-CDC读取全量数据，全量数据同步完成后，Flink-CDC会无缝切换至MySQL的binlog位点继续消费增量的变更数据，且保证不会多消费一条也不会少消费一条。
读取到的用户操作日志流水和任务参与记录流水进行关联，并做一些预聚合，然后将聚合结果Sink回Mysql即可。
```

### 集群环境与现状
```text
在集群环境搭建了CDH + Flink + Mysql分布式环境，采用的Flink-on-YARN的per-job模式运行，使用RocksDB作为state backend，HDFS作为checkpoint持久化地址，并且做好了HDFS的容错，保证checkpoint数据不丢失。
使用SQL Client提交作业，所有作业统一使用纯SQL，没有一行Java代码。
```

### 具体实现

1.查看业务数据库是否开启binlog
```text
show variables like 'log_bin';
```
2.sql-client.sh embedded 启动SQL CLI客户端提交SQL任务
```shell script
--切换为默认catalog
use catalog default_catalog;
--切换为默认方言
SET table.sql-dialect=default;
```
3.创建对应的source表以及sink表
```shell script
--任务参与记录表
CREATE TABLE activity_records (
  record_id STRING NOT NULL
  ,created_at STRING
  ,updated_at STRING
  ,activity_id STRING
  ,activity_name STRING 
  ,cust_id STRING
  ,reward_id STRING
  ,activity_type_id STRING
  ,activity_type_name STRING
  ,current_progress STRING
  ,status STRING
  ,attribute STRING
  ,repeat_time STRING
  ,repeat_begin_time STRING
  ,repeat_end_time STRING
  ,meet_date STRING
  ,meet_date_str STRING
  ,done_date STRING
  ,give_date STRING
  ,meet_timeout_date STRING
  ,last_give_date STRING
  ,real_give_date STRING
  ,web_show STRING
  ,txn_seq STRING
  ,activity_timeout_date STRING
  ,active_receive STRING
  ,is_delete STRING
) WITH (
 'connector' = 'mysql-cdc',
 'hostname' = '172.16.0.23',
 'port' = '3306',
 'username' = 'root',
 'password' = 'xysh1234',
 'database-name' = 'flinkcdc',
 'table-name' = 'activity_records',
 'debezium.snapshot.locking.mode' = 'none'
);
```
```shell script
--用户在交互平台行为日志记录表
CREATE TABLE interactive_events (
  weixin_event_id  STRING NOT NULL
  ,created_at STRING 
  ,updated_at STRING 
  ,cust_id STRING 
  ,event_key STRING 
  ,content STRING  
  ,record_date STRING 
) WITH (
 'connector' = 'mysql-cdc',
 'hostname' = '172.16.0.23',
 'port' = '3306',
 'username' = 'root',
 'password' = 'xysh1234',
 'database-name' = 'flinkcdc',
 'table-name' = 'interactive_events',
 'debezium.snapshot.locking.mode' = 'none'
);
```
```shell script
--聚合结果表
CREATE TABLE reslut (
  cust_id STRING NOT NULL
  ,setquery_cnt BIGINT 
  ,cipher_cnt BIGINT 
  ,openapp_day BIGINT 
  ,openapp_cnt BIGINT 
  ,thumbsup_day BIGINT 
  ,thumbsup_cnt BIGINT 
  ,save_cnt BIGINT 
  ,save_day BIGINT 
  ,setup_cnt BIGINT 
  ,novicedone_cnt BIGINT 
  ,dailydone_cnt BIGINT 
  ,flashdone_cnt BIGINT 
  ,passivedone_cnt BIGINT 
  ,elsedone_cnt BIGINT 
  ,done_cnt BIGINT 
  ,app_cnt BIGINT 
  ,PRIMARY KEY (cust_id) NOT ENFORCED
) WITH (
  'connector' = 'jdbc',
  'url' = 'jdbc:mysql://172.16.0.23:3306/flinkcdc?useSSL=false&autoReconnect=true',
  'driver' = 'com.mysql.cj.jdbc.Driver',
  'table-name' = 'reslut',
  'username' = 'root',
  'password' = 'xysh1234',
  'lookup.cache.max-rows' = '3000',
  'lookup.cache.ttl' = '10s',
  'lookup.max-retries' = '3'
);
```
4.进行聚合操作并插入聚合结果表
```shell script
INSERT INTO reslut
select 
t1.cust_id
,sum(case when t1.event_key = '6' then 1 else 0 end) as setquery_cnt
,sum(case when t1.event_key = '7' then 1 else 0 end) as cipher_cnt
,count(DISTINCT case when t1.event_key = '2' then t1.record_date else null end) as openapp_day
,sum(case when t1.event_key = '2' then 1 else 0 end) as openapp_cnt
,count(DISTINCT case when t1.event_key = '3' then t1.record_date else null end) as thumbsup_day
,sum(case when t1.event_key = '3' then 1 else 0 end) as thumbsup_cnt
,sum(case when t1.event_key = '4' then 1 else 0 end) as save_cnt
,count(DISTINCT case when t1.event_key = '4' then t1.record_date else null end) as save_day
,sum(case when t1.event_key = '5' then 1 else 0 end) as setup_cnt
,COALESCE(max(t2.novicedone_cnt),0) as novicedone_cnt
,COALESCE(max(t2.dailydone_cnt),0) as dailydone_cnt
,COALESCE(max(t2.flashdone_cnt),0) as flashdone_cnt
,COALESCE(max(t2.passivedone_cnt),0) as passivedone_cnt
,COALESCE(max(t2.elsedone_cnt),0) as elsedone_cnt
,COALESCE(max(t2.done_cnt),0) as done_cnt
,sum(case when t1.event_key = 0 then 1 else 0 end) as app_cnt
from interactive_events t1
left join (
SELECT
tmp.cust_id
,sum(case when tmp.attribute='0' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as novicedone_cnt
,sum(case when tmp.attribute='1' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as dailydone_cnt
,sum(case when tmp.attribute='2' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as flashdone_cnt
,sum(case when tmp.attribute='3' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as passivedone_cnt
,sum(case when tmp.attribute='4' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as elsedone_cnt
,sum(case when tmp.status in ('1','2') then 1 else 0 end) as done_cnt
from activity_records tmp
group by tmp.cust_id
) t2
on t2.cust_id = t1.cust_id
group by t1.cust_id;
```
### 踩过的坑和学到的经验
1.执行聚合sql报错：
```shell script
[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.table.planner.codegen.CodeGenException: Unable to find common type of GeneratedExpression(field$206,isNull$205,,STRING,None) and ArrayBuffer(GeneratedExpression(((int) 0),false,,INT NOT NULL,Some(0))).
```
1.解决方案
```shell script
因为在创建表时attribute、status、event_key等字段是STRING类型，但代码中=1(INT)导致报错，改为字符串类型即可
```
2.Flink-CDC在全表扫描阶段慢
```text
扫描全表阶段慢不一定是cdc source的问题，可能是下游聚合节点处理太慢导致反压。
```
2.解决方案
```yaml
##修改sql-client-defaults.yaml文件配上MiniBatch相关参数和开启distinct优化
configuration:
  table.exec.mini-batch.enabled: true
  table.exec.mini-batch.allow-latency: 2s
  table.exec.mini-batch.size: 5000
  table.optimizer.distinct-agg.split.enabled: true
```
```shell script
##优化SQL: 在distinct聚合上使用FILTER修饰符 
INSERT INTO reslut
select 
t1.cust_id
,sum(case when t1.event_key = '6' then 1 else 0 end) as setquery_cnt
,sum(case when t1.event_key = '7' then 1 else 0 end) as cipher_cnt
,count(DISTINCT t1.record_date) FILTER(where t1.event_key = '2') AS openapp_day
,sum(case when t1.event_key = '2' then 1 else 0 end) as openapp_cnt
,count(DISTINCT t1.record_date) FILTER(where t1.event_key = '3') AS thumbsup_day
,sum(case when t1.event_key = '3' then 1 else 0 end) as thumbsup_cnt
,sum(case when t1.event_key = '4' then 1 else 0 end) as save_cnt
,count(DISTINCT t1.record_date) FILTER(where t1.event_key = '4') AS save_day
,sum(case when t1.event_key = '5' then 1 else 0 end) as setup_cnt
,COALESCE(max(t2.novicedone_cnt),0) as novicedone_cnt
,COALESCE(max(t2.dailydone_cnt),0) as dailydone_cnt
,COALESCE(max(t2.flashdone_cnt),0) as flashdone_cnt
,COALESCE(max(t2.passivedone_cnt),0) as passivedone_cnt
,COALESCE(max(t2.elsedone_cnt),0) as elsedone_cnt
,COALESCE(max(t2.done_cnt),0) as done_cnt
,sum(case when t1.event_key = 0 then 1 else 0 end) as app_cnt
from interactive_events t1
left join (
SELECT
tmp.cust_id
,sum(case when tmp.attribute='0' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as novicedone_cnt
,sum(case when tmp.attribute='1' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as dailydone_cnt
,sum(case when tmp.attribute='2' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as flashdone_cnt
,sum(case when tmp.attribute='3' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as passivedone_cnt
,sum(case when tmp.attribute='4' and (tmp.status='3' or tmp.status='2') then 1 else 0 end) as elsedone_cnt
,sum(case when tmp.status in ('1','2') then 1 else 0 end) as done_cnt
from activity_records tmp
group by tmp.cust_id
) t2
on t2.cust_id = t1.cust_id
group by t1.cust_id;
```
3.Flink-CDC source扫描MySQL表期间，发现无法往该表insert数据
```text
由于使用的MySQL用户未授权RELOAD权限，导致无法获取全局读锁（FLUSH TABLES WITH READ LOCK），CDC source就会退化成表级读锁，而使用表级读锁需要等到全表scan完，才能释放锁，所以会发现持锁时间过长的现象，影响其他业务写入数据。
```
3.解决方案
```text
给使用的MySQL用户授予RELOAD权限即可。所需的权限列表详见文档：https://github.com/ververica/flink-cdc-connectors/wiki/mysql-cdc-connector#setup-mysql-server。如果出于某些原因无法授予RELOAD权限，也可以显式配上 'debezium.snapshot.locking.mode' = 'none'来避免所有锁的获取，但要注意只有当快照期间表的schema不会变更才安全。
```