##Hudi同步Hive数据踩坑记录

    1.Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/log4j/LogManager
      	at org.apache.hudi.hive.HiveSyncTool.<clinit>(HiveSyncTool.java:55)
      Caused by: java.lang.ClassNotFoundException: org.apache.log4j.LogManager
      	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
      	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
      	... 1 more
      	
    
    
    初步排查：cdh-hive/lib/log4j-1.2.16.jar 与 hudi/lib/log4j-1.2.17.jar 冲突导致
    TODO：issue#2728(closed)
    解决方案：run_sync_tool.sh脚本中会自动加载${HADOOP_HOME}下hdfs、mapreduce、common等jar包，但CDH-Hadoop会存在差异
            修改 HADOOP_HIVE_JARS=${HIVE_JARS}:/opt/cloudera/parcels/CDH/lib/hadoop/*:/opt/cloudera/parcels/CDH/lib/hadoop-mapreduce/*:/opt/cloudera/parcels/CDH/lib/hadoop-hdfs/*:/opt/cloudera/parcels/CDH/lib/hadoop/lib/*:/opt/cloudera/parcels/CDH/lib/hadoop-hdfs/lib/*
            
    2.Caused by: java.lang.NoClassDefFoundError: javax/jdo/JDOException
      	at org.apache.hadoop.hive.metastore.HiveMetaStore.newRetryingHMSHandler(HiveMetaStore.java:6480)
      	at org.apache.hadoop.hive.metastore.HiveMetaStoreClient.<init>(HiveMetaStoreClient.java:207)
      	at org.apache.hadoop.hive.ql.metadata.SessionHiveMetaStoreClient.<init>(SessionHiveMetaStoreClient.java:74)
      	... 18 more
      Caused by: java.lang.ClassNotFoundException: javax.jdo.JDOException
      	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
      	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
      	... 21 more
      	
    初步排查：CDH-HIVE 的hive-metastore jar包与Hudi官方版本存在冲突
    
    