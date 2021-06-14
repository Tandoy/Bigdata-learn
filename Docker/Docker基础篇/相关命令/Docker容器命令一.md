## Docker容器命令
    
    1.启动容器
        docker run [OPTIONS] IMAGE [COMMAND] [ARG...]
             OPTIONS说明（常用）：
             有些是一个减号，有些是两个减号 --name="容器新名字": 为容器指定一个名称；
             -d: 后台运行容器，并返回容器ID，也即启动守护式容器；
             -i：以交互模式运行容器，通常与 -t 同时使用；
             -t：为容器重新分配一个伪输入终端，通常与 -i 同时使用；
             -P: 随机端口映射；
             -p: 指定端口映射，有以下四种格式      
                    ip:hostPort:containerPort      
                    ip::containerPort      
                    hostPort:containerPort      
                    containerPort
                    
    2.列出当前所有正在运行的容器
        docker ps [OPTIONS]
            OPTIONS说明（常用）： 
            -a :列出当前所有正在运行的容器+历史上运行过的
            -l :显示最近创建的容器。
            -n：显示最近n个创建的容器。
            -q :静默模式，只显示容器编号。
            --no-trunc :不截断输出。
            
    3.退出容器
        exit 容器停止退出
        ctrl+P+Q 容器不停止退出
        
    4.启动容器
        docker start 容器ID或者容器名
        
    5.重启容器
        docker restart 容器ID或者容器名
        
    6.停止容器
        docker stop 容器ID或者容器名
        
    7.强制停止容器
        docker kill 容器ID或者容器名
        
    8.删除已停止的容器
        docker rm 容器ID
            一次性删除多个容器
            	docker rm -f $(docker ps -a -q)
            	docker ps -a -q | xargs docker rm