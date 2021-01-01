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
            --（1）linkis服务是否正常启动 http://dxbigdata102:20303/
            --（2） 使用官方测试类进行测试 UJESClientImplTestJ.java
### DSS标准版安装
#### 1. 前后端基础软件安装
        Linkis标准版(0.9.4及以上) -->已安装
        JDK (1.8.0_141以上) -->已安装
        MySQL (5.5+) -->已安装
        Nginx
            (1) 更新yum源 yum -y update
            (2) yum install epel-release
            (3) yum install nginx
            (4) # systemctl start nginx
                # systemctl enable nginx
                # systemctl status nginx
            (5) 测试nginx启动情况 http://dxbigdata102 默认为centos欢迎界面
        Qualitis
            搭建手册：https://github.com/WeBankFinTech/Qualitis/blob/master/docs/zh_CN/ch1/%E5%BF%AB%E9%80%9F%E6%90%AD%E5%BB%BA%E6%89%8B%E5%86%8C%E2%80%94%E2%80%94%E5%8D%95%E6%9C%BA%E7%89%88.md
            测试访问 http://dxbigdata102:8090/
        Azkaban
            (1) 下载https://github.com/azkaban/azkaban/releases
            (2) 解压
            (3) cd azkaban; ./gradlew build installDist -x test 
                其中编译环境需要gradle与git，需提前yum进行安装
            (4) 修改默认时区为Asia/Shanghai 配置文件目录：azkaban-3.90.0/azkaban-solo-server/build/install/azkaban-solo-server/conf
            (5) cd azkaban-solo-server/build/install/azkaban-solo-server; 
            (6) bin/start-solo.sh （一定在bin同级目录执行）
            (7) bin/shutdown-solo.sh （一定在bin同级目录执行）
            (8) 测试访问 http://dxbigdata102:8081/ 默认的账户和密码: azkaban/azkaban
        Linkis-jobtype（失败）
            (1) 解压：unzip linkis-jobtype-0.9.0.zip -d /home/appuser/linkis-jobtype/
            (2) cd linkis/bin/ 对config.sh进行相关配置的修改
#### 2. 后端环境配置准备
        2.1. 创建用户 
        注意:用户需要有sudo权限，且可免密登陆本机。如何配置SSH免密登陆
              vi /etc/sudoers
              hadoop  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL 
        2.2. 安装包准备
        从DSS已发布的release中（点击这里进入下载页面），下载对应安装包。先解压安装包到安装目录，并对解压后的文件进行配置修改
        2.3 进行解压
        tar -xvf  wedatasphere-dss-x.x.x-dist.tar.gz
        2.4 修改基础配置
        vi conf/config.sh  
                    <!--说明：以下为标准版DSS必须配置项，请确保外部服务可用-->
                    
                        deployUser=hadoop  #指定部署用户
                    
                        DSS_INSTALL_HOME=$workDir    #默认为上一级目录  
                        
                        WORKSPACE_USER_ROOT_PATH=file:///tmp/Linkis   #指定用户根目录，存储用户的脚本文件和日志文件等，是用户的工作空间。
                    
                        RESULT_SET_ROOT_PATH=hdfs:///tmp/linkis  # 结果集文件路径，用于存储Job的结果集文件 
                        
                        WDS_SCHEDULER_PATH=file:///appcom/tmp/wds/scheduler #DSS工程转换成Azkaban工程后zip包的存储路径
                    
                        #1、用于DATACHECK
                        HIVE_META_URL=jdbc:mysql://127.0.0.1:3306/hivemeta?characterEncoding=UTF-8
                        HIVE_META_USER=xxx
                        HIVE_META_PASSWORD=xxx
                        #2、用于Qualitis
                        QUALITIS_ADRESS_IP=127.0.0.1 #QUALITIS服务IP地址
                        QUALITIS_ADRESS_PORT=8090 #QUALITIS服务端口号 
                        #3、用于AZKABAN
                        AZKABAN_ADRESS_IP=127.0.0.1 #AZKABAN服务IP地址
                        AZKABAN_ADRESS_PORT=8091 #AZKABAN服务端口号
        2.5 修改数据库配置
        vi conf/db.sh 
                        # 设置DSS-Server和Eventchecker AppJoint的数据库的连接信息,需要和linkis保持同库
                        MYSQL_HOST=
                        MYSQL_PORT=
                        MYSQL_DB=
                        MYSQL_USER=
                        MYSQL_PASSWORD=
#### 3. 前端环境配置准备
        3.1 获取安装包
        3.2 解压 unzip /opt/softwares/wedatasphere-dss-web-0.9.0-dist.zip -d /home/appuser/dss-web/
        3.3 配置修改
            # Configuring front-end ports
            dss_port="8088"
            # URL of the backend linkis gateway
            linkis_url="http://127.0.0.1:9001"
            # dss ip address
            dss_ipaddr=$(ip addr | awk '/^[0-9]+: / {}; /inet.*global/ {print gensub(/(.*)\/(.*)/, "\\1", "g", $2)}'|awk 'NR==1')
#### 4. 测试访问 dxbigdata102:443