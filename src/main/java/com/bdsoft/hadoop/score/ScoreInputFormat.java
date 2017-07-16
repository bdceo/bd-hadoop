/**
 * ScoreInputFormat.java
 * com.bdsoft.hadoop.score 
*/

package com.bdsoft.hadoop.score;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * 自定义[分数信息]输入格式
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-11
 * @version  1.0.0
 */
public class ScoreInputFormat extends FileInputFormat<Text, ScoreWritable> {

	@Override
	protected boolean isSplitable(JobContext context, Path filename) {
		return false;
	}

	@Override
	public RecordReader<Text, ScoreWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new ScoreRecordReader();
	}

}
