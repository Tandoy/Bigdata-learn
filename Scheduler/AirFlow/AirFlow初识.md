一、调度工具比较

Apache Oozie：

	使用XML配置, Oozie任务的资源文件都必须存放在HDFS上. 配置不方便同时也只能用于Hadoop.
	
Linkedin Azkaban：

	web界面尤其很赞, 使用java properties文件维护任务依赖关系, 任务资源文件需要打包成zip, 部署不是很方便.
	
airflow：

	具有自己的web任务管理界面，dag任务创建通过python代码，可以保证其灵活性和适应性
	
二、web界面使用介绍

UI界面包含8个圆圈，每个圆圈代表task的执行状态和次数

	圈1 success：现实成功的task数，基本上就是该tag包含多少个task，这里基本上就显示几。
	圈2 running：正在运行的task数
	圈3 failed：失败的task数
	圈4 unstream_failed:
	圈5 skipped:跳过的task数
	圈6 up_for_retry:执行失败的task，重新执行的task数
	圈7 queued：队列，等待执行的task数
	圈8 scheduled:刚开始调度dag时，这一次执行总共调度了dag下面多少个task数，并且随着task的执行成功，数值逐渐减少。

这里显示dag的执行信息，包括3个圆圈，每个圆圈代表dag的执行状态和次数

	圈1 success：总共执行成功的dag数，执行次数
	圈2 runing：正在执行dag数
	圈3 faild：执行失败的dag数
	
Links：

	Trigger Dag 人为执行触发
	Tree View 当dag执行的时候，可以点入，查看每个task的执行状态（基于树状视图）,状态:success,running,failed,skipped,retry,queued,no status
	Graph View 同上，基于图视图（有向无环图），查看每个task的执行状态，状态:success,running,failed,skipped,retry,queued,no status
	Tasks Duration 每个task的执行时间统计，可以选择最近多少次执行（number of runs）
	Task Tries 每个task的重试次数
	Landing Times
	Gantt View 基于甘特图的视图，每个task的执行状态
	Code View 查看任务执行代码
	Logs 查看执行日志，比如失败原因
	Refresh 刷新dag任务
	-Delete Dag 删除该dag任务


当某dag执行失败，可以通过3个View视图去查看是哪个task执行失败。

	Ad Hoc Query：特殊查询
	通过UI界面对一些数据库,数据仓库的进行简单的SQL交互操作.

Charts:

	图表实现数据可视化和图表的工作。通过SQL去源数据库检索一些数据，保存下来，供后续使用。
 	Known Events:已知的事件
	Browse 浏览 

SLA Misses

	Task Instances：查看每个task实例执行情况
	Logs:查看所有dag下面对应的task的日志，并且包含检索
	Jobs：查看dag的执行状态，开始时间和结束时间等指标
	
DAG Runs

	Configuration：查看airflow的配置，即：./airflow_home/airflow.cfg
	Users:查看用户列表，创建用户，删除用户

Connections

	我们的Task需要通过Hook访问其他资源, Hook仅仅是一种访问方式, 就像是JDBC driver一样, 要连接DB, 我们还需要DB的IP/Port/User/Pwd等信息. 这些信息不太适合hard code在每个task中, 可以把它们定义成Connection, airflow将这些connection信息存放在后台的connection表中. 我们可以在WebUI的Admin->Connections管理这些连接.

Variables

	Variable 没有task_id/dag_id属性, 往往用来定义一些系统级的常量或变量, 我们可以在WebUI或代码中新建/更新/删除Variable. 也可以在WebUI上维护变量.
	Variable 的另一个重要的用途是, 我们为Prod/Dev环境做不同的设置, 详见后面的开发小节.
	
XComs

	XCom和Variable类似, 用于Task之间共享一些信息. XCom 包含task_id/dag_id属性, 适合于Task之间传递数据, XCom使用方法比Variables复杂些. 比如有一个dag, 两个task组成(T1->T2), 可以在T1中使用xcom_push()来推送一个kv, 在T2中使用xcom_pull()来获取这个kv.
