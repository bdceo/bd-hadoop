package com.bdsoft.hadoop.zimi;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
 * 一本英文书籍包含成千上万个单词或者短语，现在我们需要在大量的单词中，找出相同字母组成的所有anagrams(字谜)
 */
public class ZimiGame extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/zimi/";
		String out = in + "out/";
		String[] arg4job = { in, out };

		arg4job = (args != null && args.length == 2) ? args : arg4job;

		int ec = ToolRunner.run(new Configuration(), new ZimiGame(), arg4job);

		System.out.println("job-zimi run finish > " + ec);
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

		Job job = new Job(conf, this.getClass().getName()); // 定义一个job任务
		job.setJarByClass(this.getClass());// 设置主类

		FileInputFormat.addInputPath(job, new Path(args[0]));// 输入路径
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // 输出路径

		job.setMapperClass(ZimiMapper.class);// 设置mapper和reduce
		job.setReducerClass(ZimiReduce.class);

		// 设置map和reduce的输出键值类型，一致！
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1; // 提交job任务
	}

	public static class ZimiMapper extends Mapper<Object, Text, Text, Text> {

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String word = value.toString(); // 一行一个单词

			// 将单词每个字母进行排序
			char[] words = word.toCharArray();
			Arrays.sort(words);
			String sword = new String(words);

			context.write(new Text(sword), new Text(word));
		}
	}

	public static class ZimiReduce extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			String output = "";
			for (Text value : values) {
				if (!output.equals("")) {
					output += "~";
				}
				output += value.toString();
			}

			StringTokenizer st = new StringTokenizer(output, "~");
			if (st.countTokens() >= 2) {
				output = output.replace("~", ",");
				context.write(key, new Text(output));
			}

		}

	}

}
