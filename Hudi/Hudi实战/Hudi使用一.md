## Hudi使用一

### 一、通过spark-shell启动hudi
    1.其实hudi是一个spark库，所以依赖于spark环境，第一步通过spark-shell启动hudi
    
    spark-shell --packages org.apache.spark:spark-avro_2.11:2.4.4 --conf 'spark.serializer=org.apache.spark.serializer.KryoSerializer' --jars /opt/apps/hudi/packaging/hudi-spark-bundle/target/hudi-spark-bundle_2.11-0.5.2-incubating.jar 
          
    启动spark-shell无报错即hudi启动成功
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/spark-shell%E5%90%AF%E5%8A%A8Hudi.PNG)
    
### 二、插入数据测试

    import org.apache.hudi.QuickstartUtils._
    import scala.collection.JavaConversions._
    import org.apache.spark.sql.SaveMode._
    import org.apache.hudi.DataSourceReadOptions._
    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._
    val tableName = "hudi_trips_cow"
    val basePath = "hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_trips_cow"
    val dataGen = new DataGenerator
    val inserts = convertToStringList(dataGen.generateInserts(10))
    val df = spark.read.json(spark.sparkContext.parallelize(inserts, 2))
    df.write.format("org.apache.hudi").
      options(getQuickstartWriteConfigs).
      option(PRECOMBINE_FIELD_OPT_KEY, "ts").
      option(RECORDKEY_FIELD_OPT_KEY, "uuid").
      option(PARTITIONPATH_FIELD_OPT_KEY, "partitionpath").
      option(TABLE_NAME, tableName).
      mode(Overwrite).
      save(basePath)
    
### 三、查询数据测试

    val roViewDF = spark.
        read.
        format("org.apache.hudi").
        load(basePath + "/*/*/*/*")
        //load(basePath) 如果使用 "/partitionKey=partitionValue" 文件夹命名格式，Spark将自动识别分区信息
    
    roViewDF.registerTempTable("hudi_trips_cow")
    spark.sql("select fare, begin_lon, begin_lat, ts from  hudi_trips_cow where fare > 20.0").show()
    spark.sql("select _hoodie_commit_time, _hoodie_record_key, _hoodie_partition_path, rider, driver, fare from  hudi_trips_cow").show()

### 四、更新数据测试

    val updates = convertToStringList(dataGen.generateUpdates(10))
    val df = spark.read.json(spark.sparkContext.parallelize(updates, 2));
    df.write.format("org.apache.hudi").
        options(getQuickstartWriteConfigs).
        option(PRECOMBINE_FIELD_OPT_KEY, "ts").
        option(RECORDKEY_FIELD_OPT_KEY, "uuid").
        option(PARTITIONPATH_FIELD_OPT_KEY, "partitionpath").
        option(TABLE_NAME, tableName).
        mode(Append).
        save(basePath);
        
    // reload data
    spark.
        read.
        format("org.apache.hudi").
        load(basePath + "/*/*/*/*").
        createOrReplaceTempView("hudi_ro_table")
    
    val commits = spark.sql("select distinct(_hoodie_commit_time) as commitTime from  hudi_ro_table order by commitTime").map(k => k.getString(0)).take(50)
    val beginTime = commits(commits.length - 2) // commit time we are interested in
    
    // 增量查询数据
    val incViewDF = spark.
        read.
        format("org.apache.hudi").
        option(VIEW_TYPE_OPT_KEY, VIEW_TYPE_INCREMENTAL_OPT_VAL).
        option(BEGIN_INSTANTTIME_OPT_KEY, beginTime).
        load(basePath);
    incViewDF.registerTempTable("hudi_incr_table")
    spark.sql("select `_hoodie_commit_time`, fare, begin_lon, begin_lat, ts from  hudi_incr_table where fare > 20.0").show()
    
### 五、特定时间点查询

    val beginTime = "000" // Represents all commits > this time.
    val endTime = commits(commits.length - 2) // commit time we are interested in
    
    // 增量查询数据
    val incViewDF = spark.read.format("org.apache.hudi").
        option(VIEW_TYPE_OPT_KEY, VIEW_TYPE_INCREMENTAL_OPT_VAL).
        option(BEGIN_INSTANTTIME_OPT_KEY, beginTime).
        option(END_INSTANTTIME_OPT_KEY, endTime).
        load(basePath);
    incViewDF.registerTempTable("hudi_incr_table")
    spark.sql("select `_hoodie_commit_time`, fare, begin_lon, begin_lat, ts from  hudi_incr_table where fare > 20.0").show()

    
