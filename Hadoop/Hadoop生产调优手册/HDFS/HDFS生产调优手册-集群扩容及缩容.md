## HDFS—集群扩容及缩容

### 1.磁盘间数据均衡
```text
生产环境，由于硬盘空间不足，往往需要增加一块硬盘。刚加载的硬盘没有数据时，可以执行磁盘数据均衡命令。（Hadoop3.x 新特性）

（1）生成均衡计划（只有一块磁盘，不会生成计划）
hdfs diskbalancer -plan hadoop103
（2）执行均衡计划
hdfs diskbalancer -execute hadoop103.plan.json
（3）查看当前均衡任务的执行情况
hdfs diskbalancer -query hadoop103
（4）取消均衡任务
hdfs diskbalancer -cancel hadoop103.plan.json
```

### 2.服役新服务器

```text
1）环境准备
（1）在 hadoop100 主机上再克隆一台 hadoop105 主机

（2）修改 IP 地址和主机名称
        vim /etc/sysconfig/network-scripts/ifcfg-ens33
        vim /etc/hostname

（3）拷贝 hadoop102 的/opt/module 目录和/etc/profile.d/my_env.sh 到 hadoop105
        scp -r module/* atguigu@hadoop105:/opt/module/
        sudo scp /etc/profile.d/my_env.sh root@hadoop105:/etc/profile.d/my_env.sh
        source /etc/profile

（4）删除 hadoop105 上 Hadoop 的历史数据，data 和 log 数据
        rm -rf data/ logs/

（5）配置 hadoop102 和 hadoop103 到 hadoop105 的 ssh 无密登录
        ssh-copy-id hadoop105

2）服役新节点具体步骤
        dfs --daemon start datanode
        yarn --daemon start nodemanager
```

### 3.服务器间数据均衡

```text
命令：
sbin/start-balancer.sh -threshold 10

注意：
由于 HDFS 需要启动单独的 Rebalance Server 来执行 Rebalance 操作，所以尽量不要在 NameNode 上执行 start-balancer.sh，而是找一台比较空闲的机器。
```

### 4.黑名单退役服务器

```text
黑名单：表示在黑名单的主机 IP 地址不可以，用来存储数据。
企业中：配置黑名单，用来退役服务器。
```
1 ）编辑/opt/module/hadoop-3.1.3/etc/hadoop 目录 下的 的 blacklist 文件
```shell script
vim blacklist
```
```xml
<!-- 黑名单 -->
<property>
<name>dfs.hosts.exclude</name>
<value>/opt/module/hadoop-3.1.3/etc/hadoop/blacklist</value>
</property>
```
2 ）分发配置 文件 blacklist，hdfs-site.xml

3 ）第一次添加黑名单必须重启集群，不是第一次，只需要刷新NameNode节点即可

4 ）等待 退役节点 状态为 为 decommissioned （ 所有块已经复制完成 ），停止该节点及节点资源管理器。 注意 ：如果副本数是3，服役的节点小于等于3，是不能退役成功的，需要修改副本数后才能退役

5 ）如果数据不均衡，可以用命令实现集群的再平衡：sbin/start-balancer.sh -threshold 10