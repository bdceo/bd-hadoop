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
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// 测试，map输出多个文件路径，没有reduce任务
public class MutiFile extends Configured implements Tool {

	public static class MapClass extends MapReduceBase implements
			Mapper<LongWritable, Text, NullWritable, Text> {

		public void map(LongWritable key, Text value,
				OutputCollector<NullWritable, Text> output, Reporter reporter)
				throws IOException {
			output.collect(NullWritable.get(), value);
		}
	}

	// 定制多文件输出
	public static class PartitionByCountryMTOF extends
			MultipleTextOutputFormat<NullWritable, Text> {

		protected String generateFileNameForKeyValue(NullWritable key,
				Text value, String name) {
			String[] arr = value.toString().split(",", -1);
			String country = arr[4].substring(1, 3);
			return country + "/" + name;
		}

	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, MutiFile.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(MutiFile.class.getName());
		job.setMapperClass(MapClass.class);
		job.setNumReduceTasks(0);

		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(PartitionByCountryMTOF.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		JobClient.runJob(job);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MutiFile(), args);
		System.exit(res);
	}

}
