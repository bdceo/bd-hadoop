/**
 * HadoopCounter.java
 * com.bdsoft.hadoop.counter
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.counter;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 计数器和输出压缩
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-30
 * @version  1.0.0
 */
public class CounterAndCompress extends Configured implements Tool {

	static String jobName = "counter";

	static CounterAndCompress jobClass = new CounterAndCompress();

	/**
	 * job运行入口
	 */
	public static void main(String[] args) throws Exception {
		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/" + jobName + "/";
		String out = in + "out/";

		String[] arg4job = { in, out };
		arg4job = (args != null && args.length == 2) ? args : arg4job;
		int ec = ToolRunner.run(new Configuration(), jobClass, arg4job);

		System.out.println(jobName + " job run finish > " + ec);
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Path mypath = new Path(args[1]); // 输出路径
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {// 清空输出路径
			hdfs.delete(mypath, true);
		}

		// 初始job名称及运行主类
		Job job = Job.getInstance(conf, jobName);
		job.setJarByClass(jobClass.getClass());

		// 设置输入输出路径
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// 设置计数
		job.setMapperClass(MyCounterMapper.class);

		// 计数并输出压缩
		job.setMapperClass(CounterAndCompressionMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		FileOutputFormat.setCompressOutput(job, true);
		FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class CounterAndCompressionMapper extends Mapper<LongWritable, Text, Text, Text> {

		/**
		 * 自定义计数器枚举，错误记录条数
		 */
		private static enum LOG_PROCESS_COUNTER {
			BAD_RECORDS;
		}

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			List<String> list = ParseTVData.transData(value.toString());
			if (list.size() == 0) {
				context.getCounter("ErrorRecordCounter", "ERROR_Record_TVData").increment(1);
				context.getCounter(LOG_PROCESS_COUNTER.BAD_RECORDS).increment(1);
			} else {
				//输出解析后的用户数据
				for (String validateRecord : list) {
					context.write(new Text(validateRecord), new Text(""));
				}
			}

		}
	}

	public static class MyCounterMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		/*
		jim    1       28
		kate   0       26
		tom    1
		kaka   1       22
		lily   0       29      22
		  */

		/**
		 * 自定义计数器枚举，错误记录条数
		 */
		private static enum LOG_PROCESS_COUNTER {
			BAD_RECORDS_SHORT, BAD_RECORDS_LONG;
		}

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] records = value.toString().split("\t");

			if (records.length > 3) {
				context.getCounter("ErrorCounter", "toolong").increment(1);
				context.getCounter(LOG_PROCESS_COUNTER.BAD_RECORDS_LONG).increment(1);
			} else {
				context.getCounter("ErrorCounter", "tooshort").increment(1);
				context.getCounter(LOG_PROCESS_COUNTER.BAD_RECORDS_SHORT).increment(1);
			}

		}
	}
}
