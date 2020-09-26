1.	Hive基本概念

1.1	 Hive简介

1.1.1	什么是Hive

Hive是基于Hadoop的一个数据仓库工具，可以将结构化的数据文件映射为一张数据库表，并提供类SQL查询功能。
1.1.2	为什么使用Hive

	直接使用hadoop所面临的问题 
人员学习成本太高 
项目周期要求太短 
MapReduce实现复杂查询逻辑开发难度太大 

	为什么要使用Hive 

操作接口采用类SQL语法，提供快速开发的能力。 
避免了去写MapReduce，减少开发人员的学习成本。 
扩展功能很方便。

1.1.3	Hive的特点

	可扩展 
Hive可以自由的扩展集群的规模，一般情况下不需要重启服务。

	延展性 
Hive支持用户自定义函数，用户可以根据自己的需求来实现自己的函数。

	容错 
良好的容错性，节点出现问题SQL仍可完成执行。
1.2	 Hive架构
1.2.1	架构图
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Hive/images/Hive%E6%9E%B6%E6%9E%84%E5%9B%BE.jpg)

Jobtracker是hadoop1.x中的组件，它的功能相当于： Resourcemanager+AppMaster

TaskTracker 相当于：  Nodemanager  +  yarnchild




1.2.2	基本组成
	用户接口：包括 CLI、JDBC/ODBC、WebGUI。
	元数据存储：通常是存储在关系数据库如 mysql , derby中。
	解释器、编译器、优化器、执行器。
1.2.3	各组件的基本功能
	用户接口主要由三个：CLI、JDBC/ODBC和WebGUI。其中，CLI为shell命令行；JDBC/ODBC是Hive的JAVA实现，与传统数据库JDBC类似；WebGUI是通过浏览器访问Hive。
	元数据存储：Hive 将元数据存储在数据库中。Hive 中的元数据包括表的名字，表的列和分区及其属性，表的属性（是否为外部表等），表的数据所在目录等。
	解释器、编译器、优化器完成 HQL 查询语句从词法分析、语法分析、编译、优化以及查询计划的生成。生成的查询计划存储在 HDFS 中，并在随后有 MapReduce 调用执行。
1.3	Hive与Hadoop的关系 
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Hive/images/Hive%E4%B8%8EHadoop%E7%9A%84%E5%85%B3%E7%B3%BB.png)

Hive利用HDFS存储数据，利用MapReduce查询数据

1.4	Hive与传统数据库对比
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/Hive/images/Hive%E4%B8%8E%E4%BC%A0%E7%BB%9F%E6%95%B0%E6%8D%AE%E5%BA%93%E5%AF%B9%E6%AF%94.png)

总结：hive具有sql数据库的外表，但应用场景完全不同，hive只适合用来做批量数据统计分析
1.5	Hive的数据存储
1、Hive中所有的数据都存储在 HDFS 中，没有专门的数据存储格式（可支持Text，SequenceFile，ParquetFile，RCFILE等）
2、只需要在创建表的时候告诉 Hive 数据中的列分隔符和行分隔符，Hive 就可以解析数据。
3、Hive 中包含以下数据模型：DB、Table，External Table，Partition，Bucket。
	db：在hdfs中表现为${hive.metastore.warehouse.dir}目录下一个文件夹
	table：在hdfs中表现所属db目录下一个文件夹
	external table：外部表, 与table类似，不过其数据存放位置可以在任意指定路径
普通表: 删除表后, hdfs上的文件都删了
External外部表删除后, hdfs上的文件没有删除, 只是把文件删除了
	partition：在hdfs中表现为table目录下的子目录
	bucket：桶, 在hdfs中表现为同一个表目录下根据hash散列之后的多个文件, 会根据不同的文件把数据放到不同的文件中 
