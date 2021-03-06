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
 * 输出某一专利被引用次数
 * 
 * 专利\t被引用次数
 * 
 * @author bdceo
 * 
 */
public class PatentCitedCountJob extends Configured implements Tool {

	// map过程处理逻辑
	public static class MapClass extends MapReduceBase implements
			Mapper<Text, Text, Text, IntWritable> {

		public void map(Text key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			// 翻转专利引用数据，得出专利被引用次数，在reduce中汇总统计
			output.collect(value, new IntWritable(1));
		}
	}

	// reduce过程处理逻辑
	public static class ReduceClass extends MapReduceBase implements
			Reducer<Text, Text, Text, IntWritable> {

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			int count = 0;
			while (values.hasNext()) {
				values.next();
				count++;
			}
			output.collect(key, new IntWritable(count));
		}
	}

	// 启动入口
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(),
				new PatentCitedCountJob(), args);
		System.exit(res);
	}

	// Job配置，启动MapReduce执行的驱动
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, PatentCitedCountJob.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(PatentCitedCountJob.class.getName());
		job.setMapperClass(MapClass.class);
		job.setReducerClass(ReduceClass.class);

		job.setInputFormat(KeyValueTextInputFormat.class);
		job.set("key.value.separator.in.input.line", ",");

		// 此处设置的输出格式及输出键和值的类，在map和reduce输出时都需要遵从，不只是reduce阶段的真正输出
		// 在map阶段的临时输出，也需要遵从，此处出现过异常，需要深刻理解和体会@20131105
		job.setOutputFormat(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		JobClient.runJob(job);
		return 0;
	}

}