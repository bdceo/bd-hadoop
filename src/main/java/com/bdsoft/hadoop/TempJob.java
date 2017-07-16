/**
 * TempJob.java
 * com.bdsoft.hadoop
 */
package com.bdsoft.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.score.ScoreWritable;

/**
 * job模板
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-12
 * @version 1.0.0
 */
public class TempJob extends Configured implements Tool {

	static String jobName = "xxx";

	static TempJob jobClass = new TempJob();

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

		// 设置map阶段的输出压缩		
		//		conf.setBoolean("mapred.compress.map.output", true);
		//		conf.setClass("mapred.map.output.compression.codec", GzipCodec.class, CompressionCodec.class);

		// 设置mapred整个结果的输出压缩
		//		FileOutputFormat.setCompressOutput(job, true);
		//		FileOutputFormat.setOutputCompressorClass(job, CompressionCodec.class);		
		conf.setBoolean("mapred.output.compress", true);
		conf.setStrings("mapred.output.compression.type", "BLOCK"); // 按块压缩
		conf.setClass("mapred.output.compression.codec", GzipCodec.class, CompressionCodec.class);

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
		job.setMapOutputValueClass(ScoreWritable.class);
		// job.setInputFormatClass(null);// 输入格式

		// 设置：合并，分区，reduce数量
		// job.setCombinerClass(MyCombiner.class);
		// job.setPartitionerClass(MyPartitioner.class);
		// job.setNumReduceTasks(2);
		// job.setSortComparatorClass(cls); key 排序
		// job.setGroupingComparatorClass(cls);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileOutputFormat.setCompressOutput(job, true);
		// job.setOutputFormatClass(null); // 输出格式
		// 自定义输出
		// MultipleOutputs.addNamedOutput(job, "fans", TextOutputFormat.class,
		// Text.class, IntWritable.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

		}
	}

	public static class MyReduce extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
				InterruptedException {

		}
	}
}
