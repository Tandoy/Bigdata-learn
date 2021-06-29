## Hudi使用二

### 一、使用Hudi-Clustering
```text
Clustering可重新组织数据以提高查询性能，也不会影响摄取速度。
```
```shell script
spark-submit \
--master yarn \
--deploy-mode cluster \
--conf spark.task.cpus=1 \
--conf spark.executor.cores=1 \
--packages org.apache.hudi:hudi-spark-bundle_2.11-0.9.0,org.apache.spark:spark-avro_2.11:2.4.4 \
--class org.apache.hudi.utilities.HoodieClusteringJob `ls /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-spark-bundle/target/hudi-spark-bundle_2.11-0.9.0-SNAPSHOT.jar` \
--schedule \
--base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka \
--table-name hudi_test_kafka \
--spark-memory 1G
```
