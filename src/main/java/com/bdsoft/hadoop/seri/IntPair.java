/**
 * IntPair.java
 * com.bdsoft.hadoop.seri
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.seri;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * 二次排序 
 *
 * @author   丁辰叶
 * @date	 2016-2-16
 * @version  1.0.0
 */
public class IntPair implements WritableComparable<IntPair> {

	int first;
	int second;

	public void set(int f, int s) {
		first = f;
		second = s;
	}

	public int getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(first);
		out.writeInt(second);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		first = in.readInt();
		second = in.readInt();
	}

	@Override
	public int compareTo(IntPair o) {
		if (first != o.getFirst()) {
			return (first < o.getFirst()) ? -1 : 1;
		} else if (second != o.getSecond()) {
			return (second < o.getSecond()) ? -1 : 1;
		} else {
			return 0;
		}
	}

	@Override
	public int hashCode() {
		return first * 157 + second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof IntPair) {
			IntPair ip = (IntPair) obj;
			return (this.getFirst() == ip.getFirst()) && (this.getSecond() == ip.getSecond());
		} else {
			return false;
		}
	}

	// 自定义分区函数，根据IntPair中的first分区
	public static class FirstPartioner extends Partitioner<IntPair, IntWritable> {

		@Override
		public int getPartition(IntPair key, IntWritable value, int numPartitions) {
			return Math.abs(key.getFirst() * 127) % numPartitions;
		}
	}

	// 自动分组函数，实现分区内的数据分组
	public static class GroupComparator extends WritableComparator {

		public GroupComparator() {
			super(IntPair.class, true);
		}

		@Override
		public int compare(WritableComparable a, WritableComparable b) {
			IntPair i1 = (IntPair) a;
			IntPair i2 = (IntPair) b;

			int f1 = i1.getFirst();
			int f2 = i2.getFirst();

			return (f1 == f2) ? 0 : ((f1 < f2) ? -1 : 1);
		}

	}
}
