package com.bdsoft.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.log4j.Logger;


public class HBaseConnector {
	private static final Logger log = Logger.getLogger(HBaseConnector.class);

	private static HBaseConnector instance ;
	int poolSize = 5 ;
	private HBaseConnector(){
		configuration = HBaseConfiguration.create();
		pool = new HTablePool(configuration, poolSize);
	}
	public static HBaseConnector getInstance(){
		if (instance == null) 
			instance = new HBaseConnector();
		return instance ;
	}
	
	private  HTablePool pool;
	private  Configuration configuration;

	
	public HTableInterface getTable(String tableName){
		return pool.getTable(tableName);
	}

	public void destroy(){
		if (pool!=null){
			try {
				pool.close();
			} catch (IOException e) {
				log.error("pool destroy exception!",e);
			}
		}
	}
	
}



