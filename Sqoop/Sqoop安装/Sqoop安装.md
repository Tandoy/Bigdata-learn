 sqoop安装
安装sqoop的前提是已经具备java和hadoop的环境
1、下载并解压

最新版下载地址http://ftp.wayne.edu/apache/sqoop/1.4.6/


2、修改配置文件

$ cd $SQOOP_HOME/conf
$ mv sqoop-env-template.sh sqoop-env.sh
打开sqoop-env.sh并编辑下面几行：
export HADOOP_COMMON_HOME=/home/hadoop/apps/hadoop-2.6.1/ 
export HADOOP_MAPRED_HOME=/home/hadoop/apps/hadoop-2.6.1/
export HIVE_HOME=/home/hadoop/apps/hive-1.2.1


3、加入mysql的jdbc驱动包

cp  ~/app/hive/lib/mysql-connector-java-5.1.28.jar   $SQOOP_HOME/lib/

4、验证启动

$ cd $SQOOP_HOME/bin
$ sqoop-version
预期的输出：
15/12/17 14:52:32 INFO sqoop.Sqoop: Running Sqoop version: 1.4.6
Sqoop 1.4.6 git commit id 5b34accaca7de251fc91161733f906af2eddbe83
Compiled by abe on Fri Aug 1 11:19:26 PDT 2015
到这里，整个Sqoop安装工作完成。
