/**
 * TempLinkJob.java
 * com.bdsoft.hadoop
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 链接mapreduce
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */

public class TempLinkJob extends Configured implements Tool {

	static String jobName = "xxx";

	static TempLinkJob jobClass = new TempLinkJob();

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
		return 0;
	}

	// 【线性链式】:对于任意一个MapReduce作业，Map和Reduce阶段可以有无限个Mapper，但是Reduce只能有一个。所以包含多个Reduce的作业，不能使用 ChainMapper/ChainReduce来完成
	public void link(String args[]) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "chain-job");
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[0]));

		// map1阶段
		Configuration map1conf = new Configuration(false);
		ChainMapper.addMapper(job, Mapper1.class, LongWritable.class, Text.class, Text.class, Text.class, map1conf);
		// map2阶段
		Configuration map2conf = new Configuration(false);
		ChainMapper.addMapper(job, Mapper2.class, LongWritable.class, Text.class, Text.class, Text.class, map2conf);
		// reduce阶段
		Configuration reduceconf = new Configuration(false);
		ChainReducer.setReducer(job, Reduce.class, Text.class, Text.class, Text.class, Text.class, reduceconf);
		// map3阶段
		Configuration map3conf = new Configuration(false);
		ChainMapper.addMapper(job, Mapper3.class, LongWritable.class, Text.class, Text.class, Text.class, map3conf);
		// map4阶段
		Configuration map4conf = new Configuration(false);
		ChainMapper.addMapper(job, Mapper4.class, LongWritable.class, Text.class, Text.class, Text.class, map4conf);

		job.waitForCompletion(true);
	}

	// 【依赖关系式】
	public void depend(String[] args) throws Exception {
		Configuration conf1 = new Configuration();
		Job job1 = new Job(conf1, "job#1");

		Configuration conf2 = new Configuration();
		Job job2 = new Job(conf2, "job#2");

		Configuration conf3 = new Configuration();
		Job job3 = new Job(conf3, "job#3");

		ControlledJob cJob1 = new ControlledJob(conf1);
		cJob1.setJob(job1);
		ControlledJob cJob2 = new ControlledJob(conf2);
		cJob2.setJob(job2);
		ControlledJob cJob3 = new ControlledJob(conf3);
		cJob3.setJob(job3);

		// 依赖关系
		cJob3.addDependingJob(cJob1);
		cJob3.addDependingJob(cJob2);

		// 启动任务
		JobControl jc = new JobControl("job-1-2-3");
		jc.addJob(cJob1);
		jc.addJob(cJob2);
		jc.addJob(cJob3);

		new Thread(jc).start();

		while (true) {
			if (jc.allFinished()) {
				jc.stop();
				break;
			}
		}
	}

	// 【迭代式】 缺点：Job 对象重复创建，代价将非常高;数据都要写入本地，然后从本地读取，I/O和网络传输的代价比较大。
	public void diedai(String[] args) throws Exception {
		Configuration conf = new Configuration();

		// 第一个job
		Job job1 = new Job(conf, "job#1");
		FileInputFormat.addInputPath(job1, new Path(args[0]));
		FileOutputFormat.setOutputPath(job1, new Path(args[1]));
		job1.waitForCompletion(true);

		// 第二个job
		Job job2 = new Job(conf, "job#2");
		FileInputFormat.addInputPath(job2, new Path(args[0]));
		FileOutputFormat.setOutputPath(job2, new Path(args[1]));
		job2.waitForCompletion(true);

		// 第三个job
		Job job3 = new Job(conf, "job#3");
		FileInputFormat.addInputPath(job3, new Path(args[0]));
		FileOutputFormat.setOutputPath(job3, new Path(args[1]));
		job3.waitForCompletion(true);
	}

	public static class Mapper1 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		}
	}

	public static class Mapper2 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
		}
	}

	public static class Mapper3 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		}
	}

	public static class Mapper4 extends Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		}
	}
}
