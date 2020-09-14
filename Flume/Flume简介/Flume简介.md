Flume介绍
1.1.1 概述

	Flume是一个分布式、可靠、和高可用的海量日志采集、聚合和传输的系统。

	Flume可以采集文件，socket数据包等各种形式源数据，又可以将采集到的数据输出到HDFS、hbase、hive、kafka等众多外部存储系统中

	一般的采集需求，通过对flume的简单配置即可实现

	Flume针对特殊场景也具备良好的自定义扩展能力，因此，flume可以适用于大部分的日常数据采集场景


1.1.2 运行机制

1、	Flume分布式系统中最核心的角色是agent，flume采集系统就是由一个个agent所连接起来形成
2、	每一个agent相当于一个数据传递员，内部有三个组件：
a)	Source：采集源，用于跟数据源对接，以获取数据
b)	Sink：下沉地，采集数据的传送目的，用于往下一级agent传递数据或者往最终存储系统传递数据
c)	Channel：angent内部的数据传输通道，用于从source将数据传递到sink

![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Flume/images/Flume%E6%9E%B6%E6%9E%84.png)



1.1.4 Flume采集系统结构图

1. 简单结构

单个agent采集数据
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Flume/images/Flume%E9%87%87%E9%9B%86%E7%AE%80%E5%8D%95%E6%A8%A1%E5%BC%8F.png)

2. 复杂结构

多级agent之间串联
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Flume/images/Flume%E9%87%87%E9%9B%86%E5%A4%8D%E6%9D%82%E6%A8%A1%E5%BC%8F.png)
 
