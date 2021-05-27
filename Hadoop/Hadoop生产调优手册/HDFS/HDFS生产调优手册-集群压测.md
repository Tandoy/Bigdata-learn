##HDFS—集群压测

###1.测试HDFS写性能

0）写测试底层原理
```text
记录每个Map的写入时间以及平均速度，汇总每个MapTask向HDFS的写入时间和平均速度
测试文件个数=集群CORE总核数 - 1
```

1）测试内容：向 HDFS 集群写 10 个 128M 的文件
```text
命令：
hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-3.1.3-tests.jar TestDFSIO -write -nrFiles 10 -fileSize 128MB

测试结果：
INFO fs.TestDFSIO: ----- TestDFSIO ----- : write
INFO fs.TestDFSIO: Number of files: 10
INFO fs.TestDFSIO: Total MBytes processed: 1280
INFO fs.TestDFSIO: Throughput mb/sec: 1.61
INFO fs.TestDFSIO: Average IO rate mb/sec: 1.9
INFO fs.TestDFSIO: IO rate std deviation: 0.76
INFO fs.TestDFSIO: Test exec time sec: 133.05

测试结果分析：
Number of files：生成 mapTask 数量，一般是集群中（CPU 核数-1），我们测试虚拟机就按照实际的物理内存-1 分配即可
Total MBytes processed：单个 map 处理的文件大小
Throughput mb/sec:单个 mapTak 的吞吐量    计算方式：处理的总文件大小/每一个 mapTask 写数据的时间累加;集群整体吞吐量：生成 mapTask 数量*单个 mapTak 的吞吐量
Average IO rate mb/sec::平均 mapTak 的吞吐量  计算方式：每个 mapTask 处理文件大小/每一个 mapTask 写数据的时间全部相加除以 task 数量
IO rate std deviation:方差、反映各个 mapTask 处理的差值，越小越均衡
```

2）注意：如果测试过程中，出现异常
```text
（1）可以在 yarn-site.xml 中设置虚拟内存检测为 false
        <!--是否启动一个线程检查每个任务正使用的虚拟内存量，如果任务超出分配值，则直接将其杀掉，默认是 true -->
        <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
        </property>
（2）分发配置并重启 Yarn 集群
```

###2.测试HDFS读性能

1）测试内容：读取 HDFS 集群 10 个 128M 的文件
```text
命令：
hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-3.1.3-tests.jar TestDFSIO -read -nrFiles 10 -fileSize 128MB

测试结果：
INFO fs.TestDFSIO: ----- TestDFSIO ----- : read
INFO fs.TestDFSIO: Number of files: 10
INFO fs.TestDFSIO: Total MBytes processed: 1280
INFO fs.TestDFSIO: Throughput mb/sec: 200.28
INFO fs.TestDFSIO: Average IO rate mb/sec: 266.74
INFO fs.TestDFSIO: IO rate std deviation: 143.12
INFO fs.TestDFSIO: Test exec time sec: 20.83

测试结果分析:
为什么读取文件速度大于网络带宽？由于目前只有三台服务器，且有三个副本，数据读取就近原则，相当于都是读取的本地磁盘数据，没有走网络。
```

