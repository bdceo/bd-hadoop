package com.bdsoft.hot.bean;

import java.io.Serializable;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.List;

public class Product implements Serializable, Cloneable, SQLData {

	// 每次增删字段应重新生成
	private static final long serialVersionUID = -3278773120532938531L;
	private String id = "";// 序列号
	private String classic = "";// 产品分类
	private String classicCode = "";// 产品分类
	private String shortName = "";// 产品简称
	private String productName = "";// 产品名称
	private String price = "0";// 价格
	private Double maxPrice = 0.0;// 最大价格
	private Double minPrice = 0.0;// 最小价格
	private String contents = "";// 产品详情
	private String smallPic = "";// 产品小图
	private String bigPic = "";// 产品大图
	private String seller = "";// //卖家称号
	private int isSeller = 0;// 是否是商家 1是 0否
	private int sellerCount = 0;
	private String website = "";// 产品商家网站
	private String product_url = "";// 产品详情地址
	private String pid = "";// 产品ID
	private String cid = "";// 渠道ID
	private String keyword = "";// 相关性关键词
	private float score = 0.0f;
	private List<Seller> sellerList = null;
	private int productLikerCount = 0;
	private long groupbuyEndTime = 0;
	private int productBuyerCount = 0;
	private int groupbuyOnTime = 0;// 是否正在团购
	private int type = 0;// 商品分类 0-普通商品 1-团购商品 2-特价商品 等
	private String brand = "";// 品牌
	private String mparams = "";// 主要配置参数
	private String orgPic = "";// 产品原始图片
	private String sellerCode = "";// 卖家编码

	public Product() {
	}

	public Product(String id, String classic, String classicCode,
			String shortName, String productName, String price,
			Double maxPrice, Double minPrice, String contents, String smallPic,
			String bigPic, String seller, int isSeller, int sellerCount,
			String website, String product_url, String pid, String cid,
			String keyword, float score, List<Seller> sellerList,
			int productLikerCount, long groupbuyEndTime, int productBuyerCount,
			int groupbuyOnTime, int type, String brand) {
		super();
		this.id = id;
		this.classic = classic;
		this.classicCode = classicCode;
		this.shortName = shortName;
		this.productName = productName;
		this.price = price;
		this.maxPrice = maxPrice;
		this.minPrice = minPrice;
		this.contents = contents;
		this.smallPic = smallPic;
		this.bigPic = bigPic;
		this.seller = seller;
		this.isSeller = isSeller;
		this.sellerCount = sellerCount;
		this.website = website;
		this.product_url = product_url;
		this.pid = pid;
		this.cid = cid;
		this.keyword = keyword;
		this.score = score;
		this.sellerList = sellerList;
		this.productLikerCount = productLikerCount;
		this.groupbuyEndTime = groupbuyEndTime;
		this.productBuyerCount = productBuyerCount;
		this.groupbuyOnTime = groupbuyOnTime;
		this.type = type;
		this.brand = brand;
	}

	public int getSellerCount() {
		return sellerCount;
	}

	public void setSellerCount(int sellerCount) {
		this.sellerCount = sellerCount;
	}

	public String getClassic() {
		return classic;
	}

	public void setClassic(String classic) {
		this.classic = classic;
	}

	public String getClassicCode() {
		return classicCode;
	}

	public void setClassicCode(String classicCode) {
		this.classicCode = classicCode;
	}

	public int getIsSeller() {
		return isSeller;
	}

	public void setIsSeller(int isSeller) {
		this.isSeller = isSeller;
	}

	public int getGroupbuyOnTime() {
		return groupbuyOnTime;
	}

	public void setGroupbuyOnTime(int groupbuyOnTime) {
		this.groupbuyOnTime = groupbuyOnTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
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

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
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

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public List<Seller> getSellerList() {
		return sellerList;
	}

	public void setSellerList(List<Seller> sellerList) {
		this.sellerList = sellerList;
	}

	public int getProductLikerCount() {
		return productLikerCount;
	}

	public void setProductLikerCount(int productLikerCount) {
		this.productLikerCount = productLikerCount;
	}

	public long getGroupbuyEndTime() {
		return groupbuyEndTime;
	}

	public void setGroupbuyEndTime(long groupbuyEndTime) {
		this.groupbuyEndTime = groupbuyEndTime;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
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

	public String getSellerCode() {
		return sellerCode;
	}

	public void setSellerCode(String sellerCode) {
		this.sellerCode = sellerCode;
	}

	public int getProductBuyerCount() {
		return productBuyerCount;
	}

	public void setProductBuyerCount(int productBuyerCount) {
		this.productBuyerCount = productBuyerCount;
	}

	public Double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(Double maxPrice) {
		this.maxPrice = maxPrice;
	}

	public Double getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Double minPrice) {
		this.minPrice = minPrice;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "Product [productName=" + productName + ",bigPic=" + bigPic
				+ ", brand=" + brand + ", cid=" + cid + ", classic=" + classic
				+ ", classicCode=" + classicCode + ", contents=" + contents
				+ ", groupbuyEndTime=" + groupbuyEndTime + ", groupbuyOnTime="
				+ groupbuyOnTime + ", id=" + id + ", isSeller=" + isSeller
				+ ", keyword=" + keyword + ", maxPrice=" + maxPrice
				+ ", minPrice=" + minPrice + ", mparams=" + mparams
				+ ", orgPic=" + orgPic + ", pid=" + pid + ", price=" + price
				+ ", productBuyerCount=" + productBuyerCount
				+ ", productLikerCount=" + productLikerCount + ", product_url="
				+ product_url + ", score=" + score + ", seller=" + seller
				+ ", sellerCode=" + sellerCode + ", sellerCount=" + sellerCount
				+ ", sellerList=" + sellerList + ", shortName=" + shortName
				+ ", smallPic=" + smallPic + ", type=" + type + ", website="
				+ website + "]";
	}

	public String toBaseString() {
		return "Product [productName=" + productName + ", pid=" + pid
				+ ", classicCode=" + classicCode + ", classic=" + classic
				+ ", price=" + price + ", maxPrice=" + maxPrice + ", minPrice="
				+ minPrice + ", product_url=" + product_url + ", seller="
				+ seller + ", type=" + type + "]";
	}

	@Override
	public String getSQLTypeName() throws SQLException {
		// //TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readSQL(SQLInput stream, String typeName) throws SQLException {
		// //TODO Auto-generated method stub

	}

	@Override
	public void writeSQL(SQLOutput stream) throws SQLException {
		// //TODO Auto-generated method stub

	}

}
