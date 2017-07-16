/**
 * CallLogJob.java
 * com.bdsoft.hadoop.call
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.call;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;
import com.bdsoft.hadoop.util.BDMultipleOutputFormat;

/**
 * 通话记录 统计每个月每个家庭成员给自己打电话的次数，并按月份输出到不同文件夹
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-12
 * @version 1.0.0
 */
public class CallLogJob extends Configured implements Tool {

	static String jobName = "calllog";

	static CallLogJob jobClass = new CallLogJob();

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
		Configuration conf = new Configuration();// 读取配置文件

		// job运行前，输出路径不允许存在
		Path mypath = new Path(args[1]); // 输出路径
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {// 清空输出路径
			hdfs.delete(mypath, true);
		}

		Job job = new Job(conf, jobName);
		// 设置主类
		job.setJarByClass(jobClass.getClass());

		FileInputFormat.addInputPath(job, new Path(args[0]));// 输入路径
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // 输出路径

		// 设置mapper和reduce
		job.setMapperClass(CallLogMapper.class);
		job.setReducerClass(CallLogReduce.class);

		// 设置最终reduce的输出键值类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// 设置输入输出格式
		job.setInputFormatClass(ExcelInputFormat.class);
		job.setOutputFormatClass(CallLogOutputFormat.class);

		return job.waitForCompletion(true) ? 0 : 1; // 提交job任务
	}

	public static class CallLogMapper extends
			Mapper<LongWritable, Text, Text, Text> {

		private Text okey = new Text();
		private Text ovalue = new Text();

		/**
		 * 统计每个月每个家庭成员给自己打电话的次数，并按月份输出到不同文件夹
		 */
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 一行： 1.0, 老爸, 13999123786, 2014-12-20

			String line = value.toString();
			String[] records = line.split("[\\s]+");
			String[] dates = records[3].split("-");// 截取月份

			// 输出key：昵称+月份，value：手机号
			okey.set(records[1] + "\t" + dates[1] + "月");
			ovalue.set(records[2]);

			context.write(okey, ovalue);
		}
	}

	public static class CallLogReduce extends Reducer<Text, Text, Text, Text> {

		private Text ovalue = new Text();

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			Text outKey = values.iterator().next();

			int sum = 0; // 每个月每个家庭成员给我打电话的总数
			for (Text t : values) {
				sum++;
			}

			// 输出：key=家庭成员名称[\t]月份，value=手机号\t通话次数
			ovalue.set(outKey + "\t" + sum + "次通话");

			context.write(key, ovalue);
		}
	}

	/**
	 * 按月份输出
	 */
	public static class CallLogOutputFormat extends BDMultipleOutputFormat<Text, Text>{
//			MailMultipleOutputFormat<Text, Text> {

		@Override
		protected String genFileNmaeForOutput(Text key, Text value,
				Configuration conf) {
			// reduce 输出key=昵称\t月份 则文件名：月份.txt
			return key.toString().split("\t")[1] + ".txt";
		}

	}

}
