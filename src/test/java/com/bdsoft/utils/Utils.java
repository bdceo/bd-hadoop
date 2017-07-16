package com.bdsoft.utils;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	/**
	 * 静态常量
	 */
	public static final String C_TIME_PATTON_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	public static final String C_DATE_PATTON_DEFAULT = "yyyy-MM-dd";

	public static final int C_ONE_SECOND = 1000;
	public static final int C_ONE_MINUTE = 60 * C_ONE_SECOND;
	public static final long C_ONE_HOUR = 60 * C_ONE_MINUTE;
	public static final long C_ONE_DAY = 24 * C_ONE_HOUR;

	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String CHARSET_GBK = "GBK";
	public static final String CHARSET_GB2312 = "GB2312";

	// 浏览器客户端代理
	public static final String BROWSER_UA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.20 (KHTML, like Gecko) Chrome/25.0.1337.0 Safari/537.20";

	public static DecimalFormat df = new DecimalFormat("0.00");

	public static String doubleFormat(double value) {
		return df.format(value);
	}

	/**
	 * 两个double数值相加
	 * 
	 * @param a
	 * @param b
	 * @return 精度由其一参数确定
	 */
	public static double sum(double a, double b) {
		BigDecimal bd1 = new BigDecimal(Double.toString(a));
		BigDecimal bd2 = new BigDecimal(Double.toString(b));
		return bd1.add(bd2).doubleValue();
	}

	/**
	 * 两个double数值相减
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double sub(double a, double b) {
		BigDecimal bd1 = new BigDecimal(Double.toString(a));
		BigDecimal bd2 = new BigDecimal(Double.toString(b));
		return bd1.subtract(bd2).doubleValue();
	}

	/**
	 * 两个double数值相乘
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double mul(double a, double b) {
		BigDecimal bd1 = new BigDecimal(Double.toString(a));
		BigDecimal bd2 = new BigDecimal(Double.toString(b));
		return bd1.multiply(bd2).doubleValue();
	}

	/**
	 * 将字符转换成原样整形
	 * 
	 * @param c
	 *            待转字符
	 * @return 整形
	 */
	public static int char2int(char c) {
		String s = Character.toString(c);
		return Integer.parseInt(s);
	}
 

	public static boolean isNotEmpty(String str) {
		return (null != str && "".equals(str.trim()) == false);
	}

	public static boolean isNotEmpty(List list) {
		return (null != list && list.size() > 0);
	}

	public static boolean isNotEmpty(Object str) {
		return (null != str);
	}

	public static boolean isNotEmptyObject(Object str) {
		return (null != str && !"".equals(str));
	}

	/**
	 * 返回一个定长的随机字符串(只包含数字)
	 * 
	 * @param length
	 *            随机字符串长度
	 * @return 随机字符串
	 */
	public static String generateString(int length) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(random.nextInt(9));
		}
		return sb.toString();
	}

	/**
	 * 生成指定大小的随机数
	 * 
	 * @param num
	 *            号码区间
	 * @return
	 */
	public static int intRandom(int num) {
		Random random = new Random();
		return random.nextInt(num);
	}

	/**
	 * 生成0-9的随机数
	 * 
	 * @return
	 */
	public static int intRandom() {
		Random random = new Random();
		return random.nextInt(9);
	}

	public static String random(int num) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < num; i++) {
			buffer.append(intRandom());
		}
		return buffer.toString();
	}

	/**
	 * 正则验证
	 * 
	 * @param Content
	 *            需要验证的内容
	 * @param regex
	 *            验证正则表达式
	 * @return
	 */
	public static boolean regValidate(String Content, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Content);
		return matcher.matches();
	}

	/**
	 * 配置所有
	 * 
	 * @param Content
	 * @param regex
	 * @return
	 */
	public static List<String> regFinds(String Content, String regex) {
		List<String> list = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Content);
		while (matcher.find()) {
			list.add(matcher.group());
		}
		return list;
	}

	/**
	 * 正则查找
	 * 
	 * @param Content
	 *            查找内容
	 * @param regex
	 *            匹配正则表达式
	 * @return
	 */
	public static String regFind(String Content, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Content);
		matcher.find();
		return matcher.group();
	}

	/**
	 * 正则查找
	 * 
	 * @param Content
	 *            查找内容
	 * @param regex
	 *            匹配正则表达式
	 * @param index
	 *            分组
	 * @return
	 */
	public static String regFind(String Content, String regex, int index) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Content);
		matcher.find();
		return matcher.group(index);
	}

	/**
	 * 正则替换
	 * 
	 * @param Content
	 *            查找内容
	 * @param regex
	 *            匹配正则表达式
	 * @return
	 */
	public static String regReplace(String Content, String regex, String regStr) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(Content);
		return matcher.replaceAll(regStr);
	}

	/**
	 * 字符串包含验证,包含验证重复号码
	 * 
	 * @param content
	 *            验证内容
	 * @param vastr
	 *            对比字符串
	 * @return
	 */
	public static boolean comValidate(String content, String vastr) {
		if (null == content)
			return false;
		if ("".equals(content))
			return false;
		boolean re = true;
		String[] arr = content.split(",");
		int len = arr.length;
		for (int i = 0; i < len; i++) {
			if (vastr.indexOf(arr[i]) < 0) {
				re = false;
				break;
			} else {
				if (vastr.indexOf(arr[i], vastr.indexOf(arr[i]) + 1) > -1) {
					re = false;
					break;
				}
			}
		}
		return re;
	}

	/**
	 * 字符串包含验证,包含验证重复号码,总位数
	 * 
	 * @param content
	 *            验证内容
	 * @param vastr
	 *            对比字符串
	 * @param num
	 *            必须满足多少位
	 * @return
	 */
	public static boolean comValidate(String content, String vastr, int num) {
		if (null == content)
			return false;
		if ("".equals(content))
			return false;
		boolean re = true;
		String[] arr = content.split(",");
		int len = arr.length;
		if (len < num)
			return false;
		for (int i = 0; i < len; i++) {
			if (vastr.indexOf(arr[i]) < 0) {
				re = false;
				break;
			} else {
				if (vastr.indexOf(arr[i], vastr.indexOf(arr[i]) + 1) > -1) {
					re = false;
					break;
				}
			}
		}
		return re;
	}

	/**
	 * 中文
	 * 
	 * @return
	 */
	public static String encodStr(String str) {
		try {
			return new String(str.getBytes("iso-8859-1"), "utf-8");
		} catch (Exception e) {
			return str;
		}
	}

	/**
	 * 中文
	 * 
	 * @return
	 */
	public static String encodStr(String str, String encoding) {
		try {
			return new String(str.getBytes("iso-8859-1"), encoding);
		} catch (Exception e) {
			return str;
		}
	}

	/**
	 * 格式化日期
	 * 
	 * @return
	 */
	public static Date formatDate(String date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 格式化日期
	 * 
	 * @return
	 */
	public static Date formatDate(String date, String style) {
		date = formatStr(date);
		if (null == date) {
			return null;
		}
		SimpleDateFormat simpleDateormat = new SimpleDateFormat(style);
		try {
			return simpleDateormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 转换指定时间为指定格式
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate2Str(Date date, String format) {
		if (date == null) {
			return null;
		}

		if (format == null || format.equals("")) {
			format = C_TIME_PATTON_DEFAULT;
		}
		SimpleDateFormat sdf = getSimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 拿到指定输出格式的SimpleDateFormat
	 * 
	 * @param format
	 * @return
	 */
	public static SimpleDateFormat getSimpleDateFormat(String format) {
		SimpleDateFormat sdf;
		if (format == null) {
			sdf = new SimpleDateFormat(C_TIME_PATTON_DEFAULT);
		} else {
			sdf = new SimpleDateFormat(format);
		}

		return sdf;
	}

	/**
	 * 得到当前日期
	 * 
	 * @return
	 */
	public static String today() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return simpleDateFormat.format(new Date());
	}

	/**
	 * 根据格式得到当前日期
	 * 
	 * @param format
	 * @return
	 */

	public static String today(String format) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(new Date());
	}

	/**
	 * 得到昨天的日期yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String yesterday() {
		Date date = new Date();
		date.setTime(date.getTime() - 86400000);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return simpleDateFormat.format(date);
	}

	/**
	 * 得到明天的日期yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String tomorrow() {
		Date date = new Date();
		date.setTime(date.getTime() + 86400000);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return simpleDateFormat.format(date);
	}

	/**
	 * 得到今天的星期
	 * 
	 * @return 今天的星期
	 */
	public static String getWeek() {
		SimpleDateFormat sdf = new SimpleDateFormat("E");
		return sdf.format(new Date());
	}

	public static Date getLastDayOfMonth(Date date) {
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTime(date);
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
		lastDate.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天
		lastDate.set(Calendar.HOUR, 0);
		lastDate.set(Calendar.MINUTE, 0);
		lastDate.set(Calendar.SECOND, 0);
		return lastDate.getTime();
	}

	public static Date getFirstDayOfMonth(Date date) {
		Calendar firstDate = Calendar.getInstance();
		firstDate.setTime(date);
		firstDate.set(Calendar.DATE, 1);// 设为当前月的1号
		firstDate.set(Calendar.HOUR, 0);
		firstDate.set(Calendar.MINUTE, 0);
		firstDate.set(Calendar.SECOND, 0);
		return firstDate.getTime();
	}

	/**
	 * 日期的指定域加减
	 * 
	 * @param time
	 *            时间戳(长整形字符串)
	 * @param field
	 *            加减的域,如date表示对天进行操作,month表示对月进行操作,其它表示对年进行操作
	 * @param num
	 *            加减的数值
	 * @return 操作后的时间戳(长整形字符串)
	 */
	public static String addDate(String time, String field, int num) {
		int fieldNum = Calendar.YEAR;
		if (field.equals("m")) {
			fieldNum = Calendar.MONTH;
		}
		if (field.equals("d")) {
			fieldNum = Calendar.DATE;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(time));
		cal.add(fieldNum, num);
		return String.valueOf(cal.getTimeInMillis());
	}

	/**
	 * 日期的指定域加减
	 * 
	 * @param date
	 *            时间戳(长整形字符串)
	 * @param field
	 *            加减的域,如date表示对天进行操作,month表示对月进行操作,其它表示对年进行操作
	 * @param num
	 *            加减的数值
	 * @return 操作后的时间戳(长整形字符串)
	 */
	public static Date addDate(Date date, String field, int num) {
		if (date != null) {
			int fieldNum = Calendar.YEAR;
			if (field.equals("m")) {
				fieldNum = Calendar.MONTH;
			}
			if (field.equals("d")) {
				fieldNum = Calendar.DATE;
			}
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(date.getTime());
			cal.add(fieldNum, num);
			return new Date(cal.getTimeInMillis());
		}
		return null;
	}

	/**
	 * 日期的指定域加减
	 * 
	 * @param field
	 *            加减的域,如date表示对天进行操作,month表示对月进行操作,其它表示对年进行操作
	 * @param num
	 *            加减的数值
	 * @return 操作后的时间戳(长整形字符串)
	 */
	public static long addDate(String field, int num) {
		field = field.toLowerCase();
		int fieldNum = Calendar.YEAR;
		if (field.equals("m")) {
			fieldNum = Calendar.MONTH;
		}
		if (field.equals("d")) {
			fieldNum = Calendar.DATE;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(new Date().getTime());
		cal.add(fieldNum, num);

		return cal.getTimeInMillis();
	}

	/**
	 * 得到一个日期是否是上午
	 * 
	 * @param date
	 *            日期
	 * @return 日期为上午时返回true
	 */
	public static boolean isAm(Date date) {
		boolean flag = false;
		SimpleDateFormat sdf = new SimpleDateFormat("H");
		if (sdf.format(date).compareTo("12") < 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * IP转换成10位数字
	 * 
	 * @param ip
	 *            IP
	 * @return 10位数字
	 */
	public static long ip2num(String ip) {
		long ipNum = 0;
		try {
			if (ip != null) {
				String ips[] = ip.split("\\.");
				for (int i = 0; i < ips.length; i++) {
					int k = Integer.parseInt(ips[i]);
					ipNum = ipNum + k * (1L << ((3 - i) * 8));
				}
			}
		} catch (Exception e) {
		}
		return ipNum;
	}

	/**
	 * 将十进制整数形式转换成127.0.0.1形式的ip地址
	 */
	public static String num2ip(long longIp) {
		StringBuffer sb = new StringBuffer("");
		// 直接右移24位
		sb.append(String.valueOf((longIp >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((longIp & 0x000000FF)));
		return sb.toString();
	}

	/**
	 * 判断是否为整数
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是整数返回true, 否则返回false
	 */
	public static boolean isInt(String str) {
		if (null == str)
			return false;
		str = str.trim();
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static void main(String[] eeeee) {
		String str = "1352222e22";
		System.out.println(isInt(str));
		System.out.println(isIncludeChinese(str));
	}

	/**
	 * 判断是否为整数
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是整数返回true, 否则返回false
	 */
	public static int formatInt(String str, int def) {
		if (isNotEmpty(str) && isInt(str)) {
			return Integer.parseInt(str);
		} else {
			return def;
		}
	}

	/**
	 * 判断是否为整数
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是整数返回true, 否则返回false
	 */
	public static long formatLong(String str, int def) {
		if (isNotEmpty(str) && isInt(str)) {
			return Long.parseLong(str);
		} else {
			return def;
		}
	}

	/**
	 * 判断是否为整数
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是整数返回true, 否则返回false
	 */
	public static long formatLong(Long str, int def) {
		if (isNotEmpty(str)) {
			return str;
		} else {
			return def;
		}
	}

	/**
	 * 判断是否为整数
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是整数返回true, 否则返回false
	 */
	public static Long formatLong(String str) {
		if (isNotEmpty(str) && isInt(str)) {
			return Long.parseLong(str);
		} else {
			return null;
		}
	}

	/**
	 * 判断是否为整数
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是整数返回true, 否则返回false
	 */
	public static Integer formatInt(String str, Integer def) {
		if (isNotEmpty(str) && isInt(str)) {
			return Integer.parseInt(str);
		} else {
			return def;
		}
	}

	/**
	 * 转换字符串为浮点数
	 * 
	 * @param str
	 *            待格式化字符串
	 * @param def
	 *            默认值
	 * @return
	 */
	public static Float Double(String str, Float def) {
		if (isNotEmpty(str) && isDouble(str)) {
			return Float.parseFloat(str);
		}
		return def;

	}

	/**
	 * 转换字符串为浮点数
	 * 
	 * @param str
	 *            待格式化字符串
	 * @param def
	 *            默认值
	 * @return
	 */
	public static Double formatDouble(String str, Double def) {
		if (isNotEmpty(str) && isDouble(str)) {
			return Double.parseDouble(str);
		}
		return def;
	}

	/**
	 * 转换BigDecimal为浮点数
	 * 
	 * @param obj
	 *            待格式化字符串
	 * @return
	 */
	public static Double formatDouble(Object obj) {
		Double num = 0d;
		if (isNotEmpty(obj)) {
			num = (Double) obj;
			// BigDecimal db = (BigDecimal) obj;
			// num = db.doubleValue();
		}
		return num;
	}

	/**
	 * 转换字符串为浮点数
	 * 
	 * @param str
	 *            待格式化字符串
	 * @param def
	 *            默认值
	 * @return
	 */
	public static Double formatDouble(Double str, Double def) {
		if (isNotEmpty(str)) {
			return str;
		}
		return def;
	}

	/**
	 * 判断是否为浮点数，包括double和float
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是浮点数返回true, 否则返回false
	 */
	public static boolean isDouble(String str) {
		if (null == str)
			return false;
		Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断输入的字符串是否符合Email样式.
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 是Email样式返回true, 否则返回false
	 */
	public static boolean isEmail(String str) {
		if (null == str)
			return false;
		Pattern pattern = Pattern
				.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
		return pattern.matcher(str).matches();
	}

	/**
	 * 判断输入的字符串是否为纯汉字
	 * 
	 * @param str
	 *            传入的字符串
	 * @return 如果是纯汉字返回true, 否则返回false
	 */
	public static boolean isChinese(String str) {
		if (null == str)
			return false;
		Pattern pattern = Pattern.compile("[\u0391-\uFFE5]+$");
		return pattern.matcher(str).matches();
	}

	public static boolean isIncludeChinese(String str) {
		if (isEmpty(str)) {
			return false;
		}
		Pattern pattern = Pattern
				.compile("^\\s*\\S*[\\u0391-\\uFFE5]+\\s*\\S*$");
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	/**
	 * 是否为空白,包括null和""
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * 是否为空白,包括null和""
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(List str) {
		return str == null || str.size() == 0;
	}

	/**
	 * 奖字符转成16进制
	 * 
	 * @return
	 */
	public static String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = "0000" + Integer.toHexString(ch);
			str = str + s4.substring(s4.length() - 4) + " ";
		}
		return str;
	}

	/**
	 * 人民币转成大写
	 * 
	 * @param value
	 * @return String
	 */
	public static String hangeToBig(double value) {
		char[] hunit = { '拾', '佰', '仟' }; // 段内位置表示
		char[] vunit = { '万', '亿' }; // 段名表示
		char[] digit = { '零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖' }; // 数字表示
		long midVal = (long) (value * 100); // 转化成整形
		String valStr = String.valueOf(midVal); // 转化成字符串

		String head = valStr.substring(0, valStr.length() - 2); // 取整数部分
		String rail = valStr.substring(valStr.length() - 2); // 取小数部分

		String prefix = ""; // 整数部分转化的结果
		String suffix = ""; // 小数部分转化的结果
		// 处理小数点后面的数
		if (rail.equals("00")) { // 如果小数部分为0
			suffix = "整";
		} else {
			suffix = digit[rail.charAt(0) - '0'] + "角"
					+ digit[rail.charAt(1) - '0'] + "分"; // 否则把角分转化出来
		}
		// 处理小数点前面的数
		char[] chDig = head.toCharArray(); // 把整数部分转化成字符数组
		char zero = '0'; // 标志'0'表示出现过0
		byte zeroSerNum = 0; // 连续出现0的次数
		for (int i = 0; i < chDig.length; i++) { // 循环处理每个数字
			int idx = (chDig.length - i - 1) % 4; // 取段内位置
			int vidx = (chDig.length - i - 1) / 4; // 取段位置
			if (chDig[i] == '0') { // 如果当前字符是0
				zeroSerNum++; // 连续0次数递增
				if (zero == '0') { // 标志
					zero = digit[0];
				} else if (idx == 0 && vidx > 0 && zeroSerNum < 4) {
					prefix += vunit[vidx - 1];
					zero = '0';
				}
				continue;
			}
			zeroSerNum = 0; // 连续0次数清零
			if (zero != '0') { // 如果标志不为0,则加上,例如万,亿什么的
				prefix += zero;
				zero = '0';
			}
			prefix += digit[chDig[i] - '0']; // 转化该数字表示
			if (idx > 0)
				prefix += hunit[idx - 1];
			if (idx == 0 && vidx > 0) {
				prefix += vunit[vidx - 1]; // 段结束位置应该加上段名如万,亿
			}
		}

		if (prefix.length() > 0)
			prefix += '圆'; // 如果整数部分存在,则有圆的字样
		return prefix + suffix; // 返回正确表示
	}

	/**
	 * 在字符串头部添加字符，使原字符串达到指定的长度
	 * 
	 * @param source
	 *            源字符串
	 * @param filling
	 *            填充字符
	 * @param lastLen
	 *            填充后的总长度
	 * @return 如果源字符串长度大于lastLen，返回原字符串，否则用filling填充源字符串后返回结果
	 */
	public static String fillString(String source, char filling, int lastLen) {
		StringBuffer temp = new StringBuffer();
		if (source.length() < lastLen) {
			int fillLen = lastLen - source.length();
			for (int i = 0; i < fillLen; i++) {
				temp.append(filling);
			}
		}
		temp.append(source);
		return temp.toString();
	}

	/**
	 * 格式化一个数字字符串为9，999，999.99的格式,如果字符串无法格式化返回0.00
	 * 
	 * @param money
	 *            数字字符串
	 * @return 格式化好的数字字符串
	 */
	public static String formatMoney(String money) {
		String formatMoney = "0.00";
		try {
			DecimalFormat myformat3 = new DecimalFormat();
			myformat3.applyPattern(",##0.00");
			double n = Double.parseDouble(money);
			formatMoney = myformat3.format(n);
		} catch (Exception ex) {
		}
		return formatMoney;
	}

	/**
	 * 格式化一个数字字符串为9，999，999.99的格式,如果字符串无法格式化返回0.00
	 * 
	 * @param money
	 *            数字字符串
	 * @return 格式化好的数字字符串
	 */
	public static String formatMoney(double money) {
		String formatMoney = "0.00";
		try {
			DecimalFormat myformat3 = new DecimalFormat();
			myformat3.applyPattern(",##0.00");
			formatMoney = myformat3.format(money);
		} catch (Exception ex) {
		}
		return formatMoney;
	}

	/**
	 * 从右边截取指定长度的字符串
	 * 
	 * @param src
	 *            源字符串
	 * @param len
	 *            长度
	 * @return 截取后的字符串
	 */
	public static String right(String src, int len) {
		String dest = src;
		if (src != null) {
			if (src.length() > len) {
				dest = src.substring(src.length() - len);
			}
		}

		return dest;
	}

	/**
	 * 得到以&分割的字符数组
	 * 
	 * @param str
	 * @return
	 */
	public static String[] strToArr(String str, int num) {
		if (null == str || "".equals(str))
			return null;
		String[] arr = str.split("&");
		int len = arr.length;
		if (len != num)
			return null;
		return arr;
	}

	/**
	 * 得到以&分割的字符数组
	 * 
	 * @param str
	 * @return
	 */
	public static String[] strToArr(String str) {
		if (null == str || "".equals(str))
			return null;
		String[] arr = str.split("&");
		return arr;
	}

	/**
	 * 得到以pix规定的符号分割的数组
	 * 
	 * @param str
	 * @param pix
	 * @return
	 */
	public static String[] strToArr(String str, String pix) {
		if (null == str || "".equals(str))
			return null;
		return str.split(pix);
	}

	/**
	 * 去除号码两边的空格
	 * 
	 * @param para
	 *            原字符串
	 * @param defalut
	 *            如果为空默认值
	 * @return
	 */
	public static String formatStr(String para, String defalut) {
		if (null != para) {
			para = para.trim();
			if ("".equals(para)) {
				return defalut;
			}
			return para;
		} else {
			return defalut;
		}
	}

	/**
	 * 去除号码两边的空格
	 * 
	 * @param para
	 *            原字符串
	 * @return
	 */
	public static String formatStr(String para) {
		if (null != para) {
			para = para.trim();
			if ("".equals(para)) {
				return null;
			}
			return para;
		} else {
			return null;
		}
	}

	public static String fullByZero(int in, int len) {
		String str = Integer.toString(in);
		if (null != str) {
			while (str.length() < len) {
				str = "0" + str;
			}
		}
		return str;
	}

	public static String fullByZero(String str, int len) {
		if (null != str) {
			while (str.length() < len) {
				str = "0" + str;
			}
		}
		return str;
	}

	public static String formatNumber(double amount) {
		DecimalFormat decimalFormat = new DecimalFormat("#");
		String temp = decimalFormat.format(amount);
		return temp;
	}

	public static String formatNumberZ(double amount) {
		DecimalFormat decimalFormat = new DecimalFormat("#0.00");
		String temp = decimalFormat.format(amount);
		return temp;
	}

	public static String formatNumberEx(double amount) {
		DecimalFormat decimalFormat = new DecimalFormat("#");
		String temp = decimalFormat.format(amount);
		if (temp.indexOf(".") == 0) {
			return "0%";
		}
		return temp + "%";
	}

	public static String getClassPath() {
		URL url = Utils.class.getClassLoader().getResource("");
		if (null == url) {
			return "";
		}
		return url.getPath();
	} 

	public static String getMid(String sid) {
		return sid + today("yyyyMMddHHmmssSSS");
	}
 
	public static Map<String, Integer> getPageStartAndEnd(int page,
			int pageTotal) {
		if (pageTotal <= 0) {
			pageTotal = 1;
		}
		Map<String, Integer> result = new HashMap<String, Integer>();
		int start = 0;
		int end = 0;

		if (pageTotal > 5) {
			if (page - 2 > 0) {
				start = page - 2;
				if (page + 2 <= pageTotal) {
					end = page + 2;
				} else {
					start = pageTotal - 5;
					end = pageTotal;
				}
			} else {
				start = 1;
				end = 5;
			}
		} else {
			start = 1;
			end = pageTotal;
		}

		result.put("start", start);
		result.put("end", end);
		return result;
	}
 
	public static String reNull(String str) {
		if (null == str) {
			return "0";
		}
		return str;
	}
 

	public static String getEmailDomain(String email) {
		if (Utils.isEmpty(email)) {
			return "";
		}
		if (!Utils.isEmail(email)) {
			return "";
		}
		String[] mailArray = email.split("@");
		if (mailArray.length != 2) {
			return "";
		}
		if (mailArray[1].equals("gmail.com")) {
			return "http://www.gmail.com";
		}

		return "http://mail." + mailArray[1];
	}

	public static String getVerifyCode(int len) {
		StringBuffer temp = new StringBuffer();
		String arr = "abcdefghijklmnopkrstuvwxyzABCDEFGHIJKLMNOPKRSTUVWXYZ0123456789";
		String[] array = arr.split("");
		while (temp.length() < len) {
			int random = Utils.intRandom(62);
			if (Utils.isNotEmpty(array[random])) {
				temp.append(array[random]);
			}
		}
		return temp.toString();
	}
}
