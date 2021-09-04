## Hudi踩坑记录

### 一、jar包冲突
```text
Exception in thread "main" java.lang.IllegalArgumentException: requirement failed: Provided Maven Coordinates must be in the form 'groupId:artifactId:version'. The coordinate provided is: org.apache.hudi:hudi-spark-bundle_2.11-0.9.0
	at scala.Predef$.require(Predef.scala:224)
	at org.apache.spark.deploy.SparkSubmitUtils$$anonfun$extractMavenCoordinates$1.apply(SparkSubmit.scala:1018)
	at org.apache.spark.deploy.SparkSubmitUtils$$anonfun$extractMavenCoordinates$1.apply(SparkSubmit.scala:1016)
	at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:234)
	at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:234)
	at scala.collection.IndexedSeqOptimized$class.foreach(IndexedSeqOptimized.scala:33)
	at scala.collection.mutable.ArrayOps$ofRef.foreach(ArrayOps.scala:186)
	at scala.collection.TraversableLike$class.map(TraversableLike.scala:234)
	at scala.collection.mutable.ArrayOps$ofRef.map(ArrayOps.scala:186)
	at org.apache.spark.deploy.SparkSubmitUtils$.extractMavenCoordinates(SparkSubmit.scala:1016)
	at org.apache.spark.deploy.SparkSubmitUtils$.resolveMavenCoordinates(SparkSubmit.scala:1264)
	at org.apache.spark.deploy.DependencyUtils$.resolveMavenDependencies(DependencyUtils.scala:54)
	at org.apache.spark.deploy.SparkSubmit.prepareSubmitEnvironment(SparkSubmit.scala:315)
	at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:143)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:86)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:924)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:933)
	at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
此问题是命令问题，不用package选项即可
```

### 二、Spark相关报错
```text
org.apache.spark.SparkException: Exception thrown in awaitResult: 
	at org.apache.spark.util.ThreadUtils$.awaitResult(ThreadUtils.scala:226)
	at org.apache.spark.deploy.yarn.ApplicationMaster.runDriver(ApplicationMaster.scala:468)
	at org.apache.spark.deploy.yarn.ApplicationMaster.org$apache$spark$deploy$yarn$ApplicationMaster$$runImpl(ApplicationMaster.scala:305)
	at org.apache.spark.deploy.yarn.ApplicationMaster$$anonfun$run$1.apply$mcV$sp(ApplicationMaster.scala:245)
	at org.apache.spark.deploy.yarn.ApplicationMaster$$anonfun$run$1.apply(ApplicationMaster.scala:245)
	at org.apache.spark.deploy.yarn.ApplicationMaster$$anonfun$run$1.apply(ApplicationMaster.scala:245)
	at org.apache.spark.deploy.yarn.ApplicationMaster$$anon$3.run(ApplicationMaster.scala:789)
	at java.security.AccessController.doPrivileged(Native Method)
	at javax.security.auth.Subject.doAs(Subject.java:422)
	at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1920)
	at org.apache.spark.deploy.yarn.ApplicationMaster.doAsUser(ApplicationMaster.scala:788)
	at org.apache.spark.deploy.yarn.ApplicationMaster.run(ApplicationMaster.scala:244)
	at org.apache.spark.deploy.yarn.ApplicationMaster$.main(ApplicationMaster.scala:813)
	at org.apache.spark.deploy.yarn.ApplicationMaster.main(ApplicationMaster.scala)
Caused by: java.io.FileNotFoundException: /tmp/spark-events/application_1609324396857_92755_1.inprogress (Permission denied)
	at java.io.FileOutputStream.open0(Native Method)
	at java.io.FileOutputStream.open(FileOutputStream.java:270)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:213)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:101)
	at org.apache.spark.scheduler.EventLoggingListener.start(EventLoggingListener.scala:115)
	at org.apache.spark.SparkContext.<init>(SparkContext.scala:523)
	at org.apache.spark.api.java.JavaSparkContext.<init>(JavaSparkContext.scala:58)
	at org.apache.hudi.utilities.UtilHelpers.buildSparkContext(UtilHelpers.java:266)
	at org.apache.hudi.utilities.HoodieClusteringJob.main(HoodieClusteringJob.java:107)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.apache.spark.deploy.yarn.ApplicationMaster$$anon$2.run(ApplicationMaster.scala:694)
此问题主要是spark任务需要存放任务运行日志，创建/tmp/spark-events并chmod 777即可
```

### 三、Hudi-OCC
```text
org.apache.hudi.exception.HoodieDeltaStreamerException: Unable to find previous checkpoint. Please double check if this table was indeed built via delta streamer. Last Commit :Option{val=[20210903233550__commit__COMPLETED]}, Instants :[[20210903233550__commit__COMPLETED]], CommitMetadata={
  "partitionToWriteStats" : {
    "2021/08/14" : [ {
      "fileId" : "4cfbfc21-273d-4b79-b8c0-11269b3b3112-0",
      "path" : "2021/08/14/4cfbfc21-273d-4b79-b8c0-11269b3b3112-0_0-22-22_20210903233550.parquet",
      "prevCommit" : "null",
      "numWrites" : 1,
      "numDeletes" : 0,
      "numUpdateWrites" : 0,
      "numInserts" : 1,
      "totalWriteBytes" : 436383,
      "totalWriteErrors" : 0,
      "tempPath" : null,
      "partitionPath" : "2021/08/14",
      "totalLogRecords" : 0,
      "totalLogFilesCompacted" : 0,
      "totalLogSizeCompacted" : 0,
      "totalUpdatedRecordsCompacted" : 0,
      "totalLogBlocks" : 0,
      "totalCorruptLogBlock" : 0,
      "totalRollbackBlocks" : 0,
      "fileSizeInBytes" : 436383,
      "minEventTime" : null,
      "maxEventTime" : null
    } ]
此问题jira中已存在：https://issues.apache.org/jira/browse/HUDI-2275
暂没解决
```
