package com.bdsoft.hadoop.crime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configured;

/**
 * 旧金山犯罪数据分析基础信息
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */
public class CrimeJobBase extends Configured   {

	// 一行数据：130227859,NON-CRIMINAL,LOST PROPERTY,Saturday,03/30/2013 07:00:00 AM +0000,12:00,TENDERLOIN,NONE,JONES ST / ELLIS ST,-122.412784096502,37.7848656939526,"(37.7848656939526, -122.412784096502)"

	// 犯罪类型
	protected static final int CATEGORY_COLUMN_INDEX = 1;
	// 周几
	protected static final int DAY_OF_WEEK_COLUMN_INDEX = 3;
	// 日期
	protected static final int DATE_COLUMN_INDEX = 4;
	// 区域
	protected static final int DISTRICT_COLUMN_INDEX = 6;

	// 元数据中的日期格式
	protected static final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	// map/reduce 中输出的日期格式
	protected static final SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd");

	/**
	 * 解析字符串格式的日期
	 * 
	 * @param value 03/30/2013 07:00:00 AM +0000
	 * @return MM/dd/yyyy
	 * @throws ParseException
	 */
	protected static Date getDate(String value) throws ParseException {
		Date retVal = null;
		String[] dp = value.split(" ");
		if (dp.length > 0) {
			retVal = df.parse(dp[0]);
		}
		return retVal;
	} 

}
