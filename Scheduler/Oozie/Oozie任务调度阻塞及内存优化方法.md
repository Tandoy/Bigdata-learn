## 排查与优化方法

一、问题原因描述

    Oozie在执行过程中如果有多个fork产生并行任务的时候，这时会占用很多内存，如果机器的内存不够则会产生调度阻塞。或者是同时提交了多个Oozie任务，也会产生调度阻塞。
    导致Oozie比较占内存的原因是Oozie启动一个任务时会先启动Oozie launcher任务，该任务占内存比较多，Oozie launcher的声明周期是数据任务开始之前到结束，资源不会释放，
    如果此时数据任务得不到充足的资源就会一直等待有资源过来来执行数据任务，这样就导致了相互等待的过程，造成了死锁现象，导致整个集群陷入阻塞。

二、优化方案

（1）Oozie launcher单个占用资源较大会使用2个container，2core，如果一个container默认占2G的话，2个container则占4G

（2）优化方案

     减小oozie launcher的资源占用
     限制Oozie并发数量
     Yarn中设置不同的队列，将Oozie launcher单独作为一个队列，任务作为一个队列。
     采用公平调度器。
     降低Yarn中Container最小内存限制。
     HQL脚本优化。
     合并HiveQL脚本。（这种方式并不建议；对于出现查询出错的情况，这种合并做法的控制粒度较粗，可能在重新启动动作前需要做一些手工清理的工作。）
  
三、Yarn资源队列设置

    （1）在CDH平台中进行队列配置,将oozie launcher与数据任务放置不同资源池
    （2） 设置 yarn.resourcemanager.scheduler.class
         org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler
    （3） Capacity Scheduler，定义ooize、default两个队列，分别占30%及70%。配置内容如下：
         capacity-scheduler=null
         yarn.scheduler.capacity.default.minimum-user-limit-percent=100
         yarn.scheduler.capacity.maximum-am-resource-percent=0.4
         yarn.scheduler.capacity.maximum-applications=10000
         yarn.scheduler.capacity.node-locality-delay=40
         yarn.scheduler.capacity.root.accessible-node-labels=*
         yarn.scheduler.capacity.root.acl_administer_queue=*
         yarn.scheduler.capacity.root.capacity=100
         yarn.scheduler.capacity.root.default.acl_administer_jobs=*
         yarn.scheduler.capacity.root.default.acl_submit_applications=*
         yarn.scheduler.capacity.root.default.capacity=70
         yarn.scheduler.capacity.root.default.maximum-capacity=70
         yarn.scheduler.capacity.root.default.state=RUNNING
         yarn.scheduler.capacity.root.default.user-limit-factor=1
         yarn.scheduler.capacity.root.queues=default,oozie   
         yarn.scheduler.capacity.root.oozie.acl_administer_jobs=*
         yarn.scheduler.capacity.root.oozie.acl_submit_applications=*
         yarn.scheduler.capacity.root.oozie.capacity=30
         yarn.scheduler.capacity.root.oozie.maximum-capacity=30
         yarn.scheduler.capacity.root.oozie.state=RUNNING
         yarn.scheduler.capacity.root.oozie.user-limit-factor=1
    （4）配置完后保存并重启Yarn即可生效

四、Oozie的调度配置

（1）配置以下两个参数，用来降低单个Oozie launcher内存占用（可以将参数直接配置在workflow。xml，或者配置在oozie.site.xml中全局作用）

      oozie.launcher.mapreduce.map.memory.mb 512 --减少给“hive2等”oozie.launcher作业分配的资源
      oozie.launcher.yarn.app.mapreduce.am.resource.mb  512 --减少oozie.launcher使用AppMaster资源的总内存大小。

五、Yarn最小容器资源

     （1）上述中设置了Oozie launcher启动过程中需要的参数，降低为512M，由于Yarn中容器最小内存限制为1G，此时便会产生冲突，导致了配置参数不会生效，
          因而需要设置Yarn容器的最小限制。可以对如下参数进行设置，配置为512M。CDH直接在web页面更改即可
          yarn.scheduler.minimum-allocation-mb  

