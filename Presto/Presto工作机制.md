## 一、Presto工作机制

    Presto的体系结构几乎类似于经典的MPP（大规模并行处理）DBMS体系结构
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Presto/images/Presto%E4%BD%93%E7%B3%BB%E6%9E%B6%E6%9E%84.jpg)

    Client
    客户端（Presto CLI）将SQL语句提交给协调器以获取结果。
    
    Connectors
    协调器是主守护程序。 协调器首先解析SQL查询，然后分析和计划查询执行。 调度程序执行管道执行，将工作分配给最近的节点并监视进度。
    
    Coordlnator
    存储插件称为连接器。 Hive ，HBase，MySQL， Cassandra等充当连接器； 否则，您也可以实现自定义。 连接器为查询提供元数据和数据。 协调器使用连接器获取用于构建查询计划的元数据。
    
    Workers
    协调器将任务分配给工作节点。 工作人员从连接器获取实际数据。 最后，工作程序节点将结果传递给客户端。
    
## 二、Presto工作流程

    Presto是在节点集群上运行的分布式系统。 Presto的分布式查询引擎针对交互式分析进行了优化，并支持标准的ANSI SQL，包括复杂的查询，聚合，联接和窗口函数。 Presto体系结构既简单又可扩展。 Presto客户端（CLI）将SQL语句提交给主守护程序协调器。
    调度程序通过执行管道连接。 调度程序将工作分配给最接近数据的节点并监视进度。 协调器将任务分配给多个工作节点，最后工作节点将结果传递回客户端。 客户端从输出过程中提取数据。 可扩展性是关键设计。 Hive ，HBase，MySQL等可插拔连接器为查询提供元数据和数据。 Presto具有“简单的存储抽象”设计，可以轻松针对这些不同类型的数据源提供SQL查询功能。
