7.1.	体系图
![image](https://github.com/tang-engineer/Bigdata-learn/blob/master/HBase/images/HBase%E4%BD%93%E7%B3%BB%E5%9B%BE.png)

7.1.1.	写流程

    1、	client向hregionserver发送写请求。
    2、	hregionserver将数据写到hlog（write ahead log）。为了数据的持久化和恢复。
    3、	hregionserver将数据写到内存（memstore）
    4、	反馈client写成功。

7.1.2.	数据flush过程

    1、	当memstore数据达到阈值（默认是64M），将数据刷到硬盘，将内存中的数据删除，同时删除Hlog中的历史数据。
    2、	并将数据存储到hdfs中。
    3、	在hlog中做标记点。

7.1.3.	数据合并过程

    1、	当数据块达到4块，hmaster将数据块加载到本地，进行合并
    2、	当合并的数据超过256M，进行拆分，将拆分后的region分配给不同的hregionserver管理
    3、	当hregionser宕机后，将hregionserver上的hlog拆分，然后分配给不同的hregionserver加载，修改.META.	
    4、	注意：hlog会同步到hdfs

7.1.4.	hbase的读流程

    1、	通过zookeeper和-ROOT- .META.表定位hregionserver。
    2、	数据从内存和硬盘合并后返回给client
    3、	数据块会缓存

7.1.5.	hmaster的职责

    1、管理用户对Table的增、删、改、查操作； 
    2、记录region在哪台Hregion server上;
    3、在Region Split后，负责新Region的分配； 
    4、新机器加入时，管理HRegion Server的负载均衡，调整Region分布
    5、在HRegion Server宕机后，负责失效HRegion Server 上的Regions迁移。

7.1.6.	hregionserver的职责

    HRegion Server主要负责响应用户I/O请求，向HDFS文件系统中读写数据，是HBASE中最核心的模块。
    HRegion Server管理了很多table的分区，也就是region。

7.1.7.	client职责

    Client
    HBASE Client使用HBASE的RPC机制与HMaster和RegionServer进行通信
    管理类操作：Client与HMaster进行RPC；
    数据读写类操作：Client与HRegionServer进行RPC。
