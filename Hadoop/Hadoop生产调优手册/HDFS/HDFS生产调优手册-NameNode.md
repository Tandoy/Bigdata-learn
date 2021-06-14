## HDFS—核心参数

### 1.NameNode内存生产配置

1）NameNode 内存计算
```text
每个文件块大概占用 150byte，一台服务器 128G 内存为例，能存储多少文件块呢？
128 * 1024 * 1024 * 1024 / 150Byte ≈ 9.1 亿
G MB KB Byte
```
2）Hadoop2.x 系列，配置 NameNode 内存
```text
NameNode 内存默认 2000m，如果服务器内存 4G，NameNode 内存可以配置 3g。在hadoop-env.sh 文件中配置如下。
HADOOP_NAMENODE_OPTS=-Xmx3072m
```
3）Hadoop3.x 系列，配置 NameNode 内存
```text
（1）hadoop-env.sh 中描述 Hadoop 的内存是动态分配的
# The maximum amount of heap to use (Java -Xmx). If no unit
# is provided, it will be converted to MB. Daemons will
# prefer any Xmx setting in their respective _OPT variable.
# There is no default; the JVM will autoscale based upon machine
# memory size.
# export HADOOP_HEAPSIZE_MAX=
# The minimum amount of heap to use (Java -Xms). If no unit
# is provided, it will be converted to MB. Daemons will
# prefer any Xms setting in their respective _OPT variable.
# There is no default; the JVM will autoscale based upon machine
# memory size.
# export HADOOP_HEAPSIZE_MIN=
HADOOP_NAMENODE_OPTS=-Xmx102400m

具体修改：hadoop-env.sh
export HDFS_NAMENODE_OPTS="-Dhadoop.security.logger=INFO,RFAS -Xmx1024m"
export HDFS_DATANODE_OPTS="-Dhadoop.security.logger=ERROR,RFAS-Xmx1024m"
```
NN、DN内存配置经验参考：
```html
https://docs.cloudera.com/documentation/enterprise/6/release-notes/topics/rg_hardware_requirements.html#concept_fzz_dq4_gbb
```

### 2.NameNode心跳并发配置

1）hdfs-site.xml
```text
The number of Namenode RPC server threads that listen to requests
from clients. If dfs.namenode.servicerpc-address is not
configured then Namenode RPC server threads listen to requests
from all nodes.
NameNode 有一个工作线程池，用来处理不同 DataNode 的并发心跳以及客户端并发
的元数据操作。
对于大集群或者有大量客户端的集群来说，通常需要增大该参数。默认值是 10。
<property>
<name>dfs.namenode.handler.count</name>
<value>21</value>
</property>

企业经验：dfs.namenode.handler.count=20 × 𝑙𝑜𝑔 𝑒 𝐶𝑙𝑣𝑡𝑢𝑒𝑠 𝑆𝑖𝑧𝑒；CDH版本的Hadoop默认30
```