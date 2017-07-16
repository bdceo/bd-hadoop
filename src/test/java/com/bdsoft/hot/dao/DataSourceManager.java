package com.bdsoft.hot.dao;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class DataSourceManager {

	public static DataSource dataSource = null;
	public static DataSource setupDataSource(String driver,String connectURI,String user,String password) {

	    BasicDataSource ds = new BasicDataSource();
	    if(user!=null&&!"".equals(user)){
	    	ds.setUsername(user);
		    ds.setPassword(password);
	    }
	    ds.setDriverClassName(driver);
	    ds.setUrl(connectURI);
	    return ds;

	}


	public static void printDataSourceStats(DataSource ds) {

	    BasicDataSource bds = (BasicDataSource) ds;
	    System.out.println("NumActive: " + bds.getNumActive());
	    System.out.println("NumIdle: " + bds.getNumIdle());

	}


	public static void shutdownDataSource(DataSource ds) throws SQLException {

	    BasicDataSource bds = (BasicDataSource) ds;
	    bds.close();

	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
