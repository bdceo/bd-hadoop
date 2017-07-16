/**
 * WeiboJob.java
 * com.bdsoft.hadoop.weibo
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.weibo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
 * 自定义输入格式，将明星微博数据排序后按粉丝数 关注数 微博数 分别输出到不同文件中
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-14
 * @version 1.0.0
 */
public class WeiboJob extends Configured implements Tool {

	static String jobName = "weibo";

	static WeiboJob jobClass = new WeiboJob();

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
		// 输入格式
		job.setInputFormatClass(WeiboInputFormat.class); 

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		// 自定义输出格式，指定输出
		MultipleOutputs.addNamedOutput(job, "fans", TextOutputFormat.class,
				Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "follows", TextOutputFormat.class,
				Text.class, IntWritable.class);
		MultipleOutputs.addNamedOutput(job, "weibos", TextOutputFormat.class,
				Text.class, IntWritable.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class MyMapper extends
			Mapper<Text, WeiboWritable, Text, Text> {

		private Text keyFans = new Text("fans");
		private Text keyFollows = new Text("follows");
		private Text keyWeibos = new Text("weibos");

		/**
		 * 将明星微博数据排序后按粉丝数 关注数 微博数 分别输出到不同文件中
		 */
		@Override
		public void map(Text key, WeiboWritable value, Context context)
				throws IOException, InterruptedException {

			// 输入：key=姓名，value=粉丝数/关注数/微博数
			String keys = key.toString() + "\t";

			// 输出：key=fans/follows/weibos , value=姓名[\t]粉丝数/关注数/微博数
			context.write(keyFans, new Text(keys + value.getFans()));
			context.write(keyFollows, new Text(keys + value.getFollows()));
			context.write(keyWeibos, new Text(keys + value.getWeibos()));
			
			// 将每位明星的粉丝数全部输出到 key=fans
			// 将每位明星的关注数全部输出到 key=follows
			// 将每位明星的微博数全部输出到 key=weibos
			
			// reduce 接收到以后，将fans里的所有value，转成map，再按map-value排序，输出
		}
	}

	public static class MyReduce extends Reducer<Text, Text, Text, IntWritable> {

		private MultipleOutputs<Text, IntWritable> mulOut;

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			mulOut = new MultipleOutputs<Text, IntWritable>(context);
		}

		/**
		 * 将明星微博数据排序后按粉丝数 关注数 微博数 分别输出到不同文件中
		 */
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			String keys = key.toString(); // fans/follows/weibos

			int N = context.getConfiguration().getInt("reduceHasMaxLength",
					Integer.MAX_VALUE);

			// 提取某一个微博统计数的用户列表
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (Text value : values) {
				String[] records = value.toString().split("\t");
				// key=姓名，value=粉丝数/关注数/微博数
				map.put(records[0], Integer.parseInt(records[1]));
			}

			// 排序
			Entry[] entries = getSortedMapByValue(map);
			int size = entries.length;
			for (int i = 0; i < size && i < N; i++) {
				// 输出的key=姓名 , value=按粉丝数/关注数/微博数 排序后的
				mulOut.write(keys, entries[i].getKey(), entries[i].getValue());
			}
		}

		@Override
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			mulOut.close();
		}
	}

	/**
	 * 依据value的值，对map排序
	 */
	public static Entry[] getSortedMapByValue(Map map) {
		Set set = map.entrySet();

		Entry[] entries = (Entry[]) set.toArray(new Entry[set.size()]);

		Arrays.sort(entries, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				Long k1 = Long.valueOf(((Entry) o1).getValue().toString());
				Long k2 = Long.valueOf(((Entry) o2).getValue().toString());
				return k2.compareTo(k1);
			}
		});

		return entries;
	}
	
}
