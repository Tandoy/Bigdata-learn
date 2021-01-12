##DSS创建工程失败排查问题记录

前提：DSS换成官方看开源调度schedulis后无法创建工程

排查记录：

日志报错：Get azkaban session is null，获取session失败，"error，login in error"

1.在DSS前端进行请求地址的分析：http://dxbigdata102:443/api/rest_j/v1/dss/addProject

2.知道dss-dist后端添加工程源码部分

![1](https://github.com/Tandoy/Bigdata-learn/blob/master/DSS/images/1.PNG)

3.在DSSProjectServiceImpl中分析addProject方法，它实际还在调用createAppjointProject()

![2](https://github.com/Tandoy/Bigdata-learn/blob/master/DSS/images/2.PNG)

4.在createAppjointProject()中首先会创建session，在日志报错存在   获取session失败，所以就是后端获取session异常

5.session初始化实在AzkabanSecurityService类中调用getSession()完成

  远程调试：

export JAVA_TOOL_OPTIONS=agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044 

   -->经排查URL、username、password全部正确，在dss请求中登陆密码参数为password，而schedulis接收为passpwd

![3](https://github.com/Tandoy/Bigdata-learn/blob/master/DSS/images/3.PNG)

6.这个问题是因为0.9.0及以下的版本默认对接的是Azkaban，然后Azkaban和schedulis在登录的传参有区别导致，这个问题会在1.0的版本进行解决，支持默认对接AZ和schedulis，如果现在的版本需要将az替换为Schedulis需要将dss-azkaban-scheduler-appjoint jar 下载下来放入/dss-appjoints/schedulis/lib/ 并重启dss server
