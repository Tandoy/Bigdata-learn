## Hudi集成SparkSQL

    1. 首先需要将[HUDI-1659](https://github.com/apache/hudi/pull/2645)拉取到本地打包，生成SPARK_BUNDLE_JAR(hudi-spark-bundle_2.11-0.9.0-SNAPSHOT.jar)包
        
        1.1 方式一：
            git remote add upstream https://github.com/apache/hudi
            git fetch upstream pull/2645/head:2645
            
            如果想保存这个 PR 到自己到 Fork 里：
            git push origin pr2645
            
            编译
            mvn clean package -DskipTests
            
        1.2 方式二：
            直接git clone相应的pr
            git clone https://github.com/pengzhiwei2018/hudi.git
            
            编译
            mvn clean package -DskipTests
            
    2. 启动spark-sql
    
        spark-sql --jars /home/appuser/tangzhi/hudi-spark/hudi-sparkSql/packaging/hudi-spark-bundle/target/hudi-spark-bundle_2.11-0.9.0-SNAPSHOT.jar  --conf 'spark.serializer=org.apache.spark.serializer.KryoSerializer' --conf 'spark.sql.extensions=org.apache.spark.sql.hudi.HoodieSparkSessionExtension'

    3. 设置并发度
    
        3.1 由于Hudi默认upsert/insert/delete的并发度是1500，对于演示的小规模数据集可设置更小的并发度。     
            set hoodie.upsert.shuffle.parallelism = 1;
            set hoodie.insert.shuffle.parallelism = 1;
            set hoodie.delete.shuffle.parallelism = 1;
        
        3.2 同时设置不同步Hudi表元数据
            set hoodie.datasource.meta.sync.enable=false;
            
    4. 建表
    
        create table test_hudi_table (
          id int,
          name string,
          price double,
          ts long,
          dt string
        ) using hudi
         partitioned by (dt)
         options (
          primaryKey = 'id', --主键为id，分区字段为dt，合并字段默认为ts。
          type = 'mor' --表类型为MOR
         )
         location 'file:///tmp/test_hudi_table'
         
         show create table test_hudi_table
         
    5. 插入数据
    
        insert into test_hudi_table select 1 as id, 'hudi' as name, 10 as price, 1000 as ts, '2021-05-05' as dt

    6. 查询数据
    
        select * from test_hudi_table
        
    7. 更新数据
    
        update test_hudi_table set price = 20.0 where id = 1;
        select * from test_hudi_table;
        查看Hudi表的本地目录结构如下，可以看到在update之后又生成了一个deltacommit，同时生成了一个增量log文件。
        
    8. 删除数据
    
        delete from test_hudi_table where id = 1
        查看Hudi表的本地目录结构如下，可以看到delete之后又生成了一个deltacommit，同时生成了一个增量log文件。
        
    9. Merge Into Update
    
        merge into test_hudi_table as t0
         using (
          select 1 as id, 'a1' as name, 12 as price, 1001 as ts, '2021-03-21' as dt
         ) as s0
         on t0.id = s0.id
         when matched and s0.id % 2 = 1 then update set *
         
    10. Merge Into Delete
    
         merge into test_hudi_table t0
          using (
           select 1 as s_id, 'a2' as s_name, 15 as s_price, 1001 as s_ts, '2021-03-21' as dt
          ) s0
          on t0.id = s0.s_id
          when matched and s_ts = 1001 then delete
    11. 删除表
    
        drop table test_hudi_table;
        