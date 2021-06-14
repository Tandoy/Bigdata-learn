## zookeeper配置参数解读

    1.tickTime =2000：通信心跳数，Zookeeper服务器与客户端心跳时间，单位毫秒
    2.initLimit =10：LF初始通信时限
    3.syncLimit =5：LF同步通信时限
    4.dataDir：数据文件目录+数据持久化路径
    5.clientPort =2181：客户端连接端口
    
## zk shell相关命令
    
    help	            显示所有操作命令
    ls path [watch]	    使用ls命令来查看当前znode中所包含的内容
    ls2 path [watch]    查看当前节点数据并能看到更新次数等数据
    create	            普通创建
                    -s  含有序列
                    -e  临时（重启或者超时消失）
    get path [watch]	获得节点的值
    set	                设置节点的具体值
    stat	            查看节点状态
    delete	            删除节点
    rmr	                递归删除节点
