##Hudi同步Hive数据

     此种方式会单独起jvm或者命令行
     to sync a hoodie HDFS table with a hive metastore table
      
    同步命令如下：
        
        ./run_sync_tool_sh --jdbc-url jdbc:hive2://dxbigdata101:10000/test \
        --user hive \
        --pass hive \
        --base-path 'hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_trips_cow' \
        --database test \
        --table hudi_trips_cow