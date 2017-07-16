package com.bdsoft.hadoop.hia;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.contrib.utils.join.DataJoinMapperBase;
import org.apache.hadoop.contrib.utils.join.DataJoinReducerBase;
import org.apache.hadoop.contrib.utils.join.TaggedMapOutput;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// 数据联结，基于datajonin数据包的实现
public class DataJoin extends Configured implements Tool {

	public static class TaggedWritable extends TaggedMapOutput {

		private Writable data;

		public TaggedWritable(Writable data) {
			this.data = data;
			this.tag = new Text("");
		}

		public void readFields(DataInput in) throws IOException {
			this.data.readFields(in);
			this.tag.readFields(in);
		}

		public void write(DataOutput out) throws IOException {
			this.tag.write(out);
			this.data.write(out);
		}

		public Writable getData() {
			return data;
		}

	}

	public static class MapClass extends DataJoinMapperBase {

		// map任务开始是被调用，来为这个map任务所处理的所有记录制定一个全局的标签
		protected Text generateInputTag(String inputFile) {
			String dataSource = inputFile.split("-")[0];
			return new Text(dataSource);
		}

		// 取得被标记的记录，返回用于联结的分组键
		protected Text generateGroupKey(TaggedMapOutput aRecord) {
			String line = ((Text) aRecord.getData()).toString();
			String groupKey = line.split(",")[0];
			return new Text(groupKey);
		}

		// 封装数据
		protected TaggedMapOutput generateTaggedMapOutput(Object value) {
			TaggedWritable retv = new TaggedWritable((Text) value);
			retv.setTag(this.inputTag);
			return retv;
		}

	}

	// reduce过程，主要实现combine方法
	public static class ReduceClass extends DataJoinReducerBase {

		protected TaggedMapOutput combine(Object[] tags, Object[] values) {
			if (tags.length < 2) {
				return null;
			}
			String append = "";
			for (int i = 0; i < tags.length; i++) {
				if (i > 0) {
					append += ",";
				}
				TaggedWritable tw = (TaggedWritable) values[i];
				String line = ((Text) tw.getData()).toString();
				append += line.split(",", 2)[1];
			}

			TaggedWritable retv = new TaggedWritable(new Text(append));
			retv.setTag((Text) tags[0]);
			return retv;
		}

	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, DataJoin.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(DataJoin.class.getName());

		job.setMapperClass(MapClass.class);
		job.setReducerClass(ReduceClass.class);
		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(TaggedWritable.class);
		job.set("mapred.textoutputformat.separator", ",");

		JobClient.runJob(job);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new DataJoin(), args);
		System.exit(res);
	}

}
