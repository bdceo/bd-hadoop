package com.bdsoft.hbase;

import org.apache.hadoop.hbase.client.HTable;

public class HbaseConTest {

	public static void main(String[] args) {

		HTable htb = (HTable) HBaseConnector.getInstance().getTable("stdProducts");
		String stat = htb == null ? "bad" : "ok";
		System.out.println("hbase is " + stat);

		htb = null;
		htb = com.bdsoft.nutch.hbase.BaseDao.getHTB("stdProducts");
		stat = htb == null ? "bad" : "ok";
		System.out.println("hbase is " + stat);
	}

}
