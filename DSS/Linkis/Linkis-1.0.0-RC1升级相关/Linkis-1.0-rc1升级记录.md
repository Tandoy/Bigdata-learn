##Linkis-1.0-rc1升级记录：

###一、部署：

   1.1 编译（Windows环境下）：
   
			mvn -N install
			mvn -DskipTests clean package
		适配cdh修改后编译报错：[ERROR] Failed to execute goal net.alchim31.maven:scala-maven-plugin:3.2.2:compile (scala-compile-first) on project linkis-instance-label: Execution scala-compile-first of goal net.alchim31.maven:scala-maven-plugin:3.2.2:compile failed. CompileFailed -> [Help 1]
		
		mvn -DskipTests clean package -X 打开mvn debug模式查看具体信息 
		Caused by: sbt.compiler.CompileFailed
			at sbt.compiler.JavaCompiler$JavaTool0.compile (JavaCompiler.scala:77)
			at sbt.compiler.JavaTool$class.apply (JavaCompiler.scala:35)
			at sbt.compiler.JavaCompiler$JavaTool0.apply (JavaCompiler.scala:63)
			at sbt.compiler.JavaCompiler$class.compile (JavaCompiler.scala:21)
			
		--解决方案：
		使用linux环境进行编译即可！！！
			编译过程中存在pentaho相关jar包无法下载，添加以下镜像源即可
			<mirror>
				<id>nexus-pentaho</id>
				<mirrorOf>central</mirrorOf>
				<name>Nexus pentaho</name>
				<url>https://nexus.pentaho.org/content/repositories/omni/</url>
			</mirror>
		
   1.2 配置以及环境变量相关修改

		vim /opt/apps/linkis/conf/linkis.env.sh
		其它与Linkis-0.9.4大致相同，但需注意一以下两个配置
		
			## Engine version conf 自行修改引擎版本后须指定
			#SPARK_VERSION
			#SPARK_VERSION=2.4.3
			##HIVE_VERSION
			HIVE_VERSION=1.1.0-cdh5.13.3
			#PYTHON_VERSION=python2
			
			## 若开启Linkis代理模式1.0版本须将ECP配置为HDFS，而不能是本地目录
			ENTRANCE_CONFIG_LOG_PATH=hdfs:/tmp/linkis/ ##file:// required
			
	    vim /opt/apps/linkis/conf/db.sh
		
   1.3 执行安装脚本

		sh bin/install.sh
	
   1.4 启动Linkis

		sh sbin/linkis-start-all.sh> start.log 2>start_error.log
	
   1.5 若http://dxbigdata103:20303/ 出现10个微服务即部署成功
	
###二、用户使用引擎测试（Hive、Spark、Shell、Scala、Python）

	2.1 https://github.com/WeBankFinTech/Linkis/wiki/Linkis1.0%E7%94%A8%E6%88%B7%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3