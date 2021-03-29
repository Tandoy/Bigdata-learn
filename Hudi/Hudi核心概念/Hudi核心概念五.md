##Hudi核心概念五

###一、Hudi查询速度提升解析

    1.1简介
    
           为能够支持快速摄取的同时不影响查询性能，Hudi引入了Clustering服务来重写数据以优化Hudi数据湖文件的布局。
       Clustering服务可以异步或同步运行，Clustering会添加了一种新的REPLACE操作类型，该操作类型将在Hudi元数据时间轴中标记Clustering操作。
       Clustering分为两个部分： 
            ·调度Clustering：使用可插拔的Clustering策略创建Clustering计划。
            ·执行Clustering：使用执行策略处理计划以创建新文件并替换旧文件。
            
    1.2调度Clustering
    
        调度Clustering会有如下步骤：
        
        ·识别符合Clustering条件的文件：根据所选的Clustering策略，调度逻辑将识别符合Clustering条件的文件。
        ·根据特定条件对符合Clustering条件的文件进行分组。每个组的数据大小应为targetFileSize的倍数。分组是计划中定义的"策略"的一部分。此外还有一个选项可以限制组大小，以改善并行性并避免混排大量数据。
        ·最后将Clustering计划以avro元数据格式保存到时间线。
        
    1.3 代码实例
    
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
                option(RECORDKEY_FIELD_OPT_KEY, "uuid").
                option(PARTITIONPATH_FIELD_OPT_KEY, "partitionpath").
                option(TABLE_NAME, "tableName").
                option("hoodie.parquet.small.file.limit", "0").
                option("hoodie.clustering.inline", "true").
                option("hoodie.clustering.inline.max.commits", "4").
                option("hoodie.clustering.plan.strategy.target.file.max.bytes", "1073741824").
                option("hoodie.clustering.plan.strategy.small.file.limit", "629145600").
                option("hoodie.clustering.plan.strategy.sort.columns", "column1,column2"). //optional, if sorting is needed as part of rewriting data
                mode(Append).
                save("dfs://location");