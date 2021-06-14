Airflow安装详解

一、依赖环境准备

       1.1 Airflow需依赖DB，此处选择Mysql
       
             安装步骤可参考：  http://www.buildupchao.cn/installtutorial/2016/12/09/install_mysql5.6_on_centos7.html
             
       1.2 Airflow需python环境，此处选择python3.x
       
             安装步骤可参考： http://www.buildupchao.cn/installtutorial/2019/03/01/python3-install-tutorial.html
             备注：相关命令中文件路径应根据自己解压路径进行替换

二、Ariflow安装

       1）通过pip安装airflow脚手架
             安装之前需要设置一下临时环境变量SLUGIFY_USES_TEXT_UNIDECODE，不然，会导致安装失败，命令如下：
             
              export SLUGIFY_USES_TEXT_UNIDECODE=yes
              
     安装airflow脚手架:
     
               pip3 install apache-airflow===1.10.0

         初始化airflow
        airflow initdb
        启动airflow
        airflow webserver -p 8080

备注：虚拟机Centos由于各版本自带python不同，在安装过程中会出现各种依赖问题，若想跳过选择阿里ECS即可
