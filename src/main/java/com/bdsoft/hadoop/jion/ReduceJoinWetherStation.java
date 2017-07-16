package com.bdsoft.hadoop.jion;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;
import com.bdsoft.hadoop.seri.TextPair;

/**
 * Reduce端连接
 * 
 * @author 丁辰叶
 * @date 2016-02-15
 * @version 1.0.0
 */
public class ReduceJoinWetherStation extends Configured implements Tool {

	static String jobName = "reduce-join";

	static ReduceJoinWetherStation jobClass = new ReduceJoinWetherStation();

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

		Path mypath = new Path(args[1]); // 输出路径
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {// 清空输出路径
			hdfs.delete(mypath, true);
		}

		// 初始job名称及运行主类
		Job job = Job.getInstance(conf, jobName);
		job.setJarByClass(jobClass.getClass());

		// 设置mapper 
		MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, MyStationMapper.class);
		MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, MyWetherMapper.class);
		job.setMapOutputKeyClass(TextPair.class);
		job.setMapOutputValueClass(Text.class);
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		// 设置：分区，分组
		job.setPartitionerClass(KeyPartitioner.class);
		// TODO ,咨询，源码
		//		job.setGroupingComparatorClass(FirstComparator.class);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileOutputFormat.setCompressOutput(job, true);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	// 读取气象站数据：编号，名称
	public static class MyStationMapper extends Mapper<LongWritable, Text, TextPair, Text> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] line = value.toString().split("\\s+");
			if (line.length == 2) {
				// 输出key：编号+标志位(用于排序)，value：名称
				context.write(new TextPair(line[0], "0"), new Text(line[1]));
			}
		}
	}

	// 读取天气数据：编号，时间，温度
	public static class MyWetherMapper extends Mapper<LongWritable, Text, TextPair, Text> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] line = value.toString().split("\\s+", 2);
			if (line.length == 2) {
				// 输出key：编号+标志位(用于排序)，value：时间+温度信息
				context.write(new TextPair(line[0], "0"), new Text(line[1]));
			}
		}
	}

	// 分区规则：按复合键的第一个元素
	public static class KeyPartitioner extends Partitioner<TextPair, Text> {

		@Override
		public int getPartition(TextPair key, Text value, int numPartitions) {
			return (key.getFirst().hashCode() & Integer.MAX_VALUE) % numPartitions;
		}
	}

	// reduce侧合并输出
	public static class MyReduce extends Reducer<TextPair, Text, Text, Text> {

		@Override
		protected void reduce(TextPair key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			Iterator<Text> ite = values.iterator();
			Text stationName = ite.next();
			if (ite.hasNext()) {
				Text sencond = ite.next();
				Text outValue = new Text(stationName + "\t" + sencond.toString());

				context.write(key.getFirst(), outValue);
			}
		}
	}
}
