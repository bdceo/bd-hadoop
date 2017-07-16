/**
 * MailStatJob.java
 * com.bdsoft.hadoop.mail
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.mail;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 统计邮箱出现次数并按照邮箱的类别，将这些邮箱分别输出到不同文件路径下
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-12
 * @version 1.0.0
 */
public class MailStatJob extends Configured implements Tool {

	static String jobName = "mail";

	static MailStatJob jobClass = new MailStatJob();

	/**
	 * job运行配置入口
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

		// 设置mapper
		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		return job.waitForCompletion(true) ? 0 : 1; // 提交job任务
	}

	public static class MyMapper extends
			Mapper<LongWritable, Text, Text, IntWritable> {

		private IntWritable ovalue = new IntWritable(1);

		/**
		 * 统计邮箱出现次数并按照邮箱的类别，将这些邮箱分别输出到不同文件路径下
		 */
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 每一行：fxh852@163.com

			// 输出：key=邮箱，value=1
			context.write(value, ovalue);
		}
	}

	public static class MyReduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		private IntWritable ovalue = new IntWritable();
		// 多文件输出
		private MultipleOutputs<Text, IntWritable> outputs;

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			outputs = new MultipleOutputs<Text, IntWritable>(context);
		}

		/**
		 * 统计邮箱出现次数并按照邮箱的类别，将这些邮箱分别输出到不同文件路径下
		 */
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int begin = key.toString().indexOf("@");
			int end = key.toString().indexOf(".");
			if (begin >= end) { // 格式不合法，忽略不处理
				return;
			}

			// 提取邮箱服务类别
			String mailService = key.toString().substring(begin + 1, end);
			int sum = 0;
			for (IntWritable v : values) {
				sum += v.get();
			}
			ovalue.set(sum);

			// 单文件输出，直接context.write，此处使用的是多文件输出
			// context.write(key, ovalue);

			// Parameters:
			// key the key
			// value the value
			// baseOutputPath base-output path to write the record to. Note:
			// Framework will generate unique filename for the baseOutputPath
			outputs.write(key, ovalue, mailService);
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			outputs.close();
		}
	}
}
