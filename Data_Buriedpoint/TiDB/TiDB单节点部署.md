## TiDB单节点部署

    1.下载压缩包
      wget http://download.pingcap.org/tidb-v4.0.8-linux-amd64.tar.gz
      wget http://download.pingcap.org/tidb-v4.0.8-linux-amd64.sha256
      
    2.检查文件完整性，返回 ok 则正确
      sha256sum -c tidb-v4.0.8-linux-amd64.sha256
      
    3.解开压缩包
      tar -xzf tidb-v4.0.8-linux-amd64.tar.gz
      cd tidb-v4.0.8-linux-amd64
      
    4.启动PD
      ./bin/pd-server --data-dir=pd
      
    5.启动 TiKV
      ./bin/tikv-server --pd="127.0.0.1:2379" --store=tikv
      
    6.启动TiDB
      ./bin/tidb-server --store=tikv --path="127.0.0.1:2379"
      
    7.启动mysql客户端进行连接
      mysql -h 127.0.0.1 -P 4000 -u root -D test