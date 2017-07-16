/**
 * DataFile.java
 * com.bdsoft.hadoop.crime
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.crime;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.opencsv.CSVReader;

/**
 * 读取/提取mapreduce的输出结果数据
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */
public abstract class DataFile {

	/**
	 * 从 map/reduce 的输出结果中，提取key值集合
	 * <p>
	 *
	 * @param fn hdfs上的文件路径
	 * @param fs 文件系统
	 * @return key值集合
	 * @throws IOException
	 */
	public static List<String> extractKeys(String fn, FileSystem fs) throws IOException {
		List<String> retVal = new ArrayList<String>();

		FSDataInputStream in = fs.open(new Path(fn));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		while (line != null) {
			String[] lp = line.split("\t");
			if (lp.length > 0) {
				retVal.add(lp[0]);// 提取每行的第一个字段
			}
			line = br.readLine();
		}
		br.close();

		Collections.sort(retVal);
		return retVal;
	}

	/**
	 * 将csv文件格式的每行内容转换成数组返回
	 * <p>
	 *
	 * @param line 读取一行数据
	 * @return array 数组
	 * @throws IOException
	 */
	public static String[] getColumns(String line) throws IOException {
		CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(line.getBytes())));
		String[] retVal = reader.readNext();
		reader.close();
		return retVal;
	}
	
	public static void main(String[] args) throws Exception{
		String line = "136053509,LARCENY/THEFT,PETTY THEFT OF PROPERTY,Sunday,03/24/2013 07:00:00 AM +0000,00:43,NORTHERN,NONE,1200 Block of POLK ST,-122.420262739126,37.7882627926689,\"(37.7882627926689, -122.420262739126)\"";
		String[] cols = getColumns(line);
		System.out.println(cols.length);
		for(String col : cols){
			System.out.println(col);
		}
	}

}
