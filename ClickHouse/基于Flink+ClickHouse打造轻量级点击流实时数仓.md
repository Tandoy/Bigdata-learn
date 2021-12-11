[基于Flink+ClickHouse打造轻量级点击流实时数仓](https://mp.weixin.qq.com/s/_6YR2ZC4HTwj3aCl9jkZJA)

```
基于上文有如下思考：
    1.文中维度表存储于Mysql，为了避免频繁查询Mysql，AsyncFunction内添加内存缓存（如 Guava Cache、Caffeine 等），并设定合理的缓存驱逐机制。
    2.实时维度关联仅适用于缓慢变化维度，如地理位置信息实时维度关联仅适用于缓慢变化维度，如地理位置信息、商品及分类信息等。快速变化维度（如用户信息）则不太适合打进宽表，采用 MySQL 表引擎将快变维度表直接映射到 ClickHouse 中，而 ClickHouse 支持异构查询，也能够支撑规模较小的维表 join 场景。
    3.需要注意Clickhouse对join的支持差异，例如：比如当 Join 的左表是 subquery，而不是表的时候，ClickHouse 无法进行分布式 Join，只能在分布式表的 Initiator 的单节点进行 Join。详情可见: https://github.com/ClickHouse/ClickHouse/issues/9477
    4.如何进行数据回溯与错误填补：目前采用的是离线数据每天与实时指标进行校验
    5.Clickhouse不支持事务：可参考腾讯云实现外部事务的技术分享：https://cloud.tencent.com/developer/news/688332
    6.目前存在两套埋点技术一套是合作方GIO直接落地Clickhouse，一套是自行前端埋点 Flink-->Clickhouse，如何进行同一服务化
    7.读分布式表，写本地表
```