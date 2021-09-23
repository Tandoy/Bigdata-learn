### Flink内核原理与实现

#### 1.Flink入门
```text
1.批流一体（有界数据+无界数据），但现在生产上还不成熟，众多还是kappa结构与lambda架构相结合
2.依赖检查点进行容错
3.支持有状态计算：其实就是可以结合历史数据进行计算，例如反欺诈行为的识别，要根据用户近几分钟之内的行为数据进行判断。一旦出现异常就需要重新执行流任务，但处理所有的历史数据是不实际的，在Flink使用State能够使流计算恢复到近期的一个检查点。
4.SQL <-- Table <-- DataStream/DataSet <-- Process Function
5.运行时架构：JM: 负责集群和作业管理等；TM: 负责任务的执行等
```
#### 2.Flink应用
```text
数据流上的类型和操作
```
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Flink/image/%E6%B5%81%E8%BD%AC%E6%8D%A2.png)

[数据流上的类型和操作](http://wuchong.me/blog/2016/05/20/flink-internals-streams-and-operations-on-streams/)

#### 3.核心抽象
```text
一、环境
    Environment：运行时环境
    StreamExecutionEnvironment：执行环境
    RuntimeContext：运行时上下文

二、StreamElement（数据流元素）
    Watermark：水位线
    StreamStatus：状态标记
    StreamRecord：数据记录
    LatencyMarker：延迟标记

三、数据转换（Transformation）
    3.1 Transformation主要分为两类：PhysicalTransformation和虚拟Transformation；在运行时DataSteam调用的api都会转换成Transformation，然后从Transformation转换成底层算子。
        PhysicalTransformation会转换成算子，但虚拟Transformation不会形成实体的算子例如： Reblance、Union、Split、Select等

四、算子（单流输入算子+双流输入算子+数据源算子+异步算子）
    4.1 生命周期管理
    4.2 状态与容错管理
    4.3 数据处理

五、函数体系
    5.1 SourceFunction、一般Function（双流输入、单流输入）、SinkFunction
    5.2 函数层级：
        5.2.1 无状态函数：UDF接口 如MapFunction等
        5.2.2 RichFunction：状态管理+生命周期+UDF接口 如RichMapFunction等
        5.2.3 ProcessFunction：状态管理+生命周期+UDF接口+触发器 如JoinFunction等

六、数据分区
    6.1 用户自定义分区器
    6.2 ForwardPartitioner：在同一个操作链中直接将数据传递至下游
    6.3 SbuftlePartitioner：随机将元素进行分区，确保下游task均匀接收数据
    6.4 ReblancePartitioner：也轮询的方式进行分区分配
    6.5 RescalingPartitioner：根据下游task并行度进行分区，且不会向未分配给自己的分区写入数据
    6.6 BroadcastPartitioner：将记录广播之所有分区，即复制N份
    6.7 KeyGroupSteamPartitioner：根据KeyGroup索引进行分区，该分区不提供给用户使用

七、分布式IP
    Flink的作业管理、资源管理、TM等都需要分布式ID。
```