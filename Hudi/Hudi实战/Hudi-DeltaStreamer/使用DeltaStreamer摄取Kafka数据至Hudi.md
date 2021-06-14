## 使用DeltaStreamer摄取数据至Hudi

    HoodieDeltaStreamer实用工具 (hudi-utilities-bundle中的一部分) 提供了从DFS或Kafka等不同来源进行摄取的方式。
    
        ·从Kafka单次摄取新事件，从Sqoop、HiveIncrementalPuller输出或DFS文件夹中的多个文件增量导入
        ·支持json、avro或自定义记录类型的传入数据
        ·管理检查点，回滚和恢复
        ·利用DFS或Confluent schema注册表的Avro模式。
        ·支持自定义转换操作
        
### 一、使用DeltaStreamer同步kafka数据至Hudi

    kafka.properties
        
        hoodie.upsert.shuffle.parallelism=2
        hoodie.insert.shuffle.parallelism=2
        hoodie.bulkinsert.shuffle.parallelism=2
        hoodie.datasource.write.recordkey.field=uid
        hoodie.datasource.write.partitionpath.field=ts
        hoodie.deltastreamer.schemaprovider.source.schema.file=hdfs://dxbigdata101:8020/user/hudi/test/data/schema.avsc
        hoodie.deltastreamer.schemaprovider.target.schema.file=hdfs://dxbigdata101:8020/user/hudi/test/data/schema.avsc
        hoodie.deltastreamer.source.kafka.topic=hudi_test_kafka
        group.id=hudi_test_kafka
        bootstrap.servers=dxbigdata103:9092
        auto.offset.reset=latest
        hoodie.parquet.max.file.size=134217728
        hoodie.datasource.write.keygenerator.class=org.apache.hudi.utilities.keygen.TimestampBasedKeyGenerator
        hoodie.deltastreamer.keygen.timebased.timestamp.type=DATE_STRING
        hoodie.deltastreamer.keygen.timebased.input.dateformat=yyyy-MM-dd HH:mm:ssuid
        hoodie.deltastreamer.keygen.timebased.output.dateformat=yyyy/MM/dd


    DeltaStreamer启动命令：
    
        spark-submit --master yarn   \      
          --driver-memory 1G \
          --num-executors 2 \
          --executor-memory 1G \
          --executor-cores 4 \
          --deploy-mode cluster \
          --conf spark.yarn.executor.memoryOverhead=512 \
          --conf spark.yarn.driver.memoryOverhead=512 \
          --class org.apache.hudi.utilities.deltastreamer.HoodieDeltaStreamer `ls /opt/apps/hudi/packaging/hudi-utilities-bundle/target/hudi-utilities-bundle_2.11-0.5.2-incubating.jar` \
          --props file:///opt/apps/hudi/hudi-utilities/src/test/resources/delta-streamer-config/kafka.properties \ '启动delateStreamer所需要的配置文件'
          --schemaprovider-class org.apache.hudi.utilities.schema.FilebasedSchemaProvider \
          --source-class org.apache.hudi.utilities.sources.JsonKafkaSource \  '这里我选择从kafka消费json数据格式'
          --target-base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_test_kafka \ 'hudi数据存储地址'
          --op UPSERT \
          --target-table hudi_test_kafka  \
          --table-type MERGE_ON_READ \
          --source-ordering-field uid \
          --source-limit 5000000