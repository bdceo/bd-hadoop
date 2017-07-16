package com.bdsoft.hot.bean;

import java.io.Serializable;

public class UrlQueue implements Serializable,Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1407403341065049062L;
	private int queueId;//url 编号
	private String url;//具体url
	private String urlcode;//
	private String request_charset;
	private String response_charset;
	private String grade;
	private String parserImpl;
	private int type;
	private String seller;
	private int state;
	
	private String cat1;
	private String cat2;
	
	public UrlQueue(){}	

	public UrlQueue(int queueId, String url, String urlcode,
			String request_charset, String response_charset, String grade,
			String parserImpl, int type, String seller, int state) {
		super();
		this.queueId = queueId;
		this.url = url;
		this.urlcode = urlcode;
		this.request_charset = request_charset;
		this.response_charset = response_charset;
		this.grade = grade;
		this.parserImpl = parserImpl;
		this.type = type;
		this.seller = seller;
		this.state = state;
	}

	public int getQueueId() {
		return queueId;
	}
	public void setQueueId(int queueId) {
		this.queueId = queueId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getRequest_charset() {
		return request_charset;
	}
	public void setRequest_charset(String request_charset) {
		this.request_charset = request_charset;
	}
	public String getResponse_charset() {
		return response_charset;
	}
	public void setResponse_charset(String response_charset) {
		this.response_charset = response_charset;
	}
	public String getGrade() {
		return grade;
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

	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getParserImpl() {
		return parserImpl;
	}
	public void setParserImpl(String parserImpl) {
		this.parserImpl = parserImpl;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSeller() {
		return seller;
	}
	public void setSeller(String seller) {
		this.seller = seller;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	public String getUrlcode() {
		return urlcode;
	}

	public void setUrlcode(String urlcode) {
		this.urlcode = urlcode;
	}
	public Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}

	@Override
	public String toString() {
		return "UrlQueue [url=" + url + ", request_charset=" + request_charset
				+ ", grade=" + grade + ", parserImpl=" + parserImpl + ", type="
				+ type + ", seller=" + seller + ", state=" + state + "]";
	}
	
	
	
}
