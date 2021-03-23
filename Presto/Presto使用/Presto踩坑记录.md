##Presto踩坑记录

###一、通过Presto Cli进行HQL查询报错
    
    Qurey xxxx failed：No nodes available to run query
    
    解决过程：
    1.使用presto-cli查看节点状况
    use presto-cli execute "select * FROM system.runtime.nodes"
    
    2.排查发现 /opt/apps/presto-server/etc/config.properties 中discovery.uri=http 配置存在问题，修改为正确地址
    
        coordinator=true
        node-scheduler.include-coordinator=true
        http-server.http.port=8080
        query.max-memory=5GB
        query.max-memory-per-node=1GB
        discovery-server.enabled=true
        discovery.uri=http://dxbigdata101:8080
    