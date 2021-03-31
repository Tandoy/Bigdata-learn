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
    
    3.Caused by: java.lang.NoClassDefFoundError: org/antlr/runtime/RecognitionException
      	at java.lang.Class.forName0(Native Method)
      	at java.lang.Class.forName(Class.java:348)
      	at org.apache.hadoop.hive.metastore.MetaStoreUtils.getClass(MetaStoreUtils.java:1528)
      	at org.apache.hadoop.hive.metastore.RawStoreProxy.getProxy(RawStoreProxy.java:66)
      	at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.newRawStore(HiveMetaStore.java:682)
      	at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.getMS(HiveMetaStore.java:660)
      	at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.createDefaultDB(HiveMetaStore.java:709)
      	at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.init(HiveMetaStore.java:508)
      	at org.apache.hadoop.hive.metastore.RetryingHMSHandler.<init>(RetryingHMSHandler.java:78)
      	at org.apache.hadoop.hive.metastore.RetryingHMSHandler.getProxy(RetryingHMSHandler.java:84)
      	at org.apache.hadoop.hive.metastore.HiveMetaStore.newRetryingHMSHandler(HiveMetaStore.java:6481)
      	at org.apache.hadoop.hive.metastore.HiveMetaStoreClient.<init>(HiveMetaStoreClient.java:207)
      	at org.apache.hadoop.hive.ql.metadata.SessionHiveMetaStoreClient.<init>(SessionHiveMetaStoreClient.java:74)
      	... 18 more
      Caused by: java.lang.ClassNotFoundException: org.antlr.runtime.RecognitionException
      	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
      	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
      	... 31 more

    4.Caused by: org.datanucleus.exceptions.NucleusException: Attempt to invoke the "dbcp-builtin" plugin to create a ConnectionPool gave an error : The specified datastore driver ("org.apache.derby.jdbc.EmbeddedDriver") was not found in the CLASSPATH. Please check your CLASSPATH specification, and the name of the driver.
      	at org.datanucleus.store.rdbms.ConnectionFactoryImpl.generateDataSources(ConnectionFactoryImpl.java:259)
      	at org.datanucleus.store.rdbms.ConnectionFactoryImpl.initialiseDataSources(ConnectionFactoryImpl.java:131)
      	at org.datanucleus.store.rdbms.ConnectionFactoryImpl.<init>(ConnectionFactoryImpl.java:85)
      	... 65 more
      Caused by: org.datanucleus.store.rdbms.connectionpool.DatastoreDriverNotFoundException: The specified datastore driver ("org.apache.derby.jdbc.EmbeddedDriver") was not found in the CLASSPATH. Please check your CLASSPATH specification, and the name of the driver.
      	at org.datanucleus.store.rdbms.connectionpool.AbstractConnectionPoolFactory.loadDriver(AbstractConnectionPoolFactory.java:58)
      	at org.datanucleus.store.rdbms.connectionpool.DBCPBuiltinConnectionPoolFactory.createConnectionPool(DBCPBuiltinConnectionPoolFactory.java:49)
      	at org.datanucleus.store.rdbms.ConnectionFactoryImpl.generateDataSources(ConnectionFactoryImpl.java:238)
      	... 67 more
      	
    5.Caused by: java.lang.ClassNotFoundException: org.apache.parquet.format.converter.ParquetMetadataConverter
      	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
      	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
      	... 5 more
      	
    6.Caused by: java.lang.ClassNotFoundException: org.apache.parquet.ParquetRuntimeException
      	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
      	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
      	... 17 more

    7.Exception in thread "main" java.lang.NoSuchMethodError: shaded.parquet.org.apache.thrift.protocol.TProtocol.getScheme()Ljava/lang/Class;
      	at org.apache.parquet.format.FileMetaData.read(FileMetaData.java:945)
      	at org.apache.parquet.format.Util.read(Util.java:213)
      	at org.apache.parquet.format.Util.readFileMetaData(Util.java:73)
      	at org.apache.parquet.format.converter.ParquetMetadataConverter$2.visit(ParquetMetadataConverter.java:866)
      	at org.apache.parquet.format.converter.ParquetMetadataConverter$2.visit(ParquetMetadataConverter.java:863)
      	at org.apache.parquet.format.converter.ParquetMetadataConverter$NoFilter.accept(ParquetMetadataConverter.java:746)
      	at org.apache.parquet.format.converter.ParquetMetadataConverter.readParquetMetadata(ParquetMetadataConverter.java:863)
      	at org.apache.parquet.hadoop.ParquetFileReader.readFooter(ParquetFileReader.java:532)
      	at org.apache.parquet.hadoop.ParquetFileReader.readFooter(ParquetFileReader.java:505)
      	at org.apache.parquet.hadoop.ParquetFileReader.readFooter(ParquetFileReader.java:499)
      	at org.apache.parquet.hadoop.ParquetFileReader.readFooter(ParquetFileReader.java:448)
      	at org.apache.hudi.hive.HoodieHiveClient.readSchemaFromBaseFile(HoodieHiveClient.java:454)
      	at org.apache.hudi.hive.HoodieHiveClient.getDataSchema(HoodieHiveClient.java:357)
      	at org.apache.hudi.hive.HiveSyncTool.syncHoodieTable(HiveSyncTool.java:112)
      	at org.apache.hudi.hive.HiveSyncTool.syncHoodieTable(HiveSyncTool.java:87)
      	at org.apache.hudi.hive.HiveSyncTool.main(HiveSyncTool.java:207)


    以上问题排查：CDH-HIVE 的版本过低，多数jar包找不到
    解决方案：下载对应版本jar 放到/home/appuser/tangzhi/lib/* 并将此路径加至${HADOOP_HIVE_JARS} 注意：parquet-*-1.10.1版本可能存在问题
             parquet-avro-1.8.3.jar
             parquet-column-1.8.3.jar
             parquet-common-1.8.3.jar
             parquet-format-2.3.1.jar
             parquet-hadoop-1.8.3.jar
             
    8.Caused by: org.apache.hadoop.hive.ql.parse.SemanticException: Cannot find class 'org.apache.hudi.hadoop.HoodieParquetInputFormat'
      	at org.apache.hadoop.hive.ql.parse.ParseUtils.ensureClassExists(ParseUtils.java:227)
      	at org.apache.hadoop.hive.ql.parse.StorageFormat.fillStorageFormat(StorageFormat.java:57)
      	at org.apache.hadoop.hive.ql.parse.SemanticAnalyzer.analyzeCreateTable(SemanticAnalyzer.java:10904)
      	at org.apache.hadoop.hive.ql.parse.SemanticAnalyzer.genResolvedParseTree(SemanticAnalyzer.java:10142)
      	at org.apache.hadoop.hive.ql.parse.SemanticAnalyzer.analyzeInternal(SemanticAnalyzer.java:10223)
      	at org.apache.hadoop.hive.ql.parse.SemanticAnalyzer.analyzeInternal(SemanticAnalyzer.java:10108)
      	at org.apache.hadoop.hive.ql.parse.BaseSemanticAnalyzer.analyze(BaseSemanticAnalyzer.java:223)
      	at org.apache.hadoop.hive.ql.Driver.compile(Driver.java:558)
      	at org.apache.hadoop.hive.ql.Driver.compileInternal(Driver.java:1356)
      	at org.apache.hadoop.hive.ql.Driver.compileAndRespond(Driver.java:1343)
      	at org.apache.hive.service.cli.operation.SQLOperation.prepare(SQLOperation.java:185)
      	... 15 more
      Caused by: java.lang.ClassNotFoundException: org.apache.hudi.hadoop.HoodieParquetInputFormat
      	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
      	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
      	at java.lang.Class.forName0(Native Method)
      	at java.lang.Class.forName(Class.java:348)
      	at org.apache.hadoop.hive.ql.parse.ParseUtils.ensureClassExists(ParseUtils.java:225)
      	... 25 more
      	
    初步排查：将hudi-hadoop-mr-0.5.2-incubating.jar 添加到hive/lib 目录下
            cp hudi-hadoop-mr-0.5.2-incubating.jar /opt/cloudera/parcels/CDH/lib/hive/lib/

    
    