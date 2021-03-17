##Canal Admin安装

1.下载 canal-admin, 访问 release 页面 , 选择需要的包下载, 如以 1.1.4 版本为例

    wget https://github.com/alibaba/canal/releases/download/canal-1.1.4/canal.admin-1.1.4.tar.gz
  
2.解压缩

    mkdir /tmp/canal-admin
    tar zxvf canal.admin-$version.tar.gz  -C /tmp/canal-admin
    
3.解压完成后，进入 /tmp/canal 目录，可以看到如下结构

    drwxr-xr-x   6 appuser  appuser   204B  8 31 15:37 bin
    drwxr-xr-x   8 appuser  appuser   272B  8 31 15:37 conf
    drwxr-xr-x  90 appuser  appuser   3.0K  8 31 15:37 lib
    drwxr-xr-x   2 appuser  appuser    68B  8 31 15:26 logs
   
4.配置修改

    vi conf/application.yml
    
    server:
      port: 8089
    spring:
      jackson:
        date-format: yyyy-MM-dd HH:mm:ss
        time-zone: GMT+8
    
    spring.datasource:
      address: 127.0.0.1:3306
      database: canal_manager
      username: canal
      password: canal
      driver-class-name: com.mysql.jdbc.Driver
      url: jdbc:mysql://${spring.datasource.address}/${spring.datasource.database}?useUnicode=true&characterEncoding=UTF-8&useSSL=false
      hikari:
        maximum-pool-size: 30
        minimum-idle: 1
    
    canal:
      adminUser: admin
      adminPasswd: admin

5.初始化元数据库

    mysql -h127.1 -uroot -p
    
    # 导入初始化SQL
    > source conf/canal_manager.sql
    
    初始化SQL脚本里会默认创建canal_manager的数据库，建议使用root等有超级权限的账号进行初始化 b. canal_manager.sql默认会在conf目录下，也可以通过链接下载 canal_manager.sql
    
6.启动

    sh bin/startup.sh
    
7.查看 admin 日志

    vi logs/admin.log
    
8.关闭

    sh bin/stop.sh
    
9.canal-server端配置

    使用canal_local.properties的配置覆盖canal.properties
    
    # register ip
    canal.register.ip =
    
    # canal admin config
    canal.admin.manager = 127.0.0.1:8089
    canal.admin.port = 11110
    canal.admin.user = admin
    canal.admin.passwd = 4ACFE3202A5FF5CF467898FC58AAB1D615029441
    # admin auto register
    canal.admin.register.auto = true
    canal.admin.register.cluster =
    
10.重新启动admin-server即可。

    sh bin/startup.sh