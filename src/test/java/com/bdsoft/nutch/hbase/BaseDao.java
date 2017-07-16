package com.bdsoft.nutch.hbase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.bdsoft.utils.StrUtil;

public class BaseDao implements ExecuteResult {

	public static final Logger LOG = Logger.getLogger(BaseDao.class);

	private static HTablePool pool;
	private static Configuration configuration;
	static {
		configuration = HBaseConfiguration.create();
		pool = new HTablePool(configuration, 20);// 1000=>20 attention
	}

	public static HTable getHTB(String table) {
		return (HTable) pool.getTable(table);
	}

	/**
	 * 创建表
	 * 
	 * @param tableName
	 */
	public static void createTable(String tableName, String[] familyArr) {
		try {
			HBaseAdmin hBaseAdmin = new HBaseAdmin(configuration);
			if (hBaseAdmin.tableExists(tableName)) {// 如果存在要创建的表，那么先删除，再创建
				hBaseAdmin.disableTable(tableName);
				hBaseAdmin.deleteTable(tableName);
			}
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			for (String family : familyArr) {// 添加family列族
				tableDescriptor.addFamily(new HColumnDescriptor(family));
			}
			hBaseAdmin.createTable(tableDescriptor); // 创建数据库表
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除一张表
	 * 
	 * @param tableName
	 */
	public static void dropTable(String tableName) {
		try {
			HBaseAdmin admin = new HBaseAdmin(configuration);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 插入数据
	 * 
	 * @param tableName 注意:put的add方法需要三个参数：family列族,qualifier column列名,value值 步骤： 1.找到table。
	 *            2.设置rowId。 3.设置要存储的值
	 */
	public static boolean insertData(String tableName, String rowKey, String family,
			String[] columnArr, String[] values) {
		boolean resut = false;
		HTable table = null;
		try {
			table = (HTable) pool.getTable(tableName);
			// 设置存储id
			Put put = new Put(rowKey.getBytes());
			// 设置存储内容
			for (int i = 0; i < columnArr.length; i++) {
				if (values[i] == null || values[i].equals("null")) {
					values[i] = "";
				}
				put.add(family.getBytes(), columnArr[i].getBytes(), values[i].getBytes());// 本行数据的第i列
			}
			// 执行存储
			table.put(put);
			resut = true;
		} catch (IOException e) {
			LOG.error("BaseDao->insertData error:" + e);
			e.printStackTrace();
		} finally {
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->insertData putTable error:" + e2);
				e2.printStackTrace();
			}
		}
		return resut;
	}

	/**
	 * 根据 rowkey删除一条记录
	 * 
	 * @param tablename
	 * @param rowkey
	 */
	public static boolean deleteRow(String tableName, String rowkey) {

		boolean result = false;
		HTable table = null;
		try {
			table = (HTable) pool.getTable(tableName);
			Delete d1 = new Delete(rowkey.getBytes());
			table.delete(d1);
			result = true;
		} catch (IOException e) {
			LOG.error("BaseDao->deleteRow error:" + e);
			e.printStackTrace();
		} finally {
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->deleteRow putTable error:" + e2);
				e2.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 根据 rowkey数组删除N条记录
	 * 
	 * @param tablename
	 * @param rowkey
	 */
	public static boolean deleteRowArr(String tableName, String[] rowkeyArr) {

		boolean result = false;
		HTable table = null;
		try {
			table = (HTable) pool.getTable(tableName);
			List<Delete> Deletelist = new ArrayList<Delete>();
			for (String rowkey : rowkeyArr) {
				Delete delete = new Delete(rowkey.getBytes());
				Deletelist.add(delete);
			}
			table.delete(Deletelist);
			result = true;
		} catch (IOException e) {
			LOG.error("BaseDao->deleteRowArr error:" + e);
			e.printStackTrace();
		} finally {
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->deleteRowArr putTable error:" + e2);
				e2.printStackTrace();
			}
		}
		return result;
	}

	public static void queryAll(String tableName, ExecuteResult executeResult) {
		HTable table = null;
		ResultScanner rs = null;
		try {
			table = (HTable) pool.getTable(tableName);
			rs = table.getScanner(new Scan());
			executeResult.executeResult(rs);
		} catch (IOException e) {
			LOG.error("BaseDao->queryAll error:" + e);
			e.printStackTrace();
		} finally {
			rs.close();
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->queryAll putTable error:" + e2);
				e2.printStackTrace();
			}
		}
	}

	public static void queryAllByRange(String tableName, String startRow, String stopRow,
			ExecuteResult executeResult) {
		HTable table = null;
		ResultScanner rs = null;
		try {
			Scan scan = new Scan();
			scan.setStartRow(startRow.getBytes());
			scan.setStopRow(stopRow.getBytes());
			table = (HTable) pool.getTable(tableName);
			rs = table.getScanner(scan);
			executeResult.executeResult(rs);
		} catch (IOException e) {
			LOG.error("BaseDao->queryAllByRange error:" + e);
			e.printStackTrace();
		} finally {
			rs.close();
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->queryAllByRange putTable error:" + e2);
				e2.printStackTrace();
			}
		}
	}

	public static Map<String, String> queryOne(String tableName, String rowkey) {

		Map<String, String> map = new LinkedHashMap<String, String>();
		HTable table = null;
		try {
			table = (HTable) pool.getTable(tableName);
			Get scan = new Get(rowkey.getBytes());
			Result r = table.get(scan);
			if (r.getRow() != null) {
				map.put("rowkey", new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					map.put(new String(keyValue.getQualifier()).toLowerCase(),
							new String(keyValue.getValue()));
					// System.out.println(printKeyValue(keyValue));
				}
			}
		} catch (IOException e) {
			LOG.error("BaseDao->queryOne error:" + e);
			e.printStackTrace();
		} finally {
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->queryOne putTable error:" + e2);
				e2.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * 单条件按查询，查询多条记录
	 * 
	 * @param tableName 注意： 1.Filter对象四个参数:family column CompareOp value
	 *            表示该family下的column的value要CompareOp(大于、小于、等于等)
	 */
	public static void QueryByCondition2(String tableName) {
		HTable table = null;
		ResultScanner rs = null;
		try {
			table = (HTable) pool.getTable(tableName);
			Filter filter = new SingleColumnValueFilter(Bytes.toBytes("column1"), null,
					CompareOp.EQUAL, Bytes.toBytes("aaa")); // 当列column1的值为aaa时进行查询
			Scan s = new Scan();
			s.setFilter(filter);
			rs = table.getScanner(s);
			for (Result r : rs) {
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					System.out.println(printKeyValue(keyValue));
				}
			}
		} catch (Exception e) {
			LOG.error("BaseDao->QueryByCondition2 error:" + e);
			e.printStackTrace();
		} finally {
			rs.close();
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->QueryByCondition2 putTable error:" + e2);
				e2.printStackTrace();
			}
		}

	}

	/**
	 * 多条件查询
	 * 
	 * @param tableName
	 */
	public static void QueryByCondition3(String tableName) {
		HTable table = null;
		ResultScanner rs = null;
		try {
			HTablePool pool = new HTablePool(configuration, 1000);
			table = (HTable) pool.getTable(tableName);
			List<Filter> filters = new ArrayList<Filter>();

			Filter filter1 = new SingleColumnValueFilter(Bytes.toBytes("column1"), null,
					CompareOp.EQUAL, Bytes.toBytes("aaa"));
			filters.add(filter1);

			Filter filter2 = new SingleColumnValueFilter(Bytes.toBytes("column2"), null,
					CompareOp.EQUAL, Bytes.toBytes("bbb"));
			filters.add(filter2);

			Filter filter3 = new SingleColumnValueFilter(Bytes.toBytes("column3"), null,
					CompareOp.EQUAL, Bytes.toBytes("ccc"));
			filters.add(filter3);

			FilterList filterList1 = new FilterList(filters);

			Scan scan = new Scan();
			scan.setFilter(filterList1);
			rs = table.getScanner(scan);
			for (Result r : rs) {
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					System.out.println(printKeyValue(keyValue));
				}
			}
			rs.close();

		} catch (Exception e) {
			LOG.error("BaseDao->QueryByCondition3 error:" + e);
			e.printStackTrace();
		} finally {
			rs.close();
			try {
				pool.putTable(table);
			} catch (Exception e2) {
				LOG.error("BaseDao->QueryByCondition3 putTable error:" + e2);
				e2.printStackTrace();
			}
		}

	}

	/**
	 * 打印每一条记录
	 * 
	 * @param keyValue
	 * @return
	 */
	public static String printKeyValue(KeyValue keyValue) {

		return "family:" + new String(keyValue.getFamily()) + "  column:"
				+ new String(keyValue.getQualifier()) + "  value:"
				+ new String(keyValue.getValue());
	}

	/**
	 * 用于获取Result结果集中某一些给定的column的值
	 * 
	 * @param result 结果集
	 * @param family family
	 * @param qualifier
	 * @param toLowCase
	 * @return 根据给定famyli和qualifier(即column)得到该column对应的值。toLowCase表示是否将值转换为小写
	 */
	public static String getValue(Result result, String family, String qualifier, boolean toLowCase) {

		String str = "";
		try {
			byte[] b = result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier));
			if (b != null && b.length > 0) {
				str = StrUtil.trim(new String(b, "utf8"));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return toLowCase ? str.toLowerCase() : str;
	}

	public void executeResult(ResultScanner rs) {
		int count = 0;
		for (Result r : rs) {
			count++;
			/*
			 * System.out.println("获得到rowkey:" + new String(r.getRow())); for (KeyValue keyValue :
			 * r.raw()) { System.out.println(printKeyValue(keyValue)); }
			 */
			System.out.println(new String(r.getRow()));
		}
		System.out.println(count);
	}

}
