/**
 * TvPlayJob.java
 * com.bdsoft.hadoop.tv
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.tv;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 按网站类别 统计每个电视剧的每个指标的总量
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-14
 * @version 1.0.0
 */
public class TvPlayJob extends Configured implements Tool {

	static String jobName = "tv";

	static TvPlayJob jobClass = new TvPlayJob();

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
		job.setMapOutputValueClass(TvPlayWritable.class);
		// 输入格式
		job.setInputFormatClass(TvPlayInputFormat.class);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// 自定义输出
		MultipleOutputs.addNamedOutput(job, "youku", TextOutputFormat.class,
				Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "souhu", TextOutputFormat.class,
				Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "tudou", TextOutputFormat.class,
				Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "iqiyi", TextOutputFormat.class,
				Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "xunlei", TextOutputFormat.class,
				Text.class, IntWritable.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class MyMapper extends
			Mapper<Text, TvPlayWritable, Text, TvPlayWritable> {

		/**
		 * 输入数据：5个网站的 每天电视剧的 播放量 收藏数 评论数 踩数 赞数
		 */
		@Override
		public void map(Text key, TvPlayWritable value, Context context)
				throws IOException, InterruptedException {
			// 电视剧 播放数 收藏数 评论数 踩数 赞数
			// 你迷上了我 1 37421 16 6 0 0
			// 输入：key=你迷上了我[\t]1，value=[播放数|收藏数|评论数|踩数|赞数]
			// 直接输出 key/value
			context.write(key, value);
		}

	}

	public static class MyReduce extends
			Reducer<Text, TvPlayWritable, Text, Text> {

		private MultipleOutputs<Text, Text> mout;

		private Text okey = new Text();
		private Text ovalue = new Text();

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			mout = new MultipleOutputs<Text, Text>(context);
		}

		/**
		 * 输出数据：按网站类别 统计每个电视剧的每个指标的总量
		 */
		@Override
		protected void reduce(Text key, Iterable<TvPlayWritable> values,
				Context context) throws IOException, InterruptedException {

			// 按网站类别 统计每个电视剧的每个指标的总量
			int plyNum = 0, colNum = 0, cmnNum = 0, caiNum = 0, zanNum = 0;
			for (TvPlayWritable tpw : values) {
				plyNum += tpw.getPlyNum();
				colNum += tpw.getColNum();
				cmnNum += tpw.getCmnNum();
				caiNum += tpw.getCaiNum();
				zanNum += tpw.getZanNum();
			}

			String[] records = key.toString().split("\t");

			// 输出：key=电视剧名称，value=统计数据
			okey.set(records[0]);
			ovalue.set(plyNum + "\t" + colNum + "\t" + cmnNum + "\t" + caiNum
					+ "\t" + zanNum);

			// 网站类别：1优酷2搜狐3土豆4爱奇艺5迅雷看看
			int source = Integer.parseInt(records[1]);
			switch (source) {
			case 1:
				mout.write("youku", okey, ovalue);
				break;
			case 2:
				mout.write("souhu", okey, ovalue);
				break;
			case 3:
				mout.write("tudou", okey, ovalue);
				break;
			case 4:
				mout.write("iqiyi", okey, ovalue);
				break;
			case 5:
				mout.write("xunlei", okey, ovalue);
				break;
			}
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			mout.close();
		}
	}
}
