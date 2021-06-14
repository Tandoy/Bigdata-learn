## Canel安装
  
1.下载 canal, 访问 release 页面 , 选择需要的包下载, 如以 1.0.17 版本为例

    wget https://github.com/alibaba/canal/releases/download/canal-1.0.17/canal.deployer-1.0.17.tar.gz

2.解压缩

    mkdir /tmp/canal
    tar zxvf canal.deployer-$version.tar.gz  -C /tmp/canal
    
3.解压完成后，进入 /tmp/canal 目录，可以看到如下结构

    drwxr-xr-x 2 appuser appuser  136 2013-02-05 21:51 bin
    drwxr-xr-x 4 appuser appuser  160 2013-02-05 21:51 conf
    drwxr-xr-x 2 appuser appuser 1.3K 2013-02-05 21:51 lib
    drwxr-xr-x 2 appuser appuser   48 2013-02-05 21:29 logs
    
4.打开配置文件conf/example/instance.properties，配置信息如下：
      
      ## mysql serverId
      canal.instance.mysql.slaveId = 1234
      #position info，需要改成自己的数据库信息
      canal.instance.master.address = 127.0.0.1:3306 
      canal.instance.master.journal.name = 
      canal.instance.master.position = 
      canal.instance.master.timestamp = 
      #canal.instance.standby.address = 
      #canal.instance.standby.journal.name =
      #canal.instance.standby.position = 
      #canal.instance.standby.timestamp = 
      #username/password，需要改成自己的数据库信息
      canal.instance.dbUsername = canal  
      canal.instance.dbPassword = canal
      canal.instance.defaultDatabaseName =
      canal.instance.connectionCharset = UTF-8
      #table regex
      canal.instance.filter.regex = .\*\\\\..\*
      
5.启动

      sh bin/startup.sh
      
6.查看 server 日志

    vi logs/canal/canal.log 
    
7.查看 instance 的日志

    vi logs/example/example.log