Phoenix二级索引特点：

    Covered Indexes(覆盖索引) ：把关注的数据字段也附在索引表上，只需要通过索引表就能返回所要查询的数据（列）， 所以索引的列必须包含所需查询的列(SELECT的列和WHRER的列)。
    Functional indexes(函数索引)： 索引不局限于列，支持任意的表达式来创建索引。
    Global indexes(全局索引)：适用于读多写少场景。通过维护全局索引表，所有的更新和写操作都会引起索引的更新，写入性能受到影响。 在读数据时，Phoenix SQL会基于索引字段，执行快速查询。
    Local indexes(本地索引)：适用于写多读少场景。 在数据写入时，索引数据和表数据都会存储在本地。在数据读取时， 由于无法预先确定region的位置，所以在读取数据时需要检查每个region（以找到索引数据），会带来一定性能（网络）开销。
    其他的在网上也很多自己基于Coprocessor实现二级索引的文章，大体都是遵循类似的思路：构建一份“索引”的映射关系，存储在另一张HBase表或者其他DB里面。

方案优缺点：

    优点：基于Coprocessor的方案，从开发设计的角度看， 把很多对二级索引管理的细节都封装在的Coprocessor具体实现类里面， 这些细节对外面读写的人是无感知的，简化了数据访问者的使用。
    缺点：但是Coprocessor的方案入侵性比较强， 增加了在Regionserver内部需要运行和维护二级索引关系表的代码逻辑等，对Regionserver的性能会有一定影响。


## 配置HBase支持Phoenix二级索引过程

1.修改配置文件

如果要启用phoenix的二级索引功能，需要修改HBase的配置文件hbase-site.xml，在hbase集群的conf/hbase-site.xml文件中添加以下内容。

```xml
<property> 
          <name>hbase.regionserver.wal.codec</name> 
          <value>org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec</value> 
</property>
 
<property> 
           <name>hbase.region.server.rpc.scheduler.factory.class</name>
           <value>org.apache.hadoop.hbase.ipc.PhoenixRpcSchedulerFactory</value> 
</property>
 <property>
         <name>hbase.rpc.controllerfactory.class</name>
         <value>org.apache.hadoop.hbase.ipc.controller.ServerRpcControllerFactory</value>    
 </property> 
```

2.然后重启HBase

3.在phoenix中创建一个user表

```sql
create table user (
"session_id" varchar(100) not null primary key, 
"f"."cookie_id" varchar(100), 
"f"."visit_time" varchar(100), 
"f"."user_id" varchar(100), 
"f"."age" Integer, 
"f"."sex" varchar(100), 
"f"."visit_url" varchar(100), 
"f"."visit_os" varchar(100), 
"f"."browser_name" varchar(100),
"f"."visit_ip" varchar(100), 
"f"."province" varchar(100),
"f"."city" varchar(100),
"f"."page_id" varchar(100), 
"f"."goods_id" varchar(100),
"f"."shop_id" varchar(100)) column_encoded_bytes=0;
```

4.先来一个全局索引的二级索引测试，正常查询一条数据所需的时间

```text
在为表USER创建secondary index之前，先看看查询一条数据所需的时间
select * from user where "cookie_id"='000036bd-9ede-4d2e';
可以看到，对名为cookie_id的列进行按值查询需要6.5秒左右。
```

![img](https://pic2.zhimg.com/80/v2-071cf478cc2801014dfa7d460d51a7f9_720w.png)

5.然后查看执行逻辑以及计划

```text
explain select * from user where "cookie_id"='000036bd-9ede-4d2e';
```

![img](https://pic1.zhimg.com/80/v2-fd03cf0ba46196320aed85ad40b08574_720w.png)

```text
由图看知先进行了全表扫描再通过过滤器来筛选出目标数据，显示这种查询方式效率是很低的。
```

6.创建Global Indexing的二级索引。

```text
在cookie_id列上面创建二级索引：
create index USER_COOKIE_ID_INDEX on USER ("f"."cookie_id");
查看当前所有表会发现多一张USER_COOKIE_ID_INDEX索引表，查询该表数据。
```

7.这个时候系统就会创建一张基于cookie_id的索引表，来查看一下

```text
select * from USER_COOKIE_ID_INDEX limit 10;
```

![img](https://pic3.zhimg.com/80/v2-4ef7ed14f018ae778f794d5f21022a8e_720w.jpg)

可见创建了一个cookie_id和session_id的对应表，这个session_id就是hbase中的rowkey.这个时候查询会根据cookie_id先找到session_id，然后hbase会利用布隆过滤器来查询。
再来查询一下上面的数据

![img](https://pic2.zhimg.com/80/v2-b286abb6e55edcd1c87fa3f922fe74ad_720w.png)

你会发现依旧和原先一样啊！这不是骗人吗？哈哈，那要不换个语句查询？

```text
select "cookie_id" from user where "cookie_id"='000036bd-9ede-4d2e';
```

![img](https://pic1.zhimg.com/80/v2-e80e4fc22ab9949b1c0ae913d12f464c_720w.png)

你看到没？速度是0.047秒了，确实快了，区别在哪里呢？其实就是原先的查询语句是 *，现在把 * 换成了 cookie_id.原因就是我们建立的是cookie_id列，这里只能查询cookie_id二级索引才会有作用，再来看一下计划。

![img](https://pic2.zhimg.com/80/v2-3f7b4cc17053574b8531c6e71e3040a9_720w.png)

可以从描述中看到他扫描的是USER_COOKIE_ID_INDEX索引表查询的。单独查询这一列是没有问题，那如果我想再增加一列咋办呢？比如下面这个语句，我把age添加上。

```text
select "cookie_id","age" from user where "cookie_id"='0000023f-53b3-421b';
```

![img](https://pic4.zhimg.com/80/v2-a5bc882224cb8096a927e0ba656d597f_720w.png)

可见用了5秒多，再来看一下执行计划。

![img](https://pic1.zhimg.com/80/v2-7fa986a406c574ab0835591d39e8edc8_720w.png)

依旧是全表扫描，所以我们就知道了，虽然cookie_id是索引字段，但age不是索引字段，所以不会使用到索引

同样下面的也不会使用索引表

```text
 select "sex" from user where "cookie_id"='0000023f-53b3-421b';
```

因为sex不是索引字段。那合着现在我建立了一个全局索引，只能查询一个字段是吧，好鸡肋啊！不急，不妨看下去。接下来是Local Indexing的二级索引建立给user_id列上面创建二级索引

```text
create local index USER_USER_ID_INDEX on USER ("f"."user_id");
```

所以本地索引比全局索引在创建语法上多了一个local.这个时候查询一下

```text
select * from user where "user_id"='06fefc79-1-415367';
注意这里使用了*，按说按照上面全局索引的表现来看，一定是索引没啥变化，但是奇迹来了。
```

![img](https://pic2.zhimg.com/80/v2-d707795c03fa3095227d274b34318f11_720w.png)

看到没？速度是0.326秒。于是我们就可以使用这种方式达到目的了，再来看一下执行计划。

![img](https://pic4.zhimg.com/80/v2-4cee6d1f25a300a5fc25d1171d2aa31f_720w.png)

8.怎么建立索引一定就可以使用索引。

    8.1 第一种方式就是使用local indexing这种本地索引来建立二级索引。
    8.2 第二种是创建converted index

```text
如果在某次查询中，查询项或者查询条件中包含除被索引列之外的列（主键MY_PK除外）。默认情况下，该查询会触发full table scan（全表扫描），但是使用covered index则可以避免全表扫描。 
创建包含某个字段的覆盖索引,创建方式如下：
create index USER_COOKIE_ID_AGE_INDEX on USER ("f"."cookie_id") include("f"."age");
查看当前所有表会发现多一张USER_COOKIE_ID_AGE_INDEX索引表，查询该表数据。
select "age" from user where "cookie_id"='000036bd-9ede-4d2e';
```

![img](https://pic2.zhimg.com/80/v2-0acdb4ef6aebbe14f73c4aeb8110fe25_720w.png)


```text
select "age","sex" from user where "cookie_id"='000036bd-9ede-4d2e';
```

![img](https://pic3.zhimg.com/80/v2-ac60cdc9a081db9cc0e116f72383459a_720w.png)

可以看到因为不包含sex字段，索引没有使用索引，只有包含的字段使用了索引。

第三种方式在查询的时候提示其使用索引

```text
在select和column_name之间加上/*+ Index(<表名> <index名>)*/，通过这种方式强制使用索引。
例如：
select /*+ index(user,USER_COOKIE_ID_AGE_INDEX) */ "age" from user where "cookie_id"='000036bd-9ede-4d2e';
如果sex是索引字段，那么就会直接从索引表中查询
如果sex不是索引字段，那么将会进行全表扫描，所以当用户明确知道表中数据较少且符合检索条件时才适用，此时的性能才是最佳的。
```

![img](https://pic3.zhimg.com/80/v2-354b133f5227c9cf65630d6cbb92bcc2_720w.png)

```text
select /*+ index(user,USER_COOKIE_ID_AGE_INDEX) */ "age","sex" from user where "cookie_id"='000036bd-9ede-4d2e';
```
这种查询就因为sex没有索引，所以查询慢。

![img](https://pic4.zhimg.com/80/v2-215ee8ef3cab13534b2e63cb0cef6feb_720w.png)

## Phoenix索引重建

```text
Phoenix的索引重建是把索引表清空后重新装配数据。
alter index USER_COOKIE_ID_INDEX on user rebuild;
```

## Phoenix删除索引

```text
删除某个表的某张索引：
	语法	drop index 索引名称 on 表名
	例如  drop index USER_COOKIE_ID_INDEX on user;
如果表中的一个索引列被删除，则索引也将被自动删除，如果删除的是
覆盖索引上的列，则此列将从覆盖索引中被自动删除。
```

## Phoenix索引性能调优

一般来说，索引已经很快了，不需要特别的优化。这里也提供了一些方法，让你在面对特定的环境和负载的时候可以进行一些调优。下面的这些需要在hbase-site.xml文件中设置，针对所有的服务器。
```text
1. index.builder.threads.max 
创建索引时，使用的最大线程数。 
默认值: 10。

2. index.builder.threads.keepalivetime 
创建索引的创建线程池中线程的存活时间，单位：秒。 
默认值: 60

3. index.writer.threads.max 
写索引表数据的写线程池的最大线程数。 
更新索引表可以用的最大线程数，也就是同时可以更新多少张索引表，数量最好和索引表的数量一致。 
默认值: 10

4. index.writer.threads.keepalivetime 
索引写线程池中，线程的存活时间，单位：秒。
默认值：60
 

5. hbase.htable.threads.max 
每一张索引表可用于写的线程数。 
默认值: 2,147,483,647

6. hbase.htable.threads.keepalivetime 
索引表线程池中线程的存活时间，单位：秒。 
默认值: 60

7. index.tablefactory.cache.size 
允许缓存的索引表的数量。 
增加此值，可以在写索引表时不用每次都去重复的创建htable，这个值越大，内存消耗越多。 
默认值: 10

8. org.apache.phoenix.regionserver.index.handler.count 
处理全局索引写请求时，可以使用的线程数。 
默认值: 30
```
## 直白话：
## 全局索引是表，适合重读轻写的场景         
## 本地索引是列族，适合重写轻读的场景


9.Phoenix global index 测试

```sql
##建立表

create table test_global(id varchar primary key,f1 varchar,f2 varchar);
##建立global index
create index test_global_index on test_global(f1) include(f2);
##插入两条数据
upsert into test_global values('1','2','3');
upsert into test_global values('4','5','6');
##查看索引表数据(注意这里查的是索引表数据，不是原始表。创建global index会生成一个索引表)
select * from test_global_index;
+-------+------+-------+
| 0:F1  | :ID  | 0:F2  |
+-------+------+-------+
| 2     | 1    | 3     |
| 5     | 4    | 6     |
+-------+------+-------+
2 rows selected (0.037 seconds)
##查看hbase表上面的数据（注意这里查的是索引表在hbase中的数据）
scan 'TEST_GLOBAL_INDEX'
hbase(main):002:0> scan 'TEST_GLOBAL_INDEX'
ROW                    COLUMN+CELL                                                                                                                                                           
2\x001                 column=0:\x00\x00\x00\x00, timestamp=1501489856443, value=\x00\x00\x00\x00                                                                                            
2\x001                 column=0:\x80\x0B, timestamp=1501489856443, value=3                                                                                                                   
5\x004                 column=0:\x00\x00\x00\x00, timestamp=1501489860185, value=\x00\x00\x00\x00                                                                                            
5\x004                 column=0:\x80\x0B, timestamp=1501489860185, value=6                                                                                                                   
2 row(s) in 0.0620 seconds         
```

```text
1.以上可以看出global index的设计方式，会单独写一张索引表，列族为include字段，rowkey的设计方位是:二级索引字段1+"\x00"+二级索引字段2(复合索引)…+"\x00"+原表rowkey
2.查询的时候，会直接定位到索引表，通过rowkey查到位置，然后从索引表中带出数据
3.因为建立索引的时候还要多写一份include字段，读的时候直接从索引表定位并读出信息。所以这种表的应用场景定位是写的慢，读得快
```


10.Phoenix local index 测试

```sql
##建立表
create table test_local(id varchar primary key,f1 varchar,f2 varchar);
##建立local index
create local index test_local_index on test_local(f1);
##插入两条数据
upsert into test_local values('1','2','3');
upsert into test_local values('4','5','6');
##注意：local index 并不会创建新的表，而是在原来的表里面写入索引数据
##查看hbase表中数据
hbase(main):014:0> scan 'TEST_LOCAL'
ROW                        COLUMN+CELL                                                                                                                                                           
\x00\x002\x001             column=L#0:\x00\x00\x00\x00, timestamp=1501491310774, value=\x00\x00\x00\x00                                                                                          
\x00\x005\x004             column=L#0:\x00\x00\x00\x00, timestamp=1501491315118, value=\x00\x00\x00\x00                                                                                          
1                          column=0:\x00\x00\x00\x00, timestamp=1501491310774, value=x                                                                                                           
1                          column=0:\x80\x0B, timestamp=1501491310774, value=2                                                                                                                   
1                          column=0:\x80\x0C, timestamp=1501491310774, value=3                                                                                                                   
4                          column=0:\x00\x00\x00\x00, timestamp=1501491315118, value=x                                                                                                           
4                          column=0:\x80\x0B, timestamp=1501491315118, value=5                                                                                                                   
4                          column=0:\x80\x0C, timestamp=1501491315118, value=6                                                                                                                   
4 row(s) in 0.0250 seconds
```

```text
1.以上可以看出local index的设计方式，索引数据直接写在原表rowkey中，列族不写任何实际信息，local index的rowkey的设计方位是:原数据region的start key+"\x00"+二级索引字段1+"\x00"+二级索引字段2(复合索引)…+"\x00"+原rowkey;第一条信息"原数据region的start key"，这样做的目的是保证索引数据和原数据在一个region上，定位到二级索引后根据原rowkey就可以很快在本region上获取到其它信息，减少网络开销和检索成本。
2.查询的时候，会在不同的region里面分别对二级索引字段定位，查到原rowkey后在本region上获取到其它信息
3.因为这种索引设计方式只写索引数据，省了不少空间的占用，根据索引信息拿到原rowkey后再根据rowkey到原数据里面获取其它信息。所以这种表的应用场景定位是写的快，读得慢.
```