##ClickHouse-rpm安装
```shell script
1.sudo yum install curl
    
2.sudo rpm --import https://repo.clickhouse.tech/CLICKHOUSE-KEY.GPG
    
3.sudo yum-config-manager --add-repo https://repo.clickhouse.tech/rpm/stable/x86_64
    
4.sudo yum install clickhouse-server clickhouse-client
    
5.service clickhouse-server start 
    
6.clickhouse-client
```
    
##ClickHouse-docker安装
```shell script
1.sudo docker run -d --name ch-server --ulimit nofile=262144:262144 -p 9000:9000 yandex/clickhouse-serve

2.sudo docker run -it ef20987bce90 /bin/bash

3.clickhouse-client --host 172.16.0.222
```