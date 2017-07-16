package com.bdsoft.hot.bean;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Seller implements Serializable{
	//每次增删字段应重新生成
	private static final long serialVersionUID = -7474629816647048244L;
	private String sellerName = null;
	private String price = null;
	private String sellerUrl = null;
	private String productName = null;
	private int type = 0;
	
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getSellerUrl() {
		return sellerUrl;
	}
	public void setSellerUrl(String sellerUrl) {
		this.sellerUrl = sellerUrl;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String toString() {
        return new ReflectionToStringBuilder(this).toString();
	}
}
