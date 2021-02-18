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
    
###1.表引擎

    1.Log 家族 本地表、本地存储、测试使用
      1.1 StripeLog 会分块
      1.2 TinyLog 不会分块，不能同时读写，不能并行，没有索引
      1.3 Log 会分块
      
    2.