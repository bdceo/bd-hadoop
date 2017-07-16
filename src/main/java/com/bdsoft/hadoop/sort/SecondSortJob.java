package com.bdsoft.hadoop.sort;

import java.io.IOException;
import java.util.StringTokenizer;

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
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;
import com.bdsoft.hadoop.seri.IntPair;
import com.bdsoft.hadoop.seri.IntPair.FirstPartioner;
import com.bdsoft.hadoop.seri.IntPair.GroupComparator;

/**
 * 二次排序
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */
public class SecondSortJob extends Configured implements Tool {

	static String jobName = "second-sort";

	static SecondSortJob jobClass = new SecondSortJob();

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

		// 设置输入输出路径
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// 设置mapper
		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(IntPair.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setInputFormatClass(TextInputFormat.class);

		// 设置：分区，分组，二次排序
		job.setPartitionerClass(FirstPartioner.class);
		//		job.setSortComparatorClass(cls); // 未实现，使用IntPair的compareTo方法
		job.setGroupingComparatorClass(GroupComparator.class);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class MyMapper extends Mapper<LongWritable, Text, IntPair, IntWritable> {

		private final IntPair outKey = new IntPair();
		private final IntWritable outVal = new IntWritable();

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String lien = value.toString();
			StringTokenizer st = new StringTokenizer(lien);
			int left = 0, right = 0;
			if (st.hasMoreTokens()) {
				left = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens()) {
					right = Integer.parseInt(st.nextToken());
				}

				outKey.set(left, right);
				outVal.set(right);
				context.write(outKey, outVal);
			}
		}
	}

	public static class MyReduce extends Reducer<IntPair, IntWritable, Text, IntWritable> {

		private final Text left = new Text();

		@Override
		protected void reduce(IntPair key, Iterable<IntWritable> values, Context context) throws IOException,
				InterruptedException {
			left.set(Integer.toString(key.getFirst()));
			for (IntWritable val : values) {
				context.write(left, val);
			}
		}
	}
}
