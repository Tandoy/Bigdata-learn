[Flink CDC 实现数据增量备份到 Clickhouse](https://mp.weixin.qq.com/s/0wbG-u964UVAubKnzkqtxw)

```
基于上文有如下思考：
    1.几种CDC采集工具 Debezium > Maxwell > Flink CDC > Canal。
    2.Clickhouse数据重分布繁琐：目前采用方法是通过低效的数据重新导入的方式来进行人工平衡
```