## Calcite解析SQL步骤
![image](https://github.com/Tandoy/Bigdata-learn/blob/master/Calcite/images/Calcite%E8%A7%A3%E6%9E%90%E6%AD%A5%E9%AA%A4.png)
```text
1.Parser：此步中Calcite通过Java CC将SQL解析成未经校验的AST
2.Validate：该步骤主要作用是校证Parser步骤中的AST是否合法,如验证SQL scheme、字段、函数等是否存在; SQL语句是否合法等. 此步完成之后就生成了RelNode树（关于RelNode树, 请参考下文）
3.Optimize：该步骤主要的作用优化RelNode树, 并将其转化成物理执行计划。主要涉及SQL规则优化如:基于规则优化(RBO)及基于代价(CBO)优化; Optimze 这一步原则上来说是可选的, 通过Validate后的RelNode树已经可以直接转化物理执行计划，但现代的SQL解析器基本上都包括有这一步，目的是优化SQL执行计划。此步得到的结果为物理执行计划。
4.Execute：即执行阶段。此阶段主要做的是:将物理执行计划转化成可在特定的平台执行的程序。如Hive与Flink都在在此阶段将物理执行计划CodeGen生成相应的可执行代码。
```
```text
INSERT INTO tmp_node
SELECT s1.id1, s1.id2, s2.val1
FROM source1 as s1 INNER JOIN source2 AS s2
ON s1.id1 = s2.id1 and s1.id2 = s2.id2 where s1.val1 > 5 and s2.val2 = 3;
```
针对上述SQL解析过程如下：
```shell script
1.Parser解析&Validate校验
LogicalTableModify(table=[[TMP_NODE]], operation=[INSERT], flattened=[false])
  LogicalProject(ID1=[$0], ID2=[$1], VAL1=[$7])
    LogicalFilter(condition=[AND(>($2, 5), =($8, 3))])
      LogicalJoin(condition=[AND(=($0, $5), =($1, $6))], joinType=[INNER])
        LogicalTableScan(table=[[SOURCE1]])
        LogicalTableScan(table=[[SOURCE2]])
```
```shell script
2.Optimize优化(谓词下推、投影下推、关系代数定律优化)
LogicalTableModify(table=[[TMP_NODE]], operation=[INSERT], flattened=[false])
  LogicalProject(ID1=[$0], ID2=[$1], VAL1=[$7])
      LogicalJoin(condition=[AND(=($0, $5), =($1, $6))], joinType=[inner])
        LogicalFilter(condition=[=($4, 3)])  
          LogicalProject(ID1=[$0], ID2=[$1],      ID3=[$2], VAL1=[$3], VAL2=[$4],VAL3=[$5])
            LogicalTableScan(table=[[SOURCE1]])
        LogicalFilter(condition=[>($3,5)])    
          LogicalProject(ID1=[$0], ID2=[$1], ID3=[$2], VAL1=[$3], VAL2=[$4],VAL3=[$5])
            LogicalTableScan(table=[[SOURCE2]])
```
```shell script
3.Execute
Hive与Flink都在在此阶段将物理执行计划CodeGen生成相应的可执行代码
```