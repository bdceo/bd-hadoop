package com.bdsoft.nutch.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.nutch.util.URLUtil;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bdsoft.nutch.bean.Product;
import com.bdsoft.nutch.parser.exception.ProductIDIsNullException;
import com.bdsoft.nutch.parser.exception.ProductNameIsNullException;
import com.bdsoft.nutch.parser.exception.ProductPriceIsNullException;
import com.bdsoft.utils.Constant;

/**
 * 解析的父类，公共方法写在该类下。 该类无法实现的功能由子类覆盖实现。
 */
public class ParserParent extends TemplateParser {

	public static final Logger LOG = Logger
			.getLogger(ParserParent.class);

	protected String sellerCode = "";
	protected String seller = "";

	public ParserParent() {
	}

	public ParserParent(DocumentFragment doc) {
		this.root = doc;
	}

	// -------------------------------xpath
	protected String marketPriceXpath = "null";
	protected String priceXpath = "null";
	protected String discountPriceXpath = "null";
	protected String brandXpath = "null";
	protected String classicXpath = "null";
	protected String productNameXpath = "null";
	protected String keywordXpath = "null";
	protected String imgXpath = "null";
	protected String contentDetailXpath = "null";
	protected String mparasXpath = "null";
	protected String nextPageXpath = "null";
	protected String isCargoXpath = "null";
	protected String filterStr = "";// 二次处理方法使用,即secondDeal方法
	protected String shortNameXpath = "";
	protected String catGoryXpath = "null";
	// -----------------------------过滤
	protected List<Pattern> throughList = new ArrayList<Pattern>();// 通过的filter
	protected List<Pattern> filterList = new ArrayList<Pattern>();// 拒绝的filter
	protected List<Pattern> productUrlList = new ArrayList<Pattern>();// 过滤商品页面的URL集合
	protected List<String> classFilter = new ArrayList<String>();// 过滤分类
	protected List<String> keywordsFilter = new ArrayList<String>();// keywords
	protected List<String> outLinkXpath = new ArrayList<String>();// 页面外链xpath定义集合	
	protected String imgPrefix = "";// 为图片赋于前缀 ,因为有时候图片抓取的地址是相对路径

	@Override
	public boolean urlFilter(String target) {

		if (throughList.size() > 0) {// 通过的正则只要有符合的，就返回true
			for (int i = 0; i < throughList.size(); i++) {
				if (throughList.get(i).matcher(target.toLowerCase()).matches()) {
					return true;
				}
			}
		}
		if (isProductPage(target)) {
			return true;
		}
		if (filterList.size() > 0) {// 不需要的正则，只要有匹配上的立即返回false。
			for (int i = 0; i < filterList.size(); i++) {
				if (filterList.get(i).matcher(target.toLowerCase()).matches()) {
					return false;
				}
			}
		}

		return false;
	}

	@Override
	/**
	 * @ 正则表达式 全部小写
	 */
	public boolean isProductPage(String url) {
//		LOG.info("ParserParent->isProductPage url=" + url);
		if (productFlag.matcher(url.toLowerCase()).matches()) {
			return true;
		}
		if (productUrlList.size() > 0) {// 通过的正则只要有符合的，就返回true
			for (int i = 0; i < productUrlList.size(); i++) {
				if (productUrlList.get(i).matcher(url.toLowerCase()).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 解析产品id
	 */
	@Override
	public void setId() throws ProductIDIsNullException {
		String pid = null;
//		url="http://www.suning.com/emall/prd_10052_10051_-7_5023261_.html";
		Matcher ma1 = productFlag.matcher(url.toLowerCase());
		if (ma1.find()) {
			pid = ma1.group(1);
		}
		if (pid == null || pid.isEmpty()) {
			LOG.info("setId===>ERROR==>" + pid);
			throw new ProductIDIsNullException(url + ":未能获取商品id！！");
		} else {
			String id = sellerCode + pid;
			super.product.setId(id);
			super.product.setPid(pid);
		}
	}

	@Override
	public void setBrand() {
		Node node = TemplateUtil.getNode(root, brandXpath);
		if (node != null) {
			String value = node.getTextContent().replaceAll("[\\s]{1,}", "")
					.replaceAll("：", ":");
			String[] values = value.split(":");
			if (values.length == 2) {
				super.product.setBrand(values[1]);
			} else {
				super.product.setBrand(value);
			}
		}
	}

	@Override
	public void setClassic() {
		classFilter.add("首页");
		Node node_location = TemplateUtil.getNode(root, classicXpath);
		if (node_location != null) {
			String value = node_location.getTextContent().replaceAll(
					"[\\s]{3,}", " ");
			value = value.replaceAll("[\\r\\n]+", "");
			if (classFilter.size() > 0) {
				for (String filterString : classFilter) {
					value = value.replaceAll(filterString + "[\\s| ]{0,}>{0,}",
							"");
				}
			}
			value = value.replaceAll(">", Constant.XMTAG);
			super.product.setClassic(value);
		}
	}

	@Override
	public void setShortName() {

	}

	@Override
	public void setProductName() throws ProductNameIsNullException {
		Node node = TemplateUtil.getNode(root, productNameXpath);
		if (node != null) {
			String productName = node.getTextContent()
					.replaceAll("\\s{2,}", "");
			if (productName == null || productName.isEmpty()) {
				LOG.info("productName===>ERROR==>" + productName);
				new ProductNameIsNullException(url + "商品名称为空！"
						+ productNameXpath + "不匹配 ");
			} else {
				if(keywordsFilter.size()>0){
					for (String keyFilter : keywordsFilter) {
						productName = productName.replaceAll(keyFilter, "");
					}
				}
				super.product.setProductName(productName);
			}
		} else {
			new ProductNameIsNullException(url + "商品名称为空！" + productNameXpath
					+ "不匹配 ");
		}
	}

	@Override
	public void setKeyword() {
		Node node = TemplateUtil.getNode(root, keywordXpath);
		if (node != null) {
			String keyword = node.getTextContent();
			if (keywordsFilter.size() > 0) {
				for (String keyFilter : keywordsFilter) {
					keyword = keyword.replaceAll(keyFilter, "");
				}
			}
			keyword = keyword.replaceAll("，|,", " ").trim();
			super.product.setKeyword(keyword);
		}
	}

	@Override
	public void setOrgPic() {
		Node node = TemplateUtil.getNode(root, imgXpath);
		if (node != null) {
			String img = imgPrefix + node.getNodeValue();
			if (img != null && img.contains("\\")) {
				img = img.replace("\\", "/");
			}
			try {
				super.product.setOrgPic(img);
				/*
				 * Img_Middle imgDown = new
				 * Img_Middle(product.getId(),product.getSellerCode
				 * (),product.getPid(),product.getOrgPic());
				 * super.product.setBigPic(imgDown.getBigPicUrl());
				 * super.product.setSmallPic(imgDown.getSmallPicUrl());
				 * imgDown.toStorePic();
				 */
			} catch (Exception ex) {
				LOG.error("图片解析错误");
			}
		}
	}

	@Override
	public void setPrice() throws ProductPriceIsNullException {
		String price = "";
		try {
			Node priceNode = TemplateUtil.getNode(root, priceXpath);
			if (priceNode != null) {
				price = trimPrice(priceNode.getTextContent().replaceAll(
						"[\\s-]{1,}", ""));
				if (price == null || price.isEmpty()) {
					LOG.info("price===>ERROR==>" + price);
					throw new ProductPriceIsNullException(url
							+ "未能获取商品销售价格！setPrice异常不匹配");
				}
				super.product.setPrice(price);
			} else {
				LOG.info(url + "未能获取商品销售价格！setPrice异常不匹配");
				throw new ProductPriceIsNullException(url
						+ "未能获取商品销售价格！setPrice异常不匹配");
			}
		} catch (Exception ex) {
			LOG.info(url + "未能获取商品销售价格！setPrice异常不匹配!!", ex);
		}
	}

	@Override
	public void setMaketPrice() {
		Node marketPriceNode = TemplateUtil.getNode(root, marketPriceXpath);
		if (marketPriceNode != null) {
			String marketPrice = trimPrice(marketPriceNode.getTextContent()
					.replaceAll("\\s", ""));
			super.product.setMaketPrice(marketPrice);
		}
	}

	@Override
	public void setDiscountPrice() {
		String discountPrice = "";
		try {
			Node discountPriceNode = TemplateUtil.getNode(root,
					discountPriceXpath);
			if (discountPriceNode != null) {
				discountPrice = trimPrice(discountPriceNode.getTextContent()
						.replaceAll("\\s", ""));
				if (discountPrice == null || discountPrice.isEmpty()) {
					LOG.info("price===>ERROR==>" + discountPrice);
					throw new ProductPriceIsNullException(url
							+ "未能获取商品折扣价格！setDiscountPrice异常不匹配");
				}
			}
			super.product.setDiscountPrice(discountPrice);
		} catch (Exception ex) {
			super.product.setDiscountPrice(discountPrice);
		}

	}

	@Override
	public void setMparams() {
		String def = "";
		String value = TemplateUtil.getMParas(root, mparasXpath);
		if (value != null && !"".equals(value)) {
			value = TemplateUtil.formatMparams(value);
		}else{
			value = def;
		}
		super.product.setMparams(value);
	}

	@Override
	public void setContents() {
		StringBuffer sb = new StringBuffer();
		NodeList nodeList = TemplateUtil.getNodeList(root, contentDetailXpath);
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				TemplateUtil.getTextHelper(sb, node);
			}
		} else {
			sb.append("无商品信息......");
		}
		super.product.setContents(sb.toString());
	}

	@Override
	public void setSeller() {
		super.product.setSeller(seller);
		super.product.setSellerCode(sellerCode);
	}

	@Override
	public void setCreatTime() {

	}

	@Override
	public void setUpdateTime() {

	}

	@Override
	public void setStatus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIsCargo() {
		// TODO Auto-generated method stub
		super.setIsCargo();
	}

	public String nextUrl(URL baseUrl) {
		String nextUrl = "";
		Node node = TemplateUtil.getNode(root, nextPageXpath);
		if (node != null) {
			String urlTarget = node.getTextContent();
			try {
				URL urls = URLUtil.resolveURL(baseUrl, urlTarget);
				nextUrl = urls.toString();
			} catch (MalformedURLException e) {
			}
		}
		return nextUrl;
	}

	protected String trimPrice(String price) {
		if (price != null && price != "") {
			if (price.contains("¥")) {
				price = price.substring(price.indexOf("¥") + 1);
			} else if (price.contains("￥")) {
				price = price.substring(price.indexOf("￥") + 1);
			}
			if (price.contains(".") && price.indexOf(".") + 3 < price.length()) {
				price = price.substring(0, price.indexOf(".") + 3);
			}
			if (price.contains(",")) {
				price = price.replaceAll(",", "");
			}
			if (price.contains("元")) {
				price = price.replaceAll("元", "");
			}
		}
		return price;
	}

	/**
	 * 使UL--LI生成key-value形式
	 * 
	 * @param xpath
	 * @param flag
	 *            备用字段
	 */
	public String ParseLi(String xpath, String... flag) {

		List<Map<String, String>> list = new LinkedList<Map<String, String>>();

		NodeList nodeList = TemplateUtil.getNodeList(root, xpath);
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				Node node = nodeList.item(i);
				String value = node.getTextContent().replaceAll("：", ":")
						.replaceAll("　| ", "").trim();
				if (!value.equals("")) {
					String[] values = value.split(":");
					if (values.length > 1) {
						String temp = value.substring(value.indexOf(":") + 1);
						map.put(values[0], temp);
					} else if (values.length == 1) {
						map.put(values[0], "null");
					}
					list.add(map);
				}
			}
		}

		StringBuffer sb = new StringBuffer();

		for (Map<String, String> map : list) {
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = map.get(key).trim();
				sb.append(key + "=" + value).append(Constant.XMTAG);
			}
		}
		int lastSplit = sb.lastIndexOf("[xm99]");
		if (lastSplit > 0) {
			sb.delete(lastSplit, sb.length());
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param xpath
	 * @throws ProductIDIsNullException
	 * @1.有idxPath时id给
	 * @2.无idxpath时idxpath赋值"null"
	 */
	public void setId(String xpath, String id) throws ProductIDIsNullException {
		String value = id;
		if (xpath.length() > 0) {
			Node node = TemplateUtil.getNode(root, xpath);
			if (node == null) {
				LOG.info("setid===>Error===>");
				new ProductIDIsNullException(url + ":未能获取到商品id！！");
			} else {
				value = node.getTextContent().trim();

			}

		}

		if (value != null && value.length() > 0) {
			product.setId(sellerCode + value);
			product.setPid(value);
		}
	}

	/**
	 * 
	 * @param xPath
	 * @param pName
	 * @参数不使用时传入用空字符串
	 */
	public void setProductName(String xpath, String pName)
			throws ProductNameIsNullException {
		String name = pName;
		if (xpath.length() > 0) {
			Node node = TemplateUtil.getNode(root, xpath);
			if (node != null) {
				name = node.getTextContent().trim();
			}
		}
		if (name == null || name.length() <= 0) {
			LOG.info("setProductName===>Error===>" + name);
			throw new ProductNameIsNullException(url + ":未能获取到商品name！！");
		} else {
			product.setProductName(name);
		}
	}

	/**
	 * 
	 * @param xPath
	 * @param price
	 * @参数不使用时传入用空字符串
	 */
	public void setPrice(String xpath, String price)
			throws ProductPriceIsNullException {
		String value = price;
		if (xpath.length() > 0) {
			Node node = TemplateUtil.getNode(root, xpath);
			if (node != null) {
				value = node.getTextContent().trim();
			}
		}

		if (value == null || value.length() <= 0) {
			LOG.info("setPrice===========>Error===>" + value);
			// throw new ProductPriceIsNullException(url+ ":未能获取到商品price！！");
			super.product.setPrice("0");
		} else {
			super.product.setPrice(value);
		}
	}

	/**
	 * 
	 * @param xPath
	 * @param url
	 * @参数不使用时传入用空字符串\
	 * 
	 */
	public void setOrgPic(String xPath, String url) {

		String img = url;
		if (xPath.length() > 0) {
			Node node = TemplateUtil.getNode(root, imgXpath);
			if (node != null) {
				try {
					URL baseUrl = new URL(this.url);
					img = node.getTextContent().trim();
					URL urls = URLUtil.resolveURL(baseUrl, img);
					img = urls.toString();
				} catch (MalformedURLException e) {
					// e.printStackTrace();
				}
			}
		}

		if (img != null && img.contains("\\")) {
			img = img.replace("\\", "/");
		}
		try {
			super.product.setOrgPic(img);
			/*
			 * Img_Middle imgDown = new
			 * Img_Middle(product.getId(),product.getSellerCode
			 * (),product.getPid(),product.getOrgPic());
			 * super.product.setBigPic(imgDown.getBigPicUrl());
			 * super.product.setSmallPic(imgDown.getSmallPicUrl());
			 * imgDown.toStorePic();
			 */
		} catch (Exception ex) {
			LOG.error("图片解析错误");
		}
	}

	/**
	 * 
	 * @param xPath
	 *            直接定位到各个分类标签
	 * @param text
	 *            生成好的值
	 * @参数不使用时传入用空字符串
	 */
	public void setClassic(String xPath, String text) {
		StringBuilder sb = new StringBuilder(text);
		boolean indexFlag = true;
		if (xPath.length() > 0) {
			NodeList nodeList = TemplateUtil.getNodeList(root, classicXpath);
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					String value = node.getTextContent().trim();
					classFilter.add("首页");
					if (classFilter.size() > 0) {
						for (String filterString : classFilter) {
							value = value.replaceAll(filterString, "");
						}
					}
					if (value.length() > 0) {
						if (indexFlag) {
							indexFlag = false;
						} else {
							sb.append("[xm99]");
						}
						sb.append(value);
					}
				}
			}
		}
		super.product.setClassic(sb.toString());
	}

	/**
	 * 二次处理分为以下方法： 1.dealClassic预处理导航条，将其改成分类 2.dealShop处理店铺字段 3.dealType
	 * 处理商品型号,用于短名称 4.dealBrand 处理品牌 5.dealCategory 处理分类
	 * 
	 * 目前国美没有移植,以后移植
	 */
	@Override
	public void secondDeal() {
		String rowkey = super.product.getId();
		try {
			String classic = super.product.getClassic();
			String mparams = super.product.getMparams();
			String brand = super.product.getBrand();
			String category = super.product.getCategory();
			String shopName = super.product.getShortName();

			classic = this.dealClassic(classic, filterStr);
			brand = this.dealBrand(mparams, product);
//			LOG.info("ParserParent->secondDeal handl category,brand=" + brand + ",classic" + classic);
			String type = this.dealType(mparams, product);
			category = this.dealCategory(brand, classic, product);
			this.dealShop(mparams, product);
			this.saveSecond(brand, category, type, shopName);
//			LOG.info("ParserParent->secondDeal set category=" + this.product.getCategory());
		} catch (Exception ex) {
			LOG.info("ParserParent->secondDeal error with " + rowkey, ex);
		}
	}

	protected void saveSecond(String brand, String category, String type,
			String shopName) throws Exception {
		String code = "";
		String shortName = "";
		String rowkey = super.product.getId();
		if ("无型号".equals(type)) {
			// 需要写入类型的code
			code = "1001";// 确定无法解析类型
		}

		if (!"".equals(brand) && !"".equals(type) && !"无型号".equals(type)) {
			shortName = brand + " " + type;
		}

		if ("".equals(category) && "1011".startsWith(rowkey)
				&& "1001".equals(code)) {// 亚马逊的,无分类同时也确定无类型,则肯定不要
			code = "1002";// 既确定无型号同时也是亚马逊的无分类商品
		} else if ("".equals(category) && "1011".startsWith(rowkey)
				&& !"".equals(code)) {
			code = "1003";// 亚马逊无型号商品,但是有商品类型
		} else if ("".equals(category) && "1011".startsWith(rowkey)) {
			code = "1004";// 需要通知的,亚马逊无商品分类，但是有类型但是没抓取回来，需要看类型出现问题原因
		}
		super.product.setCategory(category);
		super.product.setBrand(brand);
		super.product.setShortName(shortName);
		super.product.setTheThird(shopName);
		super.product.setOpCode(code);
	}

	// 处理原始分类
	@Override
	public void setCategory() {
	}

	// 20130614添加，处理商品原始分类码
	@Override
	public void setCatCode() {
	}

	/**
	 * 预处理导航条 首页xm99
	 */
	protected String dealClassic(String classic, String filterStr) {
		List<String> filters = new ArrayList<String>();
		String[] arr = filterStr.split("xm99");
		for (String str : arr) {
			filters.add(str);
		}
		classic = ProductDealUtil.dealClassic(classic, filters);
		return classic;
	}

	/**
	 * 处理店铺
	 */
	protected String dealShop(String mparams, Product product) {
		return "";
	}

	/**
	 * 处理型号
	 */
	protected String dealType(String mparams, Product product) {
		String type = ProductDealUtil.dealMparam(mparams, "型号");
		if (type.equals("")) {
			if (mparams.indexOf("型号") < 0) {
				type = "无型号";
			}
		}
		return type;
	}

	/**
	 * 处理品牌
	 */
	protected String dealBrand(String mparams, Product product) {
		String brand = ProductDealUtil.dealMparam(mparams, "品牌");
		if (brand.equals("")) {
			brand = product.getBrand();
		}
		return brand;
	}

	/**
	 * 处理分类
	 */
	protected String dealCategory(String brand, String classic, Product product) {
		Map<String, String> map = ProductDealUtil.dealCategory(brand, classic);
		String category = map.get("category");
		return category;
	}
}
