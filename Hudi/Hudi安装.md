##Hudi安装

    1.编译
        --pull源码
        git clone https://github.com/apache/hudi.git && cd hudi
        --修改maven+项目镜像源地址为aliyun
        mvn clean package -DskipTests（window环境一定要加上-DskipITs，不然会编译docker文件启动服务运行linux命令导致报错）
        
    2.Hudi CLI测试
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hive/images/Hive%E7%9F%A5%E8%AF%86%E4%BD%93%E7%B3%BB%E6%80%BB%E7%BB%93.png)