/**
 * ScoreWritable.java
 * com.bdsoft.hadoop.score 
*/
package com.bdsoft.hadoop.score;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * 学生分数
 * 
 * 数据格式参考：19020090017 小讲 90 99 100 89 95
 * 
 * @author	丁辰叶
 * @date	2015-12-11
 */
public class ScoreWritable implements WritableComparable<Object> {

	// 总学科数
	public static final int subject = 5;

	private float yuwen;
	private float shuxue;
	private float yingyu;
	private float wuli;
	private float huaxue;

	public ScoreWritable() {
	}

	public void set(float wu, float sx, float yy, float wl, float hx) {
		this.yuwen = wu;
		this.shuxue = sx;
		this.yingyu = yy;
		this.wuli = wl;
		this.huaxue = hx;
	}

	public float getTotal() {
		return this.yingyu + this.shuxue + this.yingyu + this.wuli + this.huaxue;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeFloat(yuwen);
		out.writeFloat(shuxue);
		out.writeFloat(yingyu);
		out.writeFloat(wuli);
		out.writeFloat(huaxue);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.yuwen = in.readFloat();
		this.shuxue = in.readFloat();
		this.yingyu = in.readFloat();
		this.wuli = in.readFloat();
		this.huaxue = in.readFloat();
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	public float getYuwen() {
		return yuwen;
	}

	public void setYuwen(float yuwen) {
		this.yuwen = yuwen;
	}

	public float getShuxue() {
		return shuxue;
	}

	public void setShuxue(float shuxue) {
		this.shuxue = shuxue;
	}

	public float getYingyu() {
		return yingyu;
	}

	public void setYingyu(float yingyu) {
		this.yingyu = yingyu;
	}

	public float getWuli() {
		return wuli;
	}

	public void setWuli(float wuli) {
		this.wuli = wuli;
	}

	public float getHuaxue() {
		return huaxue;
	}

	public void setHuaxue(float huaxue) {
		this.huaxue = huaxue;
	}

}
