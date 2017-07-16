package com.bdsoft.nutch.bean;

import java.io.Serializable;

import com.wlu.orm.hbase.annotation.DatabaseField;
import com.wlu.orm.hbase.annotation.DatabaseTable;

@DatabaseTable(tableName="products")
public class Product
  implements Serializable
{
  private static final long serialVersionUID = 1L;

  @DatabaseField(familyName="properties", qualifierName="classic")
  private String classic;

  @DatabaseField(familyName="properties", qualifierName="category")
  private String category;

  @DatabaseField(familyName="properties", qualifierName="theThird")
  private String theThird;

  @DatabaseField(familyName="properties", qualifierName="shortName")
  private String shortName;

  @DatabaseField(familyName="properties", qualifierName="productName")
  private String productName;

  @DatabaseField(familyName="properties", qualifierName="title")
  private String title;

  @DatabaseField(familyName="properties", qualifierName="keyword")
  private String keyword;

  @DatabaseField(familyName="properties", qualifierName="price")
  private String price;

  @DatabaseField(familyName="properties", qualifierName="maketPrice")
  private String maketPrice;

  @DatabaseField(familyName="properties", qualifierName="discountPrice")
  private String discountPrice;

  @DatabaseField(familyName="properties", qualifierName="contents")
  private String contents;

  @DatabaseField(familyName="properties", qualifierName="mparams")
  private String mparams;

  @DatabaseField(familyName="properties", qualifierName="orgPic")
  private String orgPic;

  @DatabaseField(familyName="properties", qualifierName="smallPic")
  private String smallPic;

  @DatabaseField(familyName="properties", qualifierName="bigPic")
  private String bigPic;

  @DatabaseField(familyName="properties", qualifierName="sellerCode")
  private String sellerCode;

  @DatabaseField(familyName="properties", qualifierName="seller")
  private String seller;

  @DatabaseField(familyName="properties", qualifierName="product_url")
  private String product_url;

  @DatabaseField(familyName="properties", qualifierName="pid")
  private String pid;

  @DatabaseField(familyName="properties", qualifierName="brand")
  private String brand;

  @DatabaseField(familyName="properties", qualifierName="isCargo")
  private int isCargo;

  @DatabaseField(id=true)
  protected String id;

  @DatabaseField(familyName="properties", qualifierName="creatTime")
  protected String creatTime = System.currentTimeMillis()+"";

  @DatabaseField(familyName="properties", qualifierName="updateTime")
  protected String updateTime = System.currentTimeMillis()+"";

  @DatabaseField(familyName="properties", qualifierName="status")
  protected int status = 0;
  
  @DatabaseField(familyName="properties", qualifierName="catCode")
  protected String catCode;

  protected String opCode = "";
  
  
    public String getOpCode() {
    	return opCode;	
    }

	public void setOpCode(String opCode) {
		this.opCode = opCode;
	}

public String getId()
  {
    return this.id; }

  public void setId(String id) {
    this.id = id; }

  public String getCreatTime() {
    return this.creatTime; }

  public void setCreatTime(String creatTime) {
    this.creatTime = creatTime; }

  public String getUpdateTime() {
    return this.updateTime; }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime; }

  public int getStatus() {
    return this.status; }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getClassic()
  {
    return this.classic;
  }

  public void setClassic(String classic)
  {
    this.classic = classic;
  }

  public String getShortName()
  {
    return this.shortName;
  }

  public void setShortName(String shortName)
  {
    this.shortName = shortName;
  }

  public String getProductName()
  {
    return this.productName;
  }

  public void setProductName(String productName)
  {
    this.productName = productName;
  }

  public String getKeyword()
  {
    return this.keyword;
  }

  public void setKeyword(String keyword)
  {
    this.keyword = keyword;
  }

  public String getPrice()
  {
    return this.price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getMaketPrice()
  {
    return this.maketPrice;
  }

  public void setMaketPrice(String maketPrice)
  {
    this.maketPrice = maketPrice;
  }

  public String getDiscountPrice()
  {
    return this.discountPrice;
  }

  public void setDiscountPrice(String discountPrice)
  {
    this.discountPrice = discountPrice;
  }

  public String getContents()
  {
    return this.contents;
  }

  public void setContents(String contents)
  {
    this.contents = contents;
  }

  public String getMparams()
  {
    return this.mparams;
  }

  public void setMparams(String mparams)
  {
    this.mparams = mparams;
  }

  public String getOrgPic()
  {
    return this.orgPic;
  }

  public void setOrgPic(String orgPic)
  {
    this.orgPic = orgPic;
  }

  public String getSmallPic()
  {
    return this.smallPic;
  }

  public void setSmallPic(String smallPic)
  {
    this.smallPic = smallPic;
  }

  public String getBigPic()
  {
    return this.bigPic;
  }

  public void setBigPic(String bigPic)
  {
    this.bigPic = bigPic;
  }

  public String getSellerCode()
  {
    return this.sellerCode;
  }

  public void setSellerCode(String sellerCode)
  {
    this.sellerCode = sellerCode;
  }

  public String getSeller()
  {
    return this.seller;
  }

  public void setSeller(String seller)
  {
    this.seller = seller;
  }

  public String getProduct_url()
  {
    return this.product_url;
  }

  public void setProduct_url(String product_url)
  {
    this.product_url = product_url;
  }

  public String getPid()
  {
    return this.pid;
  }

  public void setPid(String pid)
  {
    this.pid = pid;
  }

  public String getBrand()
  {
    return this.brand;
  }

  public void setBrand(String brand)
  {
    this.brand = brand;
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return this.category;
  }

  public void setTheThird(String theThird) {
    this.theThird = theThird;
  }

  public String getTheThird() {
    return this.theThird; }

  public void setIsCargo(int isCargo) {
    this.isCargo = isCargo; }

  public int getIsCargo() {
    return this.isCargo;
  }

	public String getCatCode() {
		return catCode;
	}
	
	public void setCatCode(String catCode) {
		this.catCode = catCode;
	}
  
}