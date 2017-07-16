package com.bdsoft.hadoop.hia;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;
import org.apache.hadoop.mapred.lib.db.DBOutputFormat;
import org.apache.hadoop.mapred.lib.db.DBWritable;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Out2DB extends Configured implements Tool {

	public static class CiteDBWritable implements Writable, DBWritable {

		private String citing;
		private String cited;

		public CiteDBWritable(String citing, String cited) {
			this.cited = cited;
			this.citing = citing;
		}

		// Writable接口实现方法
		public void readFields(DataInput in) throws IOException {
			citing = in.readUTF();
			cited = in.readUTF();
		}

		public void write(DataOutput out) throws IOException {
			out.writeUTF(citing);
			out.writeUTF(cited);
		}

		// DBWritable接口实现方法
		public void readFields(ResultSet rs) throws SQLException {
			citing = rs.getString("CITING");
			cited = rs.getString("CITED");
		}

		public void write(PreparedStatement pre) throws SQLException {
			pre.setString(1, citing);
			pre.setString(2, cited);
		}

	}

	public static class MapClass extends MapReduceBase implements
			Mapper<Text, Text, CiteDBWritable, NullWritable> {
		public void configure(JobConf job) {
		}

		public void map(Text key, Text value,
				OutputCollector<CiteDBWritable, NullWritable> output,
				Reporter reporter) throws IOException {
			CiteDBWritable row = new CiteDBWritable(value.toString(),
					key.toString());

			output.collect(row, NullWritable.get());
		}

		public void close() throws IOException {
		}
	}

	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		JobConf job = new JobConf(conf, Out2DB.class);

		Path in = new Path(args[0]);
		Path out = new Path(args[1]);
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setJobName(Out2DB.class.getName());
		job.setMapperClass(MapClass.class);
		job.setNumReduceTasks(0);

		job.setInputFormat(KeyValueTextInputFormat.class);
		job.set("key.value.separator.in.input.line", ",");

		job.setOutputFormat(DBOutputFormat.class);
		DBConfiguration.configureDB(job, "com.mysql.jdbc.Driver",
				"jdbc:mysql://192.168.0.3:3306/test", "root", "root");
		DBOutputFormat.setOutput(job, "cite", "CITING", "CITED");

		JobClient.runJob(job);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Out2DB(), args);
		System.exit(res);
	}

}
