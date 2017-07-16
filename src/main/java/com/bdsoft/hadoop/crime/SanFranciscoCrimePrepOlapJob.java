/**
 * SanFranciscoCrimePrepOlapJob.java
 * com.bdsoft.hadoop.crime
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.crime;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 旧金山犯罪数据分析
 * <p>
 * 统计出每天每种犯罪类别在不同区域内发生犯罪的次数
 * <p>
 * ==>  每天/每种类别/每个区域，犯罪次数统计
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */
public class SanFranciscoCrimePrepOlapJob extends CrimeJobBase implements Tool {

	private static Logger log = Logger.getLogger(SanFranciscoCrimePrepOlapJob.class.getCanonicalName());

	// 类别，区域名称集合
	private static List<String> categories = null;
	private static List<String> districts = null;
	// key=类别/区域，value=索引
	private static final Map<String, Integer> categoryLookup = new HashMap<String, Integer>();
	private static final Map<String, Integer> districtLookup = new HashMap<String, Integer>();

	/**
	 * job运行入口
	 */
	public static void main(String[] args) throws Exception {
		String[] cmds = new String[] { BaseConfig.HDFS_PATH + "/bdceo/crime/crime.txt",
				BaseConfig.HDFS_PATH + "/bdceo/crime/out1/part-r-00000",
				BaseConfig.HDFS_PATH + "/bdceo/crime/out2/part-r-00000", BaseConfig.HDFS_PATH + "/bdceo/crime/out3" };
		if (cmds.length == 4) {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(BaseConfig.HDFS_PATH), conf);

			// 初始化，读取第一个mr任务的输出
			setup(cmds[1], cmds[2], fs);

			// 执行第二个mr任务
			int code = ToolRunner.run(conf, new SanFranciscoCrimePrepOlapJob(), cmds);
			System.exit(code);
		} else {
			System.err
					.println("\nusage: bin/hadoop jar sfcrime.hadoop.mapreduce.jobs-0.0.1-SNAPSHOT.jar SanFranciscoCrimePrepOlap path/to/category/report path/to/district/report path/to/input/data path/to/output/data");
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Path out = new Path(args[3]);
		FileSystem hdfs = out.getFileSystem(conf);
		if (hdfs.isDirectory(out)) {// 清空输出路径
			hdfs.delete(out, true);
		}

		// 任务1
		Job job1 = new Job(conf, "crime2");
		job1.setJarByClass(SanFranciscoCrimePrepOlapJob.class);

		job1.setMapperClass(DateMapByCategoryAndDistrict.class);
		job1.setReducerClass(CrimeReduceByDate.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job1, new Path(args[0]));
		FileOutputFormat.setOutputPath(job1, new Path(args[3]));

		job1.waitForCompletion(true);
		return 0;
	}

	/**
	 * 抽象父类
	 * */
	public static abstract class AMap extends Mapper<LongWritable, Text, Text, Text> {

		protected int keyId = 0; // 日期
		protected int valId1 = 0; // 犯罪区域
		protected int valId2 = 0; // 犯罪类型

		/**
		 * 将key之转换成规范的数据格式
		 * 
		 * @param value 包含不规范的key值
		 * @return 返回规范的key值
		 * @throws ParseException
		 */
		protected abstract String formatKey(String value) throws ParseException;

		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			try {
				String[] cols = DataFile.getColumns(line);
				if (cols != null) {
					if (cols.length >= (DISTRICT_COLUMN_INDEX + 1)) {
						Text tk = new Text();
						tk.set(formatKey(cols[keyId]));// 将日期作为key

						Text tv = new Text();
						StringBuffer sb = new StringBuffer();
						sb.append("\"").append(cols[valId1]).append("\",");// 犯罪区域
						sb.append("\"").append(cols[valId2]).append("\""); // 犯罪类型
						tv.set(sb.toString());

						context.write(tk, tv);
					} else {
						log.warning(MessageFormat.format("Data {0} did not parse into columns.", new Object[] { line }));
					}
				}
			} catch (NumberFormatException nfe) {
				log.log(Level.WARNING, MessageFormat.format("Expected {0} to be a number.\n", new Object[] { line }),
						nfe);
			} catch (IOException e) {
				log.log(Level.WARNING, MessageFormat.format("Cannot parse {0} into columns.\n", new Object[] { line }),
						e);
			} catch (ParseException e) {
				log.log(Level.WARNING,
						MessageFormat.format("Expected {0} to be a date but it was not.\n", new Object[] { line }), e);
			}
		}
	}

	/**
	 * 将 map 输入数据的日期作为key，犯罪区域和犯罪类型作为value，然后输出
	 * */
	public static class DateMapByCategoryAndDistrict extends AMap {

		public DateMapByCategoryAndDistrict() {
			this.keyId = DATE_COLUMN_INDEX; // 日期
			this.valId1 = DISTRICT_COLUMN_INDEX; // 犯罪区域
			this.valId2 = CATEGORY_COLUMN_INDEX; // 犯罪类型
		}

		@Override
		public String formatKey(String value) throws ParseException {
			return outputDateFormat.format(getDate(value));
		}
	}

	/**
	 * 按：日期/类型/区域，统计犯罪发生次数
	 * */
	public static class CrimeReduceByDate extends Reducer<Text, Text, Text, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			// 犯罪类型及分布区域的二维数组，初始次数=0
			int[][] crimes = new int[categories.size()][districts.size()];
			for (int i = 0; i < categories.size(); i++) {
				for (int j = 0; j < districts.size(); j++) {
					crimes[i][j] = 0;
				}
			}

			// 统计犯罪类型/犯罪区域，发生事件次数
			for (Text crime : values) {
				String[] cols = DataFile.getColumns(crime.toString());
				if (cols.length == 2) {
					if (categoryLookup.containsKey(cols[1])) {
						if (districtLookup.containsKey(cols[0])) {
							int cat = categoryLookup.get(cols[1]);
							int dic = districtLookup.get(cols[0]);
							crimes[cat][dic]++;
						} else {
							log.warning(MessageFormat.format("District {0} not found.", new Object[] { cols[0] }));
						}
					} else {
						log.warning(MessageFormat.format("Category {0} not found.", new Object[] { cols[1] }));
					}
				} else {
					log.warning(MessageFormat.format("Input {0} was in unexpected format", new Object[] { crime }));
				}
			}

			// 格式化输出
			for (int i = 0; i < categories.size(); i++) {
				for (int j = 0; j < districts.size(); j++) {
					if (crimes[i][j] > 0) {
						StringBuffer sb = new StringBuffer();
						// 犯罪类别下标，犯罪区域下表，犯罪次数
						sb.append(i).append(",").append(j).append(",").append(crimes[i][j]);

						Text tv = new Text(sb.toString());
						context.write(key, tv);
					}
				}
			}
		}
	}

	/**
	 * 加载已经计算好的，犯罪类别和犯罪区域数据
	 *
	 * @param categoryReport SanFranciscoCrime-job输出的犯罪类别数据
	 * @param discrictReport SanFranciscoCrime-job输出的犯罪区域数据
	 * @throws IOException
	 */
	public static void setup(String categoryReport, String discrictReport, FileSystem fs) throws IOException {
		categories = DataFile.extractKeys(categoryReport, fs);
		districts = DataFile.extractKeys(discrictReport, fs);
		int i = 0;
		for (String cat : categories) {
			categoryLookup.put(cat, i++);
		}
		i = 0;
		for (String dic : districts) {
			districtLookup.put(dic, i++);
		}
	}

}
