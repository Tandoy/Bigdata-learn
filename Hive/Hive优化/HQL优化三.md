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

2）SQL语句优化

  1.大小表Join
  
  使用map join让小的维度表（1000条以下的记录条数） 先进内存，在map端完成reduce。如下：
  
    select /*+ mapjoin(a) */ 
    a.c1, b.c1 ,b.c2
    from a join b 
    where a.c1 = b.c1;
  
  2.大表Join大表
  
  把空值的key变成一个字符串加上随机数，把倾斜的数据分到不同的reduce上，由于null值关联不上，处理后并不影响最终结果。如下：
  
    select * from log a 
    left outer join users b 
    on 
    case when a.user_id is null 
    then concat('hive',rand()) 
    else a.user_id end = b.user_id;
    
  3.count distinct大量相同特殊值
  
  count distinct时，将值为null的情况单独处理，如果是计算count distinct，可以不用处理，直接过滤，在最后结果中加1。如果还有其他计算，需要进行group by，可以先将值为空的记录单独处理，再和其他计算结果进行union。
  
  执行如
  
    select a,count(distinct b) from t group by a;
  
  类型的SQL时，会出现数据倾斜的问题
  
  可替换成
  
    select a,sum(1) from (select a, b from t group by a,b) group by a;
    
  4.group by维度过小
  
  采用sum() group by的方式来替换count(distinct)完成计算。
  
  5.不同数据类型关联产生数据倾斜
  
  用户表中user_id字段为int，log表中user_id字段既有string类型也有int类型。当按照user_id进行两个表的Join操作时，默认的Hash操作会按int型的id来进行分配，这样会导致所有string类型id的记录都分配到一个Reducer中。
  
    select * from users a
    left outer join logs b
    on a.usr_id = cast(b.user_id as string)
    
  6.小表不小不大，怎么用 map join 解决倾斜问题
  
  使用 map join 解决小表(记录数少)关联大表的数据倾斜问题，这个方法使用的频率非常高，但如果小表很大，大到map join会出现bug或异常，这时就需要特别的处理。 以下例子:
  
    select * from log a
    left outer join users b
    on a.user_id = b.user_id;
  
  users 表有 600w+ 的记录，把 users 分发到所有的 map 上也是个不小的开销，而且 map join 不支持这么大的小表。如果用普通的 join，又会碰到数据倾斜的问题。
  解决方法：
  
    select /*+mapjoin(x)*/* from log a
      left outer join (
        select  /*+mapjoin(c)*/d.*
          from ( select distinct user_id from log ) c
          join users d
          on c.user_id = d.user_id
        ) x
      on a.user_id = b.user_id;

3）调参方面

Hadoop和Spark都自带了很多的参数和机制来调节数据倾斜，合理利用它们就能解决大部分问题。

    set hive.map.aggr = true
  
在map中会做部分聚集操作，效率更高但需要更多的内存。

    set hive.groupby.skewindata = true
    
数据倾斜的时候进行负载均衡，查询计划生成两个MR job，第一个job先进行key随机分配处理，随机分布到Reduce中，每个Reduce做部分聚合操作，先缩小数据量。第二个job再进行真正的group by key处理，根据预处理的数据结果按照Group By Key分布到Reduce中（这个过程可以保证相同的Key被分布到同一个Reduce中），完成最终的聚合操作。

      set hive.merge.mapfiles=true

当出现小文件过多，需要合并小文件

    set hive.exec.reducers.bytes.per.reducer=1000000000 （单位是字节）
    
每个reduce能够处理的数据量大小，默认是1G。

    hive.exec.reducers.max=999

最大可以开启的reduce个数，默认是999个。在只配了hive.exec.reducers.bytes.per.reducer以及hive.exec.reducers.max的情况下，实际的reduce个数会根据实际的数据总量/每个reduce处理的数据量来决定。

    set mapred.reduce.tasks=-1
    
实际运行的reduce个数，默认是-1，可以认为指定，但是如果认为在此指定了，那么就不会通过实际的总数据量hive.exec.reducers.bytes.per.reducer来决定reduce的个数了。

4）从业务和数据上解决数据倾斜

很多数据倾斜都是在数据的使用上造成的。我们举几个场景，并分别给出它们的解决方案。

l 有损的方法：找到异常数据，比如ip为0的数据，过滤掉

l 无损的方法：对分布不均匀的数据，单独计算

l 先对key做一层hash，先将数据随机打散让它的并行度变大，再汇集

l 数据预处理

