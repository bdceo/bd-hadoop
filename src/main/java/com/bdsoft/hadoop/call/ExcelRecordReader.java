/**
 * ExcelRecordReader.java
 * com.bdsoft.hadoop.call
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.call;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.bdsoft.hadoop.util.BDExcelParser;

/**
 * 通话记录，excel解析
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-12
 * @version 1.0.0
 */
public class ExcelRecordReader extends RecordReader<LongWritable, Text> {

	private LongWritable key;
	private Text value;

	private InputStream in;

	private String[] lines;

	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		FileSplit split = (FileSplit) inputSplit;
		Path file = split.getPath();
		FileSystem fs = file.getFileSystem(context.getConfiguration());

		this.in = fs.open(split.getPath());

		// 将excel全部解析完毕，并以换行分隔每条记录
		String content = new BDExcelParser().parseExcelData(in);
		this.lines = content.split("\n");
	}

	/**
	 * Read the next key, value pair.
	 * 
	 * @return true if a key/value pair was read
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		System.out.println("call-->ExcelRecordReader.nextKeyValue()");
		// 第一次读取初始化
		if (key == null) {
			key = new LongWritable(0);
			value = new Text(lines[0]);
		}
		// 循环读取数组
		else {
			if (key.get() < (lines.length - 1)) {

				key.set(key.get() + 1); // 读取的数组索引位置
				value.set(lines[(int) key.get()]);

			} else {
				return false;
			}
		}

		if (key == null || value == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public LongWritable getCurrentKey() throws IOException,
			InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return 0;
	}

	@Override
	public void close() throws IOException {
		if (in != null) {
			in.close();
		}
	}

}
