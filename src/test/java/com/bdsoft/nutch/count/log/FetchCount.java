package com.bdsoft.nutch.count.log;

public class FetchCount {

	public static void main(String[] args) {

	}

	long costT = 0L; // 耗时累计
	long kbT = 0L; // 字节累计

	int maxCost = 0; // 最长耗时
	int minCost = 0; // 最短耗时
	FetchCost maxCostFc = null;
	FetchCost minCostFc = null;

	int maxKb = 0; // 最大字节
	int minKb = 0; // 最小字节
	FetchCost maxKbFc = null;
	FetchCost minKbFc = null;

	long costA = 0L;// 平均耗时
	long kbA = 0L; // 平均字节

	public FetchCount() {
		super();
	}

	// 执行统计
	public void doCount(FetchCost fc) {
		costT += fc.getCost();
		kbT += fc.getKb();
		if (fc.getCost() > maxCost) {
			maxCost = fc.getCost();
			maxCostFc = fc;
		} else if (fc.getCost() < minCost) {
			minCost = fc.getCost();
			minCostFc = fc;
		} else if (minCost == 0) {
			minCost = fc.getCost();
		}
		if (fc.getKb() > maxKb) {
			maxKb = fc.getKb();
			maxKbFc = fc;
		} else if (fc.getKb() < minKb) {
			minKb = fc.getKb();
			minKbFc = fc;
		} else if (minKb == 0) {
			minKb = fc.getKb();
		}
	}

	// 计算平均值
	public void doAvg(long total) {
		if (total <= 0) {
			return;
		}
		costA = costT / total;
		kbA = kbT / total;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("抓取统计 > \n");
		sb.append("\t耗时(ms)：");
		sb.append(minCost);
		sb.append(" - ");
		sb.append(maxCost);
		sb.append("\n\t平均耗时：" + costA);
		sb.append("\n\t最短耗时网页：" + minCostFc.getUrl());
		sb.append("\n\t最长耗时网页：" + maxCostFc.getUrl());
		sb.append("\n\t大小(KB)：");
		sb.append(minKb);
		sb.append(" - ");
		sb.append(maxKb);
		sb.append("\n\t平均大小：" + kbA);
		sb.append("\n\t最大网页：" + maxKbFc.getUrl());
		sb.append("\n\t最小网页：" + minKbFc.getUrl());
		return sb.toString();
	}

	public long getCostT() {
		return costT;
	}

	public void setCostT(long costT) {
		this.costT = costT;
	}

	public long getKbT() {
		return kbT;
	}

	public void setKbT(long kbT) {
		this.kbT = kbT;
	}

	public int getMaxCost() {
		return maxCost;
	}

	public void setMaxCost(int maxCost) {
		this.maxCost = maxCost;
	}

	public int getMinCost() {
		return minCost;
	}

	public void setMinCost(int minCost) {
		this.minCost = minCost;
	}

	public int getMaxKb() {
		return maxKb;
	}

	public void setMaxKb(int maxKb) {
		this.maxKb = maxKb;
	}

	public int getMinKb() {
		return minKb;
	}

	public void setMinKb(int minKb) {
		this.minKb = minKb;
	}

	public FetchCost getMaxCostFc() {
		return maxCostFc;
	}

	public void setMaxCostFc(FetchCost maxCostFc) {
		this.maxCostFc = maxCostFc;
	}

	public FetchCost getMinCostFc() {
		return minCostFc;
	}

	public void setMinCostFc(FetchCost minCostFc) {
		this.minCostFc = minCostFc;
	}

	public FetchCost getMaxKbFc() {
		return maxKbFc;
	}

	public void setMaxKbFc(FetchCost maxKbFc) {
		this.maxKbFc = maxKbFc;
	}

	public FetchCost getMinKbFc() {
		return minKbFc;
	}

	public void setMinKbFc(FetchCost minKbFc) {
		this.minKbFc = minKbFc;
	}

}
