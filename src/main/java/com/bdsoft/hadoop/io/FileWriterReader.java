/**
 * SequenceFileWriter.java
 * com.bdsoft.hadoop.io
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.io;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.util.ReflectionUtils;

import com.bdsoft.hadoop.BaseConfig;

/**
 * SequenceFile 读写
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-24
 * @version  1.0.0
 */

public class FileWriterReader {

	public static void main(String[] args) throws Exception {

	}

	/**
	 * SequenceFile 的 write
	 */
	public static void showWrite() throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(BaseConfig.HDFS_PATH), conf);
		Path path = new Path("/tmp.seq");

		IntWritable key = new IntWritable();
		Text val = new Text();

		SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf, path, key.getClass(), val.getClass());

		String[] data = new String[] { "bdceo", "bdcfo", "bdcto", "bdcoo", "bdcio", "bdcmo", "bdcxo" };
		for (int i = 0; i < 100; i++) {
			key.set(100 - i);
			val.set(data[i % data.length]);

			writer.append(key, val);
		}

		IOUtils.closeStream(writer);
	}

	/**
	 * SequenceFile 的 read
	 */
	public static void showRead() throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(BaseConfig.HDFS_PATH), conf);
		Path path = new Path("/tmp.seq");

		SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);

		Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
		Writable val = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);

		while (reader.next(key, val)) {
			System.out.println("key=" + key);
			System.out.println("value=" + val);
			System.out.println("position=" + reader.getPosition());
		}

		IOUtils.closeStream(reader);
	}

	/**
	 * MapFile 的 write
	 */
	public static void showMapWrite() throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(BaseConfig.HDFS_PATH), conf);
		Path path = new Path("/tmp1.map");

		IntWritable key = new IntWritable();
		Text val = new Text();

		MapFile.Writer writer = new MapFile.Writer(conf, fs, path.toString(), key.getClass(), val.getClass());

		String[] data = new String[] { "bdceo", "bdcfo", "bdcto", "bdcoo", "bdcio", "bdcmo", "bdcxo" };
		for (int i = 0; i < 100; i++) {
			key.set(100 - i);
			val.set(data[i % data.length]);

			writer.append(key, val);
		}

		IOUtils.closeStream(writer);

		// 产生了一个/tmpdata.map的文件夹，里面有data和index两个文件
	}

	/**
	 * MapFile 的 read
	 */
	public static void showMapReade() throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(BaseConfig.HDFS_PATH), conf);
		Path path = new Path("/tmp1.map");

		MapFile.Reader reader = new MapFile.Reader(fs, path.toString(), conf);

		WritableComparable key = (WritableComparable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
		Writable val = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), conf);

		while (reader.next(key, val)) {
			System.out.println("key=" + key);
			System.out.println("value=" + val);
		}

		IOUtils.closeStream(reader);
	}

	/**
	 *	SequenceFile 转换为 MapFile：
	 * 
	 *	1、已有一个名为/tmp1.seq 的 SequenceFile 文件（为前面实验产生，已经排好序，如未排序，则需要先排序）。
	 *	2、创建一个 /tmp1.map 文件夹。
	 *	3、将/tmp1.seq文件移到/tmp1.map文件夹内，并重命名为/tmp1.map/data。
	 *	》 $./hadoop fs -mv /tmp1.seq /tmp1.map/data
	 */
	public static void sequenceToMap() throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(BaseConfig.HDFS_PATH), conf);

		// 文件夹的位置
		Path map = new Path("/tmp1.map");
		Path mapData = new Path(map, MapFile.DATA_FILE_NAME);

		SequenceFile.Reader reader = new SequenceFile.Reader(fs, mapData, conf);

		Class key = reader.getKeyClass();
		Class val = reader.getValueClass();
		reader.close();

		// 转换
		long entries = MapFile.fix(fs, map, key, val, false, conf);

		System.out.printf("Created MapFile %s with %d entries\n", map, entries);
	}

}
