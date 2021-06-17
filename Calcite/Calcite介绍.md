## Calcite介绍
```text
Apache Calcite是一款开源的动态数据管理框架，它提供了标准的 SQL 语言、多种查询优化和连接各种数据源的能力，但不包括数据存储、处理数据的算法和存储元数据的存储库。
目前, 使用Calcite作为SQL解析与处理引擎有Hive、Drill、Flink、Phoenix和Storm等
```
### Calcite主要功能
```text
·SQL解析
·SQL校验
·查询优化
·SQL生成器
·数据连接
```
### Calcite相关组件
```text
·Catelog: 主要定义SQL语义相关的元数据与命名空间。
·SQL parser: 主要是把SQL转化成AST.
·SQL validator: 通过Catalog来校证AST.
·Query optimizer: 将AST转化成物理执行计划、优化物理执行计划.
·SQL generator: 反向将物理执行计划转化成SQL语句.
```