## Hudi集成SparkSQL踩坑记录

    1.由于集群为cdh-spark2，没有spark-sql cli环境，需重新编译 spark-assembly相关jar包以及配置
    
    2.hudi 集成spark sql的时候，需要将spark.sql.hive.convertMetastoreParquet这个参数设置为false（测试的是如果不设为false查询的结果不准确可能会有重复数据）,不过这个参数是spark 用自己的类来序列化和反序列化parquet文件.
      这个参数如果不改为false，就不会走spark sql 自己的org.apache.spark.sql.execution.datasources.parquet.ParquetFileFormat类，而会走HoodieParquetInputFormat 这个类,会导致数据不对.