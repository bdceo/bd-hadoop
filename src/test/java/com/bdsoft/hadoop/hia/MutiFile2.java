package com.bdsoft.hadoop.hia;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MutiFile2 extends Configured implements Tool {

	// map过程处理逻辑
	public static class MapClass extends MapReduceBase implements
			Mapper<LongWritable, Text, NullWritable, Text> {

		private MultipleOutputs mos;
		private OutputCollector<NullWritable, Text> collector;

		public void configure(JobConf job) {
			mos = new MultipleOutputs(job);
		}

		public void map(LongWritable key, Text value,
				OutputCollector<NullWritable, Text> output, Reporter reporter)
				throws IOException {
			String[] arr = value.toString().split(",", -1);
			String chrono = arr[0] + "," + arr[1] + "," + arr[2];
			String geo = arr[0] + "," + arr[4] + "," + arr[5];

			collector = mos.getCollector("chrono", reporter);
			collector.collect(NullWritable.get(), new Text(chrono));

			collector = mos.getCollector("geo", reporter);
			collector.collect(NullWritable.get(), new Text(geo));
		}

		public void close() throws IOException {
			mos.close();
		}
	}

	// 启动入口
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MutiFile2(), args);
		System.exit(res);
	}

	// Job配置，启动MapReduce执行的驱动
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, MutiFile2.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(MutiFile2.class.getName());
		job.setMapperClass(MapClass.class);
		job.setNumReduceTasks(0);

		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		// 添加自定义多个目录输出
		MultipleOutputs.addNamedOutput(job, "chrono",
				TextOutputFormat.class, NullWritable.class, Text.class);
		MultipleOutputs.addNamedOutput(job, "geo", TextOutputFormat.class,
				NullWritable.class, Text.class);

		JobClient.runJob(job);
		return 0;
	}

}