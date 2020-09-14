1.	hbase简介
1.1.	什么是hbase
HBASE是一个高可靠性、高性能、面向列、可伸缩的分布式存储系统，利用HBASE技术可在廉价PC Server上搭建起大规模结构化存储集群。
HBASE的目标是存储并处理大型的数据，更具体来说是仅需使用普通的硬件配置，就能够处理由成千上万的行和列所组成的大型数据。
HBASE是Google Bigtable的开源实现，但是也有很多不同之处。比如：Google Bigtable利用GFS作为其文件存储系统，HBASE利用Hadoop HDFS作为其文件存储系统；Google运行MAPREDUCE来处理Bigtable中的海量数据，HBASE同样利用Hadoop MapReduce来处理HBASE中的海量数据；Google Bigtable利用Chubby作为协同服务，HBASE利用Zookeeper作为对应。
1.2.	与传统数据库的对比
1、传统数据库遇到的问题：
1）数据量很大的时候无法存储
2）没有很好的备份机制
3）数据达到一定数量开始缓慢，很大的话基本无法支撑
 2、HBASE优势：
1）线性扩展，随着数据量增多可以通过节点扩展进行支撑
2）数据存储在hdfs上，备份机制健全
3）通过zookeeper协调查找数据，访问速度块。
1.3.	hbase集群中的角色
1、一个或者多个主节点，Hmaster
2、多个从节点，HregionServer