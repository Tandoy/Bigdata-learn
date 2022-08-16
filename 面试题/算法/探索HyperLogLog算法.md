## 探索HyperLogLog算法

### 介绍
```text
基数就是指一个集合中不同值的数目，比如[a,b,c,d]的基数就是4，[a,b,c,d,a]的基数还是4，因为a重复了一个，不算。基数也可以称之为Distinct Value，简称DV。
HyperLogLog算法经常在数据库中被用来统计某一字段的Distinct Value（下文简称DV），可以使用固定大小的字节计算任意大小的DV。HyperLogLog算法就是用来计算基数的。
```
### 