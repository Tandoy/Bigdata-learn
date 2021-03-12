##Hudi使用一

###一、通过spark-shell启动hudi
    1.其实hudi是一个spark库，所以依赖于spark环境，第一步通过spark-shell启动hudi
    
    spark-shell --packages org.apache.spark:spark-avro_2.11:2.4.0 --conf 'spark.serializer=org.apache.spark.serializer.KryoSerializer' --jars /opt/apps/hudi/packaging/hudi-spark-bundle/target/hudi-spark-bundle_2.11-0.7.0-SNAPSHOT.jar 
          
    启动spark-shell无报错即hudi启动成功
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/Hudi-cli%E6%B5%8B%E8%AF%95.PNG)
    
###二、插入数据测试

    

###三、查询数据测试


###四、更新数据测试

    