## Docker容器命令

    1.启动守护式容器
        docker run -d 容器名
             #使用镜像centos:latest以后台模式启动一个容器docker run -d centos 
             问题：然后docker ps -a 进行查看, 会发现容器已经退出很重要的要说明的一点: Docker容器后台运行,就必须有一个前台进程.容器运行的命令如果不是那些一直挂起的命令（比如运行top，tail），就是会自动退出的。 
             这个是docker的机制问题,比如你的web容器,我们以nginx为例，正常情况下,我们配置启动服务只需要启动响应的service即可。例如service nginx start但是,这样做,nginx为后台进程模式运行,就导致docker前台没有运行的应用,这样的容器后台启动后,会立即自杀因为他觉得他没事可做了.
             所以，最佳的解决方案是,将你要运行的程序以前台进程的形式运行
             
    2.查看容器日志
        docker logs -f -t --tail 容器ID
            *   -t 是加入时间戳
            *   -f 跟随最新的日志打印
            *   --tail 数字 显示最后多少条
            
    3.查看容器内运行的进程
        docker top 容器ID
        
    4.查看容器内部细节
        docker inspect 容器ID
        
    5.进入正在运行的容器并以命令行交互
      	docker exec -it 容器ID bashShell
      	重新进入docker attach 容器ID
      	上述两个区别
      		attach 直接进入容器启动命令的终端，不会启动新的进程
      		exec 是在容器中打开新的终端，并且可以启动新的进程
      		
    6.从容器内拷贝文件到主机上
      	docker cp  容器ID:容器内路径 目的主机路径