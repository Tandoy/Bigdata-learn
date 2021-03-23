##Hive连接器允许查询存储在Hive数据仓库中的数据

    1.前提条件
    
    已经安装了Hadoop和Hive，并起了hive metastore五福
    
    2.Presto相关配置
    
    cd etc/catalog 
    vi hive.properties 
    
        connector.name = hive-cdh4 
        hive.metastore.uri = thrift://localhost:9083
        
    3.使用Presto Cli连接Hive
    
    ./presto --server localhost:8080 --catalog hive --schema wcl_dwh; 
    
    4.进行SQL查询