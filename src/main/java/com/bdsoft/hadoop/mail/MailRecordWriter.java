/**
 * MailRecordWriter.java
 * com.bdsoft.hadoop.mail
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.mail;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * key-value记录输出对象
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-12
 * @version  1.0.0
 */
public class MailRecordWriter<K, V> extends RecordWriter<K, V> {

	private static final String utf8 = "UTF-8";
	private static final byte[] newline;

	protected DataOutputStream out;
	private final byte[] keyValueSeparator;

	static {
		try {
			newline = "\n".getBytes(utf8);//换行符 "/n"不对  
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("can't find " + utf8 + " encoding");
		}
	}

	public MailRecordWriter(DataOutputStream out) {
		this(out, "/t");
	}

	/**
	 * 创建 MailRecordWriter对象.
	 *
	 * @param out 输出流
	 * @param keyValueSeparator 输出key-value时的分隔符
	 */
	public MailRecordWriter(DataOutputStream out, String keyValueSeparator) {
		this.out = out;
		try {
			this.keyValueSeparator = keyValueSeparator.getBytes(utf8);
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalArgumentException("can't find " + utf8 + " encoding");
		}
	}

	@Override
	public synchronized void write(K key, V value) throws IOException {
		boolean nullKey = key == null || key instanceof NullWritable;
		boolean nullValue = value == null || value instanceof NullWritable;
		if (nullKey && nullValue) {
			return;
		}
		if (!nullKey) {
			writeObject(key);
		}
		if (!(nullKey || nullValue)) {
			out.write(keyValueSeparator);
		}
		if (!nullValue) {
			writeObject(value);
		}
		out.write(newline);
	}

	/**
	 * 输出一个对象
	 */
	private void writeObject(Object o) throws IOException {
		if (o instanceof Text) {
			Text to = (Text) o;
			out.write(to.getBytes(), 0, to.getLength());
		} else {
			out.write(o.toString().getBytes(utf8));
		}
	}

	@Override
	public synchronized void close(TaskAttemptContext context) throws IOException {
		out.close();
	}
}