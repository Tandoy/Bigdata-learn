YARN概述

Yarn是一个资源调度平台，负责为运算程序提供服务器运算资源，相当于一个分布式的操作系统平台，而mapreduce等运算程序则相当于运行于操作系统之上的应用程序

3.3.2 YARN的重要概念

    1、	yarn并不清楚用户提交的程序的运行机制
    2、	yarn只提供运算资源的调度（用户程序向yarn申请资源，yarn就负责分配资源）
    3、	yarn中的主管角色叫ResourceManager
    4、	yarn中具体提供运算资源的角色叫NodeManager
    5、	这样一来，yarn其实就与运行的用户程序完全解耦，就意味着yarn上可以运行各种类型的分布式运算程序（mapreduce只是其中的一种），比如mapreduce、storm程序，spark程序，tez ……
    6、	所以，spark、storm等运算框架都可以整合在yarn上运行，只要他们各自的框架中有符合yarn规范的资源请求机制即可
    7、	Yarn就成为一个通用的资源调度平台，从此，企业中以前存在的各种运算集群都可以整合在一个物理集群上，提高资源利用率，方便数据共享

3.3.3 Yarn中运行运算程序的示例

mapreduce程序的调度过程，如下图

![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Hadoop/Yarn/images/MR%E8%B0%83%E5%BA%A6%E8%BF%87%E7%A8%8B.png)
