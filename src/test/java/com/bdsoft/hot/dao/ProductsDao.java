package com.bdsoft.hot.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bdsoft.hot.bean.Product;
import com.bdsoft.hot.zol.ZOLProductFeed;
import com.bdsoft.utils.Constant;

public class ProductsDao {
	
	private static final Log logger = LogFactory.getLog(ProductsDao.class);	
//	private static final int INTERVAL_TIME=14400000;  //团购和特价间隔时间
	private static final String bjdg_groupbuy_rmd = "bjdg_groupbuy_rmd";
	private static final String bjdg_cheepbuy_rmd = "bjdg_cheepbuy_rmd";
	private static final String bjdg_cheepbuy_rmd_bak = "bjdg_cheepbuy_rmd_bak";
	
//	public static Connection connection = ConnectionFactory.getInstance().getConnection2();
	
	public static Connection getConnection(){
		      return ConnectionFactory.getInstance().getConnection2();
	}
	
	
//	----------------------------------------------------------------------------
//	这部分程序是为了将数据入库机制改为，先入库，然后再逐个清除过期数据而写的
	
	//DELETE FROM bjdg_groupbuy_rmd WHERE pid IN(SELECT a.pid FROM 
	//		(SELECT r.* FROM bjdg_groupbuy_rmd  r) a INNER JOIN bjdg_products p 
//			ON p.pid=a.pid AND p.groupbuyEndTime < 1334798953886 ORDER BY a.product_id ASC);
	
	/**从商品表中找出来推荐表中有的商品，并查看商品的结束时间，如果商品结束时间<当前时间，
	 * @param tableName
	 */
	public int clearOutTimeProductFromRMDTabel(String tableName){
		Connection conn = getConnection();
		PreparedStatement ps = null;
//		时间间隔2小时，主要是为了特价数据，目前没有时间将每个模板的时间获取，为了调试程序采取的折中方法；
		String selectString = "SELECT a.pid FROM (SELECT r.* FROM " + tableName
				+ " r) a INNER JOIN bjdg_products p ON p.pid=a.pid AND p.groupbuyEndTime < " 
				+ (System.currentTimeMillis())+" ORDER BY 'a.product_id' ASC";
		String delectString = "DELETE FROM " + tableName + " WHERE pid IN(" + selectString + ")";
		int resultCount = 0;
		try {
			ps = conn.prepareStatement(delectString);
			resultCount = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("数据库异常！返回0值！"+e.toString());
			return 0;
		} finally {
			close(conn, ps, null);
		}
		return resultCount;
	}
	
	/*-------------------2013-05-21 新版想买数据源--------------------------------------------*/
	public void saveProductsToRCMD(List<Product> productList){
		Product product =null ;
		int ic = 0, uc=0;
		for (int i = 0; i < productList.size(); i++) {
			try {
				product = productList.get(i);
				// 20130725：推荐库pid重复问题，暂且同步处理看效果，临时处理办法
				synchronized (product) {
					int resultCout = refreshRCMD(product); 
					if (resultCout < 1) {
						saveRCMD(product);
						ic++;
					}else{
						uc++;
					}	
				}
			} catch (Exception e) {
				logger.error("推荐商品-入库异常:" + product.toBaseString(), e);
				e.printStackTrace();
			}
		}
		logger.info("入推荐库=" + ic + "，更新推荐库=" + uc);
	}
	
	// 入推荐库
	public void saveRCMD(Product product) throws SQLException{
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
    	String sql = "insert into bjdg_products_rmd(pid,price,pstat,ptype,utime) values(?,?,?,?,?)";
    	String sqlz = "select pid from bjdg_products where product_url=?";
    	String pid = product.getPid();// 默认从商品中直接获取，中关村需要查询
    	boolean zol = (product.getType() == Constant.COMMON_PRODUCT);
        try{
        	if(zol){ // 中关村商品根据url从商品表查pid
        		ps = conn.prepareStatement(sqlz);
    			ps.setString(1, product.getProduct_url());
    			rs = ps.executeQuery();
    			if(rs.next()){
    				pid = rs.getString("pid");
    				logger.info("中关村url=[" + product.getProduct_url() + "]");
    				logger.info("准备入推荐库-根据url获得商品pid=" + pid);
    			}
    			close(null, ps, rs);
				if(pid==null || "".equals(pid)){
					logger.info("未查到商品pid，url=" + product.getProduct_url());
					return;
				}
        	}
			ps = conn.prepareStatement(sql);
			ps.setString(1, pid);
			ps.setDouble(2, product.getMinPrice());
			ps.setInt(3, Constant.RCMD_STAT_OK);
			ps.setInt(4, product.getType());
			ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			ps.executeUpdate();
			logger.info("入推荐库成功：" + ps.toString());
		} catch (Exception ex) {
			logger.error("入推荐库出错：" + ex.getMessage());
			ex.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
	}
	
	// 更新推荐库
	public int refreshRCMD(Product product) {
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id,pstat from bjdg_products_rmd where pid=?";
		String sqlz = "select r.id,r.pstat from bjdg_products_rmd r,bjdg_products p where r.pid=p.pid and p.product_url=?";
		boolean zol = (product.getType() == Constant.COMMON_PRODUCT);
		int resultCout = 0;
		try {
			ps = conn.prepareStatement(zol ? sqlz : sql);
			ps.setString(1, zol ? product.getProduct_url().trim() : product.getPid().trim());
			rs = ps.executeQuery();
			if(rs.next()) {
				int id = rs.getInt("id");
				int st = rs.getInt("pstat");
				close(null, ps, rs);
				if(zol){
					logger.info("中关村url=[" + product.getProduct_url()+"]");
					logger.info("更新推荐库-根据url获得推荐id="+ id);
				}
				// 下线商品，不更新
				if(st == Constant.RCMD_STAT_OFF) {
					return 1;
				} else {
					sql = "update bjdg_products_rmd set price=?,pstat=?,utime=? where id=?";
					ps = conn.prepareStatement(sql);
					ps.setDouble(1, product.getMinPrice());
					ps.setInt(2, Constant.RCMD_STAT_OK);// 过期数据将重新上线
					ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					ps.setInt(4, id);
					resultCout = ps.executeUpdate();
				}
			} else {
				return 0;
			}
		} catch (SQLException e) {
			logger.error("更新推荐库出错：" + e.getMessage());
			e.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		return resultCout;
	}
	
	// 清理过期商品数据
	public int clearTimeoutProductsFromRCMD(){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select r.pid from (select pid from bjdg_products_rmd where pstat=?) r,bjdg_products p" +
				" where p.pid=r.pid and p.groupbuyEndTime<" + (System.currentTimeMillis());
		// 20130718：不删除数据，改状态
//		String dsql = "DELETE FROM bjdg_products_rmd WHERE pid IN(" + sql + ")";
		String usql = "update bjdg_products_rmd set pstat=?,utime=? where pid in(" + sql + ")"; 
		int resultCount = 0;
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, Constant.RCMD_STAT_OK);
			rs = ps.executeQuery();
			StringBuffer sb = new StringBuffer();
			while(rs.next()){
				sb.append(rs.getString("pid")).append("  ");
			}
			close(null, ps, rs);
			logger.info("过期pid=[" + sb.toString() + "]");
			
			ps = conn.prepareStatement(usql);
			ps.setInt(1, Constant.RCMD_STAT_OUT);
			ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			ps.setInt(3, Constant.RCMD_STAT_OK);
			resultCount = ps.executeUpdate();
			logger.info("清理sql=" + ps.toString());
		} catch (SQLException e) {
			logger.error("清理推荐过期商品出错："+e.getMessage());
			e.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		return resultCount;
	}
	
	// 调用搜索服务，更新商品价格信息
	public int updateProductPriceFromSearch(String pid,Product product){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		String sql = "update bjdg_products set pid=?,price=?,minPrice=?,maxPrice=? where pid=?";
		int affect = 0;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, product.getPid());
			ps.setString(1, product.getPrice());
			ps.setDouble(3, product.getMinPrice());
			ps.setDouble(4, product.getMaxPrice());
			ps.setString(5, pid);
			affect = ps.executeUpdate();
			if(affect > 0){
				logger.info("搜索服务->更新商品价格信息成功！");
			}
		} catch (SQLException e) {
			logger.error("搜索服务->更新商品价格信息出错："+e.getMessage());
			e.printStackTrace();
		} finally {
			close(conn, ps, null);
		}
		return affect;
	}

	// 加载未设定价格的商品
	public List<Product> loadNoPriceProducts(String type){
		List<Product> pros = new ArrayList<Product>();
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select pid,shortName from bjdg_products where type=? and price=? and sellerCode=?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, type);
			ps.setString(2, "0");
			ps.setString(3, "1040");			
 		    rs = ps.executeQuery();
			while(rs!=null && rs.next()){
			   Product product = new Product();
			   product.setPid(rs.getString("pid"));
			   product.setShortName(rs.getString("shortName"));
			   pros.add(product);
			}
		} catch (SQLException e) {
			logger.error("加载未设定价格的商品出错："+e.getMessage());
			e.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		return pros;
	}
	
	// 中关村入口商品数据排行更新
	public void refreshZOLorder(Map<String, ZOLProductFeed> data){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql1 = "select r.* from bjdg_products_rmd r,bjdg_products p where r.pid=p.pid and p.product_url=? and r.pstat=?";
		String sql2 = "update bjdg_products_rmd set ondays=?,lastpos=?,newpos=?,difpos=?,cat1=?,cat2=?,subpos=?,utime=? where id=?";
		try{
			logger.info("开始更新中关村商品排行");
			for(Entry<String, ZOLProductFeed> tmp : data.entrySet()){
				ZOLProductFeed feed = tmp.getValue();
				// 1，先加载推荐数据
				ps = conn.prepareStatement(sql1);
				ps.setString(1, tmp.getKey());
				ps.setInt(2, Constant.RCMD_STAT_OK);
				rs = ps.executeQuery();
				int id=0, od=0, lpos=0, npos=0, dpos=0, spos=0;
				if(rs.next()){
					id = rs.getInt("id");
					od = rs.getInt("ondays");
					lpos = rs.getInt("lastpos");
					npos = rs.getInt("newpos");
					dpos = rs.getInt("difpos");
					spos = rs.getInt("subpos");
					logger.info("加载旧排名："+od+"/"+lpos+"/"+npos+"/"+dpos+"/"+spos); 
				} else {
					logger.info("商品地址不存在=" + tmp.getKey());
				}
				close(null, ps, rs);
				// 2，计算更新最新排行
				if(id > 0){
					int unow = feed.getOrder();// 最新排名
					int ulast = npos==0 ? unow : npos;
					int udif = npos==0 ? 0 : (ulast - unow);
					int subpos = spos==0 ? unow : spos;
					if(unow == npos){
						logger.info("排名无变动:" + unow);
						continue;
					}
					ps = conn.prepareStatement(sql2);
					ps.setInt(1, feed.getOnDays());
					ps.setInt(2, ulast);// 上次排名
					ps.setInt(3, unow);// 当前最新排名
					ps.setInt(4, udif);// 排名变动，升>0,降<0
					ps.setString(5, feed.getCat1());
					ps.setString(6, feed.getCat2());
					ps.setInt(7, subpos);
					ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
					ps.setInt(9, id);
					ps.executeUpdate();
					logger.info("更新新排名："+feed.getOnDays()+"/"+ulast+"/"+unow+"/"+udif+"/"+subpos);
					close(null, ps, null);
					logger.info("商品排名更新成功#" + id);
				}
			}
		} catch (SQLException e) {
			logger.error("中关村入口商品数据排行更新出错："+e.getMessage());
			e.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
	}
	
	//20130529 新版想买数据排序-升序 
	public List<Integer> getZOLProductsAsc(String cat1, String cat2) {
		long s = System.currentTimeMillis();
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select r.id from bjdg_products_rmd r,bjdg_products p " 
				+ "where p.pid=r.pid and p.price>0 and r.pstat=1 and r.ptype=6 and "
				+ "r.difpos>0 and r.cat1=? and r.cat2=? order by r.difpos desc,r.lastpos desc";
		List<Integer> ids = new ArrayList<Integer>();
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, cat1);
			ps.setString(2, cat2);
			rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				ids.add(rs.getInt("id"));
			}
		} catch (Exception ex) {
			logger.error(null, ex);
			ex.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		logger.info(String.format("加载排名上升商品: 分类=[%s],结果=[%s],耗时:%sms", 
				cat2, ids.size(), System.currentTimeMillis() - s));
		return ids;
	}
	
	//20130529 新版想买数据排序-降序
	public List<Integer> getZOLProductsDesc(String cat1, String cat2, int base, boolean flag) {
		long s = System.currentTimeMillis();
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql1 = "select r.id from bjdg_products_rmd r,bjdg_products p " 
				+ "where p.pid=r.pid and p.price>0 and r.pstat=1 and r.ptype=6 and r.difpos<0 and r.newpos<? "
				+ "and r.cat1=? and r.cat2=? order by r.difpos asc,r.lastpos asc";
		String sql2 = "select r.id from bjdg_products_rmd r,bjdg_products p " 
				+ "where p.pid=r.pid and p.price>0 and r.pstat=1 and r.ptype=6 and r.difpos<0 and r.newpos>=? "
				+ "and r.cat1=? and r.cat2=? order by r.difpos desc,r.lastpos desc";
		String sql = flag ? sql1 : sql2;// true-未跌破30的，false-排名跌破30的
		List<Integer> ids = new ArrayList<Integer>();
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, base); 
			ps.setString(2, cat1); 
			ps.setString(3, cat2);
			rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				ids.add(rs.getInt("id"));
			}
		} catch (Exception ex) {
			logger.error(null, ex);
			ex.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		logger.info(String.format("加载排名下降商品: 分类=[%s],结果=[%s],耗时:%sms", 
				cat2, ids.size(), System.currentTimeMillis() - s));
		return ids;
	}
	
	//20130529 新版想买数据排序-正常
	public List<Integer> getZOLProducts(String cat1, String cat2) {
		long s = System.currentTimeMillis();
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select r.id from bjdg_products_rmd r,bjdg_products p " 
				+ "where p.pid=r.pid and p.price>0 and r.pstat=1 and r.ptype=6 and "
				+ "r.difpos=0 and r.cat1=? and r.cat2=? order by r.ondays desc,r.newpos desc";
		List<Integer> ids = new ArrayList<Integer>();
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, cat1);
			ps.setString(2, cat2);
			rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				ids.add(rs.getInt("id"));
			}
		} catch (Exception ex) {
			logger.error(null, ex);
			ex.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		logger.info(String.format("加载排名无变动商品: 分类=[%s],结果=[%s],耗时:%sms", 
				cat2, ids.size(), System.currentTimeMillis() - s));
		return ids;
	}
	 
	// 刷新客户端商品排行顺序
	public void refreshZOLpos(List<Integer> ids){
		long s = System.currentTimeMillis();
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "update bjdg_products_rmd set subpos=?,utime=? where id=?";
		int[] bak = new int[]{};
		try {
			ps = conn.prepareStatement(sql);
			int i=1;
			for(int id : ids){
				ps.setInt(1, i);
				ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				ps.setInt(3, id);
				ps.addBatch();
				i++;
			}
			bak = ps.executeBatch();
		} catch (Exception ex) {
			logger.error(null, ex);
			ex.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		logger.info(String.format("刷新商品排序: 更新记录=[%s],耗时:%sms",bak.length, System.currentTimeMillis() - s));
	}
	
	/*-------------------2013-05-21 新版想买数据源--------------------------------------------*/
	/**根据商品类型，自动决定保存至团购和特价推荐表中，但是为了效率，必须用数据库连接池技术实现
	 * @param productList
	 * @param Type
	 */
	////TODO 必须实现数据库连接池技术
	public void saveProductListToRecommendTable(List<Product> productList,int Type){
		String tableName = "";
		String tableName2 = "";
		if(Type==Constant.GROUPBUY_PRODUCT){
			tableName = bjdg_groupbuy_rmd;
		}else if(Type==Constant.HOT_PRODUCT){
			tableName = bjdg_cheepbuy_rmd;  
			tableName2 = bjdg_cheepbuy_rmd_bak; 
		}
		Product product =null ;
		for (int i = 0; i < productList.size(); i++) {
			try {
				product = productList.get(i);
				int resultCout = updateData(product, tableName);
//				logger.info("saveProductListToRecommendTable p.getType()= " + product.getType()
//						+ "; tableName=" + tableName+ "; tableName2=" + tableName2 
//						+"; \n resultCout=" + resultCout);
				if (resultCout < 1) {
					insertData(product, tableName);
				}
				if (Type == Constant.HOT_PRODUCT) {
					resultCout = updateData(product, tableName2);
					if (resultCout < 1) {
						insertData(product, tableName2);
					}
				}
			} catch (Exception e) {
				logger.error("推荐商品-入库异常:" + product, e);
			}
		}
	}
	
	
	/**按照查询语句获取指定条件的product对象的Pid,返回结果集
	 * @param queryString
	 * @return List<Product> 
	 */
	public List<Product> selectData(String queryString){
		List<Product> productList = null;
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet result = null;
		try {
			ps = conn.prepareStatement(queryString);
			result = ps.executeQuery();
			productList = new ArrayList<Product>();
			while (result != null && result.next()) {
				Product product = new Product();
				product.setPid(result.getString("pid"));
				productList.add(product);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(conn, ps, result);
		}
		return productList;	
	}
	
	/**删除指定pid的product记录
	 * @param product
	 * @param tableName
	 * @return resultCount
	 */
	public int deleteData(Product product,String tableName){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		int resultCount=0;
		String deleteProduct = "delete from "+tableName+" where pid=?";
		try {
//			conn.setAutoCommit(false);
			ps = conn.prepareStatement(deleteProduct);
			ps.setString(1, product.getPid());
			resultCount = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("数据库异常！返回0值！"+e.toString());
			return 0;
		} finally {
			close(conn, ps, null);
		}
		return resultCount;
	}

	/**更新数据表bjdg_cheepbuy_rmd、bjdg_groupbuy_rmd中指定pid的product‘s price
	 * @param product
	 * @param tableName
	 * @return resultCout
	 */
	public int updateData(Product product,String tableName){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		int resultCout = 0;
		try {
			String updateProduct;
			if (tableName.equals("bjdg_products")) {
				updateProduct = "update " + tableName
						+ " set contents=?,groupbuyEndTime=?,groupbuyOnTime=?,maxPrice=?,minPrice=?,mparams=?,"
						+ "orgPic=?,price=?,product_url=?,productLikerCount=?,productName=?,type=?,updateTime=?"
						+ "where pid=?";
				conn.setAutoCommit(false);
				ps = conn.prepareStatement(updateProduct);

				ps.setString(1, product.getContents());
				ps.setLong(2, product.getGroupbuyEndTime());
				ps.setInt(3, product.getGroupbuyOnTime());
				ps.setDouble(4, product.getMaxPrice());
				ps.setDouble(5, product.getMinPrice());
				ps.setString(6, product.getMparams());
				ps.setString(7, product.getOrgPic());
				ps.setString(8, product.getPrice());
				ps.setString(9, product.getProduct_url());
				ps.setInt(10, product.getProductLikerCount());
				ps.setString(11, product.getProductName());
				ps.setInt(12, product.getType());
				ps.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
				ps.setString(14, product.getPid());
				resultCout = ps.executeUpdate();
				conn.commit();
			} else {
				updateProduct = "update " + tableName + " set price=? where pid=?";
				conn.setAutoCommit(false);
				ps = conn.prepareStatement(updateProduct);
				ps.setDouble(1, product.getMinPrice());
				ps.setString(2, product.getPid());
				resultCout = ps.executeUpdate();
				conn.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			e.printStackTrace();
			logger.error("数据库异常！返回0值！"+e.toString());
			return 0;
		} finally {
			close(conn, ps, null);
		}
		return resultCout;
	}
	
	/**
	 * 仅将product数据中的pid、price 入表bjdg_cheepbuy_rmd、bjdg_groupbuy_rmd
	 * @param product
	 * @param tableName
	 * @throws SQLException 
	 */
	////TODO 需要将该部分代码用iBase写成通用代码
	public void insertData(Product product,String tableName) throws SQLException{
		Connection conn = getConnection();
		PreparedStatement ps = null;
       try{
		String insertProduct = "insert into "+tableName+"(pid,price)value(?,?)";
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(insertProduct);
			ps.setString(1, product.getPid());
			ps.setDouble(2, product.getMinPrice());
			ps.executeUpdate();	
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			close(conn, ps, null);
		}
	}
//	------------------------------------------------------------------------------------
	/**
	 * 商品数据存储与更新进入bjdg_products库
	 * @param product_list
	 * @return
	 */
	public boolean save(List<Product> product_list){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = true;
		int updateCount = 0;
		int insertCount = 0;
		// 查询商品是否已经存在
		String sqlQuery = "select pid,productLikerCount,productBuyerCount from bjdg_products where pid=?";
		// 更新重复抓取的商品信息
		String sqlUpdate = "update bjdg_products set groupbuyEndTime=?,price=?,productLikerCount=?,updateTime=?,"
				+ "sellerCount=?,productName=?,maxPrice=?,minPrice=?,contents=?,classic=?,classicCode=?,mparams=?,orgPic=?,"
				+ "type=?,groupbuyOnTime=?,smallPic=?,bigPic=?,brand=?,product_url=?,orgPic=?,sellerCode=?,website=?,seller=?,"
				+ "productBuyerCount=?,shortName=? where pid=?";
		// 入库新抓取的商品信息
		String sqlInsert = "insert into bjdg_products(productName,shortName,"
				+ "brand,price,contents,smallPic,bigPic,seller,website,product_url,"
				+ "pid,cid,keyword,productLikerCount,groupbuyEndTime,groupbuyOnTime,type,"
				+ "createTime,updateTime,sellerCount,maxPrice,minPrice,classic,classicCode,mparams,orgPic,"
				+ "sellerCode,productBuyerCount)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			for (Product p : product_list) {
				logger.info(String.format("prepare save: productName[%s],pid[%s]", p.getProductName(),p.getPid() ));
				ps = conn.prepareStatement(sqlQuery);
				ps.setString(1, p.getPid());
				rs = ps.executeQuery();
				// 如果商品存在，则执行  strSql_update 更新
				if (rs.next() && rs.getString("pid").length() > 0) {
					// 中关村常规商品，只更新价格几个关键字段
					int productLikerCount = rs.getInt("productLikerCount");
					int productBuyerCount = rs.getInt("productBuyerCount");
					// 其他团购特价，更新所有字段
					close(null, ps, rs);
					ps = conn.prepareStatement(sqlUpdate);
					ps.setLong(1, p.getGroupbuyEndTime());
					ps.setString(2,p.getPrice());
					if (p.getType()==Constant.HOT_PRODUCT) {
//						ps.setInt(3,p.getProductLikerCount()==0?productLikerCount:p.getProductLikerCount()+productLikerCount);
						ps.setInt(3,p.getProductLikerCount()==0?productLikerCount:p.getProductLikerCount());
					} else{
						ps.setInt(3,p.getProductLikerCount()==0?productLikerCount:p.getProductLikerCount());
					}
					ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					ps.setInt(5, p.getSellerCount());
					ps.setString(6, p.getProductName());
					ps.setDouble(7, p.getMaxPrice());
					ps.setDouble(8, p.getMinPrice());
					ps.setString(9, p.getContents());
					ps.setString(10, p.getClassic());
					ps.setString(11, p.getClassicCode());
					ps.setString(12, p.getMparams());
					ps.setString(13, p.getOrgPic());
					ps.setInt(14, p.getType());
					ps.setInt(15, p.getGroupbuyOnTime());
					ps.setString(16, p.getSmallPic());
					ps.setString(17, p.getBigPic());
					ps.setString(18, p.getBrand());
					ps.setString(19, p.getProduct_url());
					ps.setString(20, p.getOrgPic());
					ps.setString(21, p.getSellerCode());
					ps.setString(22, p.getWebsite());
					ps.setString(23, p.getSeller());
					if (p.getType() == Constant.GROUPBUY_PRODUCT){
						ps.setInt(24, (p.getProductBuyerCount()>productBuyerCount) ?p.getProductBuyerCount():productBuyerCount);
					} else {
						ps.setInt(24, productBuyerCount);
					}
					ps.setString(25, p.getShortName());
					ps.setString(26, p.getPid()); 
					try {
						ps.executeUpdate();
						updateCount++;						
					} catch (Exception e) {
						logger.error("商品信息"+p.toBaseString());
						logger.error("更新出错="+e.getMessage());
					} finally{
						close(null, ps, null);
					}
		    	 } else {// 如果pid不存在，则执行商品入库保存操作
					close(null, ps, rs);
					ps = conn.prepareStatement(sqlInsert);
					ps.setString(1, p.getProductName());
					ps.setString(2, p.getShortName());
					ps.setString(3, p.getBrand());
					ps.setString(4, p.getPrice());
					ps.setString(5, p.getContents());
					ps.setString(6, p.getSmallPic());
					ps.setString(7, p.getBigPic());
					ps.setString(8, p.getSeller());
					ps.setString(9, p.getWebsite());
					ps.setString(10, p.getProduct_url());
					ps.setString(11, p.getPid());
					ps.setString(12, p.getCid());
					ps.setString(13, p.getKeyword());
					ps.setInt(14, p.getProductLikerCount());
					ps.setLong(15, p.getGroupbuyEndTime());
					ps.setLong(16, p.getGroupbuyOnTime());
					ps.setInt(17, p.getType());
					ps.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
					ps.setTimestamp(19, new Timestamp(System.currentTimeMillis()));
					ps.setInt(20, p.getSellerCount());
					ps.setDouble(21, p.getMaxPrice());
					ps.setDouble(22, p.getMinPrice());
					ps.setString(23, p.getClassic());
					ps.setString(24, p.getClassicCode());
					ps.setString(25, p.getMparams());
					ps.setString(26, p.getOrgPic());
					ps.setString(27, p.getSellerCode());
					ps.setInt(28, p.getProductBuyerCount());
					try {
						ps.executeUpdate();
						insertCount++;						
					} catch (Exception e) { 
						logger.error("商品信息"+p.toBaseString());
						logger.error("入库出错="+e.getMessage());
					}finally{
						close(null, ps, null);
					}
		        }
			}
		} catch (Exception ex) {
			result = false;
			ex.printStackTrace();
			logger.error("商品入库出错："+ex.getMessage());
		} finally {
			close(conn, ps, rs);
		}
		logger.info("bjdg_products 更新=" + updateCount +"，入库=" + insertCount);
		return result;  
	}
	
	/**
	 * 商品数据存储与更新进入bjdg_products库
	 * @param product_list
	 * @return
	 */
	public boolean saveForZOL(List<Product> product_list){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = true;
		int updateCount = 0;
		int insertCount = 0;
		int existsCount = 0;
		int serExistsCount = 0;

		// 中关村入库更新逻辑
		String sqlQuery = "select pid,sellerCode from bjdg_products where product_url=?";
		String sqlQueryByPid = "select pid,sellerCode,product_url from bjdg_products where pid=?";
		String sqlUpdate = "update bjdg_products set groupbuyEndTime=?,price=?,updateTime=?,productName=?,"
				+ "maxPrice=?,minPrice=?,contents=?,mparams=?,orgPic=?,smallPic=?,bigPic=?,shortName=?,sellerCode=?,"
				+ "website=?,seller=?,productLikerCount=?,productBuyerCount=?,classic=?,classicCode=?,type=?,brand=?,"
				+ "product_url=? where pid=?";
		String sqlInsert = "insert into bjdg_products(productName,shortName,"
				+ "brand,price,contents,smallPic,bigPic,seller,website,product_url,"
				+ "pid,cid,keyword,productLikerCount,groupbuyEndTime,groupbuyOnTime,type,"
				+ "createTime,updateTime,sellerCount,maxPrice,minPrice,classic,classicCode,mparams,orgPic,"
				+ "sellerCode,productBuyerCount)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			for (Product p : product_list) {
				logger.info(String.format("prepare save: productName[%s],url[%s]", p.getProductName(),p.getProduct_url()));
				// 查询商品是否已经存在：中关村以商品地址为唯一条件
				ps = conn.prepareStatement(sqlQuery);
				ps.setString(1, p.getProduct_url());
				rs = ps.executeQuery();
				// 如果商品url存在，则执行  sqlUpdate 更新
				if (rs.next() && rs.getString("pid").length() > 0) {
					String pid = rs.getString("pid");
					// 中关村常规商品，只更新价格几个关键字段
					close(null, ps, rs);
					ps = conn.prepareStatement(sqlUpdate); 
					ps.setLong(1, p.getGroupbuyEndTime());
					ps.setString(2, p.getPrice());
					ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					ps.setString(4, p.getProductName());
					ps.setDouble(5, p.getMaxPrice());
					ps.setDouble(6, p.getMinPrice());
					ps.setString(7, p.getContents());
					ps.setString(8, p.getMparams());
					ps.setString(9, p.getOrgPic());
					ps.setString(10, p.getSmallPic());
					ps.setString(11, p.getBigPic());
					ps.setString(12, p.getShortName());
					ps.setString(13, p.getSellerCode());
					ps.setString(14, p.getWebsite());
					ps.setString(15, p.getSeller());
					ps.setInt(16, p.getProductLikerCount());
					ps.setInt(17, p.getProductBuyerCount());
					ps.setString(18, p.getClassic());
					ps.setString(19, p.getClassicCode());
					ps.setString(20, p.getType()+"");
					ps.setString(21, p.getBrand());
					ps.setString(22, p.getProduct_url());
					ps.setString(23, pid);
					try {
						ps.executeUpdate();
						updateCount++;
					} catch (Exception e) {
						updateCount--;
						logger.error("更新中关村数据相同url商品信息出错="+e.getMessage());
						logger.error("商品信息="+p.toBaseString());
					} finally{
						close(null, ps, null);						
					}
		    	 } else {// 如果url不存在，判断pid是否存在
					close(null, ps, rs);
					ps = conn.prepareStatement(sqlQueryByPid);
					ps.setString(1, p.getPid());
					rs = ps.executeQuery();
					if (rs.next() && rs.getString("pid").length() > 0) {
						String seller = rs.getString("sellerCode");
						String purl = rs.getString("product_url");
						logger.info("sellerCode=" + seller + ",purl=" + purl);
						close(null, ps, rs);
						// 判断存在的pid对应的sellercode是否是中关村在线
						if((seller == null || "".equals(seller)) 
								|| (purl == null || "".equals(purl) || "NULL".equals(purl)) 
								|| (purl != null && purl.contains("m.xm99.com.cn"))) {
							// 更新搜索返回同步的数据
							ps = conn.prepareStatement(sqlUpdate); 
							ps.setLong(1, p.getGroupbuyEndTime());
							ps.setString(2, p.getPrice());
							ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
							ps.setString(4, p.getProductName());
							ps.setDouble(5, p.getMaxPrice());
							ps.setDouble(6, p.getMinPrice());
							ps.setString(7, p.getContents());
							ps.setString(8, p.getMparams());
							ps.setString(9, p.getOrgPic());
							ps.setString(10, p.getSmallPic());
							ps.setString(11, p.getBigPic());
							ps.setString(12, p.getShortName());
							ps.setString(13, p.getSellerCode());
							ps.setString(14, p.getWebsite());
							ps.setString(15, p.getSeller());
							ps.setInt(16, p.getProductLikerCount());
							ps.setInt(17, p.getProductBuyerCount());
							ps.setString(18, p.getClassic());
							ps.setString(19, p.getClassicCode());
							ps.setString(20, p.getType()+"");
							ps.setString(21, p.getBrand());
							ps.setString(22, p.getProduct_url());
							ps.setString(23, p.getPid());
							try {
								ps.executeUpdate();
								serExistsCount++;								
							} catch (Exception e) { 
								serExistsCount--;
								logger.error("更新搜索同步商品信息出错=" + e.getMessage());
								logger.error("商品信息=" + p.toBaseString());
							}finally{
								close(null, ps, null);
							}
						} else {// 中关村已入库商品
							existsCount++;
							logger.error("待入库商品已存在：" + p.toBaseString());
							continue;
						}
					} else {// url不存在，pid不存在，直接入库
						close(null, ps, rs);
						ps = conn.prepareStatement(sqlInsert);
						ps.setString(1, p.getProductName());
						ps.setString(2, p.getShortName());
						ps.setString(3, p.getBrand());
						ps.setString(4, p.getPrice());
						ps.setString(5, p.getContents());
						ps.setString(6, p.getSmallPic());
						ps.setString(7, p.getBigPic());
						ps.setString(8, p.getSeller());
						ps.setString(9, p.getWebsite());
						ps.setString(10, p.getProduct_url());
						ps.setString(11, p.getPid());
						ps.setString(12, p.getCid());
						ps.setString(13, p.getKeyword());
						ps.setInt(14, p.getProductLikerCount());
						ps.setLong(15, p.getGroupbuyEndTime());
						ps.setLong(16, p.getGroupbuyOnTime());
						ps.setInt(17, p.getType());
						ps.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
						ps.setTimestamp(19, new Timestamp(System.currentTimeMillis()));
						ps.setInt(20, p.getSellerCount());
						ps.setDouble(21, p.getMaxPrice());
						ps.setDouble(22, p.getMinPrice());
						ps.setString(23, p.getClassic());
						ps.setString(24, p.getClassicCode());
						ps.setString(25, p.getMparams());
						ps.setString(26, p.getOrgPic());
						ps.setString(27, p.getSellerCode());
						ps.setInt(28, p.getProductBuyerCount());
						try {
							ps.executeUpdate();
							insertCount++;
						} catch (Exception e) { 
							insertCount--;
							logger.error("入库中关村商品信息出错="+e.getMessage());
							logger.error("商品信息="+p.toBaseString());
						}finally{
							close(null, ps, null);
						}
					}
		        }
			}
		} catch (Exception ex) {
			result = false;
			logger.error("入库中关村商品出错=" + ex.getMessage());
			ex.printStackTrace();
		} finally {
			close(conn, ps, rs);
		}
		logger.info("bjdg_products 更新=" + updateCount +"，入库=" + insertCount
				+"，搜索pid重复=" + serExistsCount + ",中关村pid重复=" + existsCount);
		return result;
	}
	
	public static boolean save(Product product){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  PreparedStatement ps2 = null;
		  ResultSet rs = null;
		  String strSql = "insert into bjdg_products(productName,shortName," +
		  		"brand,price,contents,smallPic,bigPic,seller,website," +
		  		"product_url,pid,cid,keyword,productLikerCount,groupbuyEndTime,groupbuyOnTime," +
		  		"type,createTime,updateTime,sellerCount,maxPrice,minPrice,classic,classicCode,mparams,orgPic,sellerCode)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		  String strSql2 = "select product_id from bjdg_products where pid=?";
		  boolean result = true;
		  try{
			  conn.setAutoCommit(false);
     		  ps = conn.prepareStatement(strSql);
        	  ps.setString(1, product.getProductName());
        	  ps.setString(2, product.getShortName());
        	  ps.setString(3, product.getBrand());
        	  ps.setString(4, product.getPrice());
        	  ps.setString(5, product.getContents());
        	  ps.setString(6, product.getSmallPic());
        	  ps.setString(7, product.getBigPic());
        	  ps.setString(8, product.getSeller());
        	  ps.setString(9, product.getWebsite());
        	  ps.setString(10, product.getProduct_url());
        	  ps.setString(11, product.getPid());
        	  ps.setString(12, product.getCid());
        	  ps.setString(13, product.getKeyword());
        	  ps.setInt(14, product.getProductLikerCount());
        	  ps.setLong(15, product.getGroupbuyEndTime());
        	  ps.setLong(16, product.getGroupbuyOnTime());
        	  ps.setInt(17, product.getType());
        	  ps.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
        	  ps.setTimestamp(19, new Timestamp(System.currentTimeMillis()));
        	  ps.setInt(20, product.getSellerCount());
			  ps.setDouble(21, product.getMaxPrice());
			  ps.setDouble(22, product.getMinPrice());
			  ps.setString(23, product.getClassic());
			  ps.setString(24, product.getClassicCode());
			  ps.setString(25, product.getMparams());
			  ps.setString(26, product.getOrgPic());
			  ps.setString(27, product.getSellerCode());
        	  
        	  int i = ps.executeUpdate();
        	  if(i==-1){
        		  result = false;
        		  return result;
        	  }
        	  
        	  ps2 = conn.prepareStatement(strSql2);
        	  ps2.setString(1, product.getPid());
        	  rs = ps2.executeQuery();
              if(rs.next()){
            	  product.setId(rs.getString("product_id"));
              }
        	  conn.commit();
		  }catch(Exception ex){
			   result=false;
			   ex.printStackTrace();
		  }finally{
			   try{
				  if(result==false){conn.rollback();}
			      if(ps!=null){ps.close();}
			      if(ps2!=null){ps2.close();}
			      if(conn!=null){conn.close();}   
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		  }
        return result;	  
	}
	
	public static boolean update(Product product){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql_select = "select * from bjdg_products where pid=? for update";
		  String strSql_update = "update bjdg_products set price=?,contents=?,smallPic=?,bigPic=?,updateTime=?,sellerCount=?,maxPrice=?,minPrice=?,mparams=?,orgPic=? where pid=?";
		  String strSql_save = "insert into bjdg_products(productName,shortName," +
		  		"brand,price,contents,smallPic,bigPic,seller,website," +
		  		"product_url,pid,cid,keyword,productLikerCount,groupbuyEndTime,groupbuyOnTime," +
		  		"type,createTime,updateTime,sellerCount,maxPrice,minPrice,classic,classicCode,mparams,orgPic,sellerCode)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		  boolean result = true;
		  try{
			  conn.setAutoCommit(false);
        	  ps = conn.prepareStatement(strSql_select);
        	  ps.setString(1,product.getPid());
        	  //ps.setInt(2, p.getIsGroupbuyProduct());
        	  rs = ps.executeQuery();
        	  if(rs!=null&&rs.next()){
	        		   if(rs.getString("pid").length()>0){
		        			 Product in_product = new Product();
		        			 in_product.setContents(rs.getString("contents"));
		        			 in_product.setPid(rs.getString("pid"));
		        			 in_product.setPrice(rs.getString("price"));
		        			 in_product.setMaxPrice(rs.getDouble("maxPrice"));
		        			 in_product.setMinPrice(rs.getDouble("minPrice"));
		        			 in_product.setSeller(rs.getString("seller"));
		        			 in_product.setSmallPic(rs.getString("smallPic"));
		        			 in_product.setBigPic(rs.getString("bigPic"));
		        			 in_product.setMparams(rs.getString("mparams"));
		        			 in_product.setOrgPic(rs.getString("orgPic"));
		        			 
		        			 //如果product.getPrice()无值，则用保留数据库中的值
		      	    		 in_product.setPrice(product.getPrice()==null||
		    	    				"".equals(product.getPrice())||
		    	    			    "0".equals(product.getPrice())?in_product.getPrice():product.getPrice());
		      	    		 
		      	    		 //如果数据库中getContents()无值，则将product.getContents()入库
		    	    		 in_product.setContents(in_product.getContents()==null||
		    	    				"".equals(in_product.getContents())?product.getContents():in_product.getContents());
		    	    		 
		    	    		 in_product.setSmallPic(in_product.getSmallPic()==null||
		    	    				"".equals(in_product.getSmallPic())?product.getSmallPic():in_product.getSmallPic());
		    	    		 
		    	    		 in_product.setBigPic(in_product.getBigPic()==null||
		    	    				"".equals(in_product.getBigPic())?product.getBigPic():in_product.getBigPic());
		    	    		 
		    	    		 in_product.setSellerCount(product.getSellerCount()!=0?product.getSellerCount():in_product.getSellerCount());
		    	    		 
		    	    		 in_product.setMaxPrice(product.getMaxPrice()==null||
		    	    				 product.getMaxPrice()==0?in_product.getMaxPrice():product.getMaxPrice());
		    	    		
		    	    		 in_product.setMinPrice(product.getMinPrice()==null||
		    	    				 product.getMinPrice()==0?in_product.getMinPrice():product.getMinPrice());
		    	    		 
		    	    		 in_product.setMparams(in_product.getMparams()==null||
		    	    				 "".equals(in_product.getMparams())?product.getMparams():in_product.getMparams());
		    	    		 
		    	    		 in_product.setOrgPic(product.getOrgPic()==null||
		    	    				 "".equals(in_product.getOrgPic())?in_product.getOrgPic():product.getOrgPic());
		    	    		 
		        		    try{
		        			  if(rs!=null){rs.close();}
		     			      if(ps!=null){ps.close();}
		     			    }catch(Exception ex){
		     				   ex.printStackTrace();
		     			    }
			    			ps = conn.prepareStatement(strSql_update);
			    			ps.setString(1,in_product.getPrice());
			    			ps.setString(2, in_product.getContents());
			    			ps.setString(3, in_product.getSmallPic());
			    			ps.setString(4, in_product.getBigPic());
			    			ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			    			ps.setInt(6, in_product.getSellerCount());
		        			ps.setDouble(7, in_product.getMaxPrice());
		        			ps.setDouble(8, in_product.getMinPrice());
		        			ps.setString(9, in_product.getMparams());
		        			ps.setString(10, in_product.getOrgPic());
			    			ps.setString(11, in_product.getPid());
			    			//ps.setInt(4, p.getIsGroupbuyProduct());
			    			int i = ps.executeUpdate();
			                if(i==-1){
			            		 result = false;
			            		 return result;
			            	}
		        	 }
        	  }else{
        		  
        		  try{
        			   if(ps!=null){ps.close();}
        		   }catch(Exception ex){
        		       ex.printStackTrace();
        		   }
        		   
        		  ps = conn.prepareStatement(strSql_save);
	           	  ps.setString(1, product.getProductName());
	           	  ps.setString(2, product.getShortName());
	           	  ps.setString(3, product.getBrand());
	           	  ps.setString(4, product.getPrice());
	           	  ps.setString(5, product.getContents());
	           	  ps.setString(6, product.getSmallPic());
	           	  ps.setString(7, product.getBigPic());
	           	  ps.setString(8, product.getSeller());
	           	  ps.setString(9, product.getWebsite());
	           	  ps.setString(10, product.getProduct_url());
	           	  ps.setString(11, product.getPid());
	           	  ps.setString(12, product.getCid());
	           	  ps.setString(13, product.getKeyword());
	           	  ps.setInt(14, product.getProductLikerCount());
	           	  ps.setLong(15, product.getGroupbuyEndTime());
	           	  ps.setLong(16, product.getGroupbuyOnTime());
	           	  ps.setInt(17, product.getType());
	           	  ps.setTimestamp(18, new Timestamp(System.currentTimeMillis()));
	           	  ps.setTimestamp(19, new Timestamp(System.currentTimeMillis()));
	           	  ps.setInt(20, product.getSellerCount());
	           	  ps.setDouble(21, product.getMaxPrice());
				  ps.setDouble(22, product.getMinPrice());
				  ps.setString(23, product.getClassic());
				  ps.setString(24, product.getClassicCode());
				  ps.setString(25, product.getMparams());
				  ps.setString(26, product.getOrgPic());
				  ps.setString(27, product.getSellerCode());
	           	  
	           	  int i = ps.executeUpdate();
	           	  
	           	  if(i==-1){
	           		  result = false;
	           		  return result;
	           	  }
        	  }
        	  
              conn.commit();
		  }catch(Exception ex){
			   result=false;
			   ex.printStackTrace();
		  }finally{
			   try{
				  if(result==false){conn.rollback();}
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		  }
        return result;
		
	}
	
	public static int countGroupbuyProducts(int type){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql = "select count(1) as count from bjdg_products where type=? and groupbuyEndTime >?";
		  int count = 0;
		  try{
			  ps = conn.prepareStatement(strSql);
			  ps.setInt(1, type);
			  if(type==Constant.HOT_PRODUCT){
				  ps.setLong(2, 0);
			  }else{
				  ps.setLong(2, System.currentTimeMillis());
			  }
			  rs = ps.executeQuery();
			  if(rs!=null&&rs.next()){
				  count = rs.getInt("count");
			  }
		  }catch(Exception ex){
			  ex.printStackTrace();
				logger.error("数据库异常！返回0值！"+ex.toString());
				return 0;
		  }finally{
			   try{
				  if(rs!=null){rs.close();}
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		   }
		 return count;
	}
	
	public static List<Product> getProductList(int type,int start,int length,String sortByField,String sort){		   
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql = "select * from bjdg_products where type=? and groupbuyEndTime >? order by "+sortByField+" "+sort+" limit ?,?";
		  List<Product> list = new ArrayList<Product>();
		  try{
			  ps = conn.prepareStatement(strSql);
//			  System.out.println(System.currentTimeMillis());
			  ps.setInt(1, type);
			  if(type==Constant.HOT_PRODUCT){
				  ps.setLong(2, 0);
			  }else{
				  ps.setLong(2, System.currentTimeMillis());
			  }
//			  ps.setString(3, sortByField);
//			  ps.setString(4, sort);
			  ps.setInt(3, start);
			  ps.setInt(4, length);
			  rs = ps.executeQuery();
			  while(rs!=null&&rs.next()){
				  Product product = new Product();
				  product.setBigPic(rs.getString("bigPic"));
				  product.setBrand(rs.getString("brand"));
				  product.setCid(rs.getString("cid"));
				  product.setContents(rs.getString("contents"));
				  product.setGroupbuyEndTime(rs.getLong("groupbuyEndTime"));
				  product.setGroupbuyOnTime(rs.getInt("groupbuyOnTime"));
				  product.setProductLikerCount(rs.getInt("productLikerCount"));
				  product.setProductBuyerCount(rs.getInt("productBuyerCount"));
				  product.setId(rs.getString("product_id"));
				  product.setType(rs.getInt("type"));
				  product.setKeyword(rs.getString("keyword"));
				  product.setProductName(rs.getString("productName"));
				  product.setPid(rs.getString("pid"));
				  product.setPrice(rs.getString("price"));
				  product.setMaxPrice(rs.getDouble("maxPrice"));
     			  product.setMinPrice(rs.getDouble("minPrice"));
				  product.setProduct_url(rs.getString("product_url"));
				  product.setSeller(rs.getString("seller"));
				  product.setSellerCount(rs.getInt("sellerCount"));
				  product.setShortName(rs.getString("shortName"));
				  product.setSmallPic(rs.getString("smallPic"));
				  product.setWebsite(rs.getString("website"));
				  product.setClassic(rs.getString("classic"));
				  product.setClassicCode(rs.getString("classicCode"));
				  product.setMparams(rs.getString("mparams"));
				  product.setOrgPic(rs.getString("orgPic"));
				  product.setSellerCode(rs.getString("sellerCode"));
				  list.add(product);
			  }
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }finally{
			   try{
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		   }
		  return list;
	}
	
	public static Product getProductInfo(String pid){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String strSql = "select * from bjdg_products where pid=?";
		String strSql2 = "update bjdg_products set productLikerCountTmp=productLikerCountTmp+? where pid=?";
		Product product = null;
		try {
			conn.setAutoCommit(false);
			// 临时想买人数累计器
			ps = conn.prepareStatement(strSql2);
			Random rd = new Random();
			ps.setInt(1, rd.nextInt(3));
			ps.setString(2, pid);
			ps.executeUpdate();
			try {
				if (ps != null) {
					ps.close();
					ps = null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			ps = conn.prepareStatement(strSql);
			ps.setString(1, pid);
			rs = ps.executeQuery();
			if (rs != null && rs.next()) {
				product = new Product();
				product.setBigPic(rs.getString("bigPic"));
				product.setBrand(rs.getString("brand"));
				product.setCid(rs.getString("cid"));
				product.setContents(rs.getString("contents"));
				product.setGroupbuyEndTime(rs.getLong("groupbuyEndTime"));
				product.setGroupbuyOnTime(rs.getInt("groupbuyOnTime"));
				product.setType(rs.getInt("type"));
				if (product.getType() != Constant.GROUPBUY_PRODUCT) {
					product.setProductLikerCount(rs.getInt("productLikerCount")
							+ rs.getInt("productLikerCountTmp"));
				} else {
					product.setProductLikerCount(rs.getInt("productLikerCount"));
				}
				product.setProductBuyerCount(rs.getInt("productBuyerCount"));
				product.setId(rs.getString("product_id"));
				product.setKeyword(rs.getString("keyword"));
				product.setProductName(rs.getString("productName"));
				product.setPid(rs.getString("pid"));
				product.setPrice(rs.getString("price"));
				product.setMaxPrice(rs.getDouble("maxPrice"));
				product.setMinPrice(rs.getDouble("minPrice"));
				product.setProduct_url(rs.getString("product_url"));
				product.setSeller(rs.getString("seller"));
				product.setSellerCount(rs.getInt("sellerCount"));
				product.setShortName(rs.getString("shortName"));
				product.setSmallPic(rs.getString("smallPic"));
				product.setWebsite(rs.getString("website"));
				product.setClassic(rs.getString("classic"));
				product.setClassicCode(rs.getString("classicCode"));
				product.setMparams(rs.getString("mparams"));
				product.setOrgPic(rs.getString("orgPic"));
				product.setSellerCode(rs.getString("sellerCode"));
			  }
			  conn.commit();
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }finally{
			   try{
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		   }
		  return product;
	}
	
	public static String getProductPID(String id){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String strSql = "select pid from bjdg_products where product_id=?";
		String pid = "";
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(strSql);
			ps.setString(1, id);
			rs = ps.executeQuery();
			if (rs != null && rs.next()) {
				pid = rs.getString("pid");
			}
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return pid;
	}
	
	public static boolean updateGroupbuyEndtimeAndnameByPid(String pid,long groupbuyEndtime,String productName){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  String strSql = "update bjdg_products set groupbuyEndTime=?,productName=? where pid=?";
		  boolean result = true;
		  try{
			  conn.setAutoCommit(false);
        	  ps = conn.prepareStatement(strSql);
        	  ps.setLong(1, groupbuyEndtime);
        	  ps.setString(2, productName);
        	  ps.setString(3, pid);
        	  int i = ps.executeUpdate();
        	  if(i==-1){
        		  result = false;
        		  return result;
        	  }
        	  conn.commit();
		  }catch(Exception ex){
			  ex.printStackTrace();
			  result=false;
		  }finally{
			   try{
				  if(result==false){conn.rollback();}
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		  }
		  return result;
	}
	
	public static boolean updateProductByPid(String pid,String price,Double maxPrice,Double minPrice,String contents,String smallPic,String bigPic,int sellerCount,String mparams,String orgPic){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		String strSql = "update bjdg_products set price=?,contents=?,smallPic=?,bigPic=?,sellerCount=?,maxPrice=?,minPrice=?,updateTime=?,mparams=?,orgPic=? where pid=?";
		boolean result = true;
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(strSql);
			ps.setString(1, price);
			ps.setString(2, contents);
			ps.setString(3, smallPic);
			ps.setString(4, bigPic);
			ps.setInt(5, sellerCount);
			ps.setDouble(6, maxPrice);
			ps.setDouble(7, minPrice);
			ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
			ps.setString(9, mparams);
			ps.setString(10, orgPic);
			ps.setString(11, pid);
			int i = ps.executeUpdate();
			if (i == -1) {
				result = false;
				return result;
			}
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
			try {
				if (result == false) {
					conn.rollback();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 团购列表前50条数据根据价格、分组排序后保存数据
	 * @param product_list
	 * @return
	 */
	public static boolean saveSortNo(List<Product> product_list){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  boolean result = true;
		  String strSql = "truncate table bjdg_groupbuy_rmd";
		  String strSql2 = "insert into bjdg_groupbuy_rmd(pid,price)values(?,?)";
		  try{
			  conn.setAutoCommit(false);
			  ps = conn.prepareStatement(strSql);
			  int point = ps.executeUpdate();
			  if(point==-1){
				  return false;
			  }
			  if(ps!=null){
				  ps.close();
			  }
			  ps = conn.prepareStatement(strSql2);
			  for(Product p:product_list){
				  
				  try{
				      ps.setString(1, p.getPid());
					  try{
					      ps.setDouble(2, p.getPrice()==null||p.getPrice().length()==0?0:Double.parseDouble(p.getPrice()));
					  }catch(Exception ex){
						  ps.setDouble(2, 0);
					  }
				      ps.executeUpdate();
				  }catch(SQLException ex){
					  ex.printStackTrace();
				  }
			  }
			  conn.commit();
		  }catch(Exception ex){
			  ex.printStackTrace();
			  result=false;
		  }finally{
			   try{
				  if(result==false){conn.rollback();}
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		  }
		  return result;
	}
	
	/**
	 * 统计团购推荐数目
	 * @return
	 */
	public static int countGroupbuyProductsRmd(){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql = "select count(1) as count from bjdg_groupbuy_rmd";
		  int count = 0;
		  try{
			  ps = conn.prepareStatement(strSql);
			  rs = ps.executeQuery();
			  if(rs!=null&&rs.next()){
				  count = rs.getInt("count");
			  }
		  }catch(Exception ex){
			ex.printStackTrace();
			logger.error("数据库异常！返回0值！" + ex.toString());
			return 0;
		  }finally{
			   try{
				  if(rs!=null){rs.close();}
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		   }
		 return count;
	}
	
    /**
     * 获取团购推荐列表
     * @param start
     * @param length
     * @return
     */
	public static List<Product> getGroupbuyProductsRmdList(int start,int length){		   
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql = "select p.* from bjdg_groupbuy_rmd r inner join bjdg_products p on p.pid=r.pid order by r.price desc limit ?,?";
		  List<Product> list = new ArrayList<Product>();
		  try{
			  ps = conn.prepareStatement(strSql);
			  ps.setInt(1, start);
			  ps.setInt(2, length);
			  rs = ps.executeQuery();
			  while(rs!=null&&rs.next()){
				  Product product = new Product();
				  product.setBigPic(rs.getString("bigPic"));
				  product.setBrand(rs.getString("brand"));
				  product.setCid(rs.getString("cid"));
				  product.setContents(rs.getString("contents"));
				  product.setGroupbuyEndTime(rs.getLong("groupbuyEndTime"));
				  product.setGroupbuyOnTime(rs.getInt("groupbuyOnTime"));
				  product.setProductLikerCount(rs.getInt("productLikerCount"));
				  product.setId(rs.getString("product_id"));
				  product.setType(rs.getInt("type"));
				  product.setKeyword(rs.getString("keyword"));
				  product.setProductName(rs.getString("productName"));
				  product.setPid(rs.getString("pid"));
				  product.setPrice(rs.getString("price"));
				  product.setMaxPrice(rs.getDouble("maxPrice"));
     			  product.setMinPrice(rs.getDouble("minPrice"));
				  product.setProduct_url(rs.getString("product_url"));
				  product.setSeller(rs.getString("seller"));
				  product.setSellerCount(rs.getInt("sellerCount"));
				  product.setShortName(rs.getString("shortName"));
				  product.setSmallPic(rs.getString("smallPic"));
				  product.setWebsite(rs.getString("website"));
				  product.setClassic(rs.getString("classic"));
				  product.setClassicCode(rs.getString("classicCode"));
				  product.setMparams(rs.getString("mparams"));
				  product.setOrgPic(rs.getString("orgPic"));
				  product.setSellerCode(rs.getString("sellerCode"));
				  list.add(product);
			  }
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }finally{
			   try{
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		   }
		  return list;
	}
	
	/**
	 * 特价列表商品save
	 * @param product_list
	 * @return
	 */
	public static boolean saveCheepProNo(List<Product> product_list){
		Connection conn = getConnection();
		PreparedStatement ps = null;
		boolean result = true;
//		String strSql_select = "select * from bjdg_products where pid=?";
		String strSql = "truncate table bjdg_cheepbuy_rmd_bak";
		String strSql2 = "insert into bjdg_cheepbuy_rmd_bak(pid,price)values(?,?)";
		String strSql3 = "truncate table bjdg_cheepbuy_rmd";
		String strSql4 = "insert into bjdg_cheepbuy_rmd(pid,price) select pid,price from bjdg_cheepbuy_rmd_bak";
		String strSql5 = "select count(1) as count from bjdg_cheepbuy_rmd_bak";
		try {
			conn.setAutoCommit(false);
//			如果product_list商品数大于800个，则执行
			if (product_list.size() > 800) {
				ps = conn.prepareStatement(strSql);
				int point = ps.executeUpdate();
				if (point == -1) {
					return false;
				}
			}
			if (ps != null) {
				ps.close();
			}
			ps = conn.prepareStatement(strSql2);
			for (Product p : product_list) {
				try {
					ps.setString(1, p.getPid());
					try {
						ps.setDouble(2, p.getPrice() == null
								|| p.getPrice().length() == 0 ? 0 : Double
								.parseDouble(p.getPrice()));
					} catch (Exception ex) {
						ps.setDouble(2, 0);
					}
					ps.executeUpdate();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			ps = conn.prepareStatement(strSql3);
			ps.executeUpdate();
			if (ps != null) {
				ps.close();
			}
			ps = conn.prepareStatement(strSql4);
			ps.executeUpdate();
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
			try {
				if (result == false) {
					conn.rollback();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 统计团购推荐数目
	 * @return
	 */
	public static int countCheepbuyProductsRmd(){
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql = "select count(1) as count from bjdg_cheepbuy_rmd";
		  int count = 0;
		  try{
			  ps = conn.prepareStatement(strSql);
			  rs = ps.executeQuery();
			  if(rs!=null&&rs.next()){
				  count = rs.getInt("count");
			  }
		  }catch(Exception ex){
			  ex.printStackTrace();
			logger.error("数据库异常！返回0值！" + ex.toString());
			return 0;
		  }finally{
			   try{
				  if(rs!=null){rs.close();}
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   }
		   }
		 return count;
	}
	
    /**
     * 获取在线特价商品列表
     * @param start
     * @param length
     * @return
     */
	public static List<Product> getCheepbuyProductsRmdList(int start,int length){		   
		  Connection conn = getConnection();
		  PreparedStatement ps = null;
		  ResultSet rs = null;
		  String strSql = "select p.* from bjdg_cheepbuy_rmd r inner join bjdg_products p on p.pid=r.pid and p.type=2 order by r.price desc limit ?,?";
		  List<Product> list = new ArrayList<Product>();
		  try{
			  ps = conn.prepareStatement(strSql);
			  ps.setInt(1, start);
			  ps.setInt(2, length);
			  rs = ps.executeQuery();
			  while(rs!=null&&rs.next()){
				  Product product = new Product();
				  product.setBigPic(rs.getString("bigPic"));
				  product.setBrand(rs.getString("brand"));
				  product.setCid(rs.getString("cid"));
				  product.setContents(rs.getString("contents"));
				  product.setGroupbuyEndTime(rs.getLong("groupbuyEndTime"));
				  product.setGroupbuyOnTime(rs.getInt("groupbuyOnTime"));
				  product.setType(rs.getInt("type"));
				  if(product.getType()!=Constant.GROUPBUY_PRODUCT){
					  product.setProductLikerCount(rs.getInt("productLikerCount")+rs.getInt("productLikerCountTmp"));  
				  }else{
					  product.setProductLikerCount(rs.getInt("productLikerCount"));
				  }
				  product.setProductBuyerCount(rs.getInt("productBuyerCount"));
				  product.setId(rs.getString("product_id"));
				  product.setKeyword(rs.getString("keyword"));
				  product.setProductName(rs.getString("productName"));
				  product.setPid(rs.getString("pid"));
				  product.setPrice(rs.getString("price"));
				  product.setMaxPrice(rs.getDouble("maxPrice"));
     			  product.setMinPrice(rs.getDouble("minPrice"));
				  product.setProduct_url(rs.getString("product_url"));
				  product.setSeller(rs.getString("seller"));
				  product.setSellerCount(rs.getInt("sellerCount"));
				  product.setShortName(rs.getString("shortName"));
				  product.setSmallPic(rs.getString("smallPic"));
				  product.setWebsite(rs.getString("website"));
				  product.setClassic(rs.getString("classic"));
				  product.setClassicCode(rs.getString("classicCode"));
				  product.setMparams(rs.getString("mparams"));
				  product.setOrgPic(rs.getString("orgPic"));
				  product.setSellerCode(rs.getString("sellerCode"));
				  list.add(product);
			  }
			  
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }finally{
			   try{
			      if(ps!=null){ps.close();}
			      if(conn!=null){conn.close();}
			   }catch(Exception ex){
				   ex.printStackTrace();
			   } 
		   }
		  return list;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		List<Product> list = new ArrayList<Product>();
//		
//		list = ProductsDao.getGroupbuyProductList(1,10, 10);
//		
//		System.out.println(list.size());
//		
		Product p =new Product();
		p.setPid("111111111111111");
		p.setMinPrice(1111111.1);
//		
//		System.out.println(p.getName());
//		
//		ProductsDao.updateGroupbuyEndtimeAndnameByPid(p.getPid(), 1312905599, "444");
		
		ProductsDao proD=new ProductsDao();
		String tableName="bjdg_groupbuy_rmd";
		p.setMinPrice(1111111111.3);
//		proD.insertData(p, tableName);
		int count=proD.deleteData(p, tableName);

//		int count=proD.updateData(p, tableName);
		if(count>0){
			System.out.println("操作成功");
		}
		String selectProduct = "select * from bjdg_products where groupbuyEndTime > "+System.currentTimeMillis();
		
		List<Product> productList = proD.selectData(selectProduct);
		
		System.out.println(productList.get(0).getPid()); 
	}
	
	
	/**
	 * 释放数据库资源
	 * @param connect 
	 * @param statement
	 * @param resultSet
	 */
	private void close(Connection connect,PreparedStatement statement,ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (Exception e) {
			logger.error("close db of rs error = " + e.getMessage());
			e.printStackTrace();
		}
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (Exception e) {
			logger.error("close db of ps error = " + e.getMessage());
			e.printStackTrace();
		}
		try {
			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {
			logger.error("close db of con error = " + e.getMessage());
			e.printStackTrace();
		}
	} 
}
