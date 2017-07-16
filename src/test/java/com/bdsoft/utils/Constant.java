package com.bdsoft.utils;

public class Constant {
	

	public static final String XMTAG = "[xm99]";
	public static final String XMFLAG = "[xm99]";
	public static final String XMTAG_R = "\\[xm99\\]";
	public static String NULLCHAR;

	// 搜索服务
	public static String SEARCH_SERVER = "";
	

	// 商品类型定义
	public static final int COMMON_PRODUCT = 6;// 常规商品类型6
	public static final int GOOGLE_PRODUCT = 4;// 谷歌商品类型4
	public static final int BARCODE_PRODUCT = 3;// 条码商品类型3
	public static final int HOT_PRODUCT = 2;// 热门推荐商品2//特价商品
	public static final int GROUPBUY_PRODUCT = 1;// 团购商品类型1
	public static final int COMPARTION_PRODUCT = 0;// 比价商品类型0
	

	public static boolean jdbc_conn = true;
	public static String url = "jdbc:mysql://localhost:3306/bjdg?user=root&password=wangli&useUnicode=true&characterEncoding=GBK";
	public static String driver = "com.mysql.jdbc.Driver";
	public static String user = "root";
	public static String pwd = "";
	

	// 商品推荐状态
	public static final int RCMD_STAT_OK = 1;// 正常
	public static final int RCMD_STAT_OUT = 2;// 过期
	public static final int RCMD_STAT_OFF = 3;// 下线
	

	public static String ext_jpg = ".jpg";
	public static String img_host = "http://p.xm99.com.cn/pic/";
	public static String pic_path = "e:/data/pic/";
	public static String ext = ".jpg";
	public static String pic_width = "85";
	public static String pic_height = "85";
	public static String pic_big_width = "330";
	public static String pic_big_height = "330";
	public static float per = 0.8f;
	public static long interval_time = 1000000;
	public static int groupbuy_seller_count = 7;
	

	// 1天毫秒值
	public static Long ONE_DAY = 24 * 60 * 60 * 1000L;
	

	public static String UTF8 = "UTF-8";
}
