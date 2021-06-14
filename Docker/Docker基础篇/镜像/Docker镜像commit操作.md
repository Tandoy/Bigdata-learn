## Docker镜像commit操作

    docker commit提交容器副本使之成为一个新的镜像
    docker commit -m=“提交的描述信息” -a=“作者” 容器ID 要创建的目标镜像名:[标签名]
    
    案例演示
    	从Hub上下载tomcat镜像到本地并成功运行
    		docker run -it -p 8080:8080 tomcat
    			-p 主机端口:docker容器端口
    			-P 随机分配端口
    			i:交互
    			t:终端
    	故意删除上一步镜像生产tomcat容器的文档
    	也即当前的tomcat运行实例是一个没有文档内容的容器，
    以它为模板commit一个没有doc的tomcat新镜像atguigu/tomcat02
    	启动我们的新镜像并和原来的对比
    		启动atguigu/tomcat02，它没有docs
    		新启动原来的tomcat，它有docs