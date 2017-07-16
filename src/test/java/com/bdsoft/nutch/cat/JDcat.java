package com.bdsoft.nutch.cat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bdsoft.utils.NetUtil;
import com.bdsoft.utils.Utils;

public class JDcat {

	private static final Log log = LogFactory.getLog(JDcat.class);
	private static String CAT_URL = "http://www.jd.com/allSort.aspx";
	private static String E_CODE = "1005";
	private static String E_NAME = "京东";

	private static String E_TABLE = "xm_ecats";// 表

	private static String XM_SP = "[xm]";
	private static String XM99_SP = "[xm99]";

	public static void main(String[] args) {
		List<Category> cats = fetchJDcats();
		System.out.println(cats);
		// saveCats(cats);
		// exportCatsForHbase(E_CODE);
	}

	/**
	 * 导出电商原始分类，做hbase原始分类映射
	 * 
	 * @param eid
	 */
	public static void exportCatsForHbase(String eid) {
		List<Category> cats1 = new ArrayList<Category>();
		try {
			long start = System.currentTimeMillis();
			cats1 = loadSubCats(eid, -1);
			System.out.println("递归加载分类，耗时="
					+ (System.currentTimeMillis() - start));
			StringBuffer sb = new StringBuffer();
			for (Category c1 : cats1) {
				String c1c = c1.getCode();
				String c1n = c1.getName();
				for (Category c2 : c1.getNextCat()) {
					String c2c = c2.getCode();
					String c2n = c2.getName();
					if (c2c == null || "".equals(c2c)) {
						break;
					}
					for (Category c3 : c2.getNextCat()) {
						String c3c = c3.getCode();
						String c3n = c3.getName();
						if (c3c == null || "".equals(c3c)) {
							continue;
						}
						sb.append(c1c).append(XM99_SP).append(c2c)
								.append(XM99_SP).append(c3c);
						sb.append(XM_SP).append(c1n).append(XM99_SP)
								.append(c2n).append(XM99_SP).append(c3n);
						sb.append("\n");
					}
				}
			}
			System.out.println(sb.toString());
			// log.info(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 递归加载子分类
	 * 
	 * @param eid
	 *            电商
	 * @param pid
	 *            夫级id
	 * @return
	 */
	public static List<Category> loadSubCats(String eid, int pid) {
		List<Category> cats = new ArrayList<Category>();
		String sql = "select * from " + E_TABLE + " where eid=? and pid=?";
		Connection con = DB.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, eid);
			ps.setInt(2, pid);
			rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String code = rs.getString("cat_code");
				String name = rs.getString("cat_name");
				Category cat = new Category(code, name);
				cat.setNextCat(loadSubCats(eid, id));
				cats.add(cat);
			}
			return cats;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DB.close(con, ps, rs);
		}
		return null;
	}

	/**
	 * 保存原始分类到mysql：xm_edata -> xm_ecats
	 * 
	 * @param cats
	 */
	public static void saveCats(List<Category> cats) {
		String sql = "insert into " + E_TABLE
				+ "(eid,ename,cat_code,cat_name,pid) values('" + E_CODE + "','"
				+ E_NAME + "',?,?,?)";
		Connection con = DB.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// 遍历一级分类
			for (Category cat1 : cats) {
				ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, cat1.getCode());
				ps.setString(2, cat1.getName());
				ps.setInt(3, -1);
				ps.execute();
				rs = ps.getGeneratedKeys();
				if (rs == null || !rs.next()) {
					continue;
				}
				int pid1 = rs.getInt(1);

				// 遍历二级分类
				List<Category> cats2 = cat1.getNextCat();
				for (Category cat2 : cats2) {
					ps = con.prepareStatement(sql,
							Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, cat2.getCode());
					ps.setString(2, cat2.getName());
					ps.setInt(3, pid1);
					ps.executeUpdate();
					rs = ps.getGeneratedKeys();
					if (rs == null || !rs.next()) {
						continue;
					}
					int pid2 = rs.getInt(1);

					// 遍历三级分类
					List<Category> cats3 = cat2.getNextCat();
					for (Category cat3 : cats3) {
						ps = con.prepareStatement(sql);
						ps.setString(1, cat3.getCode());
						ps.setString(2, cat3.getName());
						ps.setInt(3, pid2);
						ps.execute();
						if (rs == null || !rs.next()) {
							continue;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DB.close(con, ps, rs);
		}
	}

	/**
	 * 抓取京东原始分类页面 http://www.jd.com/allSort.aspx
	 * 
	 * @return
	 */
	public static List<Category> fetchJDcats() {
		try {
			String src = NetUtil.getPageContent(CAT_URL, Utils.CHARSET_GBK);
			Document doc = Jsoup.parse(src);
			Element content = doc.getElementById("allsort");
			Elements cats1 = content.getElementsByClass("m");

			List<Category> cat1s = new ArrayList<Category>();
			// 遍历一级分类
			for (int i = 0; i < cats1.size(); i++) {
				// 获取一级分类名称
				Element cat1Ele = cats1.get(i);
				String cat1Name = cat1Ele.getElementsByTag("h2").get(0).text();
				String cat1Code = "";
				if (cat1Name.contains("彩票")) {
					continue;
				}
				Category cat1 = new Category(cat1Code, cat1Name);
				List<Category> cat2s = new ArrayList<Category>();

				// 二级分类
				Elements cats2 = cat1Ele.getElementsByClass("mc").get(0)
						.getElementsByTag("dl");
				for (int j = 0; j < cats2.size(); j++) {
					Element cat2Ele = cats2.get(j);
					String cat2Name = cat2Ele.getElementsByTag("dt").get(0)
							.text();
					String cat2Code = "";
					Category cat2 = new Category(cat2Code, cat2Name);
					List<Category> cat3s = new ArrayList<Category>();

					// 三级分类
					Elements cats3 = cat2Ele.getElementsByTag("dd").get(0)
							.getElementsByTag("a");
					for (int k = 0; k < cats3.size(); k++) {
						Element cat3Ele = cats3.get(k);
						String cat3Name = cat3Ele.text();
						String cat3Code = "";
						String href = cat3Ele.attr("href");
						if (href.indexOf("e.jd.com") > 0) {
							break;
						}
						href = href.substring(href.lastIndexOf("/") + 1,
								href.lastIndexOf("."));
						String[] codes = href.split("-");
						if (codes.length == 1) {
							cat3Code = codes[0];
						}
						if (codes.length == 2) {
							cat1Code = codes[0];
							cat3Code = codes[1];
						}
						if (codes.length == 3) {
							cat1Code = codes[0];
							cat2Code = codes[1];
							cat3Code = codes[2];
						}
						Category cat3 = new Category(cat3Code, cat3Name);
						cat3s.add(cat3);
					}
					cat2.setCode(cat2Code);
					cat2.setNextCat(cat3s);
					cat2s.add(cat2);
				}
				cat1.setNextCat(cat2s);
				cat1.setCode(cat1Code);
				cat1s.add(cat1);
			}
			return cat1s;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
}

// 分类定义
class Category {

	private int id;
	private String code;
	private String name;
	private int pid;
	private List<Category> nextCat;

	public Category(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}

	public Category(String code, String name, int pid) {
		super();
		this.code = code;
		this.name = name;
		this.pid = pid;
	}

	public String toString() {
		return "Category [code=" + code + ", name=" + name + ", nextCat="
				+ nextCat + "]\n";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public List<Category> getNextCat() {
		return nextCat;
	}

	public void setNextCat(List<Category> nextCat) {
		this.nextCat = nextCat;
	}

}

class DB {
	private static String E_DATABASE = "xm_edata";// 数据库
	private static String DB_URL = "jdbc:mysql://192.168.1.3:3306/"
			+ E_DATABASE
			+ "?user=root&password=cba123&useUnicode=true&characterEncoding=utf-8";
	// private static String DB_URL =
	// "jdbc:mysql://202.85.208.169:3306/bjdg?user=root&password=ivxm99&useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
	private static String DB_DRIVER = "com.mysql.jdbc.Driver";

	public static Connection getCon() {
		try {
			Class.forName(DB_DRIVER).newInstance();
			return DriverManager.getConnection(DB_URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void close(Connection con, Statement st, ResultSet rs) {
		try {
			if (con != null) {
				con.close();
			}
			if (st != null) {
				st.close();
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
