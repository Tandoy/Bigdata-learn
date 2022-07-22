## 记一次Flink线上作业报错异常

### 背景

#### 基础环境
```text
version：1.7
集群部署：华为云
提交方式：Yarn-Per-Job
代码：DataStream API
checkpoint地址：华为云obs
失败重试机制：失败后重试三次再报错
```

#### 故障概述
```text
由于Flink生成的实时指标主要是供于实时大屏展示，窗口级别全部是天以及配合定时器进行秒级计算刷新大屏，所以基本所有任务都是在凌晨进行窗口状态清理以及一些初始化工作。
20号凌晨一个实时计算会员数的Flink作业报错且重试三次后还是失败，涉及实时指标：会员总数、新增会员数、会员发展Top5相关
会员总数实时计算逻辑：T-2仓库dws_snap_members表中会员等级不等于V0且有效会员总数 + m_membermain表中实时新增 + T-1昨日Redis缓存中新增会员总数
```

### 故障排查过程

#### 初步解决
```text
1.由于作业失败在凌晨时间点比较敏感，第一反应是所有作业都在开窗导致CU资源不够导致。所以白天立马进行从checkpoint恢复失败重新提交至华为云，但还是提交失败Yarn上报错日志如下（随便吐槽一下华为云Flink管理界面日志的可信度太低！）

java.lang.RuntimeException: org.apache.flink.runtime.client.JobExecutionException: Could not set up JobManager
	at org.apache.flink.util.function.CheckedSupplier.lambda$unchecked$0(CheckedSupplier.java:36)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1604)
	at akka.dispatch.TaskInvocation.run(AbstractDispatcher.scala:39)
	at akka.dispatch.ForkJoinExecutorConfigurator$AkkaForkJoinTask.exec(AbstractDispatcher.scala:415)
	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
Caused by: org.apache.flink.runtime.client.JobExecutionException: Could not set up JobManager
	at org.apache.flink.runtime.jobmaster.JobManagerRunner.<init>(JobManagerRunner.java:176)
	at org.apache.flink.runtime.dispatcher.Dispatcher$DefaultJobManagerRunnerFactory.createJobManagerRunner(Dispatcher.java:1065)
	at org.apache.flink.runtime.dispatcher.Dispatcher.lambda$createJobManagerRunner$5(Dispatcher.java:309)
	at org.apache.flink.util.function.CheckedSupplier.lambda$unchecked$0(CheckedSupplier.java:34)
	... 7 common frames omitted
Caused by: java.io.IOException: Cannot access file system for checkpoint/savepoint path 'obs://Jk5qYOHpsbjQtmjFCEdS:X4n2PnxMhsqhWsS7osCj9CU157UDvqRSJcnnlP0z@obs.cn-east-2.myhuaweicloud.com:443/bigdata-dev-test/tangzhi/gio_data/member_count/checkpoint/6ff104386ddc7f435d14fb9206c40bb7/chk-1680131/_metadata'.
	at org.apache.flink.runtime.state.filesystem.AbstractFsCheckpointStorage.resolveCheckpointPointer(AbstractFsCheckpointStorage.java:231)
	at org.apache.flink.runtime.state.filesystem.AbstractFsCheckpointStorage.resolveCheckpoint(AbstractFsCheckpointStorage.java:109)
	at org.apache.flink.runtime.checkpoint.CheckpointCoordinator.restoreSavepoint(CheckpointCoordinator.java:1100)
	at org.apache.flink.runtime.jobmaster.JobMaster.tryRestoreExecutionGraphFromSavepoint(JobMaster.java:1257)
	at org.apache.flink.runtime.jobmaster.JobMaster.createAndRestoreExecutionGraph(JobMaster.java:1181)
	at org.apache.flink.runtime.jobmaster.JobMaster.<init>(JobMaster.java:285)
	at org.apache.flink.runtime.jobmaster.JobManagerRunner.<init>(JobManagerRunner.java:157)
	... 10 common frames omitted
Caused by: java.io.IOException: Cannot instantiate obs file system for URI: obs://Jk5qYOHpsbjQtmjFCEdS:X4n2PnxMhsqhWsS7osCj9CU157UDvqRSJcnnlP0z@obs.cn-east-2.myhuaweicloud.com:443/bigdata-dev-test/tangzhi/gio_data/member_count/checkpoint/6ff104386ddc7f435d14fb9206c40bb7/chk-1680131/_metadata
	at org.apache.flink.fs.obshadoop.ObsFileSystemFactory.create(ObsFileSystemFactory.java:98)
	at org.apache.flink.core.fs.FileSystem.getUnguardedFileSystem(FileSystem.java:399)
	at org.apache.flink.core.fs.FileSystem.get(FileSystem.java:322)
	at org.apache.flink.core.fs.Path.getFileSystem(Path.java:298)
	at org.apache.flink.runtime.state.filesystem.AbstractFsCheckpointStorage.resolveCheckpointPointer(AbstractFsCheckpointStorage.java:228)
	... 16 common frames omitted
Caused by: java.lang.IllegalArgumentException: the bucketName is illegal
	at com.obs.services.internal.utils.ServiceUtils.generateHostnameForBucket(ServiceUtils.java:546)
	at com.obs.services.internal.RestConnectionService.setupConnection(RestConnectionService.java:150)
	at com.obs.services.internal.RestStorageService.performRestForApiVersion(RestStorageService.java:1000)
	at com.obs.services.internal.service.AbstractRequestConvertor.getAuthTypeNegotiationResponseImpl(AbstractRequestConvertor.java:355)
	at com.obs.services.internal.service.AbstractRequestConvertor.parseAuthTypeInResponse(AbstractRequestConvertor.java:338)
	at com.obs.services.internal.service.AbstractRequestConvertor.getApiVersion(AbstractRequestConvertor.java:302)
	at com.obs.services.AbstractBucketClient.access$1400(AbstractBucketClient.java:54)
	at com.obs.services.AbstractBucketClient$3.authTypeNegotiate(AbstractBucketClient.java:206)
	at com.obs.services.AbstractClient.doActionWithResult(AbstractClient.java:387)
	at com.obs.services.AbstractBucketClient.headBucket(AbstractBucketClient.java:196)
	at com.obs.services.AbstractBucketClient.headBucket(AbstractBucketClient.java:184)
	at org.apache.hadoop.fs.obs.OBSCommonUtils.innerVerifyBucketExists(OBSCommonUtils.java:1558)
	at org.apache.hadoop.fs.obs.OBSCommonUtils.verifyBucketExists(OBSCommonUtils.java:1537)
	at org.apache.hadoop.fs.obs.OBSFileSystem.initialize(OBSFileSystem.java:385)
	at org.apache.flink.fs.obshadoop.ObsFileSystemFactory.create(ObsFileSystemFactory.java:89)
	... 20 common frames omitted
```
```text
2.为了先恢复部分指标的正常使用，先跳过了从checkpoint步骤。直接从最新点位开始消费计算
```
```text
3.开始逐步排查。由于报错在凌晨首先确定代码逻辑以及source端是没有问题的，初步判定在Transformation或者Sink端中出错。
    3.1 Sink端是Mysql，经问Mysql服务一直是稳定没有异常
    3.2 Transformation过程：FlatMap --> Filter --> WaterMark --> TumblingWindow，前三步一般不会抛出异常。
    3.3 所以先从滚动窗口逻辑开始排查，窗口主要逻辑：窗口内状态清理 --> 通过JDBC链接华为云DWS跑一段SQL计算T-2离线会员总数 --> 删除OBS --> 实时计算record --> 操作redis --> 会员总数，这中间可能在凌晨出错的是JDBC链接华为云DWS、删除OBS、操作redis
    3.4 经过查看日志删除OBS、操作redis都没有异常日志
    3.5 初步确定是在凌晨开窗通过JDBC链接华为云DWS计算T-2离线会员总数时报错，果然经问仓库在前一天晚上开始对那张表开始重建处理，导致Flink这边JDBC链接的用户没有查询权限报错。
    3.6 后续重新赋予查询权限问题解决
```
```text
4.其实经过第三步的排查当天问题基本解决，各实时指标已经正常输出
```
```text
5.但第一步中从checkpoint恢复失败这个问题是存在的，而且这个才是最影响后续任务指标正常输出的关键。
```

#### 后续问题解决
```text
1.通过第一步日志可以看出Flink在从指定checkpoint地址读取数据创建JM过程中报错，并且 Caused by 是 the bucketName is illegal。经过与华为云技术支持沟通此处的 bucketName 校验不合法并不是华为云obs上的桶名称而是Flink设置的checkpoint全路径
```
```text
2.后续阅读华为云sdk-java-obs源码发现读取Flink ccheckpoint数据前会对bucketName进行相关正则校验，代码如下
```
```java
public static boolean isBucketNameValidDNSName(String bucketName) {
            // 校验长度
        if (bucketName == null || bucketName.length() > 63 || bucketName.length() < 3) {
            return false;
        }
        // 校验路径名称
        if (!Pattern.matches("^[a-z0-9][a-z0-9.-]+$", bucketName)) {
            return false;
        }

        if (Pattern.matches("(\\d{1,3}\\.){3}\\d{1,3}", bucketName)) {
            return false;
        }

        String[] fragments = bucketName.split("\\.");
        for (String fragment : fragments) {
            if (Pattern.matches("^-.*", fragment) || Pattern.matches(".*-$", fragment)
                    || Pattern.matches("^$", fragment)) {
                return false;
            }
        }

        return true;
    }

    public static String generateHostnameForBucket(String bucketName, boolean pathStyle, String endpoint) {
        if (!isBucketNameValidDNSName(bucketName)) {
            throw new IllegalArgumentException("the bucketName is illegal");
        }

        if (!pathStyle) {
            return bucketName + "." + endpoint;
        } else {
            return endpoint;
        }
    }
```
```text
3.存在以下疑问：
    3.1 为什么校验checkpoint路径长度必须是3~63，况且Flink在进行checkpoint时数据会放在一个32位的随机文件下。apache 版本Flink并不存在此长度校验
    3.2 第二个正则对路径的字符串校验会直接报错，这样会抛出异常导致读取obs文件数据失败从而导致从checkpoint恢复失败
```