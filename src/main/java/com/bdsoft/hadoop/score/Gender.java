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

/**
 * 
 * @function 统计不同年龄段内 男、女最高分数
 * @author 小讲
 * 
 */
public class Gender extends Configured implements Tool {
	/*
	 * 
	 * @function Mapper 解析输入数据，然后按需求输出
	 * 
	 * @input key=行偏移量 value=学生数据
	 * 
	 * @output key=gender value=name+age+score
	 */
	public static class PCMapper extends Mapper<Object, Text, Text, Text> {

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String[] tokens = value.toString().split("<tab>");// 使用分隔符<tab>，将数据解析为数组
																// tokens
			String gender = tokens[2].toString();// 性别
			String nameAgeScore = tokens[0] + "\t" + tokens[1] + "\t"
					+ tokens[3];
			// 输出 key=gender value=name+age+score
			context.write(new Text(gender), new Text(nameAgeScore));
		}
	}

	public static class MyHashPartitioner extends Partitioner<Text, Text> {
		/** Use {@link Object#hashCode()} to partition. */
		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			return (key.hashCode()) % numReduceTasks;
		}

	}

	/**
	 * 
	 * @function Partitioner 根据 age 选择 reduce 分区
	 * 
	 */
	public static class PCPartitioner extends Partitioner<Text, Text> {

		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			// TODO Auto-generated method stub
			String[] nameAgeScore = value.toString().split("\t");
			String age = nameAgeScore[1];// 学生年龄
			int ageInt = Integer.parseInt(age);// 按年龄段分区

			// 默认指定分区 0
			if (numReduceTasks == 0)
				return 0;

			// 年龄小于等于20，指定分区0
			if (ageInt <= 20) {
				return 0;
			}
			// 年龄大于20，小于等于50，指定分区1
			if (ageInt > 20 && ageInt <= 50) {

				return 1 % numReduceTasks;
			}
			// 剩余年龄，指定分区2
			else
				return 2 % numReduceTasks;
		}
	}

	/**
	 * 
	 * @function 定义Combiner 合并 Mapper 输出结果
	 * 
	 */
	public static class PCCombiner extends Reducer<Text, Text, Text, Text> {
		private Text text = new Text();

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			int maxScore = Integer.MIN_VALUE;
			String name = " ";
			String age = " ";
			int score = 0;
			for (Text val : values) {
				String[] valTokens = val.toString().split("\\t");
				score = Integer.parseInt(valTokens[2]);
				if (score > maxScore) {
					name = valTokens[0];
					age = valTokens[1];
					maxScore = score;
				}
			}
			text.set(name + "\t" + age + "\t" + maxScore);
			context.write(key, text);
		}
	}

	/*
	 * 
	 * @function Reducer 统计出 不同年龄段、不同性别 的最高分 input key=gender
	 * value=name+age+score output key=name value=age+gender+score
	 */
	static class PCReducer extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			int maxScore = Integer.MIN_VALUE;
			String name = " ";
			String age = " ";
			String gender = " ";
			int score = 0;
			// 根据key，迭代 values 集合，求出最高分
			for (Text val : values) {
				String[] valTokens = val.toString().split("\\t");
				score = Integer.parseInt(valTokens[2]);
				if (score > maxScore) {
					name = valTokens[0];
					age = valTokens[1];
					gender = key.toString();
					maxScore = score;
				}
			}
			context.write(new Text(name), new Text("age- " + age + "\t"
					+ gender + "\tscore-" + maxScore));
		}
	}

	/**
	 * @function 任务驱动方法
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();// 读取配置文件

		Path mypath = new Path(args[1]);
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {
			hdfs.delete(mypath, true);
		}

		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "gender");// 新建一个任务
		job.setJarByClass(Gender.class);// 主类
		job.setMapperClass(PCMapper.class);// Mapper
		job.setReducerClass(PCReducer.class);// Reducer

		job.setPartitionerClass(MyHashPartitioner.class);
//		 job.setPartitionerClass(PCPartitioner.class);//设置Partitioner类
		job.setNumReduceTasks(3);// reduce个数设置为3

		job.setMapOutputKeyClass(Text.class);// map 输出key类型
		job.setMapOutputValueClass(Text.class);// map 输出value类型

		job.setCombinerClass(PCCombiner.class);// 设置Combiner类

		job.setOutputKeyClass(Text.class);// 输出结果 key类型
		job.setOutputValueClass(Text.class);// 输出结果 value 类型

		FileInputFormat.addInputPath(job, new Path(args[0]));// 输入路径
		FileOutputFormat.setOutputPath(job, new Path(args[1]));// 输出路径
		job.waitForCompletion(true);// 提交任务
		return 0;
	}

	/**
	 * @function main 方法
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String[] args0 = { "hdfs://master:9000/bdceo/bestscore/gender.txt",
				"hdfs://master:9000/bdceo/bestscore/out/" };
		int ec = ToolRunner.run(new Configuration(), new Gender(), args0);
		System.exit(ec);
	}
}
