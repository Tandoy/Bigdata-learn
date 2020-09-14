mapreduce的shuffle机制
3.1.1 概述：
	mapreduce中，map阶段处理的数据如何传递给reduce阶段，是mapreduce框架中最关键的一个流程，这个流程就叫shuffle；
	shuffle: 洗牌、发牌——（核心机制：数据分区，排序，缓存）；
	具体来说：就是将maptask输出的处理结果数据，分发给reducetask，并在分发的过程中，对数据按key进行了分区和排序；

3.1.2 主要流程：
Shuffle缓存流程：
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Hadoop/MapReduce/images/shuffle%E7%BC%93%E5%AD%98%E6%B5%81%E7%A8%8B.png)

shuffle是MR处理流程中的一个过程，它的每一个处理步骤是分散在各个map task和reduce task节点上完成的，整体来看，分为3个操作：
1、分区partition
2、Sort根据key排序
3、Combiner进行局部value的合并

3.1.3 详细流程
1、	maptask收集我们的map()方法输出的kv对，放到内存缓冲区中
2、	从内存缓冲区不断溢出本地磁盘文件，可能会溢出多个文件
3、	多个溢出文件会被合并成大的溢出文件
4、	在溢出过程中，及合并的过程中，都要调用partitoner进行分组和针对key进行排序
5、	reducetask根据自己的分区号，去各个maptask机器上取相应的结果分区数据
6、	reducetask会取到同一个分区的来自不同maptask的结果文件，reducetask会将这些文件再进行合并（归并排序）
7、	合并成大文件后，shuffle的过程也就结束了，后面进入reducetask的逻辑运算过程（从文件中取出一个一个的键值对group，调用用户自定义的reduce()方法）

Shuffle中的缓冲区大小会影响到mapreduce程序的执行效率，原则上说，缓冲区越大，磁盘io的次数越少，执行速度就越快 
缓冲区的大小可以通过参数调整,  参数：io.sort.mb  默认100M

3.1.4 详细流程示意图
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Hadoop/MapReduce/images/%E8%AF%A6%E7%BB%86%E6%B5%81%E7%A8%8B%E7%A4%BA%E6%84%8F%E5%9B%BE.png)
