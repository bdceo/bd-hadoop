package com.bdsoft.nutch.parser;

import com.bdsoft.nutch.parser.exception.ProductIDIsNullException;
import com.bdsoft.nutch.parser.exception.ProductNameIsNullException;
import com.bdsoft.nutch.parser.exception.ProductPriceIsNullException;

public interface XMParser {
	
	public boolean urlFilter(String target);
	
	public boolean isProductPage(String url);
	
	public String formatUrl(String url);
	
//-----------------------------------------------------------------------------------------------
	public void setId()throws ProductIDIsNullException;//product.setId and product.setPid
	
	public void setBrand() ;
	
	public void setClassic();//product.setClassic .setClassicCode;
	
	public void setShortName( );
	
	public void setTitle( ) ;
	
	public void setProductName( ) throws ProductNameIsNullException;
	
	public void setKeyword();
	
	public void setOrgPic() ;//product.setBigpic .setSmallpic ,启动LoadingPicServer.addPicUrl();
	
	public void setPrice() throws ProductPriceIsNullException;
	
	public void setMaketPrice();
	
	public void setDiscountPrice() ;
	
	public void setMparams();

	public void setContents(); 

	public void setSeller() ; //product.setSeller   and .setSellerCode 
	
	public void setCreatTime();
	
	public void setUpdateTime() ;
	
	public void setStatus() ;
	
	public void setIsCargo();//是否有货   有货1  无货0
	
	public void secondDeal();//二次处理,仅仅处理 分类(category)、产类型号、产品品牌、短名称
	
	public void setCategory(); //处理原始分类
	
	public void setCatCode(); // 20130614添加，设置商品原始分类码
}
