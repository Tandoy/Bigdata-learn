## Hudi集成Flink踩坑记录

    1.使用启动命令启动flink任务时报错：
        
        java.lang.NoSuchMethodError: org.apache.flink.streaming.api.environment.StreamExecutionEnvironment.setStateBackend(Lorg/apache/flink/runtime/state/AbstractStateBackend;)Lorg/apache/flink/streaming/api/environment/StreamExecutionEnvironment;
        	at org.apache.hudi.HoodieFlinkStreamer.main(HoodieFlinkStreamer.java:75)
        	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        	at java.lang.reflect.Method.invoke(Method.java:498)
        	at org.apache.flink.client.program.PackagedProgram.callMainMethod(PackagedProgram.java:349)
        	at org.apache.flink.client.program.PackagedProgram.invokeInteractiveModeForExecution(PackagedProgram.java:219)
        	at org.apache.flink.client.ClientUtils.executeProgram(ClientUtils.java:114)
        	at org.apache.flink.client.cli.CliFrontend.executeProgram(CliFrontend.java:812)
        	at org.apache.flink.client.cli.CliFrontend.run(CliFrontend.java:246)
        	at org.apache.flink.client.cli.CliFrontend.parseAndRun(CliFrontend.java:1054)
        	at org.apache.flink.client.cli.CliFrontend.lambda$main$10(CliFrontend.java:1132)
        	at java.security.AccessController.doPrivileged(Native Method)
        	at javax.security.auth.Subject.doAs(Subject.java:422)
        	at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1692)
        	at org.apache.flink.runtime.security.contexts.HadoopSecurityContext.runSecured(HadoopSecurityContext.java:41)
        	at org.apache.flink.client.cli.CliFrontend.main(CliFrontend.java:1132)
        	
    当前集群Flink版本为1.12.2,而Hudi-0.7.0是基于Flink-1.11版本进行编译，待Hudi-0.8.0发布后会升级到Flink 1.12.2可解决
        
    2.java.lang.NoClassDefFoundError: org/apache/flink/streaming/connectors/kafka/FlinkKafkaConsumer
          	at org.apache.hudi.streamer.HoodieFlinkStreamer.main(HoodieFlinkStreamer.java:84)
          	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
          	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
          	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
          	at java.lang.reflect.Method.invoke(Method.java:498)
          	at org.apache.flink.client.program.PackagedProgram.callMainMethod(PackagedProgram.java:349)
          	at org.apache.flink.client.program.PackagedProgram.invokeInteractiveModeForExecution(PackagedProgram.java:219)
          	at org.apache.flink.client.ClientUtils.executeProgram(ClientUtils.java:114)
          	at org.apache.flink.client.cli.CliFrontend.executeProgram(CliFrontend.java:812)
          	at org.apache.flink.client.cli.CliFrontend.run(CliFrontend.java:246)
          	at org.apache.flink.client.cli.CliFrontend.parseAndRun(CliFrontend.java:1054)
          	at org.apache.flink.client.cli.CliFrontend.lambda$main$10(CliFrontend.java:1132)
          	at java.security.AccessController.doPrivileged(Native Method)
          	at javax.security.auth.Subject.doAs(Subject.java:422)
          	at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1692)
          	at org.apache.flink.runtime.security.contexts.HadoopSecurityContext.runSecured(HadoopSecurityContext.java:41)
          	at org.apache.flink.client.cli.CliFrontend.main(CliFrontend.java:1132)
          Caused by: java.lang.ClassNotFoundException: org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
          	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
          	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
          	at org.apache.flink.util.FlinkUserCodeClassLoader.loadClassWithoutExceptionHandling(FlinkUserCodeClassLoader.java:64)
          	at org.apache.flink.util.ChildFirstClassLoader.loadClassWithoutExceptionHandling(ChildFirstClassLoader.java:65)
          	at org.apache.flink.util.FlinkUserCodeClassLoader.loadClass(FlinkUserCodeClassLoader.java:48)
          	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
          	... 17 more

    由于集群Flink/lib/没有flink-connector-kafka_2.11-1.12.2.jar，下载对应jar上传至Flink/lib/
        
          

        