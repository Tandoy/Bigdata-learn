## Presto安装

    1.验证Java安装(java8及以上)
    
        java -version 
        
    2.解压
    
        tar  -zxf  presto-server-0.149.tar.gz 
        cd presto-server-0.149 
        
    3.配置设定
    
        mkdir data
        mkdir etc
        cd etc 
        vi node.properties  
        
           node.environment = production 
           node.id = ffffffff-ffff-ffff-ffff-ffffffffffff 
           node.data-dir = /Users/../workspace/Presto
    
    4.JVM配置
        
        cd etc 
        vi jvm.config  
        
            -server 
            -Xmx16G 
            -XX:+UseG1GC 
            -XX:G1HeapRegionSize = 32M 
            -XX:+UseGCOverheadLimit 
            -XX:+ExplicitGCInvokesConcurrent 
            -XX:+HeapDumpOnOutOfMemoryError 
            -XX:OnOutOfMemoryError = kill -9 %p 
            
    5.配置属性
    
        cd etc 
        vi config.properties  
        
            coordinator = true 
            node-scheduler.include-coordinator = true 
            http-server.http.port = 8080 
            query.max-memory = 5GB 
            query.max-memory-per-node = 1GB 
            discovery-server.enabled = true 
            discovery.uri = http://localhost:8080
            
    6.协调器的配置
    
        cd etc 
        vi config.properties  
        
            coordinator = true 
            node-scheduler.include-coordinator = false 
            http-server.http.port = 8080 
            query.max-memory = 50GB 
            query.max-memory-per-node = 1GB 
            discovery-server.enabled = true 
            discovery.uri = http://localhost:8080 
            
    7.worker配置
    
        cd etc 
        vi config.properties  

            coordinator = false 
            http-server.http.port = 8080 
            query.max-memory = 50GB 
            query.max-memory-per-node = 1GB 
            discovery.uri = http://localhost:8080
            
    8.日志属性
    
        cd etc 
        vi log.properties  
        
            com.facebook.presto = INFO
            
    9.目录属性
    
        cd etc 
        mkdir catalog 
        cd catalog 
        vi jmx.properties  
        connector.name = jmx 
        
    10.启动Presto
    
        bin/launcher start 
        
    11.安装Presto CLI
    
        通过访问以下链接下载Presto CLI， https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/0.149/
        现在，“ presto-cli-0.149-executable.jar”将安装在您的计算机上。
        
        mv presto-cli-0.149-executable.jar presto  
        chmod +x presto
