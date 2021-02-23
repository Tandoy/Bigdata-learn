##zookeeper安装

###1.准备环境

    （2n-1台linux机器或者虚拟机:由于zookeeper的paxos算法要求半数以上存活则为可用，所以必须准备单数个服务器搭建环境）
    
###2.上传

    用工具上传zookeeper安装文件到/home/hadoop，并确保文件的所属用户书hadoop，
    如果不是，需要用root用户修改文件的所属者。
    命令：
    su – root
    cd /home/hadoop
    chown hadoop:hadoop zookeeper-3.4.5.tar.gz
    su - hadoop

###3.解压

    用hadoop用户解压，如果不是hadoop用户，需要
    su – hadoop
    tar –zxvf zookeeper-3.4.5.tar.gz
    
###4.重命名

     需要是hadoop用户
     mv zookeeper-3.4.5 zookeeper
     
###5.修改环境变量（集群的每台zookeeper机器都要改）

    1、su – root
    2、vi /etc/profile
    3、添加内容：
    export ZOOKEEPER_HOME=/home/hadoop/zookeeper
    export PATH=$PATH:$ZOOKEEPER_HOME/bin

###6.修改zookeeper配置文件

    1、	su – hadoop
    2、	cd /home/hadoop/zookeeper/conf
    3、	cp zoo_sample.cfg zoo.cfg
    4、	vi zoo.cfg
    5、	添加内容,其中域名需要自己斟酌
    dataDir=/home/hadoop/zookeeper/data
    dataLogDir=/home/hadoop/zookeeper/log
    server.1=slave1:2888:3888 
    server.2=slave2:2888:3888
    server.3=slave3:2888:3888

###7.创建文件夹

    cd /home/hadoop/zookeeper
    mkdir data
    mkdir log
    chmod 755 data
    chmod 755 log
   
###8.创建myid文件，并添加内容
     cd /home/hadoop/zookeeper/data
     vi myid
     添加内容：
     1
     
###9.进行分发

    scp –r /home/hadoop/zookeeper hadoop@slave1:/home/hadoop

###10.修改对应机器的配置文件

    su – hadoop
    cd /home/hadoop/zookeeper/data
    vi myid
    修改内容，将1改为2
    2

###11.集群所有机器进行zk的启动

    ./bin/zkServer.sh start
    
###12.查看启动状态

    ./bin/zkServer.sh status



