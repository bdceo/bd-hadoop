package com.bdsoft.nutch.hbase;

import org.apache.hadoop.hbase.client.ResultScanner;

public interface ExecuteResult {

	public void executeResult(ResultScanner rs);
	
}
