##Hudi使用一

    1.其实hudi是一个spark库，所以依赖于spark环境，第一步通过spark-shell启动hudi
    
    spark-shell \
      --jars `ls packaging/hudi-spark-bundle/target/hudi-spark-bundle_2.11-*.*.*-SNAPSHOT.jar` \
      --conf 'spark.serializer=org.apache.spark.serializer.KryoSerializer'
      
    启动spark-shell无报错即hudi启动成功