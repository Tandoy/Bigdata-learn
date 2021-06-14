## Hudi核心概念六

### 1.Hudi的清理服务

1.回收空间以控制存储成本

```text
Hudi 提供不同的表管理服务来管理数据湖上表的数据，其中一项服务称为Cleaner（清理服务）。
随着用户向表中写入更多数据，对于每次更新，Hudi会生成一个新版本的数据文件用于保存更新后的记录(COPY_ON_WRITE) 或将这些增量更新写入日志文件以避免重写更新版本的数据文件 (MERGE_ON_READ)。
在这种情况下，根据更新频率，文件版本数可能会无限增长，但如果不需要保留无限的历史记录，则必须有一个流程（服务）来回收旧版本的数据，这就是 Hudi 的清理服务。
```

2.问题描述
```text
在数据湖架构中，读取端和写入端同时访问同一张表是非常常见的场景。
由于 Hudi 清理服务会定期回收较旧的文件版本，因此可能会出现长时间运行的查询访问到被清理服务回收的文件版本的情况，因此需要使用正确的配置来确保查询不会失败。
```

3.深入了解 Hudi清理服务
```text
先了解一下 Hudi 提供的不同清理策略以及需要配置的相应属性，Hudi提供了异步或同步清理两种方式：
·Hudi 基础文件（HoodieBaseFile）：由压缩后的最终数据组成的列式文件，基本文件的名称遵循以下命名约定：<fileId>_<writeToken>_<instantTime>.parquet。在此文件的后续写入中文件 ID 保持不变，并且提交时间会更新以显示最新版本。这也意味着记录的任何特定版本，给定其分区路径，都可以使用文件 ID 和 instantTime进行唯一定位。
·**文件切片(FileSlice)**：在 MERGE_ON_READ 表类型的情况下，文件切片由基本文件和由多个增量日志文件组成。
·**Hudi 文件组(FileGroup)**：Hudi 中的任何文件组都由分区路径和文件ID 唯一标识，该组中的文件作为其名称的一部分。文件组由特定分区路径中的所有文件片组成。此外任何分区路径都可以有多个文件组。
```

4.清理服务
```text
·KEEP_LATEST_COMMITS：这是默认策略。该清理策略可确保回溯前X次提交中发生的所有更改。假设每 30 分钟将数据摄取到 Hudi 数据集，并且最长的运行查询可能需要 5 小时才能完成，那么用户应该至少保留最后 10 次提交。通过这样的配置，我们确保文件的最旧版本在磁盘上保留至少 5 小时，从而防止运行时间最长的查询在任何时间点失败，使用此策略也可以进行增量清理。
·KEEP_LATEST_FILE_VERSIONS：此策略具有保持 N 个文件版本而不受时间限制的效果。当知道在任何给定时间想要保留多少个 MAX 版本的文件时，此策略很有用，为了实现与以前相同的防止长时间运行的查询失败的行为，应该根据数据模式进行计算，或者如果用户只想维护文件的 1 个最新版本，此策略也很有用。
```

5.示例
假设用户每 30 分钟将数据摄取到 COPY_ON_WRITE 类型的 Hudi 数据集，如下所示：

![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/Hudi-Cleaner/1.PNG)

该图显示了 DFS 上的一个特定分区，其中提交和相应的文件版本是彩色编码的。在该分区中创建了 4 个不同的文件组，如 fileId1、fileId2、fileId3 和 fileId4 所示。fileId2 对应的文件组包含所有 5 次提交的记录，而 fileId4 对应的组仅包含最近 2 次提交的记录。
假设使用以下配置进行清理：
```properties
hoodie.cleaner.policy=KEEP_LATEST_COMMITS #clean清理策略：保留最新的提交（默认）
hoodie.cleaner.commits.retained=2 #保留多少commit数目，默认是10，当clean清理策略是KEEP_LATEST_COMMITS时生效。实际上2+1次commit提交次数
```
```text
1.不应清理文件的最新版本。
2.确定最后 2 次（已配置）+ 1 次提交的提交时间。在图 1 中，commit 10:30 和 commit 10:00 对应于时间线中最新的 2 个提交。包含一个额外的提交，因为保留提交的时间窗口本质上等于最长的查询运行时间。因此如果最长的查询需要 1 小时才能完成，并且每 30 分钟发生一次摄取，则您需要保留自 2*30 = 60（1 小时）以来的最后 2 次提交。此时最长的查询仍然可以使用以相反顺序在第 3 次提交中写入的文件。这意味着如果一个查询在 commit 9:30 之后开始执行，当在 commit 10:30 之后触发清理操作时，它仍然会运行，如下图所示。
3.现在对于任何文件组，只有那些没有保存点（另一个 Hudi 表服务）且提交时间小于第 3 次提交（下图中的“提交 9:30”）的文件切片被清理。
```
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/Hudi-Cleaner/2.PNG)

假设使用以下配置进行清理：
```properties
hoodie.cleaner.policy=KEEP_LATEST_FILE_VERSIONS #clean清理策略：保留多少文件版本，默认3，这里控制的是数据文件版本而不是.hoodie数据文件
hoodie.cleaner.fileversions.retained=1
```

```text
对于任何文件组，文件切片的最新版本（包括任何待压缩的）被保留，其余的清理掉。如下图所示，如果在 commit 10:30 之后立即触发清理操作，清理服务将简单地保留每个文件组中的最新版本并删除其余的。
```

![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Hudi/images/Hudi-Cleaner/3.PNG)

6.运行命令

Hudi 的清理表服务可以作为单独的进程运行，可以与数据摄取一起运行。正如前面提到的，它会清除了任何陈旧文件。如果您想将它与摄取数据一起运行，可以使用配置同步或异步运行。或者可以使用以下命令独立运行清理服务：
```shell script
spark-submit --class org.apache.hudi.utilities.HoodieCleaner \
--props s3:///temp/hudi-ingestion-config/config.properties \
--target-base-path s3:///temp/hudi \
--spark-master yarn-cluster
```
```shell script
hoodie.clean.automatic=true #是否自动clean
hoodie.clean.async=true #是否异步clean
```
