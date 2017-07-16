/**
 * WeiboWritable.java
 * com.bdsoft.hadoop.weibo
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
 */

package com.bdsoft.hadoop.weibo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * 微博数据统计
 * <p>
 * 
 * @author 丁辰叶
 * @date 2015-12-14
 * @version 1.0.0
 */
public class WeiboWritable implements WritableComparable<Object> {

	private int fans; // 粉丝数
	private int follows; // 关注数
	private int weibos; // 微博数

	public WeiboWritable() {
	}

	public void set(int fans, int follows, int weibos) {
		this.fans = fans;
		this.follows = follows;
		this.weibos = weibos;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.write(getFans());
		out.write(getFollows());
		out.write(getWeibos());
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.fans = in.readInt();
		this.follows = in.readInt();
		this.weibos = in.readInt();
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	public int getFans() {
		return fans;
	}

	public int getFollows() {
		return follows;
	}

	public int getWeibos() {
		return weibos;
	}

}
