Oozie是一个管理 Apache Hadoop 作业的工作流调度系统。

Oozie的 workflow jobs 是由 actions 组成的 有向无环图(DAG)。

Oozie的 coordinator jobs 是由时间 (频率)和数据可用性触发的重复的 workflow jobs 。

Oozie与Hadoop生态圈的其他部分集成在一起，支持多种类型的Hadoop作业（如Java map-reduce、流式map-reduce、Pig、Hive、Sqoop和Distcp）以及特定于系统的工作（如Java程序和shell脚本）。

Oozie是一个可伸缩、可靠和可扩展的系统。

由架构图可以看出Oozie主要包含以下几个部分：

    （1）Oozie的客户端：可以通过命令行进行提交任务，任务提交的入口，主要进行一些命令行的操作，包括启动一个任务、运行任务、终止任务、恢复任务等操作。Oozie提供了RESTful API接口来接受用户的提交请求(提交工作流作业)。其实，在命令行使用oozie -job xxx命令提交作业，本质上也是发HTTP请求向OozieServer提交作业。

    （2）Oozie Server服务器：Oozie Server服务器提供了对应的执行引擎，包括了工作流执行引擎、协调器引擎、bundle引擎。Oozie Server只负责查询这些Action的执行状态和结果，从而降低了Oozie Server的负载。具体的作业不是在Oozie Server中执行， Oozie Server只对workflow负责，而不是对action负责，workflow中的action是在具体集群中某个节点中运行的。真正的作业是交给yarn去执行，yarn将具体的action分配给具有充足资源的节点，让该节点去运行。

    （3）元数据库：定义了action执行的一些状态信息，一般存放在Mysql数据库中

    （4）hadoop:Oozie是依赖于HDFS进行调度的，际作业的具体执行是由Hadoop执行的，因而Oozie的作业必须放到hadoop集群上。Oozie通过回调和轮询的方式从hadoop集群中获取作业执行结果的。其中，回调是为了降低开销，轮询是为了保证可靠性。用户在HDFS上部署好作业(MR作业)，然后向Oozie提交Workflow，Oozie以异步方式将作业(MR作业)提交给Hadoop。这也是为什么当调用Oozie 的RESTful接口提交作业之后能立即返回一个jobId的原因，用户程序不必等待作业执行完成（因为有些大作业可能会执行很久(几个小时甚至几天)）。Oozie在后台以异步方式，再将workflow对应的Action提交给hadoop执行。（这点可类比kafka，Hbase等，kafka发送端发送数据的时候也是这种异步的策略）

 1.2 Oozie的执行模型（Action原理）
 
    （1） Oozie提供了RESTful API接口来接受用户的提交请求(提交工作流作业)。当用户在客户端命令行使用oozie -job xxx命令提交作业，本质上也是发HTTP请求向OozieServer提交作业。

    （2）OozieServer收到提交的作业命令后，由工作流引擎负责workflow的执行以及状态的转换。比如，从一个Action执行到下一个Action，或者workflow状态由Suspend变成KILLED。Oozie以异步方式将作业(MR作业)提交给Hadoop。

    注：这也是为什么当调用Oozie 的RESTful接口提交作业之后能立即返回一个jobId的原因，用户程序不必等待作业执行完成（因为有些大作业可能会执行很久(几个小时甚至几天)）。Oozie在后台以异步方式，再将workflow对应的Action提交给hadoop执行

    （3） Oozie通过 launcher job 运行某个具体的Action。launcher job是一个 map-only的MR作业，该作业在集群中的执行也是分布式的。这里的launcher需要向yarn集群申请AM运行，同时真正的任务运行也需要先申请AM。

   launcher的作用：

    1）监控和运行具体的action
    2) 一个action对应一个launcher作业（一个action对应一个job，一个job启动一个oozie-launcher）
    3）Launcher job 负责调用对应任务的资源，调用对应的 CLI API 启动 Hadoop、Hive 或者 Pig 作业等等。Launcher job启动的仅是Map的mr作业，这个 Map 作业知道其所对应的工作流中某一个 Action 应该执行的操作，然后实际上执行 Action 操作的 Hadoop 作业将会被启动，你可以认为这些随后启动的作业是 Launcher 作业的子作业。
    4）Oozie launcher任务 的本质就是启动任务所需要的客户端，如hive任务，启动hive客户端，用于提交任务。
    5）除了MR Action外，launcher作业的生命周期和具体任务的生命周期一致。（这点可以在yarn上进行观察）
    由launcher任务监控运行具体任务的优势：减少了Oozie Server服务器的压力，使Oozie Server服务器稳定运行。

整个过程源码简单分析如下：

    Oozie执行Action时，即ActionExecutor（最主要的子类是JavaActionExecutor，hive、spark等action都是这个类的子类），JavaActionExecutor首先会提交一个LauncherMapper（map任务）到yarn，其中会执行LauncherMain（具体的action是其子类，比如JavaMain、SparkMain等），spark任务会执行SparkMain，在SparkMain中会调用org.apache.spark.deploy.SparkSubmit来提交任务。其实诉我的map任务就是识别你是什么样的任务（hive,shell,spark等），并通过该任务来启动任务所需要的环境来提交任务。提供了提交任务的接口（如hive任务，启动hive客户端或beeline等）。
