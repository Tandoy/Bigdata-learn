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
    5.1 
```