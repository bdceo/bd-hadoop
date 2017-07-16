/**
 * SanFranciscoCrimeJob.java
 * com.bdsoft.hadoop.crime
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.crime;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.bdsoft.hadoop.BaseConfig;

/**
 * 旧金山犯罪数据分析
 * <p>
 * 统计：
 * 不同犯罪类别在周时段内发生的次数
 * 不同犯罪区域在周时段内发生的次数
 * <p>
 * 时段系统（bucketed system），在物料需求计划（MRP）、配销资源规划（DRP）或其他时程化（time-phased）的系统里，
 * 所有时程化的资料都累积在同一时期，或称时段（buchet）。如果累积的时间是以周为时间单位，此系统就称为周时段（weekly buckets）。
 * 周时段（weekly buckets）即是一种以周为单位的统计方式 
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */
public class SanFranciscoCrimeJob extends CrimeJobBase implements Tool {

	private static Logger log = Logger.getLogger(SanFranciscoCrimeJob.class.getCanonicalName());

	/**
	 * job运行入口
	 */
	public static void main(String[] args) throws Exception {
		String[] cmds = new String[] { BaseConfig.HDFS_PATH + "/bdceo/crime/crime.txt",
				BaseConfig.HDFS_PATH + "/bdceo/crime/out1", BaseConfig.HDFS_PATH + "/bdceo/crime/out2" };
		System.exit(ToolRunner.run(new Configuration(), new SanFranciscoCrimeJob(), cmds));
	}

	@Override
	public int run(String[] args) throws Exception {
		// 任务1
		Configuration conf1 = new Configuration();
		Path out1 = new Path(args[1]); // 输出路径
		FileSystem hdfs1 = out1.getFileSystem(conf1);
		if (hdfs1.isDirectory(out1)) {// 清空输出路径
			hdfs1.delete(out1, true);
		}

		Job job1 = new Job(conf1, "crime");
		job1.setJarByClass(SanFranciscoCrimeJob.class);

		job1.setMapperClass(CategoryMapByDate.class);
		job1.setReducerClass(CrimeReduceByWeek.class);
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job1, new Path(args[0]));
		FileOutputFormat.setOutputPath(job1, new Path(args[1]));

		// 任务2
		Configuration conf2 = new Configuration();
		Path out2 = new Path(args[2]);
		FileSystem hdfs2 = out2.getFileSystem(conf2);
		if (hdfs2.isDirectory(out2)) {// 清空输出路径
			hdfs2.delete(out2, true);
		}

		Job job2 = new Job(conf2, "crime");
		job2.setJarByClass(SanFranciscoCrimeJob.class);

		job2.setMapperClass(DistrictMapByDate.class);
		job2.setReducerClass(CrimeReduceByWeek.class);
		job2.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job2, new Path(args[0]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));

		// 任务依赖关系
		ControlledJob cjob1 = new ControlledJob(conf1);
		cjob1.setJob(job1);
		ControlledJob cjob2 = new ControlledJob(conf2);
		cjob2.setJob(job2);

		//		cjob2.addDependingJob(cjob1); // 2依赖1

		JobControl jc = new JobControl("crime-job");
		jc.addJob(cjob1);
		jc.addJob(cjob2);

		new Thread(jc).start();
		while (true) {
			if (jc.allFinished()) {
				jc.stop();
				break;
			}
		}

		return 0;
	}

	/**
	 * 相似mapper业务的公共父类提取 
	 */
	public static class AMapper extends Mapper<LongWritable, Text, Text, Text> {

		protected int keyId = 0; // 提取数据集中key的索引
		protected int valId = 0; // 提取数据集中val的索引

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			try {
				String[] cols = DataFile.getColumns(line);
				if (cols != null) {
					// 防止数组越界
					if (cols.length >= (DISTRICT_COLUMN_INDEX + 1)) {
						// 过滤文件第一行头部内容
						if (!"data".equalsIgnoreCase(cols[valId])) {
							Text tk = new Text();
							tk.set(cols[keyId]);

							Text tv = new Text();
							tv.set(cols[valId]);
							context.write(tk, tv);
						}
					} else {
						log.warning(MessageFormat.format("Data {0} did not parse into columns.", new Object[] { line }));
					}
				} else {
					log.warning(MessageFormat.format("Data {0} did not parse into columns.", new Object[] { line }));
				}
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, MessageFormat.format("Expected {0} to be a number.\n", new Object[] { line }), e);
			} catch (IOException e) {
				log.log(Level.WARNING, MessageFormat.format("Cannot parse {0} into columns.\n", new Object[] { line }),
						e);
			}
		}
	}

	/**
	 * 输出key=犯罪类别，val=日期
	 */
	public static class CategoryMapByDate extends AMapper {
		public CategoryMapByDate() {
			this.keyId = CATEGORY_COLUMN_INDEX;
			this.valId = DATE_COLUMN_INDEX;
		}
	}

	/**
	 * 输出key=犯罪区域，val=日期
	 */
	public static class DistrictMapByDate extends AMapper {
		public DistrictMapByDate() {
			this.keyId = DISTRICT_COLUMN_INDEX;
			this.valId = DATE_COLUMN_INDEX;
		}
	}

	/**
	 * 按周时段统计，犯罪类型和犯罪区域的发生次数
	 * */
	public static class CrimeReduceByWeek extends Reducer<Text, Text, Text, Text> {

		// 输入：key=犯罪类型/犯罪区域，value=事发时间
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
				InterruptedException {
			// 先合并所有发生的时间段
			List<String> incidents = new ArrayList<String>();
			for (Text val : values) {
				incidents.add(val.toString());
			}

			if (incidents.size() > 0) {
				// 排序
				Collections.sort(incidents);

				// 初始化周时段:针对1-3月数据分析，周时段（weekly buckets）最大为15，所以weekSummary长度为15即可
				Map<Integer, Integer> weekSummary = new HashMap<Integer, Integer>();
				for (int i = 0; i < 16; i++) {
					weekSummary.put(i, 0);
				}

				// 统计每个周时段内，事件发生次数
				for (String incidentDay : incidents) {
					try {
						Date d = getDate(incidentDay);

						Calendar cal = Calendar.getInstance();
						cal.setTime(d);
						int week = cal.get(Calendar.WEEK_OF_MONTH);// 这个月第几周
						int month = cal.get(Calendar.MONTH);// 第几个月，从0开始
						// 如果累积的时间是以周为时间单位，此系统就称为周时段（weekly buckets）。
						// 周时段的计算公式，最大为15，它只是一种统计方式，不必深究
						int bucket = (month * 5) + week;
						// 统计每个时间段内，事件发生次数
						if (weekSummary.containsKey(bucket)) {
							weekSummary.put(bucket, weekSummary.get(bucket).intValue() + 1);
						} else {
							weekSummary.put(bucket, 1);
						}
					} catch (ParseException e) {
						log.warning(MessageFormat.format("Invalid date {0}", new Object[] { incidentDay }));
					}
				}

				// 格式化输出
				StringBuffer sb = new StringBuffer();
				boolean first = true;
				for (int week : weekSummary.keySet()) {
					if (first) {
						first = false;
					} else {
						sb.append(",");
					}
					sb.append(weekSummary.get(week).toString());
				}

				// 输出：key=犯罪类别/区域，value=每个周时段次数
				Text tv = new Text(sb.toString());
				context.write(key, tv);
			}
		}
	}
}
