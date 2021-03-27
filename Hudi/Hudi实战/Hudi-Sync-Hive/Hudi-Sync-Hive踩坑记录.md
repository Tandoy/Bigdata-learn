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
    TODO：issue#2728