## Hudi-On-Flink答疑解惑

#### 1.存储⼀直看不到数据
```text
如果是 streaming 写的话，请确保开启 checkpoint，Flink 的 writer 有两种刷数据到磁盘的策略：
1.当某个 bucket 在内存积攒到一定大小 (可配，默认 64MB)
2.当 checkpoint 触发，将内存里的数据全部 flush 出去
```

#### 2.数据有重复
```text
1.如果是 Copy On Write 写的话，需要开启参数 write.insert.drop.duplicates 为 true，COW 写每个 bucket 的第一个文件默认是不去重的，只有增量的数据会去重，如果要全局去重，需要开启该参数。
2.Merge On Read 写不需要开启任何参数，定义好 primary key 后默认全局去重。
3.如果需要多 partition 去重，需要开启参数: index.global.enabled 为 true
```

#### 3.Merge On Read 写只有log⽂件
```text
Merge On Read 默认开启了异步的 compaction，策略是 5 个 commits 压缩一次，当条件满足参会触发压缩任务，另外，压缩本身因为耗费资源，所以不一定能跟上写入效率，所以可能会有滞后。
可以先观察 log，搜索 compaction 关键词，看下是否有 compact 任务调度:After filtering, Nothing to compact for 关键词说明本次 compaction strategy 是不做压缩。
```

#### 4.影响内存占用的参数
```text
1.write.task.max.size: 1024MB
一个 write task 的最大可用内存，当前预留给 write buffer 的内存
2.write.task.max.size - compaction.max_memory
当 write task 的内存 buffer 打到阈值后会将内存里最大的 buffer flush 出去
3.write.batch.size: 64MB
Flink 的写 task 为了提高写数据效率，会按照写 bucket 提前 buffer 数据，每个 bucket 的数据在内存达到阈值之前会一直 cache 在内存中，当阈值达到会把数据 buffer 传递给 hoodie 的 writer 执行写操作
4.write.log_block.size.MB: 128MB
hoodie 的 log writer 在收到 write task 的数据后不会马上 flush 数据，writer 是以 LogBlock 为单位往磁盘刷数据的，在 LogBlock 攒够之前 records 会以序列化字节的形式 buffer 在 writer 内部
5.write.merge.max_memory: 100MB
hoodie 在 COW 写操作的时候，会有增量数据和 base file 数据 merge 的过程，增量的数据会缓存在内存的 map 结构里，这个 map 是可 spill 的，这个参数控制了 map 可以使用的堆内存大小
6.compaction.max_memory: 100MB
同 write.merge.max_memory: 100MB 类似，只是发⽣在压缩时。
7.注意：我们在内存调优的时候需要先关注 TaskManager 的数量和内存配置，以及 write task 的并发。即 write.tasks: 4 的值，确认每个 write task 能够分配到的内存，再考虑相关的内存参数设置。
```

#### 5.如何开启Checkpoint：
```xml
1.集群级别
flink-conf.yaml 设置如下参数
### set the interval as 5 minutes
execution.checkpointing.interval: 300000
state.backend: filesystem
### state.backend: rocksdb
### state.backend.incremental: true
state.checkpoints.dir: xxx
state.savepoints.dir: xxx
2.Per-Job 级别
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setStateBackend(new HashMapStateBackend());
```
#### 6.内存优化
```text
1.MOR
    1.1 state backend换成rocksdb(默认的in-memory state-backend⾮常吃内存)
    1.2 内存够的话，compaction.max_memory调⼤些 (默认是100MB可以调到1GB)
    1.3 关注TM分配给每个write task的内存，保证每个write task能够分配到write.task.max.size所配置的⼤⼩，⽐如TM的内存是4GB跑了2个StreamWriteFunction那每个write function能分到2GB，尽量预留⼀些 buffer，因为⽹络buffer，TM上其他类型task(⽐如Bucket AssignFunction也会吃些内存)
    1.4 需要关注compaction的内存变化，compaction.max_memory控制了每个compaction task读log时可以利⽤的内存⼤⼩， compaction.tasks控制了compaction task的并发
        Note: write.task.max.size-write.merge.max_memory是预留给每个write task的内存buffer

2.COW
    2.1 state backend换成rocksdb(默认的in-memory state-backend⾮常吃内存)
    2.2 write.task.max.size和write.merge.max_memory同时调⼤(默认是1GB和100MB可以调到2GB和1GB)
    2.3 关注TM分配给每个write task的内存，保证每个write task能够分配到write.task.max.size所配置的⼤⼩，⽐如TM的内存是4GB跑了2个StreamWriteFunction那每个write function能分到 2GB，尽量预留⼀些 buffer，因为⽹络buffer，TM上其他类型task(⽐如Bucket AssignFunction也会吃些内存)
        Note: write.task.max.size-write.merge.max_memory是预留给每个write task的内存buffer
```

[Hudi-On-Flink答疑解惑](https://www.yuque.com/docs/share/01c98494-a980-414c-9c45-152023bf3c17?)

[整合Apache Hudi + Flink + CDH](https://mp.weixin.qq.com/s/FYPdx3y0nJDA7ErFMqJVZA)