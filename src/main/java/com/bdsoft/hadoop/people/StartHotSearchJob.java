/**
 * StartHotSearchJob.java
 * com.bdsoft.hadoop.people
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.people;

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
 * 使用明星搜索指数数据，分别统计出搜索指数最高的男明星和女明星
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-15
 * @version  1.0.0
 */
public class StartHotSearchJob extends Configured implements Tool {

	static String jobName = "people";

	static StartHotSearchJob jobClass = new StartHotSearchJob();

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
		job.setMapOutputValueClass(Text.class);

		// 指定合并，分区，reduce数量:按性别分
		job.setCombinerClass(MyCombiner.class);
		job.setPartitionerClass(MyPartitioner.class);
		job.setNumReduceTasks(2);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class MyMapper extends Mapper<Object, Text, Text, Text> {

		private Text okey = new Text();
		private Text ovalue = new Text();

		/**
		 * 统计出搜索指数最高的男明星和女明星，-->以性别做key
		 */
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			// 一行：angelababy	female	55083
			String[] tokens = value.toString().split("\t");

			// 输出：key=性别，value=名称+热值
			okey.set(tokens[1].trim());
			ovalue.set(tokens[0] + "\t" + tokens[2]);

			context.write(okey, ovalue);
		}
	}

	/**
	 * 多个map执行时，有助于优化，reduce之前，执行一次合并
	 */
	public static class MyCombiner extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			int maxHot = Integer.MIN_VALUE;
			String name = "";
			int hot = 0;
			// 提取最热值
			for (Text val : values) {
				String[] nameHot = val.toString().split("\t");
				hot = Integer.parseInt(nameHot[1]);
				if (hot > maxHot) {
					name = nameHot[0];
					maxHot = hot;
				}
			}
			String nameHot = name + "\t" + maxHot;
			context.write(key, new Text(nameHot));
		}
	}

	/**
	 * 分区
	 */
	public static class MyPartitioner extends Partitioner<Text, Text> {

		@Override
		public int getPartition(Text key, Text value, int reduceNum) {
			// 根据性别进行分区
			String sex = key.toString();
			if (reduceNum == 0 || "male".equals(sex)) {
				return 0;
			} else if ("female".equals(sex)) {
				return 1 % reduceNum;
			} else {
				return 2 % reduceNum;
			}
		}
	}

	public static class MyReduce extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			int maxHot = Integer.MIN_VALUE;
			String name = "";
			int hot = 0;
			// 提取最大热值
			for (Text val : values) {
				String[] nameHot = val.toString().split("\t");
				hot = Integer.parseInt(nameHot[1]);
				if (hot > maxHot) {
					name = nameHot[0];
					maxHot = hot;
				}
			}
			String sexHot = key.toString() + "\t" + maxHot;
			// 输出：key=姓名，value=性别[\t]热值
			context.write(new Text(name), new Text(sexHot));
		}
	}
}
