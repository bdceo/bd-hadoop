package com.bdsoft.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import com.bdsoft.hadoop.weather.USTemperature;

/**
 * 美国气象统计测试
 */
public class USTemperatureTester {

	// 单独测试map
	private Mapper mapper;
	private MapDriver driver;

	// 单独测试reduce
	private Reducer reducer;
	private ReduceDriver redDriver;

	// 集成测试map/reduce
	private MapReduceDriver mrDriver;

	/**
	 * 初始化
	 */
	@Before
	public void init() {
		mapper = new USTemperature.TempMapper();
		driver = new MapDriver(mapper);

		reducer = new USTemperature.TempReduce();
		redDriver = new ReduceDriver(reducer);

		mrDriver = new MapReduceDriver<>(mapper, reducer);
	}

	/**
	 * 单测mapper
	 */
	@Test
	public void testMapper() throws IOException {
		String line = "2005 01 01 14   100   -33 10146   260    26     8     0 -9999";
		driver.withInput(new LongWritable(), new Text(line))
				.withOutput(new Text("xxx"), new IntWritable(100)).runTest();
	}

	/**
	 * 单测reducer
	 */
	@Test
	public void testReducer() throws IOException {
		String key = "xxx";
		List values = new ArrayList();
		values.add(new IntWritable(100));
		values.add(new IntWritable(200));

		redDriver.withInput(new Text(key), values)
				.withOutput(new Text(key), new IntWritable(150)).runTest();
	}

	/**
	 * 完整测试：mapper+reducer
	 */
	@Test
	public void testMR() throws IOException {
		String line = "2005 01 01 14   100   -33 10146   260    26     8     0 -9999";
		String line2 = "2005 01 01 14   200   -33 10146   260    26     8     0 -9999";
		mrDriver.withInput(new LongWritable(), new Text(line))
				.withInput(new LongWritable(), new Text(line2))
				.withOutput(new Text("xxx"), new IntWritable(150)).runTest();
	}

}
