# linkis探索问题记录

**shell引擎部分**

1.IDEA远程调用shell引擎报错： only owner can mkdir path /tmp/linkis/appuser/log/UJESClient-Test/2021-01-08
解决步骤：

  	1.1.将shell引擎的运行日志存放路径进行更改与linkis日志保持一致
  		vi /home/appuser/dss/linkis-ujes-shell-entrance/conf
		
  		wds.linkis.entrance.config.logPath=file:/home/appuser/dss/log
		
  		wds.linkis.resultSet.store.path=hdfs:/user/appuser/
   
2.执行shell任务报错：资源申请失败
解决步骤：

      2.1 删除shell-enginemanager目录下lib目录中的jackson-core-2.4.3.jar
      
      2.2 重启shell-enginemanager


**spark引擎部分**

1.IDEA远程调用spark引擎报错：DWCException{errCode=60035, desc='资源不足，启动引擎失败', ip='null', port=0, serviceKind='null'}

  解决步骤：
    
  	1.1 linkis源码spark版本与集群版本冲突
	
  		因为linkis源码编译是spark2.x,修改集群spark环境变量即可
		
  		sudo vim /etc/profile
		
  		export SPARK_HOME=/opt/cloudera/parcels/CDH/lib/spark2
		
  		export PATH=$PATH:$SPARK_HOME/bin
   
2.liniks执行spark引擎，appuser没有hdfs:/user/spark写的权限

sudo -u hdfs hdfs dfs -chmod -R 777 /user/spark

**********************************************
##linkis&dss多用户租户实现

###ldap安装

    1.创建管理员 appuser wCGKxDMCxpNWpcMoNEBlV3ox7AYpA5bq
    2.vim passwd.ldif
    
    		dn: dc=my-domain,dc=com
    		objectClass: top
    		objectClass: dcObject
    		objectclass: organization
    		o: my-domain org cn
    		dc: my-domain
    		
    		dn: cn=Manager,dc=my-domain,dc=com
    		objectClass: organizationalRole
    		cn: Manager
    		description: Directory Manager
    		
    		dn: ou=People,dc=my-domain,dc=com
    		objectClass: organizationalUnit
    		ou: People
    		
    		dn: ou=Group,dc=my-domain,dc=com
    		objectClass: organizationalUnit
    		ou: Group
    3.导入数据库
    	ldapadd -x -D "cn=Manager,dc=my-domain,dc=com" -W -f passwd.ldif
    4.sudo vim /etc/openldap/slapd.d/cn=config/olcDatabase\={2}hdb.ldif
    	添加：olcRootPW: {SSHA}wCGKxDMCxpNWpcMoNEBlV3ox7AYpA5bq
    5.重启ldap
    	systemctl restart slapd
    6.测试是否成功
    	ldapsearch -LLL -W -x -D "cn=Manager,dc=my-domain,dc=com" -H ldap://localhost -b "dc=my-domain,dc=com"
    7.ldap添加用户并设置密码
    	touch user.ldif
    	添加如下内容：
    	dn: uid=tangzhi,ou=people,dc=my-domain,dc=com
    	objectClass: top
    	objectClass: account
    	objectClass: posixAccount
    	objectClass: shadowAccount
    	cn: Manager
    	uid: tangzhi
    	uidNumber: 16859
    	gidNumber: 100
    	homeDirectory: /home/tangzhi
    	loginShell: /bin/bash
    	gecos: tangzhi
    	userPassword: {crypt}x
    	shadowLastChange: 0
    	shadowMax: 0
    	shadowWarning: 0
    	添加用户
    	ldapadd -x -W -D "cn=Manager,dc=my-domain,dc=com" -f user.ldif
    	设置密码
    	ldappasswd -s liurijia  -W -D "cn=Manager,dc=my-domain,dc=com" -x "uid=liurijia,ou=People,dc=my-domain,dc=com"

**********************************************
###为用户完善环境信息

    1.在所有Linkis & DSS 服务器上创建对应Linux用户。
    
    2.在Hadoop的NameNode创建对应Linux用户。
    
    3.保证Linkis & DSS 服务器上的Linux用户，可正常使用hdfs dfs -ls /等命令，同时该用户需要能正常使用Spark和hive任务， 如：通过spark-sql命令可以启动一个spark application，通过hive命令可以启动一个hive客户端。
    
    4.由于每个用户的工作空间严格隔离，您还需为该用户创建工作空间和HDFS目录，如下：
    	工作空间：mkdir -p /home/appuser/dss/log/tangzhi
    	HDFS：hdfs dfs -mkdir /user/appuser/tangzhi
    	赋权：hdfs dfs -chmod 777 /user/appuser/tangzhi
    
    5.在dss-server token.properties 添加用户名和密码
    6.schedulis使用管理员账号创建对应用户名

**********************************************
###linkis相关修改

    1.linkis 0.9.4 源码更改并重新编译打包
       https://github.com/WeBankFinTech/Linkis/pull/349/files
    2.配置文件修改
    	vim /home/appuser/dss/linkis-gateway/conf/linkis.properties
    	wds.linkis.ldap.proxy.url=ldap://172.16.0.124:389/
    	wds.linkis.ldap.proxy.baseDN=dc=my-domain,dc=com
    	wds.linkis.ldap.proxy.userNameFormat=uid=%s,ou=people,DC=my-domain,DC=com
    3.linkis代理用户
    	在linkis/linkis-gateway/conf/linkis.properties指定如下参数：
        #打开代理模式
        wds.linkis.gateway.conf.enable.proxy.user=true
        # 指定代理配置文件
        wds.linkis.gateway.conf.proxy.user.config=proxy.properties
    	在linkis/linkis-gateway/conf目录下，创建proxy.properties文件，内容如下：
        tangzhi=appuser
    4.重启linkis-gateway
        ./bin/stop-gateway.sh
    	./bin/start-gateway.sh


