package com.bdsoft.nutch.bean;

import java.io.Serializable;

import com.wlu.orm.hbase.annotation.DatabaseField;
import com.wlu.orm.hbase.annotation.DatabaseTable;

/**
 * 标准商品
 */
@DatabaseTable(tableName = "stdProducts")
public class StdProduct implements Serializable {
	private static final long serialVersionUID = 1L;

	@DatabaseField(familyName = "properties", qualifierName = "classic")
	private String classic; // 商品导航条，可能包含分类，品牌，短名称
	@DatabaseField(familyName = "properties", qualifierName = "category")
	private String category; // 商品原始分类
	@DatabaseField(familyName = "properties", qualifierName = "theThird")
	private String theThird; // 第三方
	@DatabaseField(familyName = "properties", qualifierName = "shortName")
	private String shortName; // 商品简称
	@DatabaseField(familyName = "properties", qualifierName = "productName")
	private String productName; // 商品名称
	@DatabaseField(familyName = "properties", qualifierName = "title")
	private String title; // 商品页面title
	@DatabaseField(familyName = "properties", qualifierName = "keyword")
	private String keyword; // 相关性关键词
	@DatabaseField(familyName = "properties", qualifierName = "price")
	private String price; // 现价格
	@DatabaseField(familyName = "properties", qualifierName = "maketPrice")
	private String maketPrice; // 市场价格
	@DatabaseField(familyName = "properties", qualifierName = "discountPrice")
	private String discountPrice; // 优惠价格
	@DatabaseField(familyName = "properties", qualifierName = "contents")
	private String contents; // 商品详情
	@DatabaseField(familyName = "properties", qualifierName = "mparams")
	private String mparams; // 主要配置参数
	@DatabaseField(familyName = "properties", qualifierName = "orgPic")
	private String orgPic; // 商品原始图片地址
	@DatabaseField(familyName = "properties", qualifierName = "smallPic")
	private String smallPic; // 商品小图
	@DatabaseField(familyName = "properties", qualifierName = "bigPic")
	private String bigPic; // 商品大图
	@DatabaseField(familyName = "properties", qualifierName = "sellerCode")
	private String sellerCode; // 商家编码
	@DatabaseField(familyName = "properties", qualifierName = "seller")
	private String seller; // 商家名称
	@DatabaseField(familyName = "properties", qualifierName = "product_url")
	private String product_url; // 商品详情地址
	@DatabaseField(familyName = "properties", qualifierName = "pid")
	private String pid; // 产品ID
	@DatabaseField(familyName = "properties", qualifierName = "brand")
	private String brand; // 品牌
	@DatabaseField(familyName = "properties", qualifierName = "isCargo")
	private int isCargo; // 有货 1 ,无货0

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
	
	
	public String getClassic() {

		return classic;
	}

	public void setClassic(String classic) {

		this.classic = classic;
	}

	public String getShortName() {

		return shortName;
	}

	public void setShortName(String shortName) {

		this.shortName = shortName;
	}

	public String getProductName() {

		return productName;
	}

	public void setProductName(String productName) {

		this.productName = productName;
	}

	public String getKeyword() {

		return keyword;
	}

	public void setKeyword(String keyword) {

		this.keyword = keyword;
	}

	public String getPrice() {

		return price;
	}

	public void setPrice(String price) {

		this.price = price ;
	}

	public String getMaketPrice() {

		return maketPrice;
	}

	public void setMaketPrice(String maketPrice) {

		this.maketPrice = maketPrice;
	}

	public String getDiscountPrice() {

		return discountPrice;
	}

	public void setDiscountPrice(String discountPrice) {

		this.discountPrice = discountPrice;
	}

	public String getContents() {

		return contents;
	}

	public void setContents(String contents) {

		this.contents = contents;
	}

	public String getMparams() {

		return mparams;
	}

	public void setMparams(String mparams) {

		this.mparams = mparams;
	}

	public String getOrgPic() {

		return orgPic;
	}

	public void setOrgPic(String orgPic) {

		this.orgPic = orgPic;
	}

	public String getSmallPic() {

		return smallPic;
	}

	public void setSmallPic(String smallPic) {

		this.smallPic = smallPic;
	}

	public String getBigPic() {

		return bigPic;
	}

	public void setBigPic(String bigPic) {

		this.bigPic = bigPic;
	}

	public String getSellerCode() {

		return sellerCode;
	}

	public void setSellerCode(String sellerCode) {

		this.sellerCode = sellerCode;
	}

	public String getSeller() {

		return seller;
	}

	public void setSeller(String seller) {

		this.seller = seller;
	}

	public String getProduct_url() {

		return product_url;
	}

	public void setProduct_url(String product_url) {

		this.product_url = product_url;
	}

	public String getPid() {

		return pid;
	}

	public void setPid(String pid) {

		this.pid = pid;
	}

	public String getBrand() {

		return brand;
	}

	public void setBrand(String brand) {

		this.brand = brand;
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		this.title = title;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setTheThird(String theThird) {
		this.theThird = theThird;
	}

	public String getTheThird() {
		return theThird;
	}
	public void setIsCargo(int isCargo) {
		this.isCargo = isCargo;
	}
	public int getIsCargo() {
		return isCargo;
	}

}
