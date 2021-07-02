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
异步模式：
spark-submit \
--master yarn \
--deploy-mode cluster \
--conf spark.task.cpus=1 \
--conf spark.executor.cores=1 \
--class org.apache.hudi.utilities.HoodieClusteringJob `ls /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.9.0-SNAPSHOT.jar` \
--schedule \
--base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka \
--table-name hudi_test_kafka \
--spark-memory 1G
```
### 二、Debug HoodieClusteringJob
```shell script
spark-submit \
--master spark://dxbigdata101:7077 \
--driver-java-options \
"-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8023" \
--conf spark.task.cpus=1 \
--conf spark.executor.cores=1 \
--class org.apache.hudi.utilities.HoodieClusteringJob `ls /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.9.0-SNAPSHOT.jar` \
--schedule \
--base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka \
--table-name hudi_test_kafka \
--spark-memory 1G
```