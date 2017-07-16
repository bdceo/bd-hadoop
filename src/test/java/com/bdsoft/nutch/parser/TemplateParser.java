package com.bdsoft.nutch.parser;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.nutch.protocol.Content;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bdsoft.nutch.bean.Product;
import com.bdsoft.nutch.bean.StdProduct;
import com.bdsoft.nutch.parser.exception.ProductIDIsNullException;
import com.bdsoft.nutch.parser.exception.ProductNameIsNullException;

/**
 * 解析器
 * @author user
 *
 */
public abstract class TemplateParser implements XMParser{
	public Logger LOG = Logger.getLogger(TemplateParser.class);
	private AtomicInteger seccessCount;
    protected Product product = null;
    protected String url = "";
    protected String productRowKey = "";
    protected Content content;
    protected String enCode;
    protected DocumentFragment root;
    //-------------------------正则匹配时，已做了忽略大小写，正则表达式请用小写实现
    protected Pattern productFlag;
    protected static Pattern productListFlag;
	protected String sellerCode;
	protected String seller;
    //-----------------------------------------
	public List<String> productUrlList = null;//详情网页中有，有商品编码列表时，可复写addProductUrls加入
	//-----------------------------------------
	protected String idXPath = "";
	protected String titleXPath = "//TITLE";
    protected List<String> titleFilter = new ArrayList<String>();//title中的过滤词
    protected String keywordXpath = "//META[contains(@name,'eyword')]/@*";
    protected List<String> keywordsFilter = new ArrayList<String>();//keywords中的过滤词
   
    //--------------------------------xpath
    protected String productNameXpath = "";
    protected String productNameListXpath = "";

	/**
	 * 解析商品详情信息
	 * @param content
	 * @param enCode
	 * @return
	 */
	public Product getProductInformation(Content content,String enCode){
//		System.out.println(TemplateUtil.getNode(root, "//HEAD").getTextContent());
		
		LOG.info("TemplateParser->getProductInformation url="+content.getUrl());
		long startTime = System.currentTimeMillis();
		this.content = content;
		this.enCode = enCode ;
		this.url = content.getUrl();
		
		if(!isProductPage(url)){
			LOG.info(this.getClass().getSimpleName()+"->getProductInformation,this is not a product page,url=" + url);
			return null;
		}
		
		try{
			Product product1 = new Product();
			product = product1;
			product.setProduct_url(url);

			setId();
			productRowKey = product.getId();
			setProductName();
			setTitle();
			try{setPrice();}catch(Exception e){
				LOG.error(this.getClass().getSimpleName()+"->getProductInformation parse product price error，url=" + url, e);
				this.product.setPrice("0");
			}
			setSeller();
			String errMessage = "，not importent product info parse exception url=" + url;
			try{setIsCargo();}catch (Exception e) {LOG.info("setIsCargo"+errMessage, e);}
			try{setMaketPrice();}catch (Exception e) {LOG.info("setMaketPrice"+errMessage, e);}
			try{setDiscountPrice();}catch (Exception e) {LOG.info("setDiscountPrice"+errMessage, e);}
			try{setBrand();}catch (Exception e) {LOG.info("setBrand"+errMessage, e);}
			try{setClassic();}catch (Exception e) {LOG.info("setClassic"+errMessage, e);}
			try{setCreatTime();}catch (Exception e) {LOG.info("setCreatTime"+errMessage, e);}
			try{setKeyword();}catch (Exception e) {LOG.info("setKeyword"+errMessage, e);}
			try{setMparams();}catch (Exception e) {LOG.info("setMparams"+errMessage, e);}
			try{setContents();}catch (Exception e) {LOG.info("setContents"+errMessage, e);}
			try{setOrgPic();}catch (Exception e) {LOG.info("setOrgPic"+errMessage, e);}
			try{setShortName();}catch (Exception e) {LOG.info("setShortName"+errMessage, e);}
			try{setUpdateTime();}catch (Exception e) {LOG.info("setUpdateTime"+errMessage, e);}
			try{secondDeal();}catch (Exception e) {LOG.info("secondDeal"+errMessage, e);}
			try{setCategory();}catch (Exception e) {LOG.info("setCategory"+errMessage, e);}
			// 20130614添加，设置商品原始分类码
//			try{setCatCode();}catch (Exception e) {LOG.info("setCatCode"+errMessage, e);}
			if(product.getPrice()!=null && "0".equals(product.getPrice())){
				product.setIsCargo(0);//设置为无货
			}else{
				try{
					new BigDecimal(product.getPrice());	
				}catch(Exception ex){
					LOG.info(this.getClass().getSimpleName() + "->getProductInformation，product price format error，url=" + url);
					product.setPrice("0");
					product.setIsCargo(0);//设置为无货
				}
			}
			
			
/*			LOG.info(" 商品id ： "+product.getId());
			LOG.info(" 标题        ： "+product.getTitle());
			LOG.info(" 关键字   ： "+product.getCategory());
			LOG.info(" 导航        ： "+product.getClassic());
			LOG.info(" 分类        ： "+product.getCategory());
			LOG.info(" 品牌        ："+product.getBrand());
			LOG.info(" 价格        ： "+product.getPrice());
			LOG.info(" 市场价   ： "+product.getMaketPrice());
			LOG.info(" 短名称   ： "+product.getShortName());
			LOG.info(" 图片        ： "+product.getOrgPic());
			LOG.info(" 参数        ：  "+product.getMparams());
			LOG.info(" 名称        ： "+product.getProductName());*/
			//System.out.println(" 内容        ： "+product.getContents());

		} catch (ProductIDIsNullException e) {
			LOG.error(this.getClass().getSimpleName()+"->getProductInformation take product id error，url=" + url, e);
			return null;
		} catch (Exception e) {
			LOG.error(this.getClass().getSimpleName()+"->getProductInformation take product info error，url=" + url, e);
			return null;
		}
		if(product.getContents() == null || product.getMparams() == null){
			LOG.info(this.getClass().getSimpleName() + "->getProductInformation take product content or param is null,url=" + content.getUrl());
//			return null;
		}
		if (product.getContents().length() < 10 && product.getMparams().length() < 10) {
			LOG.info(this.getClass().getSimpleName() + "->getProductInformation take product content=" + product.getContents().length() + ",param=" + product.getMparams().length());
			LOG.info(this.getClass().getSimpleName() + "->getProductInformation take product content and param length not enough,url=" + content.getUrl());
			return null;
		}
		LOG.info(this.getClass().getSimpleName()+"->getProductInformation parse success cost=" + (System.currentTimeMillis()-startTime) +"ms. url=" + url);
		return product;
	}

	//--------------------------------------------
	/**
	 * 解析标准商品网站商品列表页的商品名称
	 * @param content
	 * @param enCode
	 * @return
	 */
	public List<StdProduct> getProductNameList(){
		Set<StdProduct> set =new HashSet<StdProduct>();
		StdProduct product = null;
		NodeList nodeList = TemplateUtil.getNodeList(root, productNameListXpath);
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				product = new StdProduct();
				if(node ==null){
					continue;
				}else {
					product.setProductName(node.getTextContent());
					product.setId(ShortUrlGenerator.shortUrl(product.getProductName())[0]);
				}
				set.add(product);
			}
		}
		List<StdProduct> list = new ArrayList<StdProduct>(set);
		return list;
	}
	
	/**
	 * 将商品网页非链接关系获取的商品url加入列表中
	 * @param productUrl
	 */
	public void addProductUrls(String productUrl) {}
	
	//---------------------------------------------网页解析中可直接使用的统一方法
	
	@Override
	public void setSeller() {
		product.setSeller(seller);
		product.setSellerCode(sellerCode);
	}


	/* 
	 * 已做了忽略大小写处理
	 */
	@Override
	public void setId() throws ProductIDIsNullException {
		String pid = getProductID(url);
		if(pid==null|pid.isEmpty()){
			throw new ProductIDIsNullException(url + ":未能获取商品id！！");
		}else{
			String id = sellerCode + pid;
			product.setId(id);
			product.setPid(pid);
		}
	}
	
	
	@Override
	public void setTitle() {
		if(!titleXPath.isEmpty()){
			String title="";
			Node node = TemplateUtil.getNode(root, titleXPath);
			if(node!=null){
				title = node.getTextContent();
				if(titleFilter.size()>0){
					for(String keyFilter:titleFilter){
						title = title.replaceAll(keyFilter, "");
					}
				}
				title = title.trim();
				product.setTitle(title);
			}else{
				LOG.info(this.getClass().getSimpleName() + "->setTitle，title parse null，url=" + url);
			}
		}
	}
	
	@Override
	public void setKeyword() {
		if(!keywordXpath.isEmpty()){
			String keywords="";
			Node node = TemplateUtil.getNode(root, keywordXpath);
			if(node!=null){
				keywords = node.getTextContent();
				
				if(keywordsFilter.size()>0){
					for(String keyFilter:keywordsFilter){
						keywords = keywords.replaceAll(keyFilter, "");
					}
				}
				keywords = keywords.replaceAll("，|,|。", " ").trim();
				product.setKeyword(keywords);
			}
		}
	}
	
	@Override
	public void setProductName() throws ProductNameIsNullException{
		if(!productNameXpath.isEmpty()){
			String productName = "";
			Node node = TemplateUtil.getNode(root, productNameXpath);
			if (node != null) {
				productName = node.getTextContent().trim();
				if (productName != null && !productName.isEmpty()){
					product.setProductName(productName);
				}else{
					new ProductNameIsNullException(this.getClass().getSimpleName() + "->setProductName，productName parse empty,url=" + url);
				}
			} else {
				new ProductNameIsNullException(this.getClass().getSimpleName() + "->setProductName，productName parse null,url=" + url);
			}
		}else{
			new ProductNameIsNullException(this.getClass().getSimpleName() + "->setProductName，productName xpath nodefind,url=" + url);
		}
	}
	
	@Override
	public boolean isProductPage(String url) {
//		LOG.info("TemplateParser->isProductPage url=" + url);
		if (productFlag.matcher(url.toLowerCase()).find()) {
			return true;
		}
		LOG.info(url + " not a product url!");
		return false;
	}
	
	
	@Override
	/**
	 * 判断是否为productURL或者productListURL;均做了转小写处理，因此各个子模板的正则表达需要进行小写处理
	 * @param urlString
	 * @return Boolean
	 */
	public boolean urlFilter(String target) {
		//if (productFlag.matcher(target.toLowerCase()).matches() || productListFlag.matcher(target.toLowerCase()).matches()) {
		if (productFlag.matcher(target.toLowerCase()).matches()) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 从url中获取productID，已进行了忽略大小写处理
	 * @param urlString
	 * @return
	 */
	public String getProductID(String urlString) {
		String productID = null;
		Matcher ma=productFlag.matcher(urlString.toLowerCase());
		if(ma.find()){
			productID=ma.group(1);
		}
		return productID;
	}

	/**
	 * 从网页中获取SKU List，并组装productUrl List
	 * @return
	 */
	public List<String> getProductUrlListFromSKUList() {
		
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * 从页面中获取下一页的url
	 * baseUrl 表示当前页面的url
	 */
	public String nextUrl(URL baseUrl){
		return "";
	}
	/**
	 * 从页面中获取下一级的url
	 * baseUrl 表示当前页面的url
	 */
	public List<String> nextUrlList(URL baseUrl){
		return null;
	}
	
	public boolean isSafeUrl(String url){
		return true;
	}
	public String formatSafeUrl(String url){
		return url;
	}
	
	@Override
	public String formatUrl(String url) {
		return null;
	}
	public String getProductRowKey() {
		return productRowKey;
	}
	
	@Override
	public void setIsCargo() {
		this.product.setIsCargo(1);
	}
	
	//二次处理,仅仅处理 分类(category)、产类型号、产品品牌、短名称
	@Override
	public void secondDeal() {
	
		
	}

	//处理原始分类
	@Override
	public void setCategory(){
		
	}

	// 20130614添加，处理商品原始分类码
	@Override
	public void setCatCode() {
	}
}
