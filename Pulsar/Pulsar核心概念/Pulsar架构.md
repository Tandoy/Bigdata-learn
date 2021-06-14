## Pulsar架构

    在最高级别，单个 Pulsar 实例由一个或多个 Pulsar 集群组成。实例中的集群之间可以相互复制数据。
    
    单个 Pulsar 集群由以下三部分组成：
    
    一个或者多个 broker 负责处理和负载均衡 producer 发出的消息，并将这些消息分派给 consumer；Broker 与 Pulsar 配置存储交互来处理相应的任务，并将消息存储在 BookKeeper 实例中（又称 bookies）；Broker 依赖 ZooKeeper 集群处理特定的任务，等等。
    包含一个或多个 bookie 的 BookKeeper 集群负责消息的持久化存储。
    一个Zookeeper集群，用来处理多个Pulsar集群之间的协调任务。
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Pulsar/images/pulsar-system-architecture.png)


### 一、Brokers
    
    Pulsar的broker是一个无状态组件, 主要负责运行另外的两个组件:
        ·An HTTP server
        ·一个调度分发器
        
### 二、持久化存储

    Apache BookKeeper
    Pulsar用 Apache BookKeeper作为持久化存储。 BookKeeper是一个分布式的预写日志（WAL）系统，有如下几个特性特别适合Pulsar的应用场景：
    
    能让Pulsar创建多个独立的日志，这种独立的日志就是ledgers. 随着时间的推移，Pulsar会为Topic创建多个ledgers。
    为按条目复制的顺序数据提供了非常高效的存储。
    保证了多系统挂掉时ledgers的读取一致性。
    提供不同的Bookies之间均匀的IO分布的特性。
    容量和吞吐量都能水平扩展。并且容量可以通过在集群内添加更多的Bookies立刻提升。
    Bookies被设计成可以承载数千的并发读写的ledgers。 使用多个磁盘设备，一个用于日志，另一个用于一般存储，这样Bookies可以将读操作的影响和对于写操作的延迟分隔开。
    除了消息数据，cursors也会被持久化入BookKeeper。 Cursors是消费端订阅消费的位置。 BookKeeper让Pulsar可以用一种可扩展的方式存储消费位置。

    