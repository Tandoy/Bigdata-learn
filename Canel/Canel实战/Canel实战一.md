## Canel实战一

### 一、Mysql服务器配置
    
    -- 创建用户 用户名：canal 密码：Canal@123456
    create user 'canal'@'%' identified by 'Canal@123456';
    
    -- 授权 *.*表示所有库
    grant all privileges on *.* to 'canal'@'%' with grant option;
    
    --查找my.cnf文件
    locate my.cnf
    
    --进行配置修改
    [mysqld]
    # 打开binlog
    log-bin=mysql-bin
    # 选择ROW(行)模式
    binlog-format=ROW
    # 配置MySQL replaction需要定义，不要和canal的slaveId重复
    server_id=1
    
    --重启mysql
    sudo service mysql restart
    
    --改了配置文件之后，重启MySQL，使用命令查看是否打开binlog模式：
    show VARIABLES like 'log_bin';
    
    --查看binlog日志文件列表：
    show BINARY logs;
    
### 二、kafka进行消费

     kafka-console-consumer.sh --bootstrap-server dxbigdata103:9092 --topic test
    

    
    