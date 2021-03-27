##Hudi集成Presto

    PrestoDB是一种常用的查询引擎，可提供交互式查询性能。 Presto支持查询Hudi MOR表
    presto集成hudi是基于hive catalog 同样是访问hive 外表进行查询，如果要集成需要把hudi 包copy 到presto hive-hadoop2
    
    1. cp /opt/apps/hudi/packaging/hudi-presto-bundle/target/hudi-presto-bundle-0.5.2-incubating.jar /opt/apps/presto-server/plugin/hive-hadoop2/
       cp /opt/apps/hudi/packaging/hudi-hadoop-mr-bundle/target/hudi-hadoop-mr-bundle-0.5.2-incubating.jar /opt/apps/presto-server/plugin/hive-hadoop2/
    2. 重启Presto 
    3. ./presto --server localhost:8080 --catalog hive --schema test; 
    4. 进行查询