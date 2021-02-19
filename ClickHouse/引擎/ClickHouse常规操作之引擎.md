##ClickHouse之引擎

###1.数据库引擎
    
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
    
###2.表引擎

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
        
            