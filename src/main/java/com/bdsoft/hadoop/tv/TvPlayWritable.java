/**
 * TvPlayWritable.java
 * com.bdsoft.hadoop.tv
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.tv;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * 电视剧播放数据 
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-14
 * @version  1.0.0
 */
public class TvPlayWritable implements WritableComparable<Object> {

	// 电视剧       平台           播放数		 收藏数    评论数    踩数 	赞数
	// 你迷上了我    1    37421    16    6    0    0

	private int plyNum;
	private int colNum;
	private int cmnNum;
	private int caiNum;
	private int zanNum;

	public TvPlayWritable() {
	}

	public void set(int pn, int cn, int cmn, int can, int zn) {
		this.plyNum = pn;
		this.colNum = cn;
		this.cmnNum = cmn;
		this.caiNum = can;
		this.zanNum = zn;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(plyNum);
		out.writeInt(colNum);
		out.writeInt(cmnNum);
		out.writeInt(caiNum);
		out.writeInt(zanNum);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.plyNum = in.readInt();
		this.colNum = in.readInt();
		this.cmnNum = in.readInt();
		this.caiNum = in.readInt();
		this.zanNum = in.readInt();
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	public int getPlyNum() {
		return plyNum;
	}

	public int getColNum() {
		return colNum;
	}

	public int getCmnNum() {
		return cmnNum;
	}

	public int getCaiNum() {
		return caiNum;
	}

	public int getZanNum() {
		return zanNum;
	}

}
