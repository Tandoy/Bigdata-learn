#数据倾斜表现
1）hadoop中的数据倾斜表现：

l 有一个多几个Reduce卡住，卡在99.99%，一直不能结束。

l 各种container报错OOM

l 异常的Reducer读写的数据量极大，至少远远超过其它正常的Reducer

l 伴随着数据倾斜，会出现任务被kill等各种诡异的表现。

2）hive中数据倾斜

一般都发生在Sql中group by和join on上，而且和数据逻辑绑定比较深。

3）Spark中的数据倾斜

Spark中的数据倾斜，包括Spark Streaming和Spark Sql，表现主要有下面几种：

l Executor lost，OOM，Shuffle过程出错；

l Driver OOM；

l 单个Executor执行时间特别久，整体任务卡在某个阶段不能结束；

l 正常运行的任务突然失败；

#数据倾斜产生原因
1）key分布不均匀；

2）建表时考虑不周

我们举一个例子，就说数据默认值的设计吧，假设我们有两张表：

user（用户信息表）：userid，register_ip

ip（IP表）：ip，register_user_cnt

这可能是两个不同的人开发的数据表。如果我们的数据规范不太完善的话，会出现一种情况：

user表中的register_ip字段，如果获取不到这个信息，我们默认为null；

但是在ip表中，我们在统计这个值的时候，为了方便，我们把获取不到ip的用户，统一认为他们的ip为0。

两边其实都没有错的，但是一旦我们做关联了，这个任务会在做关联的阶段，也就是sql的on的阶段卡死。

3）业务数据激增

比如订单场景，我们在某一天在北京和上海两个城市多了强力的推广，结果可能是这两个城市的订单量增长了10000%，其余城市的数据量不变。

然后我们要统计不同城市的订单情况，这样，一做group操作，可能直接就数据倾斜了。

#解决数据倾斜思路

很多数据倾斜的问题，都可以用和平台无关的方式解决，比如更好的数据预处理，异常值的过滤等。因此，解决数据倾斜的重点在于对数据设计和业务的理解，这两个搞清楚了，数据倾斜就解决了大部分了。

1）业务逻辑

我们从业务逻辑的层面上来优化数据倾斜，比如上面的两个城市做推广活动导致那两个城市数据量激增的例子，我们可以单独对这两个城市来做count，单独做时可用两次MR，第一次打散计算，第二次再最终聚合计算。完成后和其它城市做整合。

2）程序层面

比如说在Hive中，经常遇到count(distinct)操作，这样会导致最终只有一个Reduce任务。

我们可以先group by，再在外面包一层count，就可以了。比如计算按用户名去重后的总用户量：

（1）优化前 只有一个reduce，先去重再count负担比较大：

select name,count(distinct name)from user;

（2）优化后

// 设置该任务的每个job的reducer个数为3个。Hive默认-1，自动推断。

set mapred.reduce.tasks=3;

// 启动两个job，一个负责子查询(可以有多个reduce)，另一个负责count(1)：

select count(1) from (select name from user group by name) tmp;

3）调参方面

Hadoop和Spark都自带了很多的参数和机制来调节数据倾斜，合理利用它们就能解决大部分问题。

4）从业务和数据上解决数据倾斜

很多数据倾斜都是在数据的使用上造成的。我们举几个场景，并分别给出它们的解决方案。

l 有损的方法：找到异常数据，比如ip为0的数据，过滤掉

l 无损的方法：对分布不均匀的数据，单独计算

l 先对key做一层hash，先将数据随机打散让它的并行度变大，再汇集

l 数据预处理

定位导致数据倾斜代码
Spark数据倾斜只会发生在shuffle过程中。

这里给大家罗列一些常用的并且可能会触发shuffle操作的算子：distinct、groupByKey、reduceByKey、aggregateByKey、join、cogroup、repartition等。

出现数据倾斜时，可能就是你的代码中使用了这些算子中的某一个所导致的。

4.1 某个task执行特别慢的情况
首先要看的，就是数据倾斜发生在第几个stage中：

如果是用yarn-client模式提交，那么在提交的机器本地是直接可以看到log，可以在log中找到当前运行到了第几个stage；

如果是用yarn-cluster模式提交，则可以通过Spark Web UI来查看当前运行到了第几个stage。

此外，无论是使用yarn-client模式还是yarn-cluster模式，我们都可以在Spark Web UI上深入看一下当前这个stage各个task分配的数据量，从而进一步确定是不是task分配的数据不均匀导致了数据倾斜。

看task运行时间和数据量

task运行时间

比如下图中，倒数第三列显示了每个task的运行时间。明显可以看到，有的task运行特别快，只需要几秒钟就可以运行完；而有的task运行特别慢，需要几分钟才能运行完，此时单从运行时间上看就已经能够确定发生数据倾斜了。

task数据量

此外，倒数第一列显示了每个task处理的数据量，明显可以看到，运行时间特别短的task只需要处理几百KB的数据即可，而运行时间特别长的task需要处理几千KB的数据，处理的数据量差了10倍。此时更加能够确定是发生了数据倾斜。

推断倾斜代码

知道数据倾斜发生在哪一个stage之后，接着我们就需要根据stage划分原理，推算出来发生倾斜的那个stage对应代码中的哪一部分，这部分代码中肯定会有一个shuffle类算子。

精准推算stage与代码的对应关系，需要对Spark的源码有深入的理解，这里我们可以介绍一个相对简单实用的推算方法：只要看到Spark代码中出现了一个shuffle类算子或者是Spark SQL的SQL语句中出现了会导致shuffle的语句（比如group by语句），那么就可以判定，以那个地方为界限划分出了前后两个stage。

这里我们就以如下单词计数来举例。

val conf = new SparkConf()val sc = new SparkContext(conf)val lines = sc.textFile(“hdfs://…”)val words = lines.flatMap(_.split(” “))val pairs = words.map((_, 1))val wordCounts = pairs.reduceByKey(_ + _)wordCounts.collect().foreach(println(_))

在整个代码中只有一个reduceByKey是会发生shuffle的算子，也就是说这个算子为界限划分出了前后两个stage：

stage0，主要是执行从textFile到map操作，以及shuffle write操作（对pairs RDD中的数据进行分区操作，每个task处理的数据中，相同的key会写入同一个磁盘文件内）。

stage1，主要是执行从reduceByKey到collect操作，以及stage1的各个task一开始运行，就会首先执行shuffle read操作（会从stage0的各个task所在节点拉取属于自己处理的那些key，然后对同一个key进行全局性的聚合或join等操作，在这里就是对key的value值进行累加）

stage1在执行完reduceByKey算子之后，就计算出了最终的wordCounts RDD，然后会执行collect算子，将所有数据拉取到Driver上，供我们遍历和打印输出。

123456789

通过对单词计数程序的分析，希望能够让大家了解最基本的stage划分的原理，以及stage划分后shuffle操作是如何在两个stage的边界处执行的。然后我们就知道如何快速定位出发生数据倾斜的stage对应代码的哪一个部分了。

比如我们在Spark Web UI或者本地log中发现，stage1的某几个task执行得特别慢，判定stage1出现了数据倾斜，那么就可以回到代码中，定位出stage1主要包括了reduceByKey这个shuffle类算子，此时基本就可以确定是是该算子导致了数据倾斜问题。

此时，如果某个单词出现了100万次，其他单词才出现10次，那么stage1的某个task就要处理100万数据，整个stage的速度就会被这个task拖慢。

4.2 某个task莫名其妙内存溢出的情况
这种情况下去定位出问题的代码就比较容易了。我们建议直接看yarn-client模式下本地log的异常栈，或者是通过YARN查看yarn-cluster模式下的log中的异常栈。一般来说，通过异常栈信息就可以定位到你的代码中哪一行发生了内存溢出。然后在那行代码附近找找，一般也会有shuffle类算子，此时很可能就是这个算子导致了数据倾斜。

但是大家要注意的是，不能单纯靠偶然的内存溢出就判定发生了数据倾斜。因为自己编写的代码的bug，以及偶然出现的数据异常，也可能会导致内存溢出。因此还是要按照上面所讲的方法，通过Spark Web UI查看报错的那个stage的各个task的运行时间以及分配的数据量，才能确定是否是由于数据倾斜才导致了这次内存溢出。

5 查看导致数据倾斜的key分布情况
先对pairs采样10%的样本数据，然后使用countByKey算子统计出每个key出现的次数，最后在客户端遍历和打印样本数据中各个key的出现次数。

val sampledPairs = pairs.sample(false, 0.1)

val sampledWordCounts = sampledPairs.countByKey()

sampledWordCounts.foreach(println(_))