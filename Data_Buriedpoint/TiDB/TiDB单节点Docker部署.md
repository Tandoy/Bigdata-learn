##TiDB单节点Docker部署

    1.拉取 TiDB 的 Docker 镜像
        1.1 docker pull pingcap/tidb:v4.0.9
    
        1.2 docker pull pingcap/tikv:v4.0.9
    
        1.3 docker pull pingcap/pd:v4.0.9
        
    2.启动 PD
    
        docker run -d --name pd1 \
          -p 2379:2379 \
          -p 2380:2380 \
          -v /etc/localtime:/etc/localtime:ro \
          -v /data:/data \
          pingcap/pd:v4.0.9 \
          --name="pd1" \
          --data-dir="/data/pd1" \
          --client-urls="http://0.0.0.0:2379" \
          --advertise-client-urls="http://172.16.0.23:2379" \
          --peer-urls="http://0.0.0.0:2380" \
          --advertise-peer-urls="http://172.16.0.23:2380" \
          --initial-cluster="pd1=http://172.16.0.23:2380"
          
    3.启动 TiKV
    
        docker run -d --name tikv1 \
          -p 20160:20160 \
          --ulimit nofile=1000000:1000000 \
          -v /etc/localtime:/etc/localtime:ro \
          -v /data:/data \
          pingcap/tikv:v4.0.9 \
          --addr="0.0.0.0:20160" \
          --advertise-addr="172.16.0.23:20160" \
          --data-dir="/data/tikv1" \
          --pd="172.16.0.23:2379"
          
    4.启动 TiDB
    
        docker run -d --name tidb \
          -p 4000:4000 \
          -p 10080:10080 \
          -v /etc/localtime:/etc/localtime:ro \
          pingcap/tidb:v4.0.9 \
          --store=tikv \
          --path="172.16.0.23:2379"
          
    5.使用 MySQL 标准客户端连接 TiDB 测试
        mysql -h 127.0.0.1 -P 4000 -u root -D test;
        show databases;