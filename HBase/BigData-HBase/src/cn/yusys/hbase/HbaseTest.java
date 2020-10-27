package cn.yusys.hbase;

import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HbaseTest {
	/**
	 * 配置ss
	 */
	static Configuration config = null;
	private Connection connection = null;
	private Table table = null;

	@Before
	public void init() throws Exception {
		config = HBaseConfiguration.create();// 配置
		config.set("hbase.rootdir", "hdfs://bigData:9000/hbase"); 
		config.set("hbase.zookeeper.quorum", "bigData:2181");//:2181
		config.set("hbase.master", "bigData:60000");//hbase的0系列默认端口是60000;1系列默认端口16010
		connection = ConnectionFactory.createConnection(config);
		table = connection.getTable(TableName.valueOf("test-hive")); //配置文件中创建起始表
	}

	/**
	 * 创建一个表
	 * 
	 * @throws Exception
	 */
	@Test
	public void createTable() throws Exception {
		// 创建表管理类
		@SuppressWarnings({ "deprecation", "resource" }) 
		HBaseAdmin admin = new HBaseAdmin(config); // hbase表管理
		// 创建表描述类
		TableName tableName = TableName.valueOf("test1"); // 表名称
		HTableDescriptor desc = new HTableDescriptor(tableName);
		// 创建列族的描述类
		HColumnDescriptor family = new HColumnDescriptor("info"); // 列族
		// 将列族添加到表中
		desc.addFamily(family);
		HColumnDescriptor family2 = new HColumnDescriptor("info2"); // 列族
		// 将列族添加到表中
		desc.addFamily(family2);
		// 创建表
		admin.createTable(desc); // 创建表
	}

	@Test
	@SuppressWarnings("deprecation")
	public void deleteTable() throws MasterNotRunningException,
			ZooKeeperConnectionException, Exception {
		HBaseAdmin admin = new HBaseAdmin(config);
		admin.disableTable("test1"); // 先是表为非禁用表
		admin.deleteTable("test1");
		admin.close();
	}

	/**
	 * 向hbase中增加数据
	 * @throws Exception
	 */
	@Test
	public void insertData() throws Exception {
		table.setAutoFlushTo(false);
		table.setWriteBufferSize(534534534);
		ArrayList<Put> arrayList = new ArrayList<Put>();
			Put put = new Put(Bytes.toBytes("1")); //rowkey
			put.add(Bytes.toBytes("info"), Bytes.toBytes("url"), Bytes.toBytes("www.baidu.com"));
			put.add(Bytes.toBytes("info"), Bytes.toBytes("datatime"), Bytes.toBytes("2018-11-19"));
			arrayList.add(put);
		//插入数据
		table.put(arrayList);
		//提交
		table.flushCommits();
	}
	/**
	 * 修改数据
	 * @throws Exception
	 */
	@Test
	public void uodateData() throws Exception {
		Put put = new Put(Bytes.toBytes("1")); //rowkey
		put.add(Bytes.toBytes("info"), Bytes.toBytes("url"), Bytes.toBytes("www.google.com"));
		put.add(Bytes.toBytes("info"), Bytes.toBytes("datatime"), Bytes.toBytes("2018-11-20"));
		//插入数据
		table.put(put);
		//提交
		table.flushCommits();
	}

	/**
	 * 删除数据
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteDate() throws Exception {
		Delete delete = new Delete(Bytes.toBytes("1234"));
		table.delete(delete);
		table.flushCommits();
	}

	/**
	 * 单条查询
	 * 
	 * @throws Exception
	 */
	@Test
	public void queryData() throws Exception {
		Get get = new Get(Bytes.toBytes("default509bed3a-4b98-42a5-9211-d84621bd3594"));
		Result result = table.get(get);
		System.out.println(new String(result.value(),"utf-8"));
		// System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("datatime"))));
		// System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("sex"))));
	}

	/**
	 * 全表扫描
	 * 
	 * @throws Exception
	 */
	@Test
	public void scanData() throws Exception {
		Scan scan = new Scan();
		//scan.addFamily(Bytes.toBytes("info"));
		//scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("password"));
		scan.setStartRow(Bytes.toBytes("wangsf_0"));
		scan.setStopRow(Bytes.toBytes("wangwu"));
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
			System.out.println(Bytes.toInt(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("contenttitle"))));
			System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("docno"))));
			//System.out.println(Bytes.toInt(result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("password"))));
			//System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("name"))));
		}
	}

	/**
	 * 全表扫描的过滤器
	 * 列值过滤器
	 * @throws Exception
	 */
	@Test
	public void scanDataByFilter1() throws Exception {

		// 创建全表扫描的scan
		Scan scan = new Scan();
		//过滤器：列值过滤器
		SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("info"),
				Bytes.toBytes("name"), CompareFilter.CompareOp.EQUAL,
				Bytes.toBytes("zhangsan2"));
		// 设置过滤器
		scan.setFilter(filter);

		// 打印结果集
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
			System.out.println(Bytes.toInt(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("password"))));
			System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("name"))));
			//System.out.println(Bytes.toInt(result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("password"))));
			//System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("name"))));
		}

	}
	/**
	 * rowkey过滤器
	 * @throws Exception
	 */
	@Test
	public void scanDataByFilter2() throws Exception {
		
		// 创建全表扫描的scan
		Scan scan = new Scan();
		//匹配rowkey以wangsenfeng开头的
		RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator("^12341"));
		// 设置过滤器
		scan.setFilter(filter);
		// 打印结果集
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
			System.out.println(Bytes.toInt(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("password"))));
			System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("name"))));
			//System.out.println(Bytes.toInt(result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("password"))));
			//System.out.println(Bytes.toString(result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("name"))));
		}

		
	}
	
	/**
	 * 匹配列名前缀
	 * @throws Exception
	 */
	@Test
	public void scanDataByFilter3() throws Exception {
		// 创建全表扫描的scan
		Scan scan = new Scan();
		//匹配rowkey以wangsenfeng开头的
		ColumnPrefixFilter filter = new ColumnPrefixFilter(Bytes.toBytes("name"));
		// 设置过滤器
		scan.setFilter(filter);
		// 打印结果集
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
			System.out.println("rowkey：" + Bytes.toString(result.getRow()));
			System.out.println("info:name："
					+ Bytes.toString(result.getValue(Bytes.toBytes("info"),
							Bytes.toBytes("name"))));
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info"), Bytes.toBytes("age")) != null) {
				System.out.println("info:age："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info"),
								Bytes.toBytes("age"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info"), Bytes.toBytes("sex")) != null) {
				System.out.println("infi:sex："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info"),
								Bytes.toBytes("sex"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("name")) != null) {
				System.out
				.println("info2:name："
						+ Bytes.toString(result.getValue(
								Bytes.toBytes("info2"),
								Bytes.toBytes("name"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("age")) != null) {
				System.out.println("info2:age："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info2"),
								Bytes.toBytes("age"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("sex")) != null) {
				System.out.println("info2:sex："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info2"),
								Bytes.toBytes("sex"))));
			}
		}
		
	}
	/**
	 * 过滤器集合
	 * @throws Exception
	 */
	@Test
	public void scanDataByFilter4() throws Exception {
		
		// 创建全表扫描的scan
		Scan scan = new Scan();
		//过滤器集合：MUST_PASS_ALL（and）,MUST_PASS_ONE(or)
		FilterList filterList = new FilterList(Operator.MUST_PASS_ONE);
		//匹配rowkey以wangsenfeng开头的
		RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator("^wangsenfeng"));
		//匹配name的值等于wangsenfeng
		SingleColumnValueFilter filter2 = new SingleColumnValueFilter(Bytes.toBytes("info"),
				Bytes.toBytes("name"), CompareFilter.CompareOp.EQUAL,
				Bytes.toBytes("zhangsan"));
		filterList.addFilter(filter);
		filterList.addFilter(filter2);
		// 设置过滤器
		scan.setFilter(filterList);
		// 打印结果集
		ResultScanner scanner = table.getScanner(scan);
		for (Result result : scanner) {
			System.out.println("rowkey：" + Bytes.toString(result.getRow()));
			System.out.println("info:name："
					+ Bytes.toString(result.getValue(Bytes.toBytes("info"),
							Bytes.toBytes("name"))));
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info"), Bytes.toBytes("age")) != null) {
				System.out.println("info:age："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info"),
								Bytes.toBytes("age"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info"), Bytes.toBytes("sex")) != null) {
				System.out.println("infi:sex："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info"),
								Bytes.toBytes("sex"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("name")) != null) {
				System.out
				.println("info2:name："
						+ Bytes.toString(result.getValue(
								Bytes.toBytes("info2"),
								Bytes.toBytes("name"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("age")) != null) {
				System.out.println("info2:age："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info2"),
								Bytes.toBytes("age"))));
			}
			// 判断取出来的值是否为空
			if (result.getValue(Bytes.toBytes("info2"), Bytes.toBytes("sex")) != null) {
				System.out.println("info2:sex："
						+ Bytes.toInt(result.getValue(Bytes.toBytes("info2"),
								Bytes.toBytes("sex"))));
			}
		}
		
	}

	@After
	public void close() throws Exception {
		table.close();
		connection.close();
	}
}
