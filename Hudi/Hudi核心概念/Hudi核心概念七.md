## Hudi核心概念七

### 一、前提
```text
Hudi可帮助构建和管理具有不同表类型的数据湖，以满足每个人的需求。Hudi为每条记录添加了元数据字段，例如 _hoodie_record_key、_hoodie_partition_path、_hoodie_commit_time，这些字段有多种用途。
·它们有助于避免在合并、压缩和其他表操作期间重新计算记录键、分区路径
·还有助于支持记录级增量查询（与仅跟踪文件的其他表格式相比）
·另外即使给定表的键字段在其生命周期内发生更改，它也可以通过确保唯一键约束被强制执行来确保数据质量
但有用户提出是否可以直接利用现有字段而不是添加额外的元数据字段以满足一些简单用例场景。
```
### 二、Hudi支持虚拟键
```text
Hudi现在支持虚拟键，可以从数据字段中按需计算Hudi元数据字段，而不需要显示的存储元数据字段。目前元数据字段计算一次并根据记录元数据存储并在各种操作中重复使用。如果不需要增量查询支持，用户可以使用Hudi的虚拟键支持，并且仍然继续使用Hudi来构建和管理他们的数据湖，以减少由于每条记录元数据造成的存储开销。
```
#### 2.1 配置
```text
hoodie.populate.meta.fields=false
此配置的默认值为true，意味着默认情况下将添加所有元数据字段。
虚拟键有如下限制：
·一旦启用虚拟键，就不能禁用它，因为已经存储的记录可能没有填充元字段。但是如果旧版 Hudi 中的表（包含元数据字段），则可以启用虚拟键。
·表的KeyGenerator属性在Hudi表的生命周期过程中不能更改。在此模型中，用户需要确保表中键的唯一性。例如，如果将记录键配置为指向field_5进行几批写入，然后切换到field_10，则Hudi无法保证键的唯一性，因为较早的写入可能有field_10的重复项。
使用虚拟键，每次需要时都必须重新计算键（合并、压缩、MOR 快照读取），有一定的代价。因此我们支持 Copy-On-Write 表上所有内置KeyGenerator的虚拟键，支持 Merge-On-Read 表上的所有KeyGenerator将需要从基本和增量日志中读取所有字段，这会牺牲列式存储的查询性能，这对用户来说成本过高。因此我们现在只支持简单的SimpleKeyGenerator（默认的KeyGenerator，其中记录键和分区路径都使用已有字段）。
```
#### 2.2 KeyGenerator以及索引支持
```text
COW支持的KeyGenerator如下

  ·SimpleKeyGenerator
  ·ComplexKeyGenerator
  ·CustomKeyGenerator
  ·TimestampBasedKeyGenerator
  ·NonPartitionedKeyGenerator

MOR支持的KeyGenerator如下

  ·SimpleKeyGenerator

支持索引类型如下

  ·SIMPLE
  ·GLOBAL_SIMPLE
```
### 三、样例输出
以下是常规hudi表的一些示例记录（禁用虚拟键）
```json
+--------------------+--------------------------------------+--------------------------------------+---------+---------+-------------------+
|_hoodie_commit_time |           _hoodie_record_key         |        _hoodie_partition_path        |  rider  | driver  |        fare       |
+--------------------+--------------------------------------+--------------------------------------+---------+---------+-------------------+
|   20210825154123   | eb7819f1-6f04-429d-8371-df77620b9527 | americas/united_states/san_francisco |rider-284|driver-284|98.3428192817987  |
|   20210825154123   | 37ea44f1-fda7-4ec4-84de-f43f5b5a4d84 | americas/united_states/san_francisco |rider-213|driver-213|19.179139106643607|
|   20210825154123   | aa601d6b-7cc5-4b82-9687-675d0081616e | americas/united_states/san_francisco |rider-213|driver-213|93.56018115236618 |
|   20210825154123   | 494bc080-881c-48be-8f8a-8f1739781816 | americas/united_states/san_francisco |rider-284|driver-284|90.9053809533154  |
|   20210825154123   | 09573277-e1c1-4cdd-9b45-57176f184d4d | americas/united_states/san_francisco |rider-284|driver-284|49.527694252432056|
|   20210825154123   | c9b055ed-cd28-4397-9704-93da8b2e601f | americas/brazil/sao_paulo            |rider-213|driver-213|43.4923811219014  |
|   20210825154123   | e707355a-b8c0-432d-a80f-723b93dc13a8 | americas/brazil/sao_paulo            |rider-284|driver-284|63.72504913279929 |
|   20210825154123   | d3c39c9e-d128-497a-bf3e-368882f45c28 | americas/brazil/sao_paulo            |rider-284|driver-284|91.99515909032544 |
|   20210825154123   | 159441b0-545b-460a-b671-7cc2d509f47b | asia/india/chennai                   |rider-284|driver-284|9.384124531808036 |
|   20210825154123   | 16031faf-ad8d-4968-90ff-16cead211d3c | asia/india/chennai                   |rider-284|driver-284|90.25710109008239 |
+--------------------+--------------------------------------+--------------------------------------+---------+----------+------------------+
```
以下是启用了虚拟键的Hudi表的一些示例记录
```json
+--------------------+------------------------+-------------------------+---------+---------+-------------------+
|_hoodie_commit_time |    _hoodie_record_key  |  _hoodie_partition_path |  rider  | driver  |        fare       |
+--------------------+------------------------+-------------------------+---------+---------+-------------------+
|        null        |            null        |          null           |rider-284|driver-284|98.3428192817987  |
|        null        |            null        |          null           |rider-213|driver-213|19.179139106643607|
|        null        |            null        |          null           |rider-213|driver-213|93.56018115236618 |
|        null        |            null        |          null           |rider-284|driver-284|90.9053809533154  |
|        null        |            null        |          null           |rider-284|driver-284|49.527694252432056|
|        null        |            null        |          null           |rider-213|driver-213|43.4923811219014  |
|        null        |            null        |          null           |rider-284|driver-284|63.72504913279929 |
|        null        |            null        |          null           |rider-284|driver-284|91.99515909032544 |
|        null        |            null        |          null           |rider-284|driver-284|9.384124531808036 |
|        null        |            null        |          null           |rider-284|driver-284|90.25710109008239 |
+--------------------+------------------------+-------------------------+---------+----------+------------------+
```
### 四、增量查询
由于Hudi不会为启用了虚拟键的表维护任何元数据（例如记录级别的提交时间），因此不支持增量查询。当对此类表触发增量查询时，将引发如下异常：
```shell script
org.apache.hudi.exception.HoodieException: Incremental queries are not supported when meta fields are disabled
  at org.apache.hudi.IncrementalRelation.<init>(IncrementalRelation.scala:69)
  at org.apache.hudi.DefaultSource.createRelation(DefaultSource.scala:120)
  at org.apache.hudi.DefaultSource.createRelation(DefaultSource.scala:67)
  at org.apache.spark.sql.execution.datasources.DataSource.resolveRelation(DataSource.scala:344)
  at org.apache.spark.sql.DataFrameReader.loadV1Source(DataFrameReader.scala:297)
  at org.apache.spark.sql.DataFrameReader.$anonfun$load$2(DataFrameReader.scala:286)
  at scala.Option.getOrElse(Option.scala:189)
  at org.apache.spark.sql.DataFrameReader.load(DataFrameReader.scala:286)
  at org.apache.spark.sql.DataFrameReader.load(DataFrameReader.scala:232)
  ... 61 elided
```