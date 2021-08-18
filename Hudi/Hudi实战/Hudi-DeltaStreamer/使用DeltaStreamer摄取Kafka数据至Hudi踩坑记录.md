## 使用DeltaStreamer摄取数据至Hudi踩坑记录

    1.Caused by: java.io.FileNotFoundException: /tmp/spark-events/application_1609324396857_16643_2.inprogress (Permission denied)
      	at java.io.FileOutputStream.open0(Native Method)
      	at java.io.FileOutputStream.open(FileOutputStream.java:270)
      	at java.io.FileOutputStream.<init>(FileOutputStream.java:213)
      	at java.io.FileOutputStream.<init>(FileOutputStream.java:101)
      	at org.apache.spark.scheduler.EventLoggingListener.start(EventLoggingListener.scala:115)
      	at org.apache.spark.SparkContext.<init>(SparkContext.scala:523)
      	at org.apache.spark.api.java.JavaSparkContext.<init>(JavaSparkContext.scala:58)
      	at org.apache.hudi.utilities.UtilHelpers.buildSparkContext(UtilHelpers.java:194)
      	at org.apache.hudi.utilities.deltastreamer.HoodieDeltaStreamer.main(HoodieDeltaStreamer.java:292)
      	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
      	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
      	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
      	at java.lang.reflect.Method.invoke(Method.java:498)
      	at org.apache.spark.deploy.yarn.ApplicationMaster$$anon$2.run(ApplicationMaster.scala:694)
      	
    解决方案：在集群每台节点创建/tmp/spark-events 并且 chmod 777 /tmp/spark-events
    
   
    2.Caused by: java.lang.IllegalArgumentException: java.net.UnknownHostException: user
      	at org.apache.hadoop.security.SecurityUtil.buildTokenService(SecurityUtil.java:406)
      	at org.apache.hadoop.hdfs.NameNodeProxies.createNonHAProxy(NameNodeProxies.java:310)
      	at org.apache.hadoop.hdfs.NameNodeProxies.createProxy(NameNodeProxies.java:176)
      	at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:749)
      	at org.apache.hadoop.hdfs.DFSClient.<init>(DFSClient.java:680)
      	at org.apache.hadoop.hdfs.DistributedFileSystem.initialize(DistributedFileSystem.java:158)
      	at org.apache.hadoop.fs.FileSystem.createFileSystem(FileSystem.java:2816)
      	at org.apache.hadoop.fs.FileSystem.access$200(FileSystem.java:98)
      	at org.apache.hadoop.fs.FileSystem$Cache.getInternal(FileSystem.java:2853)
      	at org.apache.hadoop.fs.FileSystem$Cache.get(FileSystem.java:2835)
      	at org.apache.hadoop.fs.FileSystem.get(FileSystem.java:387)
      	at org.apache.hadoop.fs.Path.getFileSystem(Path.java:296)
      	at org.apache.hudi.common.util.FSUtils.getFs(FSUtils.java:93)
      	at org.apache.hudi.utilities.schema.FilebasedSchemaProvider.<init>(FilebasedSchemaProvider.java:56)
      	
    由于schema.avsc是储存在HDFS上,所以kafka.properties的hoodie.deltastreamer.schemaprovider.source.schema.file=hdfs://user/hudi/test/data/schema.avsc是不准确的
    解决方案：修改kafka.properties hoodie.deltastreamer.schemaprovider.source.schema.file=hdfs://dxbigdata101:8020/user/hudi/test/data/schema.avsc
    
    3.Caused by: java.lang.NumberFormatException: For input string: "clickItem"
      	at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
      	at java.lang.Integer.parseInt(Integer.java:580)
      	at java.lang.Integer.valueOf(Integer.java:766)
      	at org.apache.hudi.avro.MercifulJsonConverter$2.convert(MercifulJsonConverter.java:168)
      	at org.apache.hudi.avro.MercifulJsonConverter$JsonToAvroFieldProcessor.convertToAvro(MercifulJsonConverter.java:139)
      	at org.apache.hudi.avro.MercifulJsonConverter.convertJsonToAvroField(MercifulJsonConverter.java:128)
      	at org.apache.hudi.avro.MercifulJsonConverter.convertJsonToAvro(MercifulJsonConverter.java:95)
      	at org.apache.hudi.avro.MercifulJsonConverter.convert(MercifulJsonConverter.java:84)
      	at org.apache.hudi.utilities.sources.helpers.AvroConvertor.fromJson(AvroConvertor.java:85)
      	at org.apache.spark.api.java.JavaPairRDD$$anonfun$toScalaFunction$1.apply(JavaPairRDD.scala:1040)
      	at scala.collection.Iterator$$anon$11.next(Iterator.scala:410)
      	at scala.collection.Iterator$$anon$10.next(Iterator.scala:394)
      	at scala.collection.Iterator$class.foreach(Iterator.scala:891)
      	at scala.collection.AbstractIterator.foreach(Iterator.scala:1334)
      	at scala.collection.generic.Growable$class.$plus$plus$eq(Growable.scala:59)
      	at scala.collection.mutable.ArrayBuffer.$plus$plus$eq(ArrayBuffer.scala:104)
      	at scala.collection.mutable.ArrayBuffer.$plus$plus$eq(ArrayBuffer.scala:48)
      	at scala.collection.TraversableOnce$class.to(TraversableOnce.scala:310)
      	at scala.collection.AbstractIterator.to(Iterator.scala:1334)
      	at scala.collection.TraversableOnce$class.toBuffer(TraversableOnce.scala:302)
      	at scala.collection.AbstractIterator.toBuffer(Iterator.scala:1334)
      	at scala.collection.TraversableOnce$class.toArray(TraversableOnce.scala:289)
      	at scala.collection.AbstractIterator.toArray(Iterator.scala:1334)
      	at org.apache.spark.rdd.RDD$$anonfun$take$1$$anonfun$29.apply(RDD.scala:1364)
      	at org.apache.spark.rdd.RDD$$anonfun$take$1$$anonfun$29.apply(RDD.scala:1364)
      	at org.apache.spark.SparkContext$$anonfun$runJob$5.apply(SparkContext.scala:2101)
      	at org.apache.spark.SparkContext$$anonfun$runJob$5.apply(SparkContext.scala:2101)
      	at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:90)
      	at org.apache.spark.scheduler.Task.run(Task.scala:121)
      	at org.apache.spark.executor.Executor$TaskRunner$$anonfun$10.apply(Executor.scala:408)
      	at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1405)
      	at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:414)
      	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
      	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
      	at java.lang.Thread.run(Thread.java:748)
      	
    自定义的schema.avsc中字段定义与topic数据数据类型不相符
    解决方案：修改schema.avsc
    
    4.21/03/26 17:52:59 WARN scheduler.TaskSetManager: Lost task 0.0 in stage 1.0 (TID 1, dxbigdata101, executor 1): org.apache.hudi.utilities.exception.HoodieDeltaStreamerException: Unable to parse input partition field :hebei
      	at org.apache.hudi.utilities.keygen.TimestampBasedKeyGenerator.getKey(TimestampBasedKeyGenerator.java:123)
      	at org.apache.hudi.utilities.deltastreamer.DeltaSync.lambda$readFromSource$f92c188c$1(DeltaSync.java:338)
      	at org.apache.spark.api.java.JavaPairRDD$$anonfun$toScalaFunction$1.apply(JavaPairRDD.scala:1040)
      	at scala.collection.Iterator$$anon$11.next(Iterator.scala:410)
      	at scala.collection.Iterator$$anon$10.next(Iterator.scala:394)
      	at scala.collection.Iterator$class.foreach(Iterator.scala:891)
      	at scala.collection.AbstractIterator.foreach(Iterator.scala:1334)
      	at scala.collection.generic.Growable$class.$plus$plus$eq(Growable.scala:59)
      	at scala.collection.mutable.ArrayBuffer.$plus$plus$eq(ArrayBuffer.scala:104)
      	at scala.collection.mutable.ArrayBuffer.$plus$plus$eq(ArrayBuffer.scala:48)
      	at scala.collection.TraversableOnce$class.to(TraversableOnce.scala:310)
      	at scala.collection.AbstractIterator.to(Iterator.scala:1334)
      	at scala.collection.TraversableOnce$class.toBuffer(TraversableOnce.scala:302)
      	at scala.collection.AbstractIterator.toBuffer(Iterator.scala:1334)
      	at scala.collection.TraversableOnce$class.toArray(TraversableOnce.scala:289)
      	at scala.collection.AbstractIterator.toArray(Iterator.scala:1334)
      	at org.apache.spark.rdd.RDD$$anonfun$take$1$$anonfun$29.apply(RDD.scala:1364)
      	at org.apache.spark.rdd.RDD$$anonfun$take$1$$anonfun$29.apply(RDD.scala:1364)
      	at org.apache.spark.SparkContext$$anonfun$runJob$5.apply(SparkContext.scala:2101)
      	at org.apache.spark.SparkContext$$anonfun$runJob$5.apply(SparkContext.scala:2101)
      	at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:90)
      	at org.apache.spark.scheduler.Task.run(Task.scala:121)
      	at org.apache.spark.executor.Executor$TaskRunner$$anonfun$10.apply(Executor.scala:408)
      	at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1405)
      	at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:414)
      	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
      	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
      	at java.lang.Thread.run(Thread.java:748)
      Caused by: java.text.ParseException: Unparseable date: "hebei"
      	at java.text.DateFormat.parse(DateFormat.java:366)
      	at org.apache.hudi.utilities.keygen.TimestampBasedKeyGenerator.getKey(TimestampBasedKeyGenerator.java:107)
      	... 27 more
      	
    由于kafka.properties的分区字段为DATE_STRING，但topic是area是string类型
    解决方案：由于Hudi-0.5.2 分区字段配置只支持 UNIX_TIMESTAMP, DATE_STRING, MIXED, EPOCHMILLISECONDS
             重新创建topic，将日志数据ts字段赋值 "ts":"2021-03-26 20:50:28"
    
    5、Hudi-DeltaStreamer总是从最新偏移量开始消费 issue#3471 & issue#3490
    
    Q：When I reverted to Kafka to produce new channel data, HoodieDeltaStreamer data can use the latest Kafka, but the original data is not ingested. But what I have set in the configuration file is auto.offset.reset=earliest.
    A：I checked the source code and found that KAFKA_AUTO_RESET_OFFSETS = "auto.reset.offsets" was used by default when creating the KafkaOffsetGen class, but the official configuration file kafka-source.properties was auto.offset.reset.
       And the latest version of hudi has been fixed