## ClickHouse物化视图实战经验

1.前言
```text
ClickHouse广泛用于用户和系统日志查询场景中，主要针对于OLAP场景，为业务方提供稳定高效的查询服务。在业务场景下，实时事件流上报可能会在不同的日志，以不同的格式、途径写入到clickhouse。
在之前的使用中，通过查询多个日志表join实现多个指标的整合。用传统JOIN方式。会遇到如下困难: 
1.每个查询会有非常长的代码，有的甚至1500行、2000行sql，使用和理解上特别痛苦;
2.性能上无法满足业务诉求，日志量大会爆内存不足; 
如何将这些数据进行整合，以ClickHouse宽表的方式呈现给上层使用，用户可以在一张表中查到所需的所有指标，避免提供多表带来的代码复杂度和性能开销问题？
本文将重点介绍如何通过物化视图有效解决上述场景的问题。在介绍之前，先简单介绍一下物化视图的简单使用，包括如何创建，如何增加维度和指标，如何结合字典增维等场景。
```

2.准备工作
```text
通过python的Faker库生成模拟数据
```

3.用户维度数据表
```shell script
create table ods.user_dim_local 
(
 day Date comment '数据分区-天',
 uid UInt32 default 0 comment 'uid',
 platform String default '' comment '平台 android/ios',
 country String default '' comment '国家',
 province String default '' comment '省及直辖市',
 isp String default '' comment '运营商',
 app_version String default '' comment '应用版本',
 os_version String default '' comment '系统版本',
 mac String default '' comment 'mac',
 ip String default '' comment 'ip',
 gender String default '' comment '性别',
 age Int16 default -1 comment '年龄'
)
engine = MergeTree()
PARTITION BY day
PRIMARY KEY day
ORDER BY day
TTL day + toIntervalDay(3) + toIntervalHour(3)
SETTINGS index_granularity = 8192;
```

4.物品维度数据表
```shell script
create table ods.item_dim_local on cluster cluster 
(
 day Date comment '数据分区-天',
 item_id UInt32 default 0 comment 'item_id',
 type_id UInt32 default 0 comment 'type_id',
 price UInt32 default 0 comment 'price'
)
engine = MergeTree()
PARTITION BY day
PRIMARY KEY day
ORDER BY day
TTL day + toIntervalDay(3) + toIntervalHour(3)
SETTINGS index_granularity = 8192;
```

5.action_001行为数据表
```shell script
create table ods.action_001_local on cluster cluster (
day Date default toDate(second) comment '数据分区-天(Date)'
,hour DateTime default toStartOfHour(second) comment '数据时间-小时(DateTime)'
,second DateTime default '1970-01-01 08:00:00' comment '数据时间-秒'
,insert_second DateTime default now() comment '数据写入时间'
,platform String default '' comment '平台 android/ios'
,ip String default '' comment 'client-ip'
,isp String default '' comment '运营商'
,uid UInt32 default 0 comment 'uid'
,ver String default '' comment '版本'
,item_id UInt32 default 0 comment '物品id'
,show_cnt UInt32 default 0 comment '曝光次数'
,click_cnt UInt32 default 0 comment '点击次数'
,show_time UInt32 default 0 comment '曝光时间'
)
engine=MergeTree()
PARTITION BY day
PRIMARY KEY (day,hour)
ORDER BY (day,hour,platform,item_id)
TTL day + toIntervalDay(10) + toIntervalHour(4)
SETTINGS index_granularity = 8192;
```

6.action_002 行为数据表
```shell script
create table ods.action_002_local on cluster cluster (
day Date default toDate(second) comment '数据分区-天(Date)'
,hour DateTime default toStartOfHour(second) comment '数据时间-小时(DateTime)'
,second DateTime default '1970-01-01 08:00:00' comment '数据时间-秒'
,insert_second DateTime default now() comment '数据写入时间'
,platform String default '' comment '平台 android/ios'
,ip String default '' comment 'client-ip'
,isp String default '' comment '运营商'
,uid UInt32 default 0 comment 'uid'
,ver String default '' comment '版本'
,item_id UInt32 default 0 comment '商品id'
,action_a_cnt UInt32 default 0 comment 'actionA次数'
,action_b_cnt UInt32 default 0 comment 'actionB次数'
,action_c_cnt UInt32 default 0 comment 'actionC次数'
,action_a_time UInt32 default 0 comment 'actionA时间'
,action_b_time UInt32 default 0 comment 'actionA时间'
,action_c_time UInt32 default 0 comment 'actionA时间'
,action_d_sum UInt32 default 0 comment 'action_d_sum'
,action_e_sum UInt32 default 0 comment 'action_e_sum'
,action_f_sum UInt32 default 0 comment 'action_f_sum'
)
engine=MergeTree()
PARTITION BY day
PRIMARY KEY (day,hour)
ORDER BY (day,hour,platform,item_id)
TTL day + toIntervalDay(10) + toIntervalHour(4)
SETTINGS index_granularity = 8192;
```

7.向表中插入模拟数据
```shell script
在dxbigdata101上执行：
cat item_dim.txt | clickhouse-client --host 172.16.0.222 --query="INSERT INTO ods.item_dim_local  FORMAT JSONEachRow"
cat user_dim.txt | clickhouse-client --host 172.16.0.222 --query="INSERT INTO ods.user_dim_local  FORMAT JSONEachRow"
python make_user_action_001.py
python make_user_action_002.py
```

### 8.物化视图初级使用
```text
在创建物化视图前评估一下数据量。物化视图会计算当前批次的数据汇总一次，然后根据维度自动merge聚合统计的指标，但是不会跨节点和分区，所以理想状况下，数据量的估算sql为
```
```shell script
select uniqCombined(hostName(),hour,item_id,platform,ver) from ods.action_001_local;
```

8.1 物化视图创建方式
```text
CREATE MATERIALIZED VIEW [IF NOT EXISTS] [db.]table_name [ON CLUSTER]
ENGINE = engine
AS SELECT 
第一种方式创建物化视图的好处是创建简单，避免自己写错聚合函数类型带来数据上的写入失败。缺点是alter有局限性，每次更改都需要替换或者修改物化视图的计算逻辑，而且也不能实现有限替代join场景。
```
```text
CREATE MATERIALIZED VIEW [IF NOT EXISTS] [db.]table_name [ON CLUSTER] TO db.]name
AS SELECT
第二种方式是先创建一个存储表，存储表是[Replicated]AggregatingMergeTree，然后通过创建的物化视图使用to的方式写入到存储表中，相当于存储的数据和计算的逻辑分为了两张表分别处理。
因为已经指定了存储的表，所以物化视图的创建也不需要指定engine，在查询中，查物化视图和查实际的存储表得到一样的数据，因为都是来自于同一份存储数据。
在建表之前还有个细节，TO db.name 后面的表不一定是本地表对本地表，还可以本地表对分布式表，可以基于shard_key处理一些分桶策略，但是会存在写放大的问题，导致集群写入频率增大，负载提高，可以但是慎用。
必须要注意的是，from的表一定是本地表。这里区分下存储表和计算表两个名词，后续场景会用到。
```

8.2 创建物化视图存储表
```shell script
create table dwm.mainpage_stat_mv_local
(
day Date comment '数据分区-天'
,hour DateTime comment '数据时间-小时(DateTime)'
,platform String comment '平台 android/ios'
,ver String comment '版本'
,item_id UInt32 comment '物品id'
,shown_uv AggregateFunction(uniqCombined,UInt32) comment '曝光人数'
,shown_cnt SimpleAggregateFunction(sum,UInt64) comment '曝光次数'
,click_uv AggregateFunction(uniqCombined,UInt32) comment '点击人数'
,click_cnt SimpleAggregateFunction(sum,UInt64) comment '点击次数'
,show_time_sum  SimpleAggregateFunction(sum,UInt64) comment '总曝光时间/秒'
)
engine=AggregatingMergeTree()
PARTITION by day
PRIMARY KEY (day,hour)
ORDER by (day,hour,platform,ver,item_id)
TTL day + toIntervalDay(92) + toIntervalHour(5)
SETTINGS index_granularity = 8192;
```

8.3 创建物化视图作为计算逻辑并使用to将数据流向AggregatingMergeTree
```shell script
create  MATERIALIZED VIEW dwm.mv_main_page_stat_mv_local to dwm.mainpage_stat_mv_local (
day Date comment '数据分区-天'
,hour DateTime comment '数据时间-小时(DateTime)'
,platform String comment '平台 android/ios'
,ver String comment '版本'
,item_id UInt32 comment '物品id'
,shown_uv AggregateFunction(uniqCombined,UInt32) comment '曝光人数'
,shown_cnt SimpleAggregateFunction(sum,UInt64) comment '曝光次数'
,click_uv AggregateFunction(uniqCombined,UInt32) comment '点击人数'
,click_cnt SimpleAggregateFunction(sum,UInt64) comment '点击次数'
,show_time_sum  SimpleAggregateFunction(sum,UInt64) comment '总曝光时间/秒'
)
AS SELECT day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,uniqCombinedStateIf(uid,a.show_cnt>0) as shown_uv
     ,sum(a.show_cnt) as show_cnt
     ,uniqCombinedStateIf(uid,a.click_cnt>0) as click_uv
     ,sum(a.click_cnt) as click_cnt
     ,sum(toUInt64(show_time/1000)) as show_time_sum
from ods.action_001_local as a
group by
      day
     ,hour
     ,platform
     ,ver
     ,item_id;
```
```shell script
create  MATERIALIZED VIEW dwm.mv_main_page_stat_mv_local to dwm.mainpage_stat_mv_local (
day Date comment '数据分区-天'
,hour DateTime comment '数据时间-小时(DateTime)'
,platform String comment '平台 android/ios'
,ver String comment '版本'
,item_id UInt32 comment '物品id'
,shown_uv AggregateFunction(uniqCombined,UInt32) comment '曝光人数'
,shown_cnt SimpleAggregateFunction(sum,UInt64) comment '曝光次数'
,click_uv AggregateFunction(uniqCombined,UInt32) comment '点击人数'
,click_cnt SimpleAggregateFunction(sum,UInt64) comment '点击次数'
,show_time_sum  SimpleAggregateFunction(sum,UInt64) comment '总曝光时间/秒'
)
AS SELECT day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,uniqCombinedStateIf(uid,a.show_cnt>0) as shown_uv
     ,sum(a.show_cnt) as show_cnt
     ,uniqCombinedStateIf(uid,a.click_cnt>0) as click_uv
     ,sum(a.click_cnt) as click_cnt
     ,sum(toUInt64(show_time/1000)) as show_time_sum
from ods.action_001_local as a
group by
      day
     ,hour
     ,platform
     ,ver
     ,item_id;
```
8.4 查询物化视图以及存储表数据
```shell script
运行python脚本生成模拟数据
python make_user_action_001.py
select * from dwm.mainpage_stat_mv_local;
select * from dwm.mv_main_page_stat_mv_local;
物化视图会根据模拟数据进行聚合计算并存储到存储表中
```

8.5 进一步聚合查询
```shell script
SELECT
    day,
    platform,
    uniqCombinedMerge(shown_uv) AS shown_uv,
    sum(shown_cnt) AS shown_cnt,
    uniqCombinedMerge(click_uv) AS click_uv,
    sum(click_cnt) AS click_cnt,
    sum(show_time_sum) AS show_time_sum
FROM dwm.mv_main_page_stat_mv_local
GROUP BY
    day,
    platform;
注意：这里使用uniqCombined结果是不正确的，因为uniqCombined将这个中间状态也作为了计算的输入重新计算了，所以在使用上一定要注意AggregateFunction中的State状态使用Merge解析才能得到正确的结果。
```

8.6 物化视图处理逻辑
```text
物化视图是计算每批次写入原表的数据，假设一批写入了10w,那么物化视图就计算了这10w的数据，然后可能聚合之后就剩1w了写入到表中，剩下的过程就交给后台去merge聚合了，这个时候就要去理解物化视图的核心字段类型，AggregateFunction和SimpleAggregateFunction了。这里主要讲两个场景的计算，去理解这个字段类型，一个是uniqCombined计算uv，一个是sum计算pv。
1.首先是uv计算场景在大数据量下，使用uniqExact去计算精确uv，存储开销大，不便于网络传输数据，查询耗时长，还容易爆内存。除非个别情况下，不推荐使用。
2.pv通常采用sum进行计算，sum计算和uv计算存在一个比较大的差异，那就是结果值可以累加。所以从逻辑上来讲，每批次计算可以直接是结果值，那么在聚合的时候可以再次进行sum操作可以得到正确的结果。那么这个时候除了采用AggregateFunction外存储中间态外也可以选择SimpleAggregateFunction存储每次计算结果，存储开销是不一样的。
以上主要针对一些单日志的固化场景处理，减少数据量级，提高查询效率。
```

### 9.物化视图进阶使用

9.1 背景
```text
其实在实际使用的场景下，经常会遇到一个维度关联的问题，比如将物品的类别带入，用户的画像信息带入等场景。这里简单列举下在clickhouse中做维度补全的操作。主要用到了用户维度数据和物品维度数据两个本地表，基于这两个本地表去生成内存字典，通过内存字典去做关联(字典有很多种存储结构，这里主要列举hashed模式)。
```

9.2 创建字典表
```shell script
--创建user字典
CREATE DICTIONARY dim.dict_user_dim(
 uid UInt64 ,
 platform String default '' ,
 country String default '' ,
 province String default '' ,
 isp String default '' ,
 app_version String default '' ,
 os_version String default '',
 mac String default '' ,
 ip String default '',
 gender String default '',
 age Int16 default -1
) PRIMARY KEY uid 
SOURCE(
  CLICKHOUSE(
    HOST '172.16.0.222' PORT 9000 USER 'default' PASSWORD '' DB 'ods' TABLE 'user_dim_local'
  )
) LIFETIME(MIN 1800 MAX 3600) LAYOUT(HASHED());
```
```shell script
--创建item字典
CREATE DICTIONARY dim.dict_item_dim(
 item_id UInt64 ,
 type_id UInt32 default 0,
 price UInt32 default 0
) PRIMARY KEY item_id 
SOURCE(
  CLICKHOUSE(
    HOST '172.16.0.222' PORT 9000 USER 'default' PASSWORD '' DB 'ods' TABLE 'item_dim_local'
  )
) LIFETIME(MIN 1800 MAX 3600) LAYOUT(HASHED());
```

9.3 字典表的使用
```text
一种是通过dictGet，另外一种方式是通过join，如果只查询一个key建议通过dictGet使用，代码复杂可读性高，同时字典查的value可以作为另一个查询的key，如果查多个key，可以通过dictGet或者join。类似于 select 1 as a,a+1 as b,b+1 as c from system.one这样。
```

9.4 优化物化视图
```shell script
--新增维度并添加到索引
alter table dwm.mainpage_stat_mv_local add column if not exists gender String comment '性别' after item_id,modify order by (day,hour,platform,ver,item_id,gender);
alter table dwm.mainpage_stat_mv_local modify column if exists gender String default '未知' comment '性别' after item_id;
alter table dwm.mainpage_stat_mv_local add column if not exists show_time_median AggregateFunction(medianExact,UInt32) comment '曝光时长中位数';
```
```shell script
--修改物化视图计算逻辑
drop TABLE dwm.mv_main_page_stat_mv_local;
CREATE MATERIALIZED VIEW dwm.mv_main_page_stat_mv_local to dwm.mainpage_stat_mv_local (
day Date comment '数据分区-天'
,hour DateTime comment '数据时间-小时(DateTime)'
,platform String comment '平台 android/ios'
,ver String comment '版本'
,item_id UInt32 comment '物品id'
,gender String  comment '性别'
,shown_uv AggregateFunction(uniqCombined,UInt32) comment '曝光人数'
,shown_cnt SimpleAggregateFunction(sum,UInt64) comment '曝光次数'
,click_uv AggregateFunction(uniqCombined,UInt32) comment '点击人数'
,click_cnt SimpleAggregateFunction(sum,UInt64) comment '点击次数'
,show_time_sum  SimpleAggregateFunction(sum,UInt64) comment '总曝光时间/秒'
,show_time_median AggregateFunction(medianExact,UInt32) comment '曝光时长中位数'
)
AS 
 SELECT day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,dictGet('dim.dict_user_dim', 'gender',toUInt64(uid)) as gender
     ,uniqCombinedStateIf(uid,a.show_cnt>0) as shown_uv
     ,sum(a.show_cnt) as show_cnt
     ,uniqCombinedStateIf(uid,a.click_cnt>0) as click_uv
     ,sum(a.click_cnt) as click_cnt
     ,sum(toUInt64(show_time/1000)) as show_time_sum
     ,medianExactState(toUInt32(show_time/1000)) as show_time_median
from ods.action_001_local as a
group by
      day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,gender;
```

### 10.物化视图再进阶使用

10.1 背景
```text
很多时候，日志上报并不是在一个日志中的，比如上文中创建的action_001和action_002，一个是主页物品的曝光和点击，一个是点击进行物品详情的其他行为。这个时候，提了一个诉求，希望可以知道曝光到点击，点击到某个更一步的行为的用户转换率。
我们最常规的方法是，使用join去将结果关联，这里只是两个log，那么后续有非常多的log，写起join来就会相当麻烦，甚至会有上千行代码去作逻辑处理，效率上也会差很多。
所以就衍生了接下来主要讲的用法，基于物化视图实现有限join场景。主要是多个不同日志指标的合并。其实更应该理解为将两部分日志聚合计算后union all至物化视图然后存储到存储表供业务人员分析。
```

10.2 修改物化视图计算逻辑
```shell script
alter table dwm.mainpage_stat_mv_local add column if not exists acta_uv AggregateFunction(uniqCombined,UInt32) comment 'acta_uv';
alter table dwm.mainpage_stat_mv_local add column if not exists acta_cnt SimpleAggregateFunction(sum,UInt64) comment 'acta_cnt';
alter table dwm.mainpage_stat_mv_local add column if not exists actb_uv AggregateFunction(uniqCombined,UInt32) comment 'actb_uv';
alter table dwm.mainpage_stat_mv_local add column if not exists actb_cnt SimpleAggregateFunction(sum,UInt64) comment 'actb_cnt';
alter table dwm.mainpage_stat_mv_local add column if not exists actc_uv AggregateFunction(uniqCombined,UInt32) comment 'actc_uv';
alter table dwm.mainpage_stat_mv_local add column if not exists actc_cnt SimpleAggregateFunction(sum,UInt64) comment 'actc_cnt';
alter table dwm.mainpage_stat_mv_local add column if not exists show_bm AggregateFunction(groupBitmap,UInt32) comment 'show_bm';
alter table dwm.mainpage_stat_mv_local add column if not exists click_bm AggregateFunction(groupBitmap,UInt32) comment 'click_bm';
alter table dwm.mainpage_stat_mv_local add column if not exists acta_bm AggregateFunction(groupBitmap,UInt32) comment 'acta_bm';
alter table dwm.mainpage_stat_mv_local add column if not exists actb_bm AggregateFunction(groupBitmap,UInt32) comment 'actb_bm';
alter table dwm.mainpage_stat_mv_local add column if not exists actc_bm AggregateFunction(groupBitmap,UInt32) comment 'actc_bm';
alter table dwm.mainpage_stat_mv_local add column if not exists actd_bm AggregateFunction(groupBitmap,UInt32) comment 'actd_bm';
```
```shell script
CREATE MATERIALIZED VIEW dwm.mv_main_page_stat_mv_001_local to dwm.mainpage_stat_mv_local (
day Date comment '数据分区-天'
,hour DateTime comment '数据时间-小时(DateTime)'
,platform String comment '平台 android/ios'
,ver String comment '版本'
,item_id UInt32 comment '物品id'
,gender String  comment '性别'
,shown_uv AggregateFunction(uniqCombined,UInt32) comment '曝光人数'
,shown_cnt SimpleAggregateFunction(sum,UInt64) comment '曝光次数'
,click_uv AggregateFunction(uniqCombined,UInt32) comment '点击人数'
,click_cnt SimpleAggregateFunction(sum,UInt64) comment '点击次数'
,show_time_sum  SimpleAggregateFunction(sum,UInt64) comment '总曝光时间/秒'
,show_bm AggregateFunction(groupBitmap,UInt32) comment 'show_bm'
,click_bm AggregateFunction(groupBitmap,UInt32) comment 'click_bm'
)
AS 
 SELECT day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,dictGet('dim.dict_user_dim', 'gender',toUInt64(uid)) as gender
     ,uniqCombinedStateIf(uid,a.show_cnt>0) as shown_uv
     ,sum(a.show_cnt) as show_cnt
     ,uniqCombinedStateIf(uid,a.click_cnt>0) as click_uv
     ,sum(a.click_cnt) as click_cnt
     ,sum(toUInt64(show_time/1000)) as show_time_sum
     ,groupBitmapStateIf(uid,a.show_cnt>0) as show_bm
     ,groupBitmapStateIf(uid,a.click_cnt>0) as click_bm
from ods.action_001_local as a
group by
      day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,gender;
```
```shell script
CREATE MATERIALIZED VIEW dwm.mv_main_page_stat_mv_002_local to dwm.mainpage_stat_mv_local (
day Date comment '数据分区-天'
,hour DateTime comment '数据时间-小时(DateTime)'
,platform String comment '平台 android/ios'
,ver String comment '版本'
,item_id UInt32 comment '物品id'
,gender String  comment '性别'
,acta_uv AggregateFunction(uniqCombined,UInt32) comment 'acta_uv'
,acta_cnt SimpleAggregateFunction(sum,UInt64) comment 'acta_cnt'
,actb_uv AggregateFunction(uniqCombined,UInt32) comment 'actb_uv'
,actb_cnt SimpleAggregateFunction(sum,UInt64) comment 'actb_cnt'
,actc_uv AggregateFunction(uniqCombined,UInt32) comment 'actc_uv'
,actc_cnt SimpleAggregateFunction(sum,UInt64) comment 'actc_cnt'
,acta_bm AggregateFunction(groupBitmap,UInt32) comment 'acta_bm'
,actb_bm AggregateFunction(groupBitmap,UInt32) comment 'actb_bm'
,actc_bm AggregateFunction(groupBitmap,UInt32) comment 'actc_bm'
,actd_bm AggregateFunction(groupBitmap,UInt32) comment 'actd_bm'
)
AS 
 SELECT day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,dictGet('dim.dict_user_dim', 'gender',toUInt64(uid)) as gender
     ,uniqCombinedStateIf(uid,a.action_a_cnt>0) as acta_uv
     ,sum(a.action_a_cnt) as acta_cnt
     ,uniqCombinedStateIf(uid,a.action_b_cnt>0) as actb_uv
     ,sum(a.action_b_cnt) as actb_cnt
     ,uniqCombinedStateIf(uid,a.action_c_cnt>0) as actc_uv
     ,sum(a.action_c_cnt) as actc_cnt
     ,groupBitmapStateIf(uid,a.action_a_cnt>0) as acta_bm
     ,groupBitmapStateIf(uid,a.action_b_cnt>0) as actb_bm
     ,groupBitmapStateIf(uid,a.action_c_cnt>0) as actc_bm
     ,groupBitmapStateIf(uid,a.action_d_sum>0) as actd_bm
from ods.action_002_local as a
group by
      day
     ,hour
     ,platform
     ,ver
     ,item_id
     ,gender;
```

10.3 多个日志指标的合并查询
```shell script
SELECT
    day,
    gender,
    uniqCombinedMerge(shown_uv) AS shown_uv,
    uniqCombinedMerge(click_uv) AS click_uv,
    uniqCombinedMerge(acta_uv) AS acta_uv,
    uniqCombinedMerge(actb_uv) AS actb_uv,
    uniqCombinedMerge(actc_uv) AS actc_uv
FROM dwm.mainpage_stat_mv_local
WHERE day = '2021-06-13'
GROUP BY
    day,
    gender;
```

10.4 基于bitmap的用户行为分析
```shell script
SELECT
    day,
    gender,
    bitmapCardinality(groupBitmapMergeState(show_bm)) AS shown_uv,
    bitmapAndCardinality(groupBitmapMergeState(show_bm), groupBitmapMergeState(click_bm)) AS show_click_uv,
    bitmapAndCardinality(groupBitmapMergeState(show_bm), bitmapAnd(groupBitmapMergeState(click_bm), groupBitmapMergeState(acta_bm))) AS show_click_a_uv,
    bitmapAndCardinality(groupBitmapMergeState(show_bm), bitmapAnd(bitmapAnd(groupBitmapMergeState(click_bm), groupBitmapMergeState(acta_bm)), groupBitmapMergeState(actb_bm))) AS show_click_ab_uv,
    bitmapAndCardinality(groupBitmapMergeState(show_bm), bitmapAnd(bitmapAnd(bitmapAnd(groupBitmapMergeState(click_bm), groupBitmapMergeState(acta_bm)), groupBitmapMergeState(actb_bm)), groupBitmapMergeState(actc_bm))) AS show_click_abc_uv,
    bitmapAndCardinality(groupBitmapMergeState(show_bm), bitmapAnd(bitmapAnd(bitmapAnd(bitmapAnd(groupBitmapMergeState(click_bm), groupBitmapMergeState(acta_bm)), groupBitmapMergeState(actb_bm)), groupBitmapMergeState(actc_bm)), groupBitmapMergeState(actd_bm))) AS show_click_abcd_uv
FROM dwm.mainpage_stat_mv_local
WHERE day = '2021-06-13'
GROUP BY
    day,
    gender;
```

11. 总结
```text
物化视图是clickhouse一个非常重要的功能，同时也做了很多优化和函数扩展，虽然在某些情况可能会带来一定的风险（比如增加错误字段导致写入失败等问题），但是也是可以在使用中留意避免的，不能因噎废食。
本文主要讲解了：
·物化视图的创建、新增维度和指标，聚合函数的使用和一些注意事项；
·物化视图结合字典的使用；
·通过物化视图组合指标宽表。
```