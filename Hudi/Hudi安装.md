##Hudi安装

    1.编译
        --pull源码
        git clone https://github.com/apache/hudi.git && cd hudi
        --修改maven+项目镜像源地址为aliyun
        mvn clean package -DskipTests（window环境一定要加上-DskipITs，不然会编译docker文件启动服务运行linux命令导致报错）
        
    2.Hudi CLI测试
    
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/Hudi-cli%E6%B5%8B%E8%AF%95.PNG)
