package com.bdsoft.hadoop.jion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Hashtable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
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

/**
 * 分布式缓存

 * @author   丁辰叶
 * @date	 2016-2-15
 * @version  1.0.0
 */
public class CacheWetherStation extends Configured implements Tool {

	static String jobName = "reduce-cache";

	static CacheWetherStation jobClass = new CacheWetherStation();

	/**
	 * job运行入口
	 */
	public static void main(String[] args) throws Exception {
		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/" + jobName + "/records.txt";
		String out = BaseConfig.HDFS_PATH + "/bdceo/" + jobName + "/out/";

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

		// 添加分布式缓存文件
		job.addCacheFile(new URI(BaseConfig.HDFS_PATH + "/bdceo/" + jobName + "/station.txt"));

		// 设置mapper
		job.setMapperClass(MyWetherMapper.class);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	// 读取天气数据：编号，时间，温度
	public static class MyWetherMapper extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] line = value.toString().split("\t", 2);
			if (line.length == 2) {
				context.write(new Text(line[0]), value);
			}
		}
	}

	// 从分布式缓存读取气象站数据，合并天气数据
	public static class MyReduce extends Reducer<Text, Text, Text, Text> {

		// 存放缓存数据：key-编号，value-名称
		private Hashtable<String, String> table = new Hashtable<String, String>();

		/**
		 * 提取分布式缓存数据
		 */
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			// 本地文件路径
			Path[] localPaths = context.getLocalCacheFiles();
			if (localPaths.length == 0) {
				throw new FileNotFoundException("Distributd cache file not found.");
			}
			// 获取本地文件系统
			FileSystem fs = FileSystem.getLocal(context.getConfiguration());

			FSDataInputStream in = fs.open(new Path(localPaths[0].toString()));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String aline = null;
			while ((aline = br.readLine()) != null) {
				String[] line = aline.split("\t");
				table.put(line[0], line[1]);
			}
		}

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			String stationName = table.get(key.toString());
			Text outKey = new Text(stationName);
			for (Text value : values) {
				context.write(outKey, value);
			}
		}
	}
}
