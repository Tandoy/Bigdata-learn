## 安装单机模式 Pulsar

### 一、系统要求

    Java 8++
    2G JVM++
    
### 二、安装过程记录

    1.下载二进制安装包 
    
        ·download from the Apache mirror (Pulsar 2.7.1 binary release)
        
        ·从 Pulsar 官网下载页下载
        
        ·从 Pulsar 发布页面下载
        
        ·使用 wget 命令下载：wget https://archive.apache.org/dist/pulsar/pulsar-2.7.1/apache-pulsar-2.7.1-bin.tar.gz
        
    2.下载好压缩文件后，解压缩并使用 cd 命令进入文件所在位置
    
        tar xvfz apache-pulsar-2.7.1-bin.tar.gz
        cd apache-pulsar-2.7.1
        
    3.解压后大致文件目录如下：
        
        bin	Pulsar 的命令行工具，比如：pulsar、pulsar-admin。
        conf	Pulsar 的配置文件，包含 broker 配置、ZooKeeper 配置等。
        examples	Java JAR 包，包含 Pulsar Functions 的示例。
        lib	Pulsar使用到的 JAR 文件
        licenses	开源许可文件，.txt 格式，Pulsar 代码库的各个组件。
        
        运行 Pulsar 会立即生成以下目录。
        
        目录	内容
        data	ZooKeeper和BookKeeper使用的数据存储目录
        instances	为 Pulsar Functions 创建的组件。
        logs	安装时生成的日志文件
        
    4.安装内置连接器（可选略）
    
    5.启动单机模式 Pulsar
    
        bin/pulsar standalone
        
    6.使用 Ctrl+C 终止单机模式 Pulsar 的运行
        