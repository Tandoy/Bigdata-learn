### 使用Flink Hudi构建流式数据湖
```text
即将发布的0.9版本，Hudi原生支持CDC format，一条record的所有变更记录都可以保存，基于此，Hudi和流计算系统结合的更加完善，可以流式读取CDC数据
```
#### 环境准备
```text
Flink SQL Client

Hudi master 打包 hudi-flink-bundle jar

Flink 1.13.1
```
#### 实践步骤
提前准备一段debezium-json格式的CDC数据
```json
{"before":null,"after":{"id":101,"ts":1000,"name":"scooter","description":"Small 2-wheel scooter","weight":3.140000104904175},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606100,"transaction":null}
{"before":null,"after":{"id":102,"ts":2000,"name":"car battery","description":"12V car battery","weight":8.100000381469727},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":103,"ts":3000,"name":"12-pack drill bits","description":"12-pack of drill bits with sizes ranging from #40 to #3","weight":0.800000011920929},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":104,"ts":4000,"name":"hammer","description":"12oz carpenter's hammer","weight":0.75},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":105,"ts":5000,"name":"hammer","description":"14oz carpenter's hammer","weight":0.875},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":106,"ts":6000,"name":"hammer","description":"16oz carpenter's hammer","weight":1},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":107,"ts":7000,"name":"rocks","description":"box of assorted rocks","weight":5.300000190734863},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":108,"ts":8000,"name":"jacket","description":"water resistent black wind breaker","weight":0.10000000149011612},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":null,"after":{"id":109,"ts":9000,"name":"spare tire","description":"24 inch spare tire","weight":22.200000762939453},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":0,"snapshot":"true","db":"inventory","table":"products","server_id":0,"gtid":null,"file":"mysql-bin.000003","pos":154,"row":0,"thread":null,"query":null},"op":"c","ts_ms":1589355606101,"transaction":null}
{"before":{"id":106,"ts":6000,"name":"hammer","description":"16oz carpenter's hammer","weight":1},"after":{"id":106,"ts":10000,"name":"hammer","description":"18oz carpenter hammer","weight":1},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589361987000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":362,"row":0,"thread":2,"query":null},"op":"u","ts_ms":1589361987936,"transaction":null}
{"before":{"id":107,"ts":7000,"name":"rocks","description":"box of assorted rocks","weight":5.300000190734863},"after":{"id":107,"ts":11000,"name":"rocks","description":"box of assorted rocks","weight":5.099999904632568},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589362099000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":717,"row":0,"thread":2,"query":null},"op":"u","ts_ms":1589362099505,"transaction":null}
{"before":null,"after":{"id":110,"ts":12000,"name":"jacket","description":"water resistent white wind breaker","weight":0.20000000298023224},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589362210000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":1068,"row":0,"thread":2,"query":null},"op":"c","ts_ms":1589362210230,"transaction":null}
{"before":null,"after":{"id":111,"ts":13000,"name":"scooter","description":"Big 2-wheel scooter ","weight":5.179999828338623},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589362243000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":1394,"row":0,"thread":2,"query":null},"op":"c","ts_ms":1589362243428,"transaction":null}
{"before":{"id":110,"ts":12000,"name":"jacket","description":"water resistent white wind breaker","weight":0.20000000298023224},"after":{"id":110,"ts":14000,"name":"jacket","description":"new water resistent white wind breaker","weight":0.5},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589362293000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":1707,"row":0,"thread":2,"query":null},"op":"u","ts_ms":1589362293539,"transaction":null}
{"before":{"id":111,"ts":13000,"name":"scooter","description":"Big 2-wheel scooter ","weight":5.179999828338623},"after":{"id":111,"ts":15000,"name":"scooter","description":"Big 2-wheel scooter ","weight":5.170000076293945},"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589362330000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":2090,"row":0,"thread":2,"query":null},"op":"u","ts_ms":1589362330904,"transaction":null}
{"before":{"id":111,"ts":16000,"name":"scooter","description":"Big 2-wheel scooter ","weight":5.170000076293945},"after":null,"source":{"version":"1.1.1.Final","connector":"mysql","name":"dbserver1","ts_ms":1589362344000,"snapshot":"false","db":"inventory","table":"products","server_id":223344,"gtid":null,"file":"mysql-bin.000003","pos":2443,"row":0,"thread":2,"query":null},"op":"d","ts_ms":1589362344455,"transaction":null}
```
通过Flink SQL Client创建表用来读取CDC数据文件
```text
CREATE TABLE debezium_source(
   id INT NOT NULL,
   ts BIGINT,
   name STRING,
   description STRING,
   weight DOUBLE
 ) WITH (
   'connector' = 'filesystem',
   'path' = '/home/appuser/tangzhi/source.data',
   'format' = 'debezium-json'
 );
```
执行SELECT观察结果，可以看到一共有20条记录，中间有一些UPDATE s，最后一条消息是DELETE
```text
select * from debezium_source;
```
创建Hudi表，这里设置表的形态为MERGE_ON_READ并且打开changelog模式属性changelog.enabled
```text
CREATE TABLE hoodie_table(
   id INT NOT NULL PRIMARY KEY NOT ENFORCED,
   ts BIGINT,
   name STRING,
   description STRING,
   weight DOUBLE
 ) WITH (
   'connector' = 'hudi',
   'path' = '/home/appuser/tangzhi/',
   'table.type' = 'MERGE_ON_READ',
   'changelog.enabled' = 'true',
   'compaction.async.enabled' = 'false'
 );
```
#### 查询
通过INSERT语句将数据导入Hudi，开启流读模式，并执行查询观察结果
```text
select * from hoodie_table/*+ OPTIONS('read.streaming.enabled'='true')*/;
```
可以看到Hudi保留了每行的变更记录，包括change log的operation类型，这里打开TABLE HINTS功能，方便动态设置表参数。
继续使用batch读模式，执行查询观察输出结果，可以看到中间的变更被合并。
```text
select * from hoodie_table;
```
#### 聚合
Bounded Source读模式下计算count(*)
```text
select count (*) from hoodie_table;
```
Streaming读模式下计算count(*)
```text
select count (*) from hoodie_table/*+OPTIONS('read.streaming.enabled'='true')*/;
```
batch和streaming模式下的计算结果是一致的。