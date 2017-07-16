/**
 * TvPlayInputFormat.java
 * com.bdsoft.hadoop.tv
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.tv;

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
 * 解析电视剧播放统计数据
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-14
 * @version 1.0.0
 */
public class TvPlayInputFormat extends FileInputFormat<Text, TvPlayWritable> {

	@Override
	public RecordReader<Text, TvPlayWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException,
			InterruptedException {
		return new TvPlayRecordReader();
	}

	/**
	 * 自定义电视剧播放统计数据读取器
	 */
	public static class TvPlayRecordReader extends
			RecordReader<Text, TvPlayWritable> {

		public LineReader in;

		public Text aline;

		public Text key;
		public TvPlayWritable value;

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
			value = new TvPlayWritable();
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			int reads = in.readLine(aline);
			if (reads == 0) {
				// false已读完
				return false;
			}

			// 电视剧 播放数 收藏数 评论数 踩数 赞数
			// 你迷上了我 1 37421 16 6 0 0
			String[] lines = aline.toString().split("\t");
			if (lines.length != 7) {
				throw new IOException("无效数据行");
			}

			// key=电视剧[\t]网站
			key.set(lines[0] + "\t" + lines[1]);
			// value=统计数据
			value.set(Integer.parseInt(lines[2]), Integer.parseInt(lines[3]),
					Integer.parseInt(lines[4]), Integer.parseInt(lines[5]),
					Integer.parseInt(lines[6]));

			return true;
		}

		@Override
		public Text getCurrentKey() throws IOException, InterruptedException {
			return key;
		}

		@Override
		public TvPlayWritable getCurrentValue() throws IOException,
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
