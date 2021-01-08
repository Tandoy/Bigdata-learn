# linkis探索问题记录

**shell引擎部分**

1.IDEA远程调用shell引擎报错： only owner can mkdir path /tmp/linkis/appuser/log/UJESClient-Test/2021-01-08
解决步骤：

  	1.1.将shell引擎的运行日志存放路径进行更改与linkis日志保持一致
  		vi /home/appuser/dss/linkis-ujes-shell-entrance/conf
		
  		wds.linkis.entrance.config.logPath=file:/home/appuser/dss/log
		
  		wds.linkis.resultSet.store.path=hdfs:/user/appuser/
   
2.执行shell任务报错：资源申请失败
解决步骤：

      2.1 删除shell-enginemanager目录下lib目录中的jackson-core-2.4.3.jar
      
      2.2 重启shell-enginemanager


**spark引擎部分**
1.IDEA远程调用spark引擎报错：DWCException{errCode=60035, desc='资源不足，启动引擎失败', ip='null', port=0, serviceKind='null'}

  解决步骤：
    
  	1.1 linkis源码spark版本与集群版本冲突
  		因为linkis源码编译是spark2.x,修改集群spark环境变量即可
		
  		sudo vim /etc/profile
		
  		export SPARK_HOME=/opt/cloudera/parcels/CDH/lib/spark2
		
  		export PATH=$PATH:$SPARK_HOME/bin
   
2.liniks执行spark引擎，appuser没有hdfs:/user/spark写的权限

	sudo -u hdfs hdfs dfs -chmod -R 777 /user/spark


