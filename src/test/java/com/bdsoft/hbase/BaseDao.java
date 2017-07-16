
package com.bdsoft.hbase;

import java.util.Map;



public interface BaseDao {
	//插入数据
	public boolean insertData(String tableName,String rowKey,String family,String[] column,String[] values)throws Exception;

	//删除一条数据
	public boolean deleteRow(String tableName, String rowkey)throws Exception;
	
	//删除多条数据
	public boolean deleteRow(String tableName, String[] rowkey) throws Exception;
	
	
	public   Map<String,String> queryOne(String tableName,String rowkey);
}



