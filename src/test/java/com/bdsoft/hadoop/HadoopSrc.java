/**
 * HadoopSrc.java
 * com.bdsoft.hadoop.bddm
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop;

import org.apache.hadoop.mapred.Counters.Counter;
import org.apache.hadoop.mapreduce.FileSystemCounter;
import org.apache.hadoop.mapreduce.TaskCounter;

/**
 * 
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-30
 * @version  1.0.0
 */
public class HadoopSrc {
 
	public static void main(String[] args) {

		// 计数器
		Class counter = Counter.class;
		TaskCounter.MAP_INPUT_RECORDS.name(); // 任务计数器
		FileSystemCounter.BYTES_READ.name(); // 文件系统计数器
		
	
	}

}
