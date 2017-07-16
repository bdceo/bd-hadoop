/**
 * WeiboInputFormat.java
 * com.bdsoft.hadoop.weibo
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.weibo;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

/**
 * 解析微博数据
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-14
 * @version 1.0.0
 */
public class WeiboInputFormat extends FileInputFormat<Text, WeiboWritable> {

	@Override
	public RecordReader<Text, WeiboWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		return new WeiboRecordReader();
	}

	/**
	 * 自定义微博数据读取器
	 */
	public class WeiboRecordReader extends RecordReader<Text, WeiboWritable> {

		public LineReader in;

		public Text aline;

		public Text key;
		public WeiboWritable value;

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context)
				throws IOException, InterruptedException {
			FileSplit fsplit = (FileSplit) split;
			Configuration conf = context.getConfiguration();
			Path path = fsplit.getPath();
			FileSystem fs = path.getFileSystem(conf);

			FSDataInputStream fin = fs.open(path); // 打开输入文件

			in = new LineReader(fin);
			aline = new Text();
			key = new Text();
			value = new WeiboWritable();
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			int reads = in.readLine(aline);
			if (reads == 0) {
				// false已读完
				return false;
			}

			// 一行：唐嫣 唐嫣 24301532 200 2391
			String[] lines = aline.toString().split("\t");
			if (lines.length != 5) {
				throw new IOException("无效数据行");
			}

			int a, b, c;
			try {
				a = Integer.parseInt(lines[2].trim());
				b = Integer.parseInt(lines[3].trim());
				c = Integer.parseInt(lines[4].trim());
			} catch (Exception e) {
				throw new IOException("数据提取失败");
			}

			key.set(lines[0]);// 名称
			value.set(a, b, c); // 微博数据

			// ture-继续读下一行
			return true;
		}

		@Override
		public Text getCurrentKey() throws IOException, InterruptedException {
			return key;
		}

		@Override
		public WeiboWritable getCurrentValue() throws IOException,
				InterruptedException {
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

}
