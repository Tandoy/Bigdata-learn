##ClickHouse之语法

###1.建表

    1.常规建表
        create table tangzhi.tb_b(
            id Int32 default 0 comment '唯一标识',
            name String comment '姓名',
            age UInt8 comment '年纪',
            gendrer String comment '性别'
            )engine=Log();
        
    2.复制表结构（也会复制表引擎）
        create table tb_c as tangzhi.tb_a;
        
    3.复制表结构以及数据
        create table tb_d engine=Memory as select * from tangzhi.tb_a;
        
###2.建临时表

    create temporary table test_enum(
    id Int32,
    name Enum('RED' = 1, 'GREEN' = 2, 'BLUE' = 3)
    );   
    
    1.临时表会覆盖常规表数据
    2.并且不会进行持久化存在内存中
    3.建立在会话级别
    4.常用于数据迁移
    5.不指定引擎
        
###3.删除表

    drop table tangzhi.tb_a;
    
###4.视图

    4.1 普通试图
    
    create view tb_a_view as select name,age from tb_a;
    1.索引指向，不实际存储数据
    2.不会提升查询效率
        
    4.2 物化视图
    
    create materialized view m_tb_a_view engine=Log populate as select name,age from tb_a;
    1.会实际存储数据，类似于liunx硬链接（深度拷贝）
    2.可用于预先聚合计算
    3.特殊的表，支持引擎&结构化
    
###5.分区表

    create table tangzhi.tb_par(data_date DateTime comment '数据日期'
    ,id Int32 default 0 comment '唯一标识'
    ,name String comment '姓名'
    ,age UInt8 comment '年纪'
    ,gendrer String comment '性别')
    engine=MergeTree() 
    partition by toYYYYMM(data_date)
    order by id;
    
    1.只有MergeTree引擎才支持数据分区
    2.可变相实现数据更新操作
    
###6.合并表数据

    optimized table xxx;
    
    
    
    
       
