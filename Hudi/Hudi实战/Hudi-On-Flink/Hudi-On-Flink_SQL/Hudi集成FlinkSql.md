##Hudi集成FlinkSql

前提：当前Flink版本(1.12.2)在hudi-master最新版，实现了新的Flink Streaming Writer、支持 Flink SQL API、支持 batch 和 streaming 的模式 Reader

    1.启动Flink集群
    
        ./bin/start-cluster.sh
        
    2.启动 Flink SQL Client（应用外部jar包）
    
        ./bin/sql-client.sh embedded -j /home/appuser/tangzhi/hudi-flink/hudi/packaging/hudi-flink-bundle/target/hudi-flink-bundle_2.11-0.9.0-SNAPSHOT.jar shell
        
###一、Batch 模式的读写

    1.建表
    
        create table t2(
        	uuid varchar(20),
        	name varchar(10),
        	age int,
        	ts timestamp(3),
        	`partition` varchar(20)
        )
        PARTITIONED BY (`partition`)
        with (
          'connector' = 'hudi',
          'path' = 'hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flinksql'
        );
        
    DDL里申明了表的path，record key 为默认值 uuid，pre-combine key 为默认值 ts 
    
    2.然后通过 VALUES 语句往表中插入数据：
    
        insert into t2 values
        ('id1','Danny',23,TIMESTAMP '1970-01-01 00:00:01','par1'),
        ('id2','Stephen',33,TIMESTAMP '1970-01-01 00:00:02','par1'),
        ('id3','Julian',53,TIMESTAMP '1970-01-01 00:00:03','par2'),
        ('id4','Fabian',31,TIMESTAMP '1970-01-01 00:00:04','par2'),
        ('id5','Sophia',18,TIMESTAMP '1970-01-01 00:00:05','par3'),
        ('id6','Emma',20,TIMESTAMP '1970-01-01 00:00:06','par3'),
        ('id7','Bob',44,TIMESTAMP '1970-01-01 00:00:07','par4'),
        ('id8','Han',56,TIMESTAMP '1970-01-01 00:00:08','par4');
        
    此时就可在Yarn或者Flink UI 看到作业已经成功提交到集群，并且数据存储至Hudi成功
    
    3.查询数据
    
        set execution.result-mode=tableau; //可以让查询结果直接输出到终端。
        
        select * from t2;
        
        通过在 WHERE 子句中添加 partition 路径来裁剪 partition：
        
            select * from t2 where `partition` = 'par1';
            
    4.更新数据
    
        相同的 record key 的数据会自动覆盖，通过 INSERT 相同 key 的数据可以实现更新操作：
            
            insert into t2 values
            ('id1','Danny',24,TIMESTAMP '1970-01-01 00:00:01','par1'),
            ('id2','Stephen',34,TIMESTAMP '1970-01-01 00:00:02','par1');
            
        再次查询即可看到 uuid 为 id1 和 id2 的数据 age 字段值发生了更新。
        
###二、Streaming 读

    1.建表
    
        create table t1(
        uuid varchar(20),
        name varchar(10),
        age int,
        ts timestamp(3),
        `partition` varchar(20)
        )
        PARTITIONED BY (`partition`)
        with (
          'connector' = 'hudi',
          'path' = 'hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flinksql2',
          'table.type' = 'MERGE_ON_READ',
          'read.streaming.enabled' = 'true',
          'read.streaming.check-interval' = '4'
        );
        
         /**
         这里将 table option read.streaming.enabled 设置为 true，表明通过 streaming 的方式读取表数据；
         opiton read.streaming.check-interval 指定了 source 监控新的 commits 的间隔为 4s；
         option table.type 设置表类型为 MERGE_ON_READ，目前只有 MERGE_ON_READ 表支持 streaming 读。
         */
        
    2.然后通过 VALUES 语句往表中插入数据：
    
        insert into t1 values
        ('id1','Danny',23,TIMESTAMP '1970-01-01 00:00:01','par1'),
        ('id2','Stephen',33,TIMESTAMP '1970-01-01 00:00:02','par1'),
        ('id3','Julian',53,TIMESTAMP '1970-01-01 00:00:03','par2'),
        ('id4','Fabian',31,TIMESTAMP '1970-01-01 00:00:04','par2'),
        ('id5','Sophia',18,TIMESTAMP '1970-01-01 00:00:05','par3'),
        ('id6','Emma',20,TIMESTAMP '1970-01-01 00:00:06','par3'),
        ('id7','Bob',44,TIMESTAMP '1970-01-01 00:00:07','par4'),
        ('id8','Han',56,TIMESTAMP '1970-01-01 00:00:08','par4');
        
    3.从新的 terminal启动 Sql Client，重新创建 t1 表并查询：
        
        ./bin/sql-client.sh embedded -j /home/appuser/tangzhi/hudi-flink/hudi/packaging/hudi-flink-bundle/target/hudi-flink-bundle_2.11-0.9.0-SNAPSHOT.jar shell
        
        create table t1(
        uuid varchar(20),
        name varchar(10),
        age int,
        ts timestamp(3),
        `partition` varchar(20)
        )
        PARTITIONED BY (`partition`)
        with (
          'connector' = 'hudi',
          'path' = 'hdfs://dxbigdata101:8020/user/hudi/test/data/hudi_on_flinksql2',
          'table.type' = 'MERGE_ON_READ',
          'read.streaming.enabled' = 'true',
          'read.streaming.check-interval' = '4'
        );
        
        select * from t1;
        
    4.回到上个terminal，继续执行 batch mode 的 INSERT 操作：
    
        insert into t1 values
        ('id1','Danny',27,TIMESTAMP '1970-01-01 00:00:01','par1');
        ('id9','Fabian',32,TIMESTAMP '1970-01-01 00:00:04','par2'),
        ('id10','Sophia',19,TIMESTAMP '1970-01-01 00:00:05','par3');
        
    5.观察 terminal_2 的输出变化：数据已经可以查询到
        

   