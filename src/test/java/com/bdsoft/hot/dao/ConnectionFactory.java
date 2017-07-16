package com.bdsoft.hot.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bdsoft.utils.Constant;

public class ConnectionFactory {

	private static final Log log = LogFactory.getLog(ConnectionFactory.class);

	protected static ConnectionFactory instance = new ConnectionFactory();

	// protected static DataSource ds = null;

	protected ConnectionFactory() {
		try {
			Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
			log.info("初始化链接工厂");
		} catch (Exception e) {
			log.error("初始化连接工厂出错：" + e.toString());
			e.printStackTrace();
		}
	}

	public static ConnectionFactory getInstance() {
		return instance;
	}

	/**
	 * 指定连接字符串直接以jdbc方式获取链接
	 * 
	 * 未指定连接字符串通过sssl方式获取链接
	 * 
	 * @return
	 */
	public Connection getConnection2() {
		try {
			if (Constant.jdbc_conn) {
				return DataSourceManager.dataSource.getConnection();
			} else {
				return getConnection3();
			}
		} catch (Exception ex) {
			log.error("getConnection2-获取数据库连接出错：" + ex.toString());
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 通过sssl方式获取链接
	 * 
	 * @return
	 */
	public Connection getConnection3() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("proxool.sssl");
		} catch (Exception e) {
			log.error("getConnection3-获取数据库连接出错：" + e.toString());
			e.printStackTrace();
		}
		// 返回连接
		return conn;
	}

	/**
	 * 直接加载驱动，获取jdbc连接
	 * 
	 * @return
	 */
	public Connection getConnection() {
		Connection conn = null;
		try {
			// String dbURL =
			// "jdbc:mysql://u01.netjsp.com:3306/za00004?user=za00004&password=bKK6pDkV&useUnicode=true&characterEncoding=utf-8";//&useUnicode=true&characterEncoding=gbk
			String dbURL = Constant.url;// &useUnicode=true&characterEncoding=gbk
			Class.forName(Constant.driver).newInstance();
			// String user = "root";
			// String password = "cba123";
			conn = DriverManager.getConnection(dbURL);// , user, password
		} catch (Exception e) {
			log.error("getConnection-获取数据库连接出错：" + e.toString());
			e.printStackTrace();
		}
		return conn;
	}

	public static String convertCharset(String content, String oldcharset,
			String newcharset) {
		String correctStr = null;
		try {
			if (oldcharset == null || "".equals(oldcharset)) {
				correctStr = new String(content.getBytes(newcharset));
			} else {
				correctStr = new String(content.getBytes(oldcharset),
						newcharset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return correctStr;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Connection conn = ConnectionFactory.getInstance().getConnection2();
		String strSql = "insert into bjdg_customer(name,regTime,lastTime)values(?,now(),now())";
		// strSql = "insert into test.test(name)values(?,now(),now())";
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(strSql);
			ps.setString(1, convertCharset("黄健林", null, "gbk"));
			int i = ps.executeUpdate();
			System.out.println("res:" + i);

			strSql = "select * from bjdg_customer";

			ps = conn.prepareStatement(strSql);
			ResultSet rs = ps.executeQuery();

			rs.next();
			System.out.println(rs.getString("name"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}