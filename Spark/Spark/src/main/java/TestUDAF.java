import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tzhi on 2020/08/21.
 * 自定义聚合函数 计算平均数
 */
public class TestUDAF extends UserDefinedAggregateFunction {
    @Override
    public StructType inputSchema() {
        //输入数据类型
        ArrayList<StructField> list = new ArrayList<>();
        list.add(DataTypes.createStructField( "field1", DataTypes.StringType, true ));
        return DataTypes.createStructType(list);
    }

    @Override
    public StructType bufferSchema() {
        //缓存数据类型 即在聚合计算过程当中的中间结果数据类型
        List<StructField> structFields = new ArrayList<>();
        structFields.add(DataTypes.createStructField( "field1", DataTypes.IntegerType, true ));
        structFields.add(DataTypes.createStructField( "field2", DataTypes.IntegerType, true ));
        return DataTypes.createStructType( structFields );
    }

    @Override
    public DataType dataType() {
        // 输出结果数据类型
        return DataTypes.IntegerType;
    }

    @Override
    public boolean deterministic() {
        // 函数是否是确定性的(幂等性)，即给定相同的输入是否具有相同的输出
        return true;
    }

    @Override
    public void initialize(MutableAggregationBuffer mutableAggregationBuffer) {
        // 初始化数据缓存
        mutableAggregationBuffer.update(0,0);// 数据个数，初始为0
        mutableAggregationBuffer.update(1,0);// 待计算的数据存入一个序列中，以备后用，初始化为一个空序列
    }

    @Override
    public void update(MutableAggregationBuffer mutableAggregationBuffer, Row input) {
        // 更新缓存的数据，使用输入的数据更新到缓冲区 同时相当于分组局部聚合
        mutableAggregationBuffer.update(0,mutableAggregationBuffer.getInt(0) + 1); //自增出现次数
        mutableAggregationBuffer.update(1,mutableAggregationBuffer.getInt(1)+Integer.valueOf(input.getString(0))); //根据key数值相加
    }

    @Override
    public void merge(MutableAggregationBuffer buffer1, Row buffer2) {
        // 合并两个聚合缓冲区并将更新后的缓冲区值存储回“buffer1” 相当于先局部聚合再全局聚合
        buffer1.update(0,buffer1.getInt(0)+buffer2.getInt(0));
        buffer1.update(1,buffer1.getInt(1)+buffer2.getInt(1));
    }

    @Override
    public Object evaluate(Row buffer) {
        //计算的逻辑与最终结果 根据Sql中分组字段一个key计算一次
//        System.out.println(buffer.getInt(0)); //每个字符出现次数
//        System.out.println(buffer.getInt(1)); //每个字符对应数值相加之和
        return buffer.getInt(1)/buffer.getInt(0);
    }


    //测试
    public static void main(String[] args) throws AnalysisException {
        //创建spark环境
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[2]");
        sparkConf.setAppName("test");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        SparkSession spark = new SparkSession.Builder().config(sparkConf).getOrCreate();
        //注册UDAF函数
        spark.udf().register("my_avg",new TestUDAF());
        //读取文件数据
        JavaRDD<String> lines = sc.textFile( "D:\\java\\spark\\src\\main\\resources\\udftest.txt" );
        JavaRDD<Row> rows = lines.map(line-> RowFactory.create(line.split("\\^")));
        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField( "a", DataTypes.StringType, true ));
        structFields.add(DataTypes.createStructField( "b", DataTypes.StringType, true ));
        StructType structType = DataTypes.createStructType(structFields);
        //创建视图
        Dataset<Row> dataFrame = spark.createDataFrame(rows, structType);
        dataFrame.createTempView("test");
        spark.sql("SELECT a,my_avg(b) FROM test GROUP BY a").show();
        sc.stop();
    }
}