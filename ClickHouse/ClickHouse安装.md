##ClickHouse安装

    1.sudo yum install curl
    
    2.sudo rpm --import https://repo.clickhouse.tech/CLICKHOUSE-KEY.GPG
    
    3.sudo yum-config-manager --add-repo https://repo.clickhouse.tech/rpm/stable/x86_64
    
    4.sudo yum install clickhouse-server clickhouse-client
    
    5.service clickhouse-server start 
    
    6.clickhouse-client