/**
 * ScoreRecordReader.java
 * com.bdsoft.hadoop.score
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.score;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

/**
 * 自定义分数信息提取 解析key/value，给mapper
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-11
 * @version 1.0.0
 */
public class ScoreRecordReader extends RecordReader<Text, ScoreWritable> {

	public LineReader in;// 行读取器

	public Text line; // 一行数据

	public Text key;
	public ScoreWritable value;

	/**
	 * Called once at initialization.
	 */
	@Override
	public void initialize(InputSplit input, TaskAttemptContext context)
			throws IOException, InterruptedException {
		System.out.println("call-->ScoreRecordReader.initialize()");

		FileSplit split = (FileSplit) input; // 分片
		Configuration conf = context.getConfiguration(); // 配置
		Path file = split.getPath(); // 分片路径
		FileSystem fs = file.getFileSystem(conf); // 文件系统
		FSDataInputStream filein = fs.open(file);// 打开文件流

		in = new LineReader(filein, conf);
		line = new Text();
		key = new Text();
		value = new ScoreWritable();
	}

	/**
	 * Read the next key, value pair.
	 * 
	 * @return true if a key/value pair was read
	 */
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		System.out.println("call-->ScoreRecordReader.nextKeyValue()");
		int linesize = in.readLine(line);
		if (linesize == 0) {
			return false;
		}
		System.out.println("read line>" + line);

		// 一行数据：﻿19020090040 秦心芯 123 131 100 95 100
		String[] lineData = line.toString().split("[\\s]+");
		if (lineData.length != 7) {
			throw new IOException("无效数据行");
		}

		float a, b, c, d, e;
		try {
			a = Float.parseFloat(lineData[2]);
			b = Float.parseFloat(lineData[3]);
			c = Float.parseFloat(lineData[4]);
			d = Float.parseFloat(lineData[5]);
			e = Float.parseFloat(lineData[6]);
		} catch (NumberFormatException nfe) {
			throw new IOException("解析数据格式化出错");
		}

		// 设置 key/value 内容
		key.set(lineData[0] + "\t" + lineData[1]);
		value.set(a, b, c, d, e);

		return true;
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		System.out.println("call-->ScoreRecordReader.getCurrentKey()");
		return key;
	}

	@Override
	public ScoreWritable getCurrentValue() throws IOException,
			InterruptedException {
		System.out.println("call-->ScoreRecordReader.getCurrentValue()");
		return value;
	}

	/**
	 * The current progress of the record reader through its data.
	 */
	@Override
	public float getProgress() throws IOException, InterruptedException {
		System.out.println("call-->ScoreRecordReader.getProgress()");
		return 0;
	}

	@Override
	public void close() throws IOException {
		System.out.println("call-->ScoreRecordReader.close()");
		if (in != null) {
			in.close();
		}
	}

}
