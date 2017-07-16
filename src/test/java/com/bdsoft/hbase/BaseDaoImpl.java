package com.bdsoft.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

public class BaseDaoImpl implements BaseDao {
	private static Logger log = Logger.getLogger(BaseDaoImpl.class);

	@Override
	public boolean deleteRow(String tableName, String rowkey)
			throws IOException {
		HTable table = (HTable) HBaseConnector.getInstance()
				.getTable(tableName);
		Delete d1 = new Delete(rowkey.getBytes());
		table.delete(d1);
		return true;
	}

	@Override
	public boolean deleteRow(String tableName, String[] rowkey)
			throws IOException {
		HTable table = (HTable) HBaseConnector.getInstance()
				.getTable(tableName);
		List<Delete> list = new ArrayList<Delete>();
		for (String key : rowkey) {
			Delete d = new Delete(key.getBytes());
			list.add(d);
		}
		table.delete(list);
		return true;
	}

	@Override
	public boolean insertData(String tableName, String rowKey, String family,
			String[] column, String[] values) throws IOException {
		HTable table = (HTable) HBaseConnector.getInstance()
				.getTable(tableName);
		// 设置存储id
		Put put = new Put(rowKey.getBytes());
		// 设置存储内容
		for (int i = 0; i < column.length; i++) {
			put.add(family.getBytes(), column[i].getBytes(),
					values[i].getBytes("UTF8"));// 本行数据的第i列
		}
		// 执行存储
		table.put(put);
		return true;
	}

	public Map<String, String> queryOne(String tableName, String rowkey) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		HTable table = null;
		try {
			table = (HTable) HBaseConnector.getInstance().getTable(tableName);
			Get scan = new Get(rowkey.getBytes());
			Result r = table.get(scan);
			if (r.getRow() != null) {
				map.put("rowkey", new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					map.put(new String(keyValue.getQualifier()).toLowerCase(),
							new String(keyValue.getValue()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (table != null) { 
				try {
					table.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		return map;
	}

}
