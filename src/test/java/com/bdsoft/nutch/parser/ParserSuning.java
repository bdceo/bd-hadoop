package com.bdsoft.nutch.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.nutch.util.URLUtil;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bdsoft.nutch.parser.exception.ProductIDIsNullException;
import com.bdsoft.nutch.parser.exception.ProductNameIsNullException;
import com.bdsoft.nutch.parser.exception.ProductPriceIsNullException;
import com.bdsoft.utils.Formatter;
import com.bdsoft.utils.RequestUtil;
import com.bdsoft.utils.StrUtil;

public class ParserSuning extends ParserParent {

	public static final Logger LOG = Logger.getLogger(ParserSuning.class);

	// http://www.suning.com/emall/prd_10052_10051_-7_1991097_.html
	// 商品页正则
	private Pattern itemFlag1 = Pattern.compile("http://www.suning.com/emall/prd_10052_10051_-7_([\\d]+)_.html.*");
	
	// http://search.suning.com/emall/pcd.do?ci=20016&cp=9		pcd.do
	// http://search.suning.com/emall/strd.do?ci=258006&cp=2	strd.do
	// http://search.suning.com/emall/trd.do?ci=262532&cp=6  	trd.do
	// 分类列表正则
	private Pattern listFlag1 = Pattern.compile("http://search.suning.com/emall/pcd.do\\?.*ci=([\\d]+).*");// group=1 for cid
	private Pattern listFlag1p = Pattern.compile("http://search.suning.com/emall/pcd.do\\?.*cp=([\\d]+).*");// group=1 for pg
	private Pattern listFlag2 = Pattern.compile("http://search.suning.com/emall/strd.do\\?.*ci=([\\d]+).*");// group=1 for cid
	private Pattern listFlag2p = Pattern.compile("http://search.suning.com/emall/strd.do\\?.*cp=([\\d]+).*");// group=1 for pg
	private Pattern listFlag3 = Pattern.compile("http://search.suning.com/emall/trd.do\\?.*ci=([\\d]+).*");// group=1 for cid
	private Pattern listFlag3p = Pattern.compile("http://search.suning.com/emall/trd.do\\?.*cp=([\\d]+).*");// group=1 for pg

	// 下一页地址xpath
	private String nextPageXpath = "//DIV[@class='page']";
	private String nextPageXpath1 = "//A[@id='nextPage' or @id='pageNext']/@href";
	private String nextPageParamXpath = "//SPAN[@id='showTab']/@class";// 获取下一页时的参数
	String jsonXpath = "//SCRIPT";
	Map<String, String> map = new HashMap<String, String>();
	private List<String> seeds = new ArrayList<String>();
	
	public ParserSuning() {
		init();
	}

	public ParserSuning(DocumentFragment doc) {
		super(doc);
		init();
		try {
			parseInit();
		} catch (Exception ex) {
			LOG.info("parseInit Error", ex);
		}
	}

	public void init() {
		sellerCode = "1001";
		seller = "苏宁易购";
		filterStr = "首页";
		idXPath="//INPUT[@name='catEntryId_1']/@value";
		brandXpath = "//DIV[@id='pro_canshu_tab_box']/TABLE/TBODY/TR[2]/TD[2]";
		classicXpath = "//DIV[@class='path w1200']";
		productNameXpath = "//H1/SPAN";
		keywordXpath = "//META[@name='keywords']/@content";
		imgXpath = "//DIV[@id='PicView']/descendant::IMG/@src";
		mparasXpath = "//DIV[@id='pro_canshu_tab_box']/descendant::TR";
		contentDetailXpath = "//DIV[@id='detail_content']";
		keywordsFilter.add("报价");
		keywordsFilter.add("价格");
		keywordsFilter.add("苏宁易购");
		keywordsFilter.add("苏宁");
		titleFilter.add("报价");
		titleFilter.add("价格表");
		titleFilter.add("【.*?】");
		titleFilter.add("苏宁易购");
		titleFilter.add("苏宁");
		
		// 合法URL规则
		productFlag = itemFlag1;
		throughList.add(itemFlag1);
		throughList.add(listFlag1);
		throughList.add(listFlag1p);	
		throughList.add(listFlag2);
		throughList.add(listFlag2p);
		throughList.add(listFlag3);
		throughList.add(listFlag3p);
		// 商品页地址规则
		productUrlList.add(itemFlag1);
		// 提取外链xpath统一定义
		outLinkXpath.add("//DIV[@class='listLeft']/descendant::A/@href");// 旧的:
		outLinkXpath.add("//DIV[@id='navBarConIndex']/descendant::A/@href");
		outLinkXpath.add("//DIV[@id='filterContent']/descendant::A/@href");
		outLinkXpath.add("//DIV[@id='proShow']/UL/LI/A/@href");// 新的： 分类页提取商品页外链
		outLinkXpath.add("//UL[@id='proList']/LI/DIV/A/@href");
		outLinkXpath.add("//DIV[@class='boxCon']/UL/LI/A/@href | //DIV[@class='listLeft' or @class='listRight']/DL/DD/SPAN/A/@href");// 总分类页提取分类地址页外链
		// 种子地址，不过滤
		seeds.add("http://www.suning.com/emall/pgv_10052_10051_1_.html");
	}

	public void parseInit() {
		String jsonValue = "";
		String value = "";
		int index1 = 0;
		int index2 = 0;
		List<Node> jsonList = new LinkedList<Node>();// 存放sn节点
		Node pageNode = null;// 分页node
		NodeList nodeList = TemplateUtil.getNodeList(root, jsonXpath);
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				value = node.getTextContent().replaceAll("[\\s]+", "");
				index1 = value.indexOf("varsn=");
				index2 = value.indexOf("var");
				if (index1 >= 0 && index2 >= 0) {
					jsonList.add(node);
				}
				index1 = value.indexOf("param.sortType");
				if (index1 >= 0) {
					pageNode = node;
				}
			}
		}
		if (jsonList.size() == 0) {
			LOG.info(this.getClass() + "没有解析到json对象");
		} else {
			for (Node node : jsonList) {
				jsonValue = node.getTextContent().replaceAll("[\\s]{3,}", " ");
				index1 = jsonValue.indexOf("sn");
				jsonValue = jsonValue.substring(index1);// 得到json字符串
				jsonValue = Formatter.getJson(jsonValue);
				if (!jsonValue.equals("")) {
					JSONObject productObject = JSONObject.fromObject(jsonValue);
					try {
						map.put("skuid", productObject.getString("productId"));
					} catch (Exception ex) {
					}
					try {
						map.put("catalogId", productObject.getString("catalogId"));
					} catch (Exception ex) {
					}
					try {
						map.put("storeId", productObject.getString("storeId"));
					} catch (Exception ex) {
					}
				}
			}
		}

		if (pageNode != null) {// 解析下一页url需要的参数
			String contents = pageNode.getTextContent().replaceAll("[\\s]{3,}", " ");
			index1 = contents.indexOf("param");
			jsonValue = contents.substring(index1);// 得到json字符串
			jsonValue = Formatter.getJson(jsonValue);
			if (!jsonValue.equals("")) {
				JSONObject productObject = JSONObject.fromObject(jsonValue);
				try {
					map.put("holdURL", productObject.getString("holdURL"));
				} catch (Exception ex) {
				}// 基础url
				try {
					map.put("currentPage",
							productObject.getString("currentPage"));
				} catch (Exception ex) {
				}// 当前页
				try {
					map.put("pageNumbers",
							productObject.getString("pageNumbers"));
				} catch (Exception ex) {
				}// 总页数
				try {
					map.put("numFound", productObject.getString("numFound"));
				} catch (Exception ex) {
				}// 商品总数量
			}

			fillMap(contents, "param.inventory", "inventory");
			fillMap(contents, "param.sortType", "sortType");
			fillMap(contents, "param.historyFilter", "historyFilter");
		}
	}

	private void fillMap(String contents, String param, String key) {
		int index1 = contents.indexOf(param);

		if (index1 >= 0) {
			index1 = contents.indexOf("\"", index1);
			int index2 = contents.indexOf("\"", index1 + 1);
			map.put(key, contents.substring(index1 + 1, index2));
		}

	}

	@Override
	public void setId() throws ProductIDIsNullException {
		Node idNode = TemplateUtil.getNode(root, idXPath);
		if (idNode != null) {
			String pid = idNode.getTextContent().trim();
			if(pid!=null && !"".equals(pid)){
				String id=sellerCode+pid;
				product.setId(id);
				product.setPid(pid);
			}
		}
		if (idNode == null) {
			throw new ProductIDIsNullException(url + ":未能获取商品id！！");
		}
	}
	@Override
	public void setProductName() throws ProductNameIsNullException {
		Node node = TemplateUtil.getNode(root, productNameXpath);
		if (node != null) {
			String productName = node.getFirstChild().getTextContent().replaceAll("\\s", "");
			if ("".equals(productName)) {
				productName = node.getTextContent().replaceAll("\\s", "");
			}
			if (productName == null || productName.isEmpty()) {
				new ProductNameIsNullException(url + "商品名称为空！" + productNameXpath + "不匹配 ");
			} else {
				product.setProductName(productName);
			}
		} else {
			new ProductNameIsNullException(url + "商品名称为空！" + productNameXpath + "不匹配 ");
		}
	}

	/**
	 * 该价格是通过异步请求加载的，根据获取的id等参数拼凑去请求url，返回的json解析
	 */
	@Override
	public void setPrice() throws ProductPriceIsNullException {
		try {
			String price = "";
			String productId = map.get("skuid");
			productId = trimPrice(productId);
			if (productId != null && !"".equals(productId)) {
				String storeId = map.get("storeId");
				String catalogId = map.get("catalogId");
				if (storeId != null && !"".equals(storeId) && catalogId != null
						&& !"".equals(catalogId)) {
					// http://www.suning.com/emall/browseHistory_10052_22001_8193575__.html
					String priceUrl = "http://www.suning.com/emall/browseHistory_"
							+ storeId + "_" + catalogId + "_" + productId + "_9017_.html";
					DocumentFragment priceDocument = RequestUtil.getDocumentFragment(priceUrl, enCode);
					if (priceDocument != null) {
						JSONObject productObject = JSONObject.fromObject(priceDocument.getTextContent());
						price = productObject.getString("price");
						if (price == null || price.isEmpty()) {
							throw new ProductPriceIsNullException(url + ",组装的"
									+ priceUrl + "未能获取商品销售价格！setPrice异常不匹配");
						}
					} else {
						priceUrl = "http://www.suning.com/emall/browseHistory_"
								+ storeId + "_" + catalogId + "_" + productId + "__.html";
						priceDocument = RequestUtil.getDocumentFragment(priceUrl, enCode);
						if (priceDocument != null) {
							JSONObject productObject = JSONObject.fromObject(priceDocument.getTextContent());
							price = productObject.getString("price");
							if (price == null || price.isEmpty()) {
								throw new ProductPriceIsNullException(url
										+ ",组装的" + priceUrl + "未能获取商品销售价格！setPrice异常不匹配");
							}
						}
					}
					product.setPrice(price);
				}
			}
		} catch (Exception ex) {
			LOG.info(url + "  url未能获取商品销售价格！setPrice异常不匹配!");
		}
	}

	@Override
	public void setMparams() {
		String value = TemplateUtil.getMParas(root, mparasXpath);
		if (value != null && !"".equals(value)) {
			value = TemplateUtil.formatMparams(value);
			value = value.replaceAll("纠错", "");
		} else {
			value = "无商品参数.........";
		}
		product.setMparams(value);
	}

	// http://script.suning.cn/search/javascript/searchjs/third_slash.js?v=8261
	// js文件中组装下一页
	// javascript的changePage方法 
	// 20130830:修改下一页url提取，优先使用新的xpath获取，再使用旧的xpath获取
	public String nextUrl(URL baseUrl) {
		String nextUrl = "";
		Node node = TemplateUtil.getNode(root, nextPageXpath1);
		if(node != null){ 
			nextUrl = node.getTextContent();
			try {
				URL urls = URLUtil.resolveURL(baseUrl, nextUrl);
				nextUrl = urls.toString();
			} catch (Exception e) {
				LOG.error("->nextUrl error url=" + baseUrl + ",info=" + e);
			}			 
		} else {
			node = TemplateUtil.getNode(root, nextPageXpath);
			if (node != null) {
				String holdURL = StrUtil.trim((String) map.get("holdURL"));
				String currentPage = StrUtil.trim((String) map.get("currentPage"));
				int pageNumbers = Integer.parseInt(StrUtil.trim((String) map.get("pageNumbers")));
				String sortType = StrUtil.trim((String) map.get("sortType"));
				String inventory = StrUtil.trim((String) map.get("inventory"));
				String historyFilter = StrUtil.trim((String) map.get("historyFilter"));
				int nextPage = Integer.parseInt(currentPage) + 1;
				Node nodeParam = TemplateUtil.getNode(root, nextPageParamXpath);
				String il = "0";
				if (nodeParam != null) {
					int index1 = nodeParam.getTextContent().indexOf("spanLS");
					if (index1 >= 0) {
						il = "1";
					}
				}
				if (pageNumbers > nextPage) {
					nextUrl = holdURL + "&cp=" + nextPage + "&il=" + il + sortType + inventory + historyFilter;
					try {
						URL urls = URLUtil.resolveURL(baseUrl, nextUrl);
						nextUrl = urls.toString();
					} catch (Exception e) {
						LOG.error("->nextUrl error url=" + baseUrl + ",info=" + e);
					}
				} 
			}
		}
		if(isSafeUrl(nextUrl)){
			return formatSafeUrl(nextUrl);
		}
		return "";
	}

	public boolean isSafeUrl(String url){
		if (urlFilter(url)) {
			LOG.info("safeUrl ++ " + url);
			return true;
		} else{
			for(String seed : seeds){
				if(seed.equals(url)){
					LOG.info("safeUrl ++ " + url);
					return true;
				}
			}
			LOG.info("invalidUrl -- " + url);
			return false;
		}
	}

	@Deprecated
	public boolean isSafeUrl_0830(String url){
		url = url.toLowerCase();
		if(!url.contains("suning.com")){
			LOG.info(this.getClass().getSimpleName()+"->isSafeUrl -- " + url);
			return false;
		}
		if (url.indexOf("tuan.suning.com") >= 0 || url.indexOf("jipiao.suning.com") >= 0
			|| url.indexOf("sh.suning.com") >= 0 || url.indexOf("baoxian.suning.com") >= 0
			|| url.indexOf("b2g.suning.com") >= 0 || url.indexOf("snbook.suning.com") >= 0
			|| url.indexOf("book.suning.com") >= 0 || url.indexOf("cart.suning.com") >= 0) {
			LOG.info(this.getClass().getSimpleName()+"->isSafeUrl -- " + url);
			return false;
		}
		LOG.info(this.getClass().getSimpleName()+"->isSafeUrl ++ " + url);
		return true;
	}
	
	/**
	 * 获取列表URL规则
	 * 2013年8月6日修改：过滤无用外链，调用格式化方法，去重
	 */
	public List<String> nextUrlList(URL baseUrl) {
		List<String> urlList = new ArrayList<String>();
		Set<String> urlSet = new LinkedHashSet<String>();
		URL urls = null;
		String urlTarget = "";
		String url = "";
		for (String xpath : outLinkXpath) {
			NodeList nodeList = TemplateUtil.getNodeList(root, xpath);
			if (nodeList.getLength() > 0) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					urlTarget = nodeList.item(i).getTextContent().replaceAll("[\\s]", "");
					try {
						urls = URLUtil.resolveURL(baseUrl, urlTarget);
						url = urls.toString();
						if (urlSet.contains(url) || !isSafeUrl(url)) {
							continue;
						}
						url = formatSafeUrl(url);
						urlSet.add(url);
					} catch (Exception e) { 
						LOG.error("->nextUrlList error,url=" + url + ",info=" + e);
					}
				}
			}
		}
		urlList.addAll(urlSet);
		return urlList;
	}

	public String formatSafeUrl(String url){
		if(StrUtil.isEmpty(url)){
			return url;
		}
		if(isProductPage(url)){
			String tmp = "#";
			int index = url.indexOf(tmp);
			if(index >= 0) {
				url = url.substring(0, index);
			}
		}
		// 分类列表页格式化去重
		if(url.indexOf("search.suning.com") >= 0){
			 String cid="", page="";
			 Matcher m = listFlag2.matcher(url);
			 if(m.find()){
				 cid = m.group(1);
				 m = listFlag2p.matcher(url);
				 if(m.find()){
					 page = "&cp=" + m.group(1);
				 }
				 return "http://search.suning.com/emall/strd.do?ci=" + cid + page;
			 }
			 m = listFlag3.matcher(url);
			 if(m.find()){
				 cid = m.group(1);
				 m = listFlag3p.matcher(url);
				if(m.find()){
					 page = "&cp=" + m.group(1);
				 }
				 return "http://search.suning.com/emall/trd.do?ci=" + cid + page;
			 }
			 m = listFlag1.matcher(url);
			 if(m.find()){
				 cid = m.group(1);
				 m = listFlag1p.matcher(url);
				 if(m.find()){
					 page = "&cp=" + m.group(1);
				 }
				 return "http://search.suning.com/emall/pcd.do?ci=" + cid + page;
			 }
		}
		return url;
	}
		
	public static void main(String[] args) throws Exception {
		ParserSuning parser = new ParserSuning();
		boolean b = false;		
		String url = "http://search.suning.com/emall/strd.do?ci=157244###";
		url = "http://search.suning.com/emall/pcd.do?ci=250511&tp=_&cityId={cityId}%20&iy=-1&cf=solr_1414_attrId:E99E8BE69EB6E99E8BE69F9C";
		url = "http://www.suning.com/emall/prd_10052_10051_-7_4004826_.html#pro_detail_tab111";
//		System.out.println("格式化前="+url);
//		url = parser.formatSafeUrl(url);		
//		System.out.println("格式化后="+url);
//		System.exit(1); 
		
		File f = new File("e:/count/6crawldb_url_suning_0901.txt");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		int ok = 0, bad = 0;
		while (!StrUtil.isEmpty(line)) {
			System.out.println("待校验URL=" + line);
			b = parser.isSafeUrl(line);
			System.out.println("\t校验结果=" + (b ? "OK" : "BAD"));
			if (b) {
				ok++;
			} else {
				bad++;
			}
			line = br.readLine();
		}
		System.out.println("累计校验："+(ok+bad)+"个URL，OK-"+ ok+"个，BAD-"+bad+"个。 时间：" + new Date().toLocaleString());
	}
}
