package com.bdsoft.hadoop.weather;

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
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 统计美国各个气象站30年来的平均气温
 */
public class USTemperature extends Configured implements Tool {

	/**
	 * 在hadoop运行一个job
	 */
	public static void main(String[] args) throws Exception {

		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/weather/";
		String out = in + "out/";
		String[] arg4job = { in, out };

		arg4job = (args != null && args.length == 2) ? args : arg4job;

		int ec = ToolRunner.run(new Configuration(), new USTemperature(),
				arg4job);

		System.out.println("job-temperature run finish > " + ec);
	}

	/**
	 * job运行配置
	 */
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();// 读取配置文件

		// job运行前，输出路径不允许存在
		Path mypath = new Path(args[1]); // 输出路径
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {// 清空输出路径
			hdfs.delete(mypath, true);
		}

		Job job = new Job(conf, this.getClass().getName()); // 定义一个job任务
		job.setJarByClass(USTemperature.class);// 设置主类

		FileInputFormat.addInputPath(job, new Path(args[0]));// 输入路径
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // 输出路径

		job.setMapperClass(TempMapper.class);// 设置mapper和reduce
		job.setReducerClass(TempReduce.class);

		// 设置map和reduce的输出键值类型，一致！
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		return job.waitForCompletion(true) ? 0 : 1; // 提交job任务
	}

	/**
	 * 读取气象数据 2005 01 01 01 17 -11 10122 220 72 7 0 -9999
	 * 
	 * 1998 #year 03 #month 09 #day 17 #hour 11 #temperature -100 #dew 10237
	 * #pressure 60 #wind_direction 72 #wind_speed 0 #sky_condition 0 #rain_1h
	 * -9999 #rain_6h
	 */
	// Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
	public static class TempMapper extends
			Mapper<LongWritable, Text, Text, IntWritable> {

		/**
		 * 解析气象站数据
		 * 
		 * 输入：key=每行偏移量，value=气象站数据
		 * 
		 * 输出：key=气象站编号，value=气温
		 */
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = value.toString(); // 每行的气象数据
			int temp = Integer.parseInt(line.substring(14, 19).trim());// 气温

			if (temp != -9999) { // 无效数据
				// 文件的输入分片
				FileSplit fs = (FileSplit) context.getInputSplit();

				// 截取文件名，获取气象站编号 ： 30yr_03103.dat
				String stationId = fs.getPath().getName().substring(5, 10);
				// String stationId = "xxx"; // mrUnit 测试时放开使用

				context.write(new Text(stationId), new IntWritable(temp));
			}
		}
	}

	/**
	 * 计算美国各个气象站的平均气温
	 */
	public static class TempReduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		private IntWritable result = new IntWritable();

		/**
		 * 统计美国各个气象站的平均气温
		 * 
		 * 输入key=气象站编号 value=气温列表
		 * 
		 * 输出 key=气象站编号 value=平均气温
		 */
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			int sum = 0;
			int count = 0;
			for (IntWritable value : values) {
				sum += value.get();
				count++;
			}
			result.set(sum / count);

			context.write(key, result);
		}

	}

}
