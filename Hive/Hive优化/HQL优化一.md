3.配置角度优化

3.1 列裁剪

    Hive在读数据的时候，可以只读取查询中所需要用到的列，而忽略其它列。 例如，若有以下查询：SELECT a,b FROM q WHERE e<10;
    在实施此项查询中，Q 表有 5 列（a，b，c，d，e），Hive 只读取查询逻辑中真实需要 的 3 列 a、b、e，而忽略列 c，d；这样做节省了读取开销，中间表存储开销和数据整合开销。
    裁剪所对应的参数项为：hive.optimize.cp=true（默认值为真）

3.2 分区裁剪

    可以在查询的过程中减少不必要的分区。 例如，若有以下查询：
     SELECT * FROM (SELECTT a1,COUNT(1) FROM T GROUP BY a1) subq WHERE subq.prtn=100; #（多余分区） 
     SELECT * FROM T1 JOIN (SELECT * FROM T2) subq ON (T1.a1=subq.a2) WHERE subq.prtn=100;
    查询语句若将“subq.prtn=100”条件放入子查询中更为高效，可以减少读入的分区 数目。 Hive 自动执行这种裁剪优化。分区参数为：hive.optimize.pruner=true（默认值为真）

3.3 JOIN操作

    在编写带有 join 操作的代码语句时，应该将条目少的表/子查询放在 Join 操作符的左边。 因为在 Reduce 阶段，位于 Join 操作符左边的表的内容会被加载进内存，载入条目较少的表 可以有效减少 OOM（out of memory）即内存溢出。所以对于同一个 key 来说，对应的 value 值小的放前，大的放后，这便是“小表放前”原则。 若一条语句中有多个 Join，依据 Join 的条件相同与否，有不同的处理方法。

3.3.1 JOIN原则

    在使用写有 Join 操作的查询语句时有一条原则：应该将条目少的表/子查询放在 Join 操作符的左边。原因是在 Join 操作的 Reduce 阶段，位于 Join 操作符左边的表的内容会被加载进内存，将条目少的表放在左边，可以有效减少发生 OOM 错误的几率。对于一条语句中有多个 Join 的情况，如果 Join 的条件相同，比如查询：
     INSERT OVERWRITE TABLE pv_users 
     SELECT pv.pageid, u.age FROM page_view p 
     JOIN user u ON (pv.userid = u.userid) 
     JOIN newuser x ON (u.userid = x.userid);  
     
    如果 Join 的 key 相同，不管有多少个表，都会则会合并为一个 Map-Reduce
    一个 Map-Reduce 任务，而不是 ‘n’ 个
    在做 OUTER JOIN 的时候也是一样
    如果 Join 的条件不相同，比如： 
    
      INSERT OVERWRITE TABLE pv_users 
      SELECT pv.pageid, u.age FROM page_view p 
      JOIN user u ON (pv.userid = u.userid) 
      JOIN newuser x on (u.age = x.age);  
       
    Map-Reduce 的任务数目和 Join 操作的数目是对应的，上述查询和以下查询是等价的：
     
      INSERT OVERWRITE TABLE tmptable 
      SELECT * FROM page_view p JOIN user u 
      ON (pv.userid = u.userid);
      INSERT OVERWRITE TABLE pv_users 
      SELECT x.pageid, x.age FROM tmptable x 
      JOIN newuser y ON (x.age = y.age);    
      
3.4 MAP JOIN操作

    Join 操作在 Map 阶段完成，不再需要Reduce，前提条件是需要的数据在 Map 的过程中可以访问到。比如查询： 
    
        INSERT OVERWRITE TABLE pv_users 
        SELECT /*+ MAPJOIN(pv) */ pv.pageid, u.age 
        FROM page_view pv 
        JOIN user u ON (pv.userid = u.userid); 
           
    相关的参数为：
    
        hive.join.emit.interval = 1000 
        hive.mapjoin.size.key = 10000
        hive.mapjoin.cache.numrows = 10000
    
3.5 GROUP BY操作

    进行GROUP BY操作时需要注意一下几点：
    Map端部分聚合
    事实上并不是所有的聚合操作都需要在reduce部分进行，很多聚合操作都可以先在Map端进行部分聚合，然后reduce端得出最终结果。
    这里需要修改的参数为：
    hive.map.aggr=true（用于设定是否在 map 端进行聚合，默认值为真） hive.groupby.mapaggr.checkinterval=100000（用于设定 map 端进行聚合操作的条目数）
    有数据倾斜时进行负载均衡
    此处需要设定 hive.groupby.skewindata，当选项设定为 true 是，生成的查询计划有两 个 MapReduce 任务。在第一个 MapReduce 中，map 的输出结果集合会随机分布到 reduce 中， 每个 reduce 做部分聚合操作，并输出结果。这样处理的结果是，相同的 Group By Key 有可 能分发到不同的 reduce 中，从而达到负载均衡的目的；第二个 MapReduce 任务再根据预处 理的数据结果按照 Group By Key 分布到 reduce 中（这个过程可以保证相同的 Group By Key 分布到同一个 reduce 中），最后完成最终的聚合操作。

3.6 合并小文件

    我们知道文件数目小，容易在文件存储端造成瓶颈，给 HDFS 带来压力，影响处理效率。对此，可以通过合并Map和Reduce的结果文件来消除这样的影响。
    用于设置合并属性的参数有：
    是否合并Map输出文件：hive.merge.mapfiles=true（默认值为真）
    是否合并Reduce 端输出文件：hive.merge.mapredfiles=false（默认值为假）
    合并文件的大小：hive.merge.size.per.task=256*1000*1000（默认值为 256000000）

4.程序角度优化

4.1 熟练使用SQL提高查询

    熟练地使用SQL，能写出高效率的查询语句。
    场景：有一张 user 表，为卖家每天收到表，user_id，ds（日期）为 key，属性有主营类目，指标有交易金额，交易笔数。每天要取前10天的总收入，总笔数，和最近一天的主营类目。 　　
    解决方法 1
    如下所示：常用方法
    
    INSERT OVERWRITE TABLE t1 
    SELECT user_id,substr(MAX(CONCAT(ds,cat),9) AS main_cat) FROM users 
    WHERE ds=20120329 // 20120329 为日期列的值，实际代码中可以用函数表示出当天日期 GROUP BY user_id; 

    INSERT OVERWRITE TABLE t2 
    SELECT user_id,sum(qty) AS qty,SUM(amt) AS amt FROM users 
    WHERE ds BETWEEN 20120301 AND 20120329 
    GROUP BY user_id 

    SELECT t1.user_id,t1.main_cat,t2.qty,t2.amt FROM t1 
    JOIN t2 ON t1.user_id=t2.user_id

4.2 无效ID在关联时的数据倾斜问题

    问题：日志中常会出现信息丢失，比如每日约为 20 亿的全网日志，其中的 user_id 为主 键，在日志收集过程中会丢失，出现主键为 null 的情况，如果取其中的 user_id 和 bmw_users 关联，就会碰到数据倾斜的问题。原因是 Hive 中，主键为 null 值的项会被当做相同的 Key 而分配进同一个计算 Map。
    解决方法 1：user_id 为空的不参与关联，子查询过滤 null
    SELECT * FROM log a 
    JOIN bmw_users b ON a.user_id IS NOT NULL AND a.user_id=b.user_id 
    UNION All SELECT * FROM log a WHERE a.user_id IS NULL
    解决方法 2 如下所示：函数过滤 null 
    SELECT * FROM log a LEFT OUTER 
    JOIN bmw_users b ON 
    CASE WHEN a.user_id IS NULL THEN CONCAT(‘dp_hive’,RAND()) ELSE a.user_id END =b.user_id;
    调优结果：原先由于数据倾斜导致运行时长超过 1 小时，解决方法 1 运行每日平均时长 25 分钟，解决方法 2 运行的每日平均时长在 20 分钟左右。优化效果很明显。
    我们在工作中总结出：解决方法2比解决方法1效果更好，不但IO少了，而且作业数也少了。解决方法1中log读取两次，job 数为2。解决方法2中 job 数是1。这个优化适合无效 id（比如-99、 ‘’，null 等）产生的倾斜问题。把空值的 key 变成一个字符串加上随机数，就能把倾斜的 数据分到不同的Reduce上，从而解决数据倾斜问题。因为空值不参与关联，即使分到不同 的 Reduce 上，也不会影响最终的结果。附上 Hadoop 通用关联的实现方法是：关联通过二次排序实现的，关联的列为 partion key，关联的列和表的 tag 组成排序的 group key，根据 pariton key分配Reduce。同一Reduce内根据group key排序。

4.3 不同数据类型关联产生的倾斜问题

    问题：不同数据类型 id 的关联会产生数据倾斜问题。
    一张表 s8 的日志，每个商品一条记录，要和商品表关联。但关联却碰到倾斜的问题。 s8 的日志中有 32 为字符串商品 id，也有数值商品 id，日志中类型是 string 的，但商品中的 数值 id 是 bigint 的。猜想问题的原因是把 s8 的商品 id 转成数值 id 做 hash 来分配 Reduce， 所以字符串 id 的 s8 日志，都到一个 Reduce 上了，解决的方法验证了这个猜测。
    解决方法：把数据类型转换成字符串类型
    SELECT * FROM s8_log a LEFT OUTER 
    JOIN r_auction_auctions b ON a.auction_id=CAST(b.auction_id AS STRING) 
    调优结果显示：数据表处理由 1 小时 30 分钟经代码调整后可以在 20 分钟内完成。

4.4 利用Hive对UNION ALL优化的特性

    多表 union all 会优化成一个 job。
    问题：比如推广效果表要和商品表关联，效果表中的 auction_id 列既有 32 为字符串商 品 id，也有数字 id，和商品表关联得到商品的信息。
    解决方法：Hive SQL 性能会比较好
    SELECT * FROM effect a 
    JOIN 
    (SELECT auction_id AS auction_id FROM auctions 
    UNION All 
    SELECT auction_string_id AS auction_id FROM auctions) b 
    ON a.auction_id=b.auction_id 
    比分别过滤数字 id，字符串 id 然后分别和商品表关联性能要好。
    这样写的好处：1 个 MapReduce 作业，商品表只读一次，推广效果表只读取一次。把 这个 SQL 换成 Map/Reduce 代码的话，Map 的时候，把 a 表的记录打上标签 a，商品表记录 每读取一条，打上标签 b，变成两个<key,value>对，<(b,数字 id),value>，<(b,字符串 id),value>。所以商品表的 HDFS 读取只会是一次。

4.5 解决Hive对UNION ALL优化的短板

    Hive 对 union all 的优化的特性：对 union all 优化只局限于非嵌套查询。消灭子查询内的 group by
    示例 1：子查询内有 group by 
    SELECT * FROM 
    (SELECT * FROM t1 GROUP BY c1,c2,c3 UNION ALL SELECT * FROM t2 GROUP BY c1,c2,c3)t3 
    GROUP BY c1,c2,c3 
    从业务逻辑上说，子查询内的 GROUP BY 怎么都看显得多余（功能上的多余，除非有 COUNT(DISTINCT)），如果不是因为 Hive Bug 或者性能上的考量（曾经出现如果不执行子查询 GROUP BY，数据得不到正确的结果的 Hive Bug）。所以这个 Hive 按经验转换成如下所示：
    SELECT * FROM (SELECT * FROM t1 UNION ALL SELECT * FROM t2)t3 GROUP BY c1,c2,c3 
    调优结果：经过测试，并未出现 union all 的 Hive Bug，数据是一致的。MapReduce 的 作业数由 3 减少到 1。 
    t1 相当于一个目录，t2 相当于一个目录，对 Map/Reduce 程序来说，t1，t2 可以作为 Map/Reduce 作业的 mutli inputs。这可以通过一个 Map/Reduce 来解决这个问题。Hadoop 的 计算框架，不怕数据多，就怕作业数多。
    但如果换成是其他计算平台如 Oracle，那就不一定了，因为把大的输入拆成两个输入， 分别排序汇总后 merge（假如两个子排序是并行的话），是有可能性能更优的（比如希尔排 序比冒泡排序的性能更优）。消灭子查询内的 COUNT(DISTINCT)，MAX，MIN。

4.6 GROUP BY替代COUNT(DISTINCT)达到优化效果

    计算 uv 的时候，经常会用到 COUNT(DISTINCT)，但在数据比较倾斜的时候 COUNT(DISTINCT) 会比较慢。这时可以尝试用 GROUP BY 改写代码计算 uv。
    原有代码
    INSERT OVERWRITE TABLE s_dw_tanx_adzone_uv PARTITION (ds=20120329) 
    SELECT 20120329 AS thedate,adzoneid,COUNT(DISTINCT acookie) AS uv FROM s_ods_log_tanx_pv t WHERE t.ds=20120329 GROUP BY adzoneid
    关于COUNT(DISTINCT)的数据倾斜问题不能一概而论，要依情况而定，下面是我测试的一组数据：
    测试数据：169857条
    #统计每日IP 
    CREATE TABLE ip_2014_12_29 AS SELECT COUNT(DISTINCT ip) AS IP FROM logdfs WHERE logdate='2014_12_29'; 
    耗时：24.805 seconds 
    #统计每日IP（改造） 
    CREATE TABLE ip_2014_12_29 AS SELECT COUNT(1) AS IP FROM (SELECT DISTINCT ip from logdfs WHERE logdate='2014_12_29') tmp; 
    耗时：46.833 seconds
    测试结果表名：明显改造后的语句比之前耗时，这是因为改造后的语句有2个SELECT，多了一个job，这样在数据量小的时候，数据不会存在倾斜问题。

5.优化的常用手段

主要由三个属性来决定：

    1.hive.exec.reducers.bytes.per.reducer   ＃这个参数控制一个job会有多少个reducer来处理，依据的是输入文件的总大小。默认1GB。
    2.hive.exec.reducers.max    ＃这个参数控制最大的reducer的数量， 如果 input / bytes per reduce > max  则会启动这个参数所指定的reduce个数。  这个并不会影响mapre.reduce.tasks参数的设置。默认的max是999。
    3.mapred.reduce.tasks ＃这个参数如果指定了，hive就不会用它的estimation函数来自动计算reduce的个数，而是用这个参数来启动reducer。默认是-1。
    
5.1 参数设置的影响

    如果reduce太少：如果数据量很大，会导致这个reduce异常的慢，从而导致这个任务不能结束，也有可能会OOM 2、如果reduce太多：  产生的小文件太多，合并起来代价太高，namenode的内存占用也会增大。如果我们不指定mapred.reduce.tasks， hive会自动计算需要多少个reducer.

6.hive实战

  6.1在实际工作中hive往往与hue进行搭配
  
    hive执行sql异常：java.lang.ArrayIndexOutOfBoundsException
    解决方案：
    执行sql前，加上如下参数，禁用hive矢量执行：
    set hive.vectorized.execution.enabled=false;
    set hive.vectorized.execution.reduce.enabled=false;
    set hive.vectorized.execution.reduce.groupby.enabled=false;
    
  6.2
  
    在多个列上进行的去重操作与hive环境变量hive.groupby.skewindata存在关系。
    当hive.groupby.skewindata=true时，hive不支持多列上的去重操作，因此执行以上SQL时出现异常， #如下：
    DISTINCT on different columns not supported with skew in data 
    默认该参数的值为false，表示不启用，要启用时，可以set hive.groupby.skewindata=ture;进行启用。
    当启用时，能够解决数据倾斜的问题，但如果要在查询语句中对多个字段进行去重统计时会报错。
