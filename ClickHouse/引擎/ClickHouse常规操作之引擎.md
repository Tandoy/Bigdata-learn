## ClickHouse之引擎

### 1.数据库引擎（其实生产上不推荐使用数据库引擎 对业务库造成入侵）
    
    1.延时引擎Lazy，在距最近一次访问间隔expiration_time_in_seconds时间段内，将表保存在内存中，仅适用于 *Log引擎表，由于针对这类表的访问间隔较长，对保存大量小的 *Log引擎表进行了优化
    CREATE DATABASE testlazy ENGINE = Lazy(expiration_time_in_seconds);
    
    2.Atomic 原子引擎，默认
    
    3.MySQL引擎
    MySQL引擎用于将远程的MySQL服务器中的表映射到ClickHouse中，并允许对表进行INSERT和SELECT查询;
    但不支持RENAME、CREATE TABLE、ALTE;
    
    CREATE DATABASE [IF NOT EXISTS] db_name [ON CLUSTER cluster]
    ENGINE = MySQL('host:port', ['database' | database], 'user', 'password')
    CREATE DATABASE IF NOT EXISTS test_mysql_db
    ENGINE = MySQL('dxbigdata103:3306', 'test', 'root', '000000')
    
    4.MaterializeMySQL引擎
    可通过binlog日志实时物化mysql数据
    
### 2.表引擎

    1.Log 家族 本地表、本地存储、测试使用
      1.1 StripeLog 会分块
      1.2 TinyLog 不会分块，不能同时读写，不能并行，没有索引
      1.3 Log 会分块
      
    2.MergeTree 列储存、自定义分区、稀疏索引、支持并发、支持修改、支持索引、支持更新删除
    
        create table tangzhi.tb_par(data_date DateTime comment '数据日期'
        ,id Int32 default 0 comment '唯一标识'
        ,name String comment '姓名'
        ,age UInt8 comment '年纪'
        ,gendrer String comment '性别')
        engine=MergeTree() 
        partition by toYYYYMM(data_date)
        PRIMARY KEY id
        order by id;
        
        2.1 分区数据存储文件详解
            .bin --> 存储各个字段二进制压缩数据
            [column].mrk2 --> 存储.bin数据文件中对应的偏移量
            primary.index --> 主键索引（稀疏索引）
            checksums.txt --> 校验文件，保证数据的准确性与完整性
            column.txt --> 记录表所有字段属性
            count.txt --> 当前分歧数据总行数
            partition.dat --> 分区信息
            minmax_xxx.idx --> 数据分区最大小数据
            
    3.ReplacingMergeTree 该引擎和MergeTree的不同之处在于它会对区内数据删除排序键值相同的重复项。
        
        create table tangzhi.tb_par(data_date DateTime comment '数据日期'
                ,id Int32 default 0 comment '唯一标识'
                ,name String comment '姓名'
                ,age UInt8 comment '年纪'
                ,gendrer String comment '性别')
                engine=ReplacingMergeTree(ver) 
                partition by toYYYYMM(data_date)
                PRIMARY KEY id
                order by id;
                
        3.1 只有在合并数据时进行去重
        3.2 ReplacingMergeTree的参数
            ver — 版本列。类型为UInt*,Date或DateTime。可选参数。
            在数据合并的时候，ReplacingMergeTree从所有具有相同排序键的行中选择一行留下：
            如果ver列未指定，保留最后一条。
            如果ver列已指定，保留ver值最大的版本。
            
    4.CollapsingMergeTree 对区内数据进行折叠删除 -1&1
        可根据指定字段进行版本进行折叠删除数据，可用于数据更新操作，只需要保持排序字段、分区一致即可。
    
        create table tangzhi.tb_par(data_date DateTime comment '数据日期'
                ,id Int32 default 0 comment '唯一标识'
                ,name String comment '姓名'
                ,age UInt8 comment '年纪'
                ,gendrer String comment '性别')
                ,flag Int8
                engine=ReplacingMergeTree(flag) 
                partition by toYYYYMM(data_date)
                PRIMARY KEY id
                order by id;
                
        4.1 折叠错乱，无法进行折叠
        4.2 -1 代表进行折叠
        4.3 1 -1 1 代表更新
            1 -1 代表删除
            -1 1 1 不进行折叠操作
            
    5.VersionedCollapsingMergeTree 可根据指定字段进行版本进行折叠删除数据，可用于数据更新操作，只需要保持排序字段、分区一致即可。
        
        create table tangzhi.tb_par(data_date DateTime comment '数据日期'
                        ,id Int32 default 0 comment '唯一标识'
                        ,name String comment '姓名'
                        ,age UInt8 comment '年纪'
                        ,gendrer String comment '性别')
                        ,flag Int8
                        engine=ReplacingMergeTree(flag,age) 
                        partition by toYYYYMM(data_date)
                        PRIMARY KEY id
                        order by id;
                        
    6.SummingMergeTree 统计聚合表引擎
    
        CREATE TABLE summtt
        (
            key UInt32,
            value UInt32
        )
        ENGINE = SummingMergeTree(value)
        partition by key
        ORDER BY key;
        INSERT INTO summtt Values(1,1),(1,2),(2,1);
        optimize table summtt final;
        当数据被插入到表中时，他们将被原样保存。ClickHouse定期合并插入的数据片段，并在这个时候对所有具有相同主键的行中的列进行汇总，将这些行替换为包含汇总数据的一行记录。
        
        1.列中数值类型的值会被汇总。这些列的集合在参数columns中被定义。
        2.如果用于汇总的所有列中的值均为0，则该行会被删除。       
        3.如果列不在主键中且无法被汇总，则会在现有的值中任选一个。  
        4.主键所在的列中的值不会被汇总。
        5.数字字段进行聚合，不是数组字段取第一条
        
    7.AggregatingMergeTree 可以使用 AggregatingMergeTree 表来做增量数据的聚合统计，包括物化视图的数据聚合。
        
        7.1普通表进行聚合方式
        drop table summtt;
        CREATE TABLE summtt
        (
            key UInt32,
            value UInt32
        )
        ENGINE = MergeTree()
        partition by key
        ORDER BY key;
        INSERT INTO summtt Values(1,1),(1,2),(2,1);
        select key,sum(value),uniq(key) from summtt group by key;
        
        7.2采用AggregatingMergeTree引擎进行预聚合，保存聚合结果的表
        drop table agg;
        CREATE TABLE agg(
         key UInt32,
         cost AggregateFunction(sum,UInt32),
         cnt  AggregateFunction(uniq,UInt32)
        )
        ENGINE = AggregatingMergeTree()
        order by key;
        INSERT INTO agg select key,sumState(value),uniqState(key) from summtt group by key;
        select key,uniqMerge(cnt),sumMerge(cost) from agg group by key;
        
        7.3使用物化视图同步聚合操作
        CREATE MATERIALIZED VIEW tangzhi.basic
        ENGINE = AggregatingMergeTree() PARTITION BY key ORDER BY key
        populate  --进行实时同步数据
        as
        select key,sumState(value) cost,uniqState(key) cnt from summtt group by key;
        select key,uniqMerge(cnt),sumMerge(cost) from tangzhi.basic group by key;
  
### 3.外部存储引擎

    1.HDFS clickhouse不存储数据，读取处理数据
    
    CREATE TABLE tangzhi.hdfs_engine_table (acct_no String, 
    custr_nbr String,
    cycle_nbr Int8,
    biz_code String
    ) ENGINE=HDFS('hdfs://dxbigdata102:8020/user/hive/warehouse/ywf_mt.db/dim_card_acct_cu/000000_0', 'CSV');
    
    2.Kafka
    
    CREATE TABLE queue (
        timestamp UInt64,
        level String,
        message String
      ) ENGINE = Kafka('dxbigdata103:9092', 'GMALL_STARTUP', 'test_ch', 'JSONEachRow');
    
      SELECT * FROM queue LIMIT 5;
    
    3.MYSQL 与数据库引擎大致相同,可向mysql插入数据，但不支持删除以及更新操作，实际上不存储mysql数据
    
    CREATE TABLE tangzhi.hdfs_engine_table (acct_no String, 
    custr_nbr String,
    cycle_nbr Int8,
    biz_code String
    )
    ENGINE=MySQL('host:port', 'database', 'table', 'user', 'password');
    
    4.File 与HDFS引擎类似，加载本地文件数据,可插入数据 数据存放目录：tb_file/data.csv
    
    CREATE TABLE tangzhi.hdfs_engine_table (acct_no String, 
    custr_nbr String,
    cycle_nbr Int8,
    biz_code String
    )
    ENGINE=File('CSV');
    
          

        
    
        
        
            