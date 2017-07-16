package com.bdsoft.nutch.count.log;

public class FetchCost {

	private int cost;
	private int kb;
	private String code;
	private String url;

	public FetchCost() {
		super();
	}

	public FetchCost(int cost, int kb, String code, String url) {
		super();
		this.cost = cost;
		this.kb = kb;
		this.code = code;
		this.url = url;
	}

	public FetchCost(String cost, String kb, String code, String url) {
		super();
		this.cost = Integer.parseInt(cost);
		this.kb = Integer.parseInt(kb);
		this.code = code;
		this.url = url;
	}

	@Override
	public String toString() {
		return "FetchCost [cost=" + cost + ", kb=" + kb + ", code=" + code
				+ ", url=" + url + "]";
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getKb() {
		return kb;
	}

	public void setKb(int kb) {
		this.kb = kb;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
