package com.bdsoft.nutch.bean;

import java.io.Serializable;

import com.wlu.orm.hbase.annotation.DatabaseField;
import com.wlu.orm.hbase.annotation.DatabaseTable;

/**
 * 品牌库
 */
@DatabaseTable(tableName = "brands")
public class Brand implements Serializable  {

	private static final long serialVersionUID = 1L;
	@DatabaseField(familyName = "properties", qualifierName = "alias")
	private String alias; // 品牌等价词

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
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}


}
