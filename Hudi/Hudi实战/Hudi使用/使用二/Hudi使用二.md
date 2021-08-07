## Hudi使用二

### 一、使用Hudi-Clustering
```text
Clustering对数据按照数据特征进行聚簇，以便优化文件大小和数据布局。可重新组织数据以提高查询性能，也不会影响摄取速度。现在Hudi支持同步和异步的Clustering模式。
```
```shell script
同步模式：
import org.apache.hudi.QuickstartUtils._
import scala.collection.JavaConversions._
import org.apache.spark.sql.SaveMode._
import org.apache.hudi.DataSourceReadOptions._
import org.apache.hudi.DataSourceWriteOptions._
import org.apache.hudi.config.HoodieWriteConfig._
val df =  //generate data frame
df.write.format("org.apache.hudi").
        options(getQuickstartWriteConfigs).
        option(PRECOMBINE_FIELD_OPT_KEY, "ts").
        option(RECORDKEY_FIELD_OPT_KEY, "uid").
        option(PARTITIONPATH_FIELD_OPT_KEY, "ts").
        option(TABLE_NAME, "hudi_test_kafka").
        option("hoodie.parquet.small.file.limit", "0").
        option("hoodie.clustering.inline", "true").
        option("hoodie.clustering.inline.max.commits", "4").
        option("hoodie.clustering.plan.strategy.target.file.max.bytes", "1073741824").
        option("hoodie.clustering.plan.strategy.small.file.limit", "629145600").
        option("hoodie.clustering.plan.strategy.sort.columns", "uid"). //optional, if sorting is needed as part of rewriting data
        mode(Append).
        save("hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka");
```
```shell script
异步模式：分为两步(1.doSchedule,生成.replacecommit.requested; 2.doCluster,拿到doCluster相关信息进行真正的Clustering)
spark-submit \
spark2-submit \
--master yarn \
--deploy-mode client \
--conf spark.task.cpus=1 \
--conf spark.executor.cores=1 \
--class org.apache.hudi.utilities.HoodieClusteringJob `ls /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.9.0-SNAPSHOT.jar` \
--schedule \
--base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flink \
--table-name hudi_on_flink \
--spark-memory 1G

spark2-submit \
--master yarn \
--deploy-mode client \
--conf spark.task.cpus=1 \
--conf spark.executor.cores=1 \
--class org.apache.hudi.utilities.HoodieClusteringJob `ls /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.9.0-SNAPSHOT.jar` \
--instant-time 20210706101201 \
--base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flink \
--table-name hudi_on_flink \
--spark-memory 1G
```
### 二、Debug HoodieClusteringJob
```shell script
spark2-submit \
--master spark://dxbigdata101:7077 \
--driver-java-options \
"-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8023" \
--conf spark.task.cpus=1 \
--conf spark.executor.cores=1 \
--class org.apache.hudi.utilities.HoodieClusteringJob `ls /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.9.0-SNAPSHOT.jar` \
--schedule \
--base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flink \
--table-name hudi_on_flink \
--spark-memory 1G
```

### 三、使用Hudi-OCC
```text
从Hudi-0.8.0版本开始，支持单表乐观锁并发写特性。Hudi支持文件级OCC，即对于发生在同一个表上的任何2个提交（或写入者），如果它们没有写入正在更改的重叠文件，则允许两个写入者成功。此功能目前处于实验阶段，需要Zookeeper或HiveMetastore来获取锁。
```
```shell script
1.Datasource Writer
    import org.apache.hudi.QuickstartUtils._
    import scala.collection.JavaConversions._
    import org.apache.spark.sql.SaveMode._
    import org.apache.hudi.DataSourceReadOptions._
    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._
    val tableName = "hudi_test_kafka"
    val basePath = "hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka"
    val df = spark.read.json(spark.sparkContext.parallelize(inserts, 2))
    df.write.format("hudi")
           .options(getQuickstartWriteConfigs)
           .option("hoodie.cleaner.policy.failed.writes", "LAZY")
           .option("hoodie.write.concurrency.mode", "optimistic_concurrency_control")
           .option("hoodie.write.lock.zookeeper.url", "dxbigdata101")
           .option("hoodie.write.lock.zookeeper.port", "2181")
           .option("hoodie.write.lock.zookeeper.lock_key", "occ")
           .option("hoodie.write.lock.zookeeper.base_path", "/hudi/occ")
           .option(PRECOMBINE_FIELD_OPT_KEY, "ts")
           .option(RECORDKEY_FIELD_OPT_KEY, "uid")
           .option(PARTITIONPATH_FIELD_OPT_KEY, "ts")
           .option(TABLE_NAME, tableName)
           .mode(Overwrite)
           .save(basePath)
```
```shell script
2.DeltaStreamer
        spark-submit --master yarn   \      
          --driver-memory 1G \
          --num-executors 2 \
          --executor-memory 1G \
          --executor-cores 4 \
          --deploy-mode cluster \
          --conf spark.yarn.executor.memoryOverhead=512 \
          --conf spark.yarn.driver.memoryOverhead=512 \
          --class org.apache.hudi.utilities.deltastreamer.HoodieDeltaStreamer `ls /opt/apps/hudi/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.5.2-incubating.jar` \
          --props file:///opt/apps/hudi/hudi-utilities/src/test/resources/delta-streamer-config/kafka.properties \
          --schemaprovider-class org.apache.hudi.utilities.schema.FilebasedSchemaProvider \
          --source-class org.apache.hudi.utilities.sources.JsonKafkaSource \
          --target-base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka \
          --op UPSERT \
          --continuous true \
          --target-table hudi_test_kafka  \
          --table-type MERGE_ON_READ \
          --source-ordering-field uid \
          --source-limit 5000000
```