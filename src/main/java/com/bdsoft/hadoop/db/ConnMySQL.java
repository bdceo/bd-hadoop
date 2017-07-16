/**
 * ConnMySQL.java
 * com.bdsoft.hadoop.db
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.db;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-26
 * @version  1.0.0
 */
public class ConnMySQL {

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		Path output = new Path(BaseConfig.HDFS_PATH + "/bdceo/mysql/out");

		FileSystem fs = FileSystem.get(new URI(output.toString()), conf);
		if (fs.exists(output)) {
			fs.delete(output);
		}

		// mysql驱动文件
		DistributedCache.addFileToClassPath(new Path(BaseConfig.HDFS_PATH + "/bdceo/jar/mysql-xxx.jar"), conf);

		// 配置数据库连接
		DBConfiguration
				.configureDB(conf, "com.mysql.jdbc.driver", "jdbc:mysql://19.168.1.10:3306/test", "root", "root");

		Job job = new Job(conf, "mysql");

		job.setJarByClass(ConnMySQL.class);

		job.setMapperClass(AMapper.class);
		job.setReducerClass(AReducer.class);

		//		job.setMapperClass(AWriteMapper.class);
		//		job.setReducerClass(AWriteReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// 从数据库读取数据
		job.setInputFormatClass(DBInputFormat.class);
		FileOutputFormat.setOutputPath(job, output);

		// 往数据库写入数据
		//		job.setOutputFormatClass(DBOutputFormat.class);
		//		FileInputFormat.addInputPath(job, new Path(BaseConfig.HDFS_PATH+"/bdceo/mysql/data.txt"));

		String[] fileds = new String[] { "uid", "email", "name" };
		DBInputFormat.setInput(job, UserRecord.class, "user", null, null, fileds);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	public static class AMapper extends Mapper<LongWritable, UserRecord, Text, Text> {

		@Override
		protected void map(LongWritable key, UserRecord value, Context context) throws IOException,
				InterruptedException {
			context.write(new Text(value.uid + ""), new Text(value.name + "  " + value.email));
		}

	}

	public static class AWriteMapper extends Mapper<LongWritable, UserRecord, Text, Text> {

		@Override
		protected void map(LongWritable key, UserRecord value, Context context) throws IOException,
				InterruptedException {
			String email = value.toString().split("\\s")[0];
			String name = value.toString().split("\\s")[1];
			context.write(new Text(email), new Text(name));
		}

	}

	public static class AReducer extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			// 读数据
			for (Iterator<Text> itr = values.iterator(); itr.hasNext();) {
				context.write(key, itr.next());
			}
		}
	}

	public static class AWriteReducer extends Reducer<Text, Text, UserRecord, UserRecord> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			//接收到的key value对即为要输入数据库的字段，所以在reduce中：
			//wirte的第一个参数，类型是自定义类型UserRecord，利用key和value将其组合成UserRecord，然后等待写入数据库
			//wirte的第二个参数，wirte的第一个参数已经涵盖了要输出的类型，所以第二个类型没有用，设为null
			for (Iterator<Text> itr = values.iterator(); itr.hasNext();) {
				context.write(new UserRecord(key.toString(), itr.next().toString()), null);
			}
		}
	}

	/**
	 * 数据对象
	 */
	public static class UserRecord implements Writable, DBWritable {

		int uid;
		String email;
		String name;

		public UserRecord() {

		}

		public UserRecord(String email, String name) {
			this.email = email;
			this.name = name;
		}

		/**
		 * 向数据库写入数据 
		 */
		@Override
		public void write(PreparedStatement st) throws SQLException {
			st.setInt(1, uid);
			st.setString(2, email);
			st.setString(3, name);
		}

		/**
		 * 从数据库读取字段
		 */
		@Override
		public void readFields(ResultSet rs) throws SQLException {
			uid = rs.getInt(1);
			email = rs.getString(2);
			name = rs.getString(3);
		}

		/**
		 * 序列化
		 */
		@Override
		public void write(DataOutput out) throws IOException {
			out.writeInt(uid);
			out.writeUTF(email);
			out.writeUTF(name);
		}

		/**
		 * 反序列化
		 */
		@Override
		public void readFields(DataInput in) throws IOException {
			uid = in.readInt();
			email = in.readUTF();
			name = in.readUTF();
		}

		@Override
		public String toString() {
			return "UserRecord [uid=" + uid + ", email=" + email + ", name=" + name + "]";
		}

	}

}
