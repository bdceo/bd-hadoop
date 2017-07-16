package com.bdsoft.hadoop.hia;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
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
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * 国家专利平均声明数量统计
 * 
 * @author bdceo
 * 
 */
public class CountryClaimCountJob extends Configured implements Tool {

	// map过程处理逻辑
	public static class MapClass extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		// 自定义枚举，在map中使用，统计
		static enum ClaimsCounter {
			MISSING, QUOTED, COLLECTED
		};

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String[] fields = value.toString().split(",", -20);
			String country = fields[4];
			String numClaims = fields[8];

			if (numClaims.length() == 0) {
				reporter.incrCounter(ClaimsCounter.MISSING, 1);
			} else if (numClaims.startsWith("\"")) {
				reporter.incrCounter(ClaimsCounter.QUOTED, 1);
			}

			if (numClaims.length() > 0 && !numClaims.startsWith("\"")) {
				output.collect(new Text(country), new Text(numClaims + ",1"));
				reporter.incrCounter(ClaimsCounter.COLLECTED, 1);
			}
		}
	}

	public static class CombinerClass extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			double sum = 0;
			int count = 0;
			while (values.hasNext()) {
				String[] fields = values.next().toString().split(",");
				sum += Double.parseDouble(fields[0]);
				count += Integer.parseInt(fields[1]);
			}
			output.collect(key, new Text(sum + "," + count));
		}

	}

	// reduce过程处理逻辑
	public static class ReduceClass extends MapReduceBase implements
			Reducer<Text, Text, Text, DoubleWritable> {

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, DoubleWritable> output, Reporter reporter)
				throws IOException {
			double sum = 0;
			int count = 0;
			while (values.hasNext()) {
				String[] fields = values.next().toString().split(",");
				sum += Double.parseDouble(fields[0]);
				count += Integer.parseInt(fields[1]);
			}
			output.collect(key, new DoubleWritable(sum / count));
		}
	}

	// 启动入口
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(),
				new CountryClaimCountJob(), args);
		System.exit(res);
	}

	// Job配置，启动MapReduce执行的驱动
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, CountryClaimCountJob.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(CountryClaimCountJob.class.getName());
		job.setMapperClass(MapClass.class);
		job.setCombinerClass(CombinerClass.class);
		job.setReducerClass(ReduceClass.class);

		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		JobClient.runJob(job);
		return 0;
	}

}