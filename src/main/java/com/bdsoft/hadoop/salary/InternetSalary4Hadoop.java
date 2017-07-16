package com.bdsoft.hadoop.salary;

import java.io.IOException;
import java.util.regex.Pattern;

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

/**
 * 互联网hadoop薪资统计
 */
public class InternetSalary4Hadoop extends Configured implements Tool {

	/**
	 * 运行入口
	 */
	public static void main(String[] args) throws Exception {
		// 指定输入输出路径
		String in = BaseConfig.HDFS_PATH + "/bdceo/salary/";
		String out = in + "out/";
		String[] arg4job = { in, out };

		arg4job = (args != null && args.length == 2) ? args : arg4job;

		int ec = ToolRunner.run(new Configuration(),
				new InternetSalary4Hadoop(), arg4job);

		System.out.println("job run finish > " + ec);
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
		job.setJarByClass(InternetSalary4Hadoop.class);// 设置主类

		FileInputFormat.addInputPath(job, new Path(args[0]));// 输入路径
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // 输出路径

		job.setMapperClass(SalaryMapper.class);// 设置mapper和reduce
		job.setReducerClass(SalaryReducer.class);

		// 设置map和reduce的输出键值类型，一致！
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		return job.waitForCompletion(true) ? 0 : 1; // 提交job任务
	}

	public static class SalaryMapper extends
			Mapper<LongWritable, Text, Text, Text> {

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			// 美团 3-5年经验 15-30k 北京 【够牛就来】hadoop高级工程...
			String line = value.toString();
			String[] record = line.split("\\s+");// 空格分隔

			if (record.length >= 3) {
				// key = record[1] 3-5年经验
				// value = record[2] 15-30k
				context.write(new Text(record[1]), new Text(record[2]));
			}
		}
	}

	public static class SalaryReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			int low = 0;
			int hig = 0;
			int cnt = 1;
			for (Text value : values) {
				String[] sals = value.toString().split("-");
				int _low = takeSalary(sals[0]);
				int _hig = takeSalary(sals[1]);
				if (cnt == 1 || _low < low) {
					low = _low;
				}
				if (cnt == 1 || _hig > hig) {
					hig = _hig;
				}
				cnt++;
			}
			String sal = String.format("%d - %d k", low, hig);
			context.write(key, new Text(sal));
		}
	}

	/**
	 * 提取薪资数，主要是去除k
	 * 
	 * @param salarys
	 * @return
	 */
	public static int takeSalary(String salarys) {
		String sal = Pattern.compile("[^0-9]").matcher(salarys).replaceAll("");
		return Integer.parseInt(sal);
	}

}
