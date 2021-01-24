##ElasticSearc安装

    1.官网下载对应系统gz
    https://www.elastic.co/cn/
    
    2.存放至/opt/softwares/
    
    3.解压至/opt/apps，并修改文件名
    
    4.修改相关配置文件
        4.1 相关日志文件配置 log4j2.properties
        4.2 jvm修改 jvm.options,根据集群性能进行修改，默认1G
    
    5. ./bin/elasticsearch -d 后台启动即可
    
    6.测试 
    curl 'http://localhost:9200/?pretty' 出现相应json数据即为安装成功，但存在云主机网络策略问题浏览器拒绝访问情况