##Hudi集成Hive

    1. 将 /opt/apps/hudi/packaging/hudi-hadoop-mr-bundle/target/hudi-hadoop-mr-bundle-0.5.2-incubating.jar 复制到 Hive lib目录下
    
    2. 重启metastore以及hiveserver2服务
    
    3. 使用spark以Hudi存储格式写数据至HDFS
        
        3.1 添加相关依赖
                <dependency>
                    <groupId>org.apache.hudi</groupId>
                    <artifactId>hudi-spark_2.11</artifactId>
                    <version>0.5.2-incubating</version>
                </dependency>
                <dependency>
                    <groupId>org.apache.hudi</groupId>
                    <artifactId>hudi-common</artifactId>
                    <version>0.5.2-incubating</version>
                </dependency>
                
        3.2 主要代码
        
                def main(args: Array[String]): Unit = {
                    val sss = SparkSession.builder.appName("hudi")
                      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                      .config("hive.metastore.uris", "thrift://ip:port")
                      .enableHiveSupport().getOrCreate()
                    val sql = "select * from ods.ods_user_event"
                    val df: DataFrame = sss.sql(sql)
                    df.write.format("org.apache.hudi")
                      .option(DataSourceWriteOptions.RECORDKEY_FIELD_OPT_KEY, "recordKey")
                      .option(DataSourceWriteOptions.PRECOMBINE_FIELD_OPT_KEY, "update_time")
                      .option(DataSourceWriteOptions.PARTITIONPATH_FIELD_OPT_KEY, "date")
                      .option(HoodieIndexConfig.BLOOM_INDEX_UPDATE_PARTITION_PATH, "true")
                      .option(HoodieIndexConfig.INDEX_TYPE_PROP, HoodieIndex.IndexType.GLOBAL_BLOOM.name())
                      .option("hoodie.insert.shuffle.parallelism", "10")
                      .option("hoodie.upsert.shuffle.parallelism", "10")
                      .option(HoodieWriteConfig.TABLE_NAME, "ods.ods_user_event_hudi")
                      .mode(SaveMode.Append)
                      .save("/user/hudi/lake/ods.db/ods_user_event_hudi")
                  }
                  
    4. 使用Hive查询数据
        
        4.1 增量表：进行更新操作时，会生成一份log日志文件；进行读取时hudi会对log+全量数据进行合并得到一份全量最新数据进行返回
            _ro：更新原始数据
            _rt：全量最新数据
            
        4.2 全量表：进行更新操作时会对历史数据进行合并，生成另外一份全量最新数据，查询时返回全量最新的数据
        
    5. 当前可使用Hive对Hudi数据进行简单查询，复杂查询还是存在jar报错问题。建议使用spark on hive模式对Hudi进行访问。