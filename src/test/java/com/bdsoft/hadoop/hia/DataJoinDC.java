package com.bdsoft.hadoop.hia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// 数据联结，基于DistributedCache
public class DataJoinDC extends Configured implements Tool {

	public static class MapClass extends MapReduceBase implements
			Mapper<Text, Text, Text, Text> {

		private Hashtable<String, String> joinData = new Hashtable<String, String>();

		public void configure(JobConf conf) {
			try {
				// 从缓存中读取本地文件副本，进行缓存，在map过程中调用
				Path[] cacheFiles = DistributedCache.getLocalCacheFiles(conf);
				if (cacheFiles != null && cacheFiles.length > 0) {
					String line;
					String[] tokens;
					BufferedReader joinReader = new BufferedReader(
							new FileReader(cacheFiles[0].toString()));
					try {
						while ((line = joinReader.readLine()) != null) {
							tokens = line.split(",", 2);
							joinData.put(tokens[0], tokens[1]);
						}
					} finally {
						joinReader.close();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public void map(Text key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String joinValue = joinData.get(key);
			if (joinValue != null) {
				output.collect(key,
						new Text(value.toString() + "," + joinValue));
			}
		}

	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, DataJoinDC.class);

		// 添加分布式缓存
		DistributedCache.addCacheFile(new Path(args[0]).toUri(), conf);

		Path in = new Path(args[1]);
		Path out = new Path(args[2]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(DataJoinDC.class.getName());

		job.setMapperClass(MapClass.class);
		// 设置不执行reduce过程
		job.setNumReduceTasks(0);

		job.setInputFormat(KeyValueTextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);
		job.set("key.value.separator.in.input.line", ",");

		JobClient.runJob(job);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new DataJoinDC(), args);
		System.exit(res);
	}

}
