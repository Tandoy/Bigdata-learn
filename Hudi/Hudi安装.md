##Hudi安装

    1.编译
        git clone https://github.com/apache/hudi.git && cd hudi
        mvn clean package -DskipTests（window环境一定要加上-DskipITs，不然会编译docker文件启动服务运行linux命令导致报错）