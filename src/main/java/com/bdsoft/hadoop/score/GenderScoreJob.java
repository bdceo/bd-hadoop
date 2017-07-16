/**
 * GenderScoreJob.java
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
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 统计出每个年龄段的 男、女 学生的最高分
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-14
 * @version 1.0.0
 */
public class GenderScoreJob extends Configured implements Tool {

	static String jobName = "bestscore";

	static GenderScoreJob jobClass = new GenderScoreJob();

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
		job.setReducerClass(MyReduce.class);

		// 设定reduce数量及分区逻辑
		job.setNumReduceTasks(3);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		// 设置合并,分区
		job.setCombinerClass(MyCombiner.class);
		job.setPartitionerClass(MyPartitioner.class);

		// 设置reduce
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class MyMapper extends Mapper<Object, Text, Text, Text> {

		/**
		 * 统计出每个年龄段的 男、女 学生的最高分 按需求将数据集解析为 key=gender，value=name+age+score，然后输出。
		 */
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			// 一行：Alice<tab>23<tab>female<tab>45

			String[] lines = value.toString().split("<tab>");

			// 输出：key=性别，value=姓名[\t]年龄[\t]分数
			String gender = lines[2];
			String nameAgeScore = lines[0] + "\t" + lines[1] + "\t" + lines[3];

			context.write(new Text(gender), new Text(nameAgeScore));
		}
	}

	/**
	 * 多个map执行时，有助于优化，reduce之前，执行一次合并 合并 Mapper 输出结果，然后输出给 Reducer。
	 */
	public static class MyCombiner extends Reducer<Text, Text, Text, Text> {
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			int maxScore = Integer.MIN_VALUE;
			String name = "", age = "";
			int score = 0;
			// 提取最大分
			for (Text val : values) {
				String[] nameAgeScores = val.toString().split("\t");
				score = Integer.parseInt(nameAgeScores[2]);
				if (score > maxScore) {
					name = nameAgeScores[0];
					age = nameAgeScores[1];
					maxScore = score;
				}
			}
			String nameAgeScore = name + "\t" + age + "\t" + maxScore;
			context.write(key, new Text(nameAgeScore));
		}
	}

	/**
	 * 分区,按年龄段，将结果指定给不同的 Reduce 执行。
	 */
	public static class MyPartitioner extends Partitioner<Text, Text> {

		@Override
		public int getPartition(Text key, Text value, int reduceNum) {

			int age = Integer.parseInt(value.toString().split("\t")[1]);

			// 根据年龄进行分区
			if (reduceNum == 0 || age <= 20) {
				return 0;
			} else if (age > 20 && age <= 50) {
				return 1 % reduceNum;
			} else {
				return 2 % reduceNum;
			}
		}
	}

	public static class MyReduce extends Reducer<Text, Text, Text, Text> {

		/**
		 * 统计出每个年龄段的 男、女 学生的最高分
		 */
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			int maxScore = Integer.MIN_VALUE;
			String name = "", age = "", gender = key.toString();
			int score = 0;

			for (Text val : values) {
				String[] nameAgeScores = val.toString().split("\t");
				score = Integer.parseInt(nameAgeScores[2]);
				if (score > maxScore) {
					name = nameAgeScores[0];
					age = nameAgeScores[1];
					maxScore = score;
				}
			}
			String bestInfo = "age=" + age + "\tgender=" + gender + "\tscore=" + maxScore;
			context.write(new Text(name), new Text(bestInfo));
		}
	}
}
