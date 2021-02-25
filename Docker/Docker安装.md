##Docker安装

###一、安装前提要求

    CentOS Docker安装Docker支持以下的CentOS版本：CentOS 7 (64-bit)CentOS 6.5 (64-bit) 或更高的版本前提条件目前，CentOS仅发行版本中的内核支持 Docker。Docker运行在 CentOS7上，要求系统为64位、系统内核版本为 3.10 以上。
    Docker运行在CentOS-6.5 或更高的版本的CentOS上，要求系统为64位、系统内核版本为 2.6.32-431 或者更高版本。 
    查看自己的内核uname命令用于打印当前系统相关信息（内核版本号、硬件架构、主机名称和操作系统类型等）。 
    查看已安装的CentOS版本信息（CentOS6.8有，CentOS7无该命令）             
    
###二、安装

    1.CentOS6.8安装Docker
    
      yum install -y epel-release
      yum install -y docker-io
      安装后的配置文件：/etc/sysconfig/docker
      启动Docker后台服务：service docker start
      docker version验证
      
    2.CentOS7安装Docker
    
      https://docs.docker.com/install/linux/docker-ce/centos/
      
    3.测试
    
      docker run hello-world
    