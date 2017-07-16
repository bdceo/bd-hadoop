package com.bdsoft.hadoop.hia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 统计专利被引用情况，翻转专利引用数据文件
 * 
 * 输出某一专利被其他专利引用过的列表
 * 
 * 被引用\t引用，引用，引用...
 * 
 * @author bdceo
 * 
 */
public class PatentCitedListJob extends Configured implements Tool {

	// map过程处理逻辑
	public static class MapClass extends MapReduceBase implements
			Mapper<Text, Text, Text, Text> {

		public void map(Text key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			output.collect(value, key);
		}
	}

	// reduce过程处理逻辑
	public static class ReduceClass extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String csv = "";
			while (values.hasNext()) {
				if (csv.length() > 0)
					csv += ",";
				csv += values.next().toString();
			}
			output.collect(key, new Text(csv));
		}
	}

	// 启动入口
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PatentCitedListJob(),
				args);
		System.exit(res);
	}

	// Job配置，启动MapReduce执行的驱动
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, PatentCitedListJob.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(PatentCitedListJob.class.getName());
		job.setMapperClass(MapClass.class);
		job.setReducerClass(ReduceClass.class);

		job.setInputFormat(KeyValueTextInputFormat.class);
		job.set("key.value.separator.in.input.line", ",");

		job.setOutputFormat(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		JobClient.runJob(job);
		return 0;
	}

}