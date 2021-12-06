## Hudi集成Flink

前提：当前Flink版本(1.11.2)的Hudi((0.7.0))还只支持读取Kafka数据，Sink到COW（COPY_ON_WRITE）类型的Hudi表中

    1. 由于考虑到稳定性，集群暂时还是使用Hudi-0.5.2，故自行下载最新版进行编译打包
    
        git clone https://github.com.cnpmjs.org/apache/hudi.git && cd hudi
        mvn clean package -DskipTests
        
        Windows 系统用户打包时会报如下错误：
        
            [ERROR] Failed to execute goal org.codehaus.mojo:exec-maven-plugin:1.6.0:exec (Setup HUDI_WS) on project hudi-integ-test: Command execution failed. Cannot run program "\bin\bash" (in directory "D:\github\hudi\hudi-integ-test"): Crea
            teProcess error=2, 系统找不到指定的文件。 -> [Help 1]
            [ERROR]
            [ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
            [ERROR] Re-run Maven using the -X switch to enable full debug logging.
            [ERROR]
            [ERROR] For more information about the errors and possible solutions, please read the following articles:
            [ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
            [ERROR]
            [ERROR] After correcting the problems, you can resume the build with the command
            [ERROR]   mvn <goals> -rf :hudi-integ-test
            
       这是 hudi-integ-test 模块的一个bash脚本无法执行导致的错误，我们可以把它注释掉。
       
       修改D:\github\hudi\pom.xml根pom文件
       
         <modules>
           <module>hudi-common</module>
           <module>hudi-cli</module>
           <module>hudi-client</module>
           <module>hudi-hadoop-mr</module>
           <module>hudi-spark</module>
           <module>hudi-timeline-service</module>
           <module>hudi-utilities</module>
           <module>hudi-sync</module>
           <module>packaging/hudi-hadoop-mr-bundle</module>
           <module>packaging/hudi-hive-sync-bundle</module>
           <module>packaging/hudi-spark-bundle</module>
           <module>packaging/hudi-presto-bundle</module>
           <module>packaging/hudi-utilities-bundle</module>
           <module>packaging/hudi-timeline-server-bundle</module>
           <module>docker/hoodie/hadoop</module>
       <!--    <module>hudi-integ-test</module>-->
       <!--    <module>packaging/hudi-integ-test-bundle</module>-->
           <module>hudi-examples</module>
           <module>hudi-flink</module>
           <module>packaging/hudi-flink-bundle</module>
         </modules>
         
        这个 hudi-flink-bundle_2.11-0.9.0-SNAPSHOT.jar 就是我们需要使用的flink客户端，类似于原版的 hudi-utilities-bundle_2.11-x.x.x.jar
        
    2. 入参介绍
    
        有几个必传的参数介绍下：
        
        •--kafka-topic ：Kafka 主题
        •--kafka-group-id ：消费组
        •--kafka-bootstrap-servers : Kafka brokers
        •--target-base-path : Hudi 表基本路径
        •--target-table ：Hudi 表名
        •--table-type ：Hudi 表类型
        •--props : 任务配置
        
        其他参数可以参考 org.apache.hudi.streamer.FlinkStreamerConfig，里面每个参数都有介绍 。
        
    3. 启动准备清单
    
        1.Kafka 主题，消费组 / topic group_id
        2.jar上传到服务器 / hudi-flink-bundle_2.11-0.9.0-SNAPSHOT.jar
        3.schema 文件 / schem.avsc
        4.Hudi任务配置文件 / hudi-conf.properties
        
            hudi-conf.properties内容如下:
                
                hoodie.datasource.write.recordkey.field=uid
                hoodie.datasource.write.partitionpath.field=ts
                bootstrap.servers=dxbigdata103:9092
                hoodie.deltastreamer.keygen.timebased.timestamp.type=DATE_STRING
                hoodie.deltastreamer.keygen.timebased.input.dateformat=yyyy-MM-dd HH:mm:ss
                hoodie.deltastreamer.keygen.timebased.output.dateformat=yyyy/MM/dd
                hoodie.datasource.write.keygenerator.class=org.apache.hudi.keygen.TimestampBasedAvroKeyGenerator
                hoodie.embed.timeline.server=false
                hoodie.deltastreamer.schemaprovider.source.schema.file=hdfs://dxbigdata101:8020/user/hudi/test/data/schema.avsc
                hoodie.deltastreamer.schemaprovider.target.schema.file=hdfs://dxbigdata101:8020/user/hudi/test/data/schema.avsc
                
            schema.avsc内容如下:
            
                {
                  "type":"record",
                  "name":"gmall_event",
                  "fields":[{
                     "name": "area",
                     "type": "string"
                  }, {
                     "name": "uid",
                     "type": "long"
                  }, {
                     "name": "itemid",
                     "type": "string"
                  },{
                     "name": "npgid",
                     "type": "string"
                  },{
                     "name": "evid",
                     "type": "string"
                  },{
                     "name": "os",
                     "type": "string"
                  },{
                     "name": "pgid",
                     "type": "string"
                  },{
                     "name": "appid",
                     "type": "string"
                  },{
                     "name": "mid",
                     "type": "string"
                  }, {
                     "name": "type",
                     "type": "string"
                  }, {
                     "name": "ts",
                     "type":"string"
                  }
                ]}
                
    4. 启动任务
    
        ./flink run -c org.apache.hudi.streamer.HoodieFlinkStreamer -m yarn-cluster -d -yjm 1024 -ytm 1024 -p 4 -ys 3 -ynm hudi_on_flink /home/appuser/tangzhi/hudi-flink/hudi/packaging/hudi-flink-bundle/target/hudi-flink-bundle_2.11-0.9.0-SNAPSHOT.jar --kafka-topic hudi-on-flink --kafka-group-id hudi_on_flink --kafka-bootstrap-servers dxbigdata103:9092 --table-type COPY_ON_WRITE --target-base-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flink --target-table hudi_on_flink  --props hdfs://dxbigdata101:8020/user/hudi/test/data/hudi-conf.properties --checkpoint-interval 3000 --flink-checkpoint-path hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flink_cp --read-schema-path hdfs://dxbigdata101:8020/user/hudi/test/data/schema.avsc

[一种Hudi on Flink动态同步元数据变化的方法](https://mp.weixin.qq.com/s/hf6WeuqFOrRVTYvSeq9z0Q)