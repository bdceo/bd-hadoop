package com.bdsoft.hadoop.jion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;
import com.bdsoft.hadoop.util.BDParseTime;

/**
 * Map端的join演示
 * 
 * 使用频道类型数据集和机顶盒用户数据集，进行数据的连接，统计出每天每个频道每分钟的收视人数。
 *
1、编写 Mapper 类，连接用户数据和频道类型数据，按需求将数据解析为key=频道类别+日期+每分钟，value=机顶盒号，然后将结果输出。

2、编写 Combiner 类，先将 Mapper 输出结果合并一次，然后输出给 Reducer。

3、编写 Reducer 类，统计出收视率，然后使用 MultipleOutputs 类将每分钟的收视率，按天输出到不同文件路径下。

4、编写驱动方法 run，执行 MapReduce 程序。
 *
 * @author   丁辰叶
 * @date	 2016-2-15
 * @version  1.0.0
 */
public class TVJoinJob extends Configured implements Tool {

	static String jobName = "map-join";

	static TVJoinJob jobClass = new TVJoinJob();

	/**
	 * job运行入口
	 */
	public static void main(String[] args) throws Exception {
		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/counter/out/";
		String out = BaseConfig.HDFS_PATH + "/bdceo/" + jobName + "/out/";
		String cache = BaseConfig.HDFS_PATH + "/bdceo/join/channelType.csv";

		String[] arg4job = { in, out, cache };
		arg4job = (args != null && args.length == 3) ? args : arg4job;
		int ec = ToolRunner.run(new Configuration(), jobClass, arg4job);

		System.out.println(jobName + " job run finish > " + ec);
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();

		// 指定分布式缓存文件
		DistributedCache.addCacheFile(new URI(args[2]), conf);

		Path mypath = new Path(args[1]); // 输出路径
		FileSystem hdfs = mypath.getFileSystem(conf);
		if (hdfs.isDirectory(mypath)) {// 清空输出路径
			hdfs.delete(mypath, true);
		}

		// 初始job名称及运行主类
		Job job = Job.getInstance(conf, jobName);
		job.setJarByClass(jobClass.getClass());

		// 设置输入输出路径
		FileInputFormat.addInputPaths(job, args[0] + "20120917," + args[1] + "20120918," + args[1] + "20120919,"
				+ args[1] + "20120920," + args[1] + "20120921," + args[1] + "20120922," + args[1] + "20120923");
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		// 添加多文件输出路径
		MultipleOutputs.addNamedOutput(job, "20120917", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "20120918", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "20120919", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "20120920", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "20120921", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "20120922", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "20120923", TextOutputFormat.class, Text.class, Text.class);

		// 设置mapper
		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MapWritable.class);

		// 设置combiner
		job.setCombinerClass(MyCombiner.class);

		// 设置reduce
		job.setReducerClass(MyReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	// Mapper 类，连接用户和频道数据
	// Map = {key = channelType+date+minute, value = Map(stbNum)}
	public static class MyMapper extends Mapper<Object, Text, Text, MapWritable> {

		private Text text = new Text();

		// key=频道名称，value=频道编号
		private Hashtable<String, String> table = new Hashtable<String, String>();

		// 读取分布式缓存文件： channelType.csv
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			// 分布式缓存文件路径列表
			URI[] uris = DistributedCache.getCacheFiles(context.getConfiguration());

			// 文件系统
			FileSystem fs = FileSystem.get(URI.create(BaseConfig.HDFS_PATH), context.getConfiguration());
			// 打开输入流
			FSDataInputStream in = fs.open(new Path(uris[0].getPath()));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// 读取文件
			String aline = null;
			while ((aline = br.readLine()) != null) {
				String[] line = aline.split("\t");
				table.put(line[0], line[1]);
			}
		}

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			//每行数据格式：stbNum + "@" + date + "@" + sn + "@" + s+ "@" + e ;
			String[] records = value.toString().split("@");
			String stbNum = records[0];// 机顶盒
			String date = records[1];// 日期
			String sn = records[2];// 频道名称
			String s = records[3];// 开始时间
			String e = records[4];// 结束时间
			if ("".equals(s) || "".equals(e)) {
				return;
			}

			String chanleType = "0";// 频道类型
			String ckind = table.get(sn);
			if (ckind != null) {
				chanleType = ckind;
			}

			// 按每条记录的 起始时间，结束时间 计算出分钟列表
			List<String[]> list = BDParseTime.getTimeSplit(s, e);
			int size = list.size();
			for (int i = 0; i < size; i++) {
				String[] time = list.get(i);
				String min = time[2];// 分钟

				// {key = channelType+date+minute, value = Map(stbNum)}
				Text outKey = new Text(chanleType + "@" + date + "@" + min);

				MapWritable avgnumMap = new MapWritable();
				avgnumMap.put(new Text(stbNum), text);

				context.write(outKey, avgnumMap);
			}
		}
	}

	// 先将 Mapper 输出结果合并一次，然后输出给 Reducer
	public static class MyCombiner extends Reducer<Text, MapWritable, Text, MapWritable> {

		@Override
		protected void reduce(Text key, Iterable<MapWritable> values, Context context) throws IOException,
				InterruptedException {
			MapWritable avgnumMap = new MapWritable();
			for (MapWritable val : values) {
				// 合并相同的机顶盒 ： distinct
				avgnumMap.putAll(val);
			}
			context.write(key, avgnumMap);
		}
	}

	// Reduce = {key = channelType, value = minute+sum(distinct(stbNum))}
	public static class MyReduce extends Reducer<Text, MapWritable, Text, Text> {

		// 多路径输出对象
		private MultipleOutputs<Text, Text> mos;

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			mos = new MultipleOutputs<Text, Text>(context);
		}

		@Override
		protected void reduce(Text key, Iterable<MapWritable> values, Context context) throws IOException,
				InterruptedException {
			// 输入数据为：key=channelType+date+min  value=map(stbNum)
			String[] kv = key.toString().split("@");
			String chanelType = kv[0]; // 频道类别
			String date = kv[1]; // 日期
			String min = kv[2]; // 分钟

			MapWritable avgnumMap = new MapWritable();
			for (MapWritable val : values) {
				avgnumMap.putAll(val);
			}

			// 去重后的机顶盒数量	
			int avgnum = avgnumMap.size();

			// 输出： {key = channelType, value = minute+sum(distinct(stbNum))}
			mos.write(date.replaceAll("-", ""), chanelType, new Text(min + "\t" + avgnum));
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			mos.close();
		}

	}
}
