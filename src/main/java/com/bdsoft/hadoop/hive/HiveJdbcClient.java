/**
 * HiveJdbcClient.java
 * com.bdsoft.hadoop.hive
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.hive;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-26
 * @version  1.0.0
 */

public class HiveJdbcClient {

	//	final String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";// hive0.11.0之前版本
	static final String driverName = "org.apache.hive.jdbc.HiveDriver";// hive0.11.0之后版本

	public static void main(String[] args) throws Exception {
		Class.forName(driverName);

		Connection con = DriverManager.getConnection("jdbc:hive//bdceo:10000/test", "root", "root");

		System.out.println(con.getClientInfo());
	}

}
