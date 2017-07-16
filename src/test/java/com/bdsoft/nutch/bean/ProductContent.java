package com.bdsoft.nutch.bean;

import java.io.Serializable;

import com.wlu.orm.hbase.annotation.DatabaseField;
import com.wlu.orm.hbase.annotation.DatabaseTable;

/**
 * 原始抓取HTML原始文本信息
 */
@DatabaseTable(tableName = "productContent")
public class ProductContent implements Serializable {
	//主键为url翻转，例如：http://www.360buy.com/product/123.html  反转为  http://com.360buy.www/product/123.html
	private static final long serialVersionUID = 1L;
	@DatabaseField(familyName = "properties" ,qualifierName="conteneHtml")
	private String conteneHtml; // 页面的HTML字符

	@DatabaseField(id = true)
	protected String id;
	// 增加字段 商品状态码（对应状态表） 创建时间 更新时间
	@DatabaseField(familyName = "properties", qualifierName = "creatTime")
	protected String creatTime = System.currentTimeMillis()+""; // 创建时间
	@DatabaseField(familyName = "properties", qualifierName = "updateTime")
	protected String updateTime = System.currentTimeMillis()+""; // 更新时间
	@DatabaseField(familyName = "properties", qualifierName = "status")
	protected int status = 0; // 状态码 0:有效,1:无效
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCreatTime() {
		return creatTime;
	}
	public void setCreatTime(String creatTime) {
		this.creatTime = creatTime;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public ProductContent(){
		
	}
	
	public ProductContent(String id,String conteneHtml){
		this.id = id;
		this.conteneHtml = conteneHtml;
	}
	

	public String getConteneHtml() {
		return conteneHtml;
	}

	public void setConteneHtml(String conteneHtml) {
		this.conteneHtml = conteneHtml;
	}

	@Override
	public String toString() {
		return conteneHtml;
	}

}
