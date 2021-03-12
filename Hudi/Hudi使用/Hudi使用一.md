##Hudi使用一

###一、通过spark-shell启动hudi
    1.其实hudi是一个spark库，所以依赖于spark环境，第一步通过spark-shell启动hudi
    
    spark-shell --packages org.apache.spark:spark-avro_2.11:2.4.0 --conf 'spark.serializer=org.apache.spark.serializer.KryoSerializer' --jars /opt/apps/hudi/packaging/hudi-spark-bundle/target/hudi-spark-bundle_2.11-0.7.0.jar 
          
    启动spark-shell无报错即hudi启动成功
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/spark-shell%E5%90%AF%E5%8A%A8Hudi.PNG)
    
###二、插入数据测试

    import org.apache.hudi.QuickstartUtils._
    import scala.collection.JavaConversions._
    import org.apache.spark.sql.SaveMode._
    import org.apache.hudi.DataSourceReadOptions._
    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._
    val tableName = "hudi_trips_cow"
    val basePath = "file:///tmp/hudi_trips_cow"
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
    

###三、查询数据测试


###四、更新数据测试

    
