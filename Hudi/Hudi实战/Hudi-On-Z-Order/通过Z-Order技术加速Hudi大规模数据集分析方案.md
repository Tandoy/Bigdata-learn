## 通过Z-Order技术加速Hudi大规模数据集分析方案

### 1.背景
```text
多维分析是大数据分析的一个典型场景，这种分析一般带有过滤条件。对于此类查询，尤其是在高基字段的过滤查询，理论上只我们对原始数据做合理的布局，结合相关过滤条件，查询引擎可以过滤掉大量不相关数据，只需读取很少部分需要的数据。
例如我们在入库之前对相关字段做排序，这样生成的每个文件相关字段的min－max值是不存在交叉的，查询引擎下推过滤条件给数据源结合每个文件的min－max统计信息，即可过滤掉大量不相干数据。
上述技术即我们通常所说的data clustering 和 data skip。直接排序可以在单个字段上产生很好的效果，如果多字段直接排序那么效果会大大折扣的，Z-Order可以较好的解决多字段排序问题。
```

### 2.Z-Order介绍
```text
Z-Order是一种可以将多维数据压缩到一维的技术，在时空索引以及图像方面使用较广。
Z曲线可以以一条无限长的一维曲线填充任意维度的空间，对于数据库的一条数据来说，我们可以将其多个要排序的字段看作是数据的多个维度，z曲线可以通过一定的规则将多维数据映射到一维数据上，构建z-value 进而可以基于该一维数据进行排序。
z-value的映射规则保证了排序后那些在多维维度临近的数据在一维曲线上仍然可以彼此临近。
```

### 3.具体实现

#### 3.1 z-value的生成和排序
```text
Z-Order的关键在于z-value的映射规则。wiki上给出了基于位交叉的技术，每个维度值的比特位交叉出现在最终的z-value里。例如假设我们想计算二维坐标（x=97, y=214）的z-value，我们可以按如下步骤进行
第一步：将每一维数据用bits表示:
    x value：01100001
    y value：11010110
第二步：从y的最左侧bit开始，我们将x和y按位做交叉，即可得到z 值，如下所示:
    z-value：1011011000101001
对于多维数据，我们可以采用同样的方法对每个维度的bit位做按位交叉形成 z-value，一旦我们生成z-values 我们即可用该值做排序，基于z值的排序自然形成z阶曲线对多个参与生成z值的维度都有良好的聚合效果。
上述生成z-value的方法看起来非常好，但在实际生产环境上我们要使用位交叉技术产生z-value 还需解决如下问题：
    1.上述介绍是基于多个unsigned int类型的递增数据，通过位交叉生成z-value的。实际上的数据类型多种多样，如何处理其他类型数据
    2.不同类型的维度值转成bit位表示，长度不一致如何处理
    3.如何选择数据类型合理的保存z-value，以及相应的z值排序策略
针对上述问题，我们可采用两种策略生成z值。
```

##### 3.1.1 基于映射策略的z-value生成策略
第一个问题：对不同的数据类型采用不同的转换策略：
```text
·无符号类型整数： 直接转换成bits位表示;
·Int类型的数据：直接转成二进制表示会有问题，因为java里面负数的二进制表示最高位（符号位）为1，而正整数的二进制表示最高位为0（如下图所示）， 直接转换后会出现负数大于正数的现象：可以直接将二进制的最高位反转，就可以保证转换后的词典顺序和原值相同。
·Long类型的数据：转换方式和Int类型一样，转成二进制形式并将最高位反转
·Double、Float类型的数据： 转成Long类型，之后转成二进制形式并将最高位反转
·Decimal/Date/TimeStamp类型数据：转换成long类型，然后直接用二进制表示。
·UTF-8 String类型的数据：String类型的数据 直接用二进制表示即可保持原来的自然序， 但是字符串是不定长的无法直接用来做位交叉。我们采用如下策略处理string类型大于8bytes的字符串截断成8bytes， 不足8bytes的string 填充成8bytes。
·null值处理：
    ·数值类型的null直接变成该数值类型的最大值，之后按上述步骤转换；
    ·String类型null 直接变成空字符串之后再做转换；
```
第二个问题：生成的二进制值统一按64位对齐即可；

第三个问题：可以用Array[Byte]来保存z值（参考Amazon的DynamoDB 可以限制该数组的长度位1024）。对于 Array[Byte]类型的数据排序，hbase的rowkey 排序器可以直接拿来解决这个问题

基于映射策略的z值生成方法，方便快捷很容易理解，但是有一定缺陷：
```text
1.参与生成z-value的字段理论上需要是从0开始的正整数，这样才能生成很好的z曲线。真实的数据集中 是不可能有这么完美的情况出现的， zorder的效果将会打折扣。比如x 字段取值(0, 1, 2)， y字段取值(100, 200, 300)， 用x, y生成的z-value只是完整z曲线的一部分，对其做z值排序的效果和直接用x排序的效果是一样的；
再比如x的基数值远远低于y的基数值时采用上述策略排序效果基本和按y值排序是一样的，真实效果还不如先按x排序再按y排序。
2.String类型的处理， 上述策略对string类型是取前8个字节的参与z值计算, 这将导致精度丢失。当出现字符串都是相同字符串前缀的情况就无法处理了，比如"https://www.baidu.com[1]" , "https://www.google.com[2]" 这两个字符串前8个字节完全一样， 对这样的数据截取前8个字节参与z值计算没有任何意义。
```

##### 3.1.2 基于RangeBounds的z-value生成策略
```text
1.对每个参与Z-Order的字段筛选规定个数（类比分区数）的Range并对进行排序，并计算出每个字段的RangeBounds；
2.实际映射过程中每个字段映射为该数据所在rangeBounds的中的下标，然后参与z-value的计算。可以看出由于区间下标是从0开始递增的正整数，完全满足z值生成条件；并且String类型的字段映射问题也被一并解决了。基于RangeBounds的z值生成方法，很好的解决了第一种方法所面临的缺陷。由于多了一步采样生成RangeBounds的过程，其效率显然不如第一种方案。
```
#### 3.2 与Hudi结合
##### 3.2.1 表数据的Z排序重组
```text
借助Hudi内部的Clustering机制结合上述z值的生成排序策略可以直接完成Hudi表数据的数据重组
``` 

##### 3.2.3 收集保存统计信息
```scala
val sc = df.sparkSession.sparkContext
val serializableConfiguration = new SerializableConfiguration(conf)
val numParallelism = inputFiles.size/3
val previousJobDescription = sc.getLocalProperty(SparkContext.SPARK_JOB_DESCRIPTION)
try {
  val description = s"Listing parquet column statistics"
  sc.setJobDescription(description)
  sc.parallelize(inputFiles, numParallelism).mapPartitions { paths =>
    val hadoopConf = serializableConfiguration.value
    paths.map(new Path(_)).flatMap { filePath =>
      val blocks = ParquetFileReader.readFooter(hadoopConf, filePath).getBlocks().asScala
      blocks.flatMap(b => b.getColumns().asScala.
        map(col => (col.getPath().toDotString(),
          FileStats(col.getStatistics().minAsString(), col.getStatistics().maxAsString(), col.getStatistics.getNumNulls.toInt))))
        .groupBy(x => x._1).mapValues(v => v.map(vv => vv._2)).
        mapValues(value => FileStats(value.map(_.minVal).min, value.map(_.maxVal).max, value.map(_.num_nulls).max)).toSeq.
        map(x => ColumnFileStats(filePath.getName(), x._1, x._2.minVal, x._2.maxVal, x._2.num_nulls))
    }.filter(p => cols.contains(p.colName))
  }.collect()
} finally {
  sc.setJobDescription(previousJobDescription)
}
```
之后将这些信息保存在Hudi表里面的hoodie目录下的index目录下，然后供Spark查询使用。

##### 3.2.3 应用到Spark查询
```text
为将统计信息应用Spark查询，需修改HudiIndex的文件过滤逻辑，将DataFilter转成对Index表的过滤，选出候选要读取的文件，返回给查询引擎，具体步骤如下:
1.将索引表加载到 IndexDataFrame
2.使用原始查询过滤器为 IndexDataFrame 构建数据过滤器
3.查询 IndexDataFrame 选择候选文件
4.使用这些候选文件来重建 HudiMemoryIndex
```
```scala
def createZindexFilter(condition: Expression): Expression = {
  val minValue = (colName: Seq[String]) =>
    col(UnresolvedAttribute(colName) + "_minValue").expr
  val maxValue = (colName: Seq[String]) =>
    col(UnresolvedAttribute(colName) + "_maxValue").expr
  val num_nulls = (colName: Seq[String]) =>
    col(UnresolvedAttribute(colName) + "_num_nulls").expr
  condition match {
    case EqualTo(attribute: AttributeReference, value: Literal) =>
      val colName = HudiMergeIntoUtils.getTargetColNameParts(attribute)
      And(LessThanOrEqual(minValue(colName), value), GreaterThanOrEqual(maxValue(colName), value))
    case EqualTo(value: Literal, attribute: AttributeReference) =>
      val colName = HudiMergeIntoUtils.getTargetColNameParts(attribute)
      And(LessThanOrEqual(minValue(colName), value), GreaterThanOrEqual(maxValue(colName), value))
    case equalNullSafe @ EqualNullSafe(_: AttributeReference, _ @ Literal(null, _)) =>
      val colName = HudiMergeIntoUtils.getTargetColNameParts(equalNullSafe.left)
      EqualTo(num_nulls(colName), equalNullSafe.right)
.......
```

[通过Z-Order技术加速Hudi大规模数据集分析方案](https://mp.weixin.qq.com/s/qos-QGfJbP36qwq9h1lUkQ)