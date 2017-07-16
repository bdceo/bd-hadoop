package com.bdsoft.hot.zol;

// 中关村入口地址-商品列表Feed
public class ZOLProductFeed {

	private int order;// 排名
	private String url;// 商品链接
	private int onDays;// 上榜天数

	private String cat1;// 商品一级分类
	private String cat2;// 二级分类

	private String pname;

	public ZOLProductFeed() {
		super();
	}

	public ZOLProductFeed(int order, String url, int onDays, String c1,
			String c2, String pname) {
		super();
		this.order = order;
		this.url = url;
		this.onDays = onDays;
		this.cat1 = c1;
		this.cat2 = c2;
		this.pname = pname;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCat1() {
		return cat1;
	}

	public void setCat1(String cat1) {
		this.cat1 = cat1;
	}

	public String getCat2() {
		return cat2;
	}

	public void setCat2(String cat2) {
		this.cat2 = cat2;
	}

	public int getOnDays() {
		return onDays;
	}

	public void setOnDays(int onDays) {
		this.onDays = onDays;
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

}
