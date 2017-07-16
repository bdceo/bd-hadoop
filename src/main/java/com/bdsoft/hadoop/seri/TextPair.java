package com.bdsoft.hadoop.seri;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * 自定义序列化对象
 */
public class TextPair implements WritableComparable<TextPair> {

	private Text first;
	private Text second;

	public TextPair() {
		new TextPair("", "");
	}

	public TextPair(String f, String s) {
		new TextPair(new Text(f), new Text(s));
	}

	public TextPair(Text f, Text s) {
		this.first = f;
		this.second = s;
	}

	// 序列化
	@Override
	public void write(DataOutput out) throws IOException {
		first.write(out);
		second.write(out);
	}

	// 反序列化
	@Override
	public void readFields(DataInput in) throws IOException {
		first.readFields(in);
		second.readFields(in);
	}

	/**
	 * HashPartitioner使用hashcode()方法来选择reduce分区
	 */
	@Override
	public int hashCode() {
		return first.hashCode() * 1988 + second.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TextPair) {
			TextPair p = (TextPair) o;
			return first.equals(p.first) && second.equals(p.second);
		}
		return false;
	}

	/**
	 * 对象排序规则
	 */
	@Override
	public int compareTo(TextPair o) {
		int cmp = this.first.compareTo(o.first);
		if (cmp != 0) {
			return cmp;
		}
		return this.second.compareTo(o.second);
	}

	@Override
	public String toString() {
		return first + "\t" + second;
	}

	public Text getFirst() {
		return first;
	}

}
