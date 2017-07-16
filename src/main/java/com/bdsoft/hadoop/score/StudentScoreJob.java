/**
 * StudentScoreJob.java
 * com.bdsoft.hadoop.score
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.score;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 统计每个学生的总分，平均分
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-12
 * @version 1.0.0
 */
public class StudentScoreJob extends Configured implements Tool {

	static String jobName = "stuscore";

	static StudentScoreJob job = new StudentScoreJob();

	/**
	 * job运行配置入口
	 */
	public static void main(String[] args) throws Exception {
		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/" + jobName + "/";
		String out = in + "out/";

		String[] arg4job = { in, out };
		arg4job = (args != null && args.length == 2) ? args : arg4job;
		int ec = ToolRunner.run(new Configuration(), job, arg4job);

		System.out.println(jobName + " job run finish > " + ec);
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();// 读取配置文件

		// job运行前，输出路径不允许存在
		Path mypath = new Path(args[1]); // 输出路径
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {// 清空输出路径
			hdfs.delete(mypath, true);
		}

		Job job = new Job(conf, jobName); // 定义一个job任务
		job.setJarByClass(job.getClass());// 设置主类

		FileInputFormat.addInputPath(job, new Path(args[0]));// 输入路径
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // 输出路径

		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReduce.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(ScoreWritable.class);

		// 设置自定义输入格式
		job.setInputFormatClass(ScoreInputFormat.class);

		return job.waitForCompletion(true) ? 0 : 1; // 提交job任务
	}

	public static class MyMapper extends
			Mapper<Text, ScoreWritable, Text, ScoreWritable> {

		@Override
		public void map(Text key, ScoreWritable value, Context context)
				throws IOException, InterruptedException {
			System.out.println("call-->MyMapper.map()");
			context.write(key, value);
		}
	}

	public static class MyReduce extends
			Reducer<Text, ScoreWritable, Text, Text> {

		private Text text = new Text();

		@Override
		protected void reduce(Text key, Iterable<ScoreWritable> values,
				Context context) throws IOException, InterruptedException {
			System.out.println("call-->MyReduce.reduce()");
			float total = 0f; // 每个学生的总分
			float avg = 0f; // 每个学生的平均分
			for (ScoreWritable val : values) {
				total = val.getTotal();
				avg = total / ScoreWritable.subject;
			}

			text.set("总分=" + total + "\t平均分=" + avg);
			context.write(key, text);
		}
	}

}
