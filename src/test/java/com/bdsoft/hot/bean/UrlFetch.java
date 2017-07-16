package com.bdsoft.hot.bean;

public class UrlFetch {

	private int id;
	private String url;
	private String cat1;
	private String cat2;

	public UrlFetch() {
		super();
	}

	public UrlFetch(int id, String url, String cat1, String cat2) {
		super();
		this.id = id;
		this.url = url;
		this.cat1 = cat1;
		this.cat2 = cat2;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

}
