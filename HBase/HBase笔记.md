 ##1.RowKey设计
 
 散列性：
 
    散列方法
        1．生成随机数、hash、散列值
        2．字符串反转
        3．字符串拼接
    保证散列的同时要根据业务数据考虑集中性

    分区键(EndRow)：
        000|
        001|
        002|
        ...
        025|
        ...
        152|
        298|
    RowKey:
        000_
        001_
        002_
        ...
        025_
        ...
        152_
        298_

唯一性：
 
    保持Rowkey唯一

长度原则：

    生产环境（70-100）

##2.相关优化

内存优化

    HBase操作过程中需要大量的内存开销，毕竟Table是可以缓存在内存中的，一般会分配整个可用内存的70%给HBase的Java堆。
    但是不建议分配非常大的堆内存，因为GC过程持续太久会导致RegionServer处于长期不可用状态，一般16~48G内存就可以了，
    如果因为框架占用内存过高导致系统内存不足，框架一样会被系统服务拖死。

 基础优化
 
    1．允许在HDFS的文件中追加内容
        hdfs-site.xml、hbase-site.xml
        属性：dfs.support.append
        解释：开启HDFS追加同步，可以优秀的配合HBase的数据同步和持久化。默认值为true。
    2．优化DataNode允许的最大文件打开数
        hdfs-site.xml
        属性：dfs.datanode.max.transfer.threads
        解释：HBase一般都会同一时间操作大量的文件，根据集群的数量和规模以及数据动作，设置为4096或者更高。默认值：4096
    3．优化延迟高的数据操作的等待时间
        hdfs-site.xml
        属性：dfs.image.transfer.timeout
        解释：如果对于某一次数据操作来讲，延迟非常高，socket需要等待更长的时间，建议把该值设置为更大的值（默认60000毫秒），以确保socket不会被timeout掉。
    4．优化数据的写入效率
        mapred-site.xml
        属性：
        mapreduce.map.output.compress
        mapreduce.map.output.compress.codec
        解释：开启这两个数据可以大大提高文件的写入效率，减少写入时间。第一个属性值修改为true，第二个属性值修改为：org.apache.hadoop.io.compress.GzipCodec或者其他压缩方式。
    5．设置RPC监听数量
        hbase-site.xml
        属性：Hbase.regionserver.handler.count
        解释：默认值为30，用于指定RPC监听的数量，可以根据客户端的请求数进行调整，读写请求较多时，增加此值。
    6．优化HStore文件大小
        hbase-site.xml
        属性：hbase.hregion.max.filesize
        解释：默认值10737418240（10GB），如果需要运行HBase的MR任务，可以减小此值，因为一个region对应一个map任务，如果单个region过大，会导致map任务执行时间过长。该值的意思就是，如果HFile的大小达到这个数值，则这个region会被切分为两个Hfile。
    7．优化HBase客户端缓存
        hbase-site.xml
        属性：hbase.client.write.buffer
        解释：用于指定Hbase客户端缓存，增大该值可以减少RPC调用次数，但是会消耗更多内存，反之则反之。一般我们需要设定一定的缓存大小，以达到减少RPC次数的目的。
    8．指定scan.next扫描HBase所获取的行数
        hbase-site.xml
        属性：hbase.client.scanner.caching
        解释：用于指定scan.next方法获取的默认行数，值越大，消耗内存越大。
    9．flush、compact、split机制
        当MemStore达到阈值，将Memstore中的数据Flush进Storefile；compact机制则是把flush出来的小文件合并成大的Storefile文件。split则是当Region达到阈值，会把过大的Region一分为二。
        涉及属性：
        即：128M就是Memstore的默认阈值
        hbase.hregion.memstore.flush.size：134217728
        即：这个参数的作用是当单个HRegion内所有的Memstore大小总和超过指定值时，flush该HRegion的所有memstore。RegionServer的flush是通过将请求添加一个队列，模拟生产消费模型来异步处理的。那这里就有一个问题，当队列来不及消费，产生大量积压请求时，可能会导致内存陡增，最坏的情况是触发OOM。
        hbase.regionserver.global.memstore.upperLimit：0.4
        hbase.regionserver.global.memstore.lowerLimit：0.38
        即：当MemStore使用内存总量达到hbase.regionserver.global.memstore.upperLimit指定值时，将会有多个MemStores flush到文件中，MemStore flush 顺序是按照大小降序执行的，直到刷新到MemStore使用内存略小于lowerLimit