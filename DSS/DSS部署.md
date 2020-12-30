## 部署记录

### Linkis标准版安装（重新编译）
#### 1.获取源码
       1.1 在github上直接获取适配了CDH5.7.6的Links源码
#### 2.根据现有CDH版本进行hadoop、hive、spark、相关网络通信组件等进行版本修改
       2.1 修改元数据模块（cdh集群采用了sentry做hive权限控制，源码采用的是hive自带的权限控制（只通过jdbc），不修改无法展示hive元数据）
       2.2 修改pom.xml
         2.2.1 修改版本部分（将hive、hadoop、spark版本指向cdh5.13.3）
                <hadoop.version>2.6.0-cdh5.13.3</hadoop.version>
                <hive.version>1.1.0-cdh5.13.3</hive.version>
#### 3.重新编译打包并上传
       3.1 上传至dxbigdata102  /opt/softwares
#### 4.安装
       4.1 创建部署用户appuser
            --因为Linkis的服务是以 sudo -u ${linux-user} 方式来切换引擎，从而执行作业，所以部署用户需要有 sudo 权限，而且是免密的。
              vi /etc/sudoers
              hadoop  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL
       4.2 在安装节点设置全局变量，以便Linkis能正常使用Hadoop、Hive和Spark
            --vim /home/hadoop/.bash_rc
       4.3 解压 
            --tar -xvf /opt/softwares/wedatasphere-linkis-0.9.4-dist.tar.gz -C /home/appuser/dss/
       4.4 基础配置修改
            --vi conf/config.sh  
            
                SSH_PORT=22        #指定SSH端口，如果单机版本安装可以不配置
                    deployUser=hadoop      #指定部署用户
                    LINKIS_INSTALL_HOME=/appcom/Install/Linkis    # 指定安装目录
                    WORKSPACE_USER_ROOT_PATH=file:///tmp/hadoop    # 指定用户根目录，一般用于存储用户的脚本文件和日志文件等，是用户的工作空间。
                    HDFS_USER_ROOT_PATH=hdfs:///tmp/linkis   # 指定用户的HDFS根目录，一般用于存储Job的结果集文件
                
                    # 如果您想配合Scriptis一起使用，CDH版的Hive，还需要配置如下参数（社区版Hive可忽略该配置）
                    HIVE_META_URL=jdbc://...   # HiveMeta元数据库的URL
                    HIVE_META_USER=   # HiveMeta元数据库的用户
                    HIVE_META_PASSWORD=    # HiveMeta元数据库的密码
                    
                    # 配置hadoop/hive/spark的配置目录 
                    HADOOP_CONF_DIR=/appcom/config/hadoop-config  #hadoop的conf目录
                    HIVE_CONF_DIR=/appcom/config/hive-config   #hive的conf目录
                    SPARK_CONF_DIR=/appcom/config/spark-config #spark的conf目录
                    
            --vi conf/db.sh 
            
                # 设置数据库的连接信息
                    # 包括IP地址、数据库名称、用户名、端口
                    # 主要用于存储用户的自定义变量、配置参数、UDF和小函数，以及提供JobHistory的底层存储
                    MYSQL_HOST=
                    MYSQL_PORT=
                    MYSQL_DB=
                    MYSQL_USER=
                    MYSQL_PASSWORD=
                    
       4.5 进行安装
            sh bin/install.sh
       4.6 测试是否启动成功
            http://dxbigdata102:20303/
### DSS标准版安装