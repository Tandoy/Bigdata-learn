 #hive调优参数篇
 
 ● 使用动态分区需要加配置：

    SET hive.exec.dynamic.partition=true;
    SET hive.exec.dynamic.partition.mode=nonstrict;
    SET hive.exec.max.dynamic.partitions=1000;
    SET hive.exec.max.dynamic.partitions.pernode=100;


 为了防止一个reduce处理写入一个分区导致速度严重降低，加入如下参数

    SET hive.optimize.sort.dynamic.partition=false;

 
 ● 使用自动优化：（mapjoin）

 首先需要注意，这个有如下限制条件

     1. Full outer joins are never converted to map-side joins.
     2. A left-outer join are converted to a map join only if the right table that is to the right side of the join conditions, is lesser than 25 MB in size.
     3. Similarly, a right-outer join is converted to a map join only if the left table size is lesser than 25 MB.
     set hive.auto.convert.join=true;
     SET hive.mapjoin.smalltable.filesize=25000000; -- 这个是设定放入内存的表的大小上限的
     set hive.auto.convert.join.noconditionaltask=true; --设置多mapjoin同时执行（a left join small_b left join small_c），而不是起多次map任务
     set hive.auto.convert.join.noconditionaltask.size=30000000;


 ● 使用负载均衡：

    SET hive.groupby.skewindata=true;

 从上面group by语句可以看出，这个变量是用于控制负载均衡的。当数据出现倾斜时，如果该变量设置为true，那么Hive会自动进行负载均衡

 比如A日志表与B码表join，但是A中的关联字段id仅是B中id的一小部分，这时候很容易出现reduce阶段倾斜，大量的reduce空跑，因为这些空跑的reduce分到的B的id在A中不存在。



 ● 设置map和reduce的任务处理的字节数

     SET mapreduce.input.fileinputformat.split.maxsize=67108864;
     SET mapreduce.input.fileinputformat.split.minsize=67108864;
     SET mapreduce.input.fileinputformat.split.minsize.per.node=67108864;
     SET mapreduce.input.fileinputformat.split.minsize.per.rack=67108864;
     SET hive.exec.reducers.bytes.per.reducer=268435456; （可以设置小一点，因为默认的是min(集群配置的，总数据量/本设置值)）


 ● 直接设置map和reduce任务数

    SET mapred.map.tasks = 400;
    SET mapred.reduce.tasks = 400;


 ● 设置最大reduce数限制

    SET hive.exec.reducers.max=1024;


 ● 修改字段类型（注意跟presto集成会有问题）

    ALTER TABLE name CHANGE column_name new_name new_type

 

 ● 设置运行内存，应对运行时报错：java 堆内存溢出

     SET mapreduce.map.memory.mb=8000;
     SET mapreduce.map.java.opts=-Xmx6000m;
     SET mapreduce.reduce.memory.mb=8000;
     SET mapreduce.reduce.java.opts=-Xmx6000m;
