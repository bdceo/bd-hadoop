package com.bdsoft.nutch.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.nutch.util.URLUtil;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bdsoft.nutch.bean.Product;
import com.bdsoft.nutch.parser.exception.ProductIDIsNullException;
import com.bdsoft.utils.Constant;
import com.bdsoft.utils.StrUtil;

public class ParserAmazonNew extends ParserParent {

	public static final Logger LOG = Logger.getLogger(ParserAmazonNew.class);

	// 商品页正则
	private Pattern itemFlag1 = Pattern.compile("(?:^.*/dp/)([a-zA-Z0-9]+).*?");
	private Pattern itemFlag2 = Pattern.compile("(?:^.*/gp/product/)([a-zA-Z0-9]+).*?");
	private String itemBase = "http://www.amazon.cn/dp/";

	// 分类列表正则
	// "http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a124912071"
	// "http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a665002051%2cp_4%3asony%20%e7%b4%a2%e5%b0%bc"
	// "http://www.amazon.cn/b/ref=sr_aj?node=51878071&ajr=0"
	// "http://www.amazon.cn/%E6%89%8B%E6%9C%BA-%E9%80%9A%E8%AE%AF/b/ref=sd_allcat_wi_?ie=UTF8&node=664978051"
	// "http://www.amazon.cn/%e7%94%b5%e5%ad%90%e4%b9%a6-%e5%b0%91%e5%84%bf/b?ie=utf8&node=143276071"
	// http://www.amazon.cn/s/ref=sr_pg_3?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051&page=3&bbn=888465051&ie=UTF8&qid=1378085295
	private Pattern listFlag1 = Pattern
			.compile("http://www.amazon.cn(?:/.*)?/b(?:/|\\?)?.*node=([\\d]+).*");// group=1 for cid
	private Pattern listFlag2 = Pattern
			.compile("http://www.amazon.cn(?:/.*)?/s(?:/|\\?)?.*rh=([%A-Za-z\\d_\\-!]+).*"); // group=1
																								// for
																								// rh
	private Pattern listFlagP = Pattern
			.compile("http://www.amazon.cn(?:/.*)?/[b|s](?:/|\\?)?.*page=([\\d]+).*");// group=1 for
																						// pg
	private String listBase = "http://www.amazon.cn/s/?";

	// 下一页地址xpath
	private String nextPageXpath = "//A[@id='pagnNextLink']/@href";
	private List<String> outLinkXpath = new ArrayList<String>();
	private List<String> seeds = new ArrayList<String>();
	private String idXpath = "//INPUT[@id='ASIN']/@value" + " | //INPUT[@name='ASIN.0']/@value";

	public ParserAmazonNew() {
		init();
	}

	public ParserAmazonNew(DocumentFragment doc) {
		super(doc);
		init();
	}

	public void init() {
		sellerCode = "1011";
		seller = "亚马逊";
		// 2013年8月13日添加：//SPAN[@class='brandLink']/A
		brandXpath = "//DIV[@class='buying']/A" + "| //SPAN[@class='brandLink']/A";
		classicXpath = "//DIV[@class='bucket']/H2";
		priceXpath = "//SPAN[@class='priceLarge']" + " | //B[@class='priceLarge']"
				+ " | //TD[@class='listprice']";
		marketPriceXpath = "//SPAN[@id='listPriceValue']" + " | //SPAN[@class='listprice']"
				+ "| //TD[@class='listprice']";
		isCargoXpath = "//SPAN[@class='availGreen']"
				+ " | //DIV[@id='buyBoxContent']//descendant::SPAN[@class='availRed']";
		productNameXpath = "//SPAN[@id='btAsinTitle']";
		// 2013年8月13日添加：//SPAN[@class='btAsinTitle']
		shortNameXpath = "//SPAN[@id='btAsinTitle']" + "| //SPAN[@class='btAsinTitle']";
		keywordXpath = "//META[@name='keywords']/@content";
		imgXpath = "//IMG[@id='original-main-image']/@src";
		contentDetailXpath = "//DIV[@class='productDescriptionWrapper'] "
				+ " | //DIV[@id='importantInformation']"
				+ " | //TABLE/TR/TD[H2[contains(text(),'基本信息')]]//descendant::LI";
		mparasXpath = "//TABLE/TR/TD[H2[contains(text(),'基本信息')]]//descendant::LI";
		catGoryXpath = "//DIV[H2[contains(text(),'查找其它相似商品')]]//descendant::LI[1]/A/@href";
		keywordsFilter.add("卓越亚马逊|卓越网|卓越|亚马逊|joyo|amazon|,|、");
		titleFilter.add("卓越亚马逊|卓越网|卓越|亚马逊|joyo|amazon|,");

		// 合法URL规则
		productFlag = itemFlag1;
		throughList.add(itemFlag1);
		throughList.add(itemFlag2);
		throughList.add(listFlag1);
		throughList.add(listFlag2);
		// 多个商品页地址规则
		productUrlList.add(itemFlag1);
		productUrlList.add(itemFlag2);
		// 提取外链xpath统一定义
		// outLinkXpath.add("//DIV[@id='nav_subcats_wrap']//descendant::A/@href");// 旧的：总分类页提取分类列表
		outLinkXpath.add("//UL[@id='nav-subnav']//descendant::LI/A/@href");
		outLinkXpath.add("//UL[@class='nav_cat_links']/LI/UL/LI/A/@href"); // 新的：总分类页，提取分类列表地址
		outLinkXpath
				.add("//DIV[@id='atfResults' or @id='btfResults']/DIV/H3[@class='newaps']/A/@href"); // 分类列表页提取商品地址
		outLinkXpath.add("//DIV[@class='bucket'][last()]/DIV/UL/LI/A[last()]/@href"); // 商品详情页，页面底部提取分类导航地址
		// 种子地址，不过滤
		seeds.add("http://www.amazon.cn/gp/site-directory");
		// 注意:还差商品详情页面的"查找其它相似商品"，如果爬取数量不够多,则可以把这个也添加进来
	}

	/**
	 * 2013年7月25日修改：控制异常打印
	 */
	@Override
	public void setId() throws ProductIDIsNullException {
		String pid = "";
		Node pidNode1 = TemplateUtil.getNode(root, idXpath);
		if (pidNode1 != null) {
			pid = pidNode1.getTextContent().replaceAll("[\\s]", "");
			if (pid != null && !pid.isEmpty()) {
				String id = sellerCode + pid;
				product.setId(id);
				product.setPid(pid);
			} else {
				throw new ProductIDIsNullException("-->Error---Product Id Is Null！,url=" + url);
			}
		} else {
			throw new ProductIDIsNullException(url + "-->Error---Product Id Is Null！");
		}
	}

	/**
	 * 2013年8月13日修改，增加了对书的品牌字段解析，一般书是没有品牌的，用出版社作为品牌字段。
	 * http://www.amazon.cn/%E5%9B%BE%E4%B9%A6/dp/B00E3966SY
	 */
	@Override
	public void setBrand() {
		NodeList nodeList = TemplateUtil.getNodeList(root, brandXpath);
		Node brandNode = null;
		String value = "";
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node tempNode = nodeList.item(i);
				int index = tempNode.getAttributes().getNamedItem("href").getTextContent()
						.indexOf("search-type=ss");
				if (index >= 0) {
					brandNode = tempNode;
					break;
				}
			}
			if (brandNode != null) {
				value = brandNode.getTextContent();
			}
		}
		product.setBrand(value);
	}

	@Override
	public void setClassic() {
		Node nodeClass = null;
		NodeList list = TemplateUtil.getNodeList(root, classicXpath);
		if (list.getLength() > 0) {
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				int index = node.getTextContent().indexOf("查找其它相似商品");
				if (index >= 0) {
					nodeClass = node.getParentNode();
					break;
				}
			}
			String value = "";
			if (nodeClass != null) {
				List<Node> listResult = TemplateUtil.getTextHelper(nodeClass, "li");
				if (listResult.size() > 0) {
					value = listResult.get(0).getTextContent();
					value = value.replaceAll(">", Constant.XMTAG).replaceAll("[\\s]{1,}", "");
				}
			}
			product.setClassic(value);
		}
	}

	@Override
	public String formatUrl(String url) {
		for (Pattern pattern : productUrlList) {
			Matcher ma1 = pattern.matcher(url);
			if (ma1.find()) {
				if (url.indexOf("?") > 0) {
					url = url.substring(0, url.indexOf("?"));
				}
				if (url.indexOf("/dp/") > 0) {
					String host = url.substring(0, url.indexOf("cn/") + 2);
					String prod = url.substring(url.indexOf("/dp/"));
					return host + prod;
				}
			}
		}
		return url;
	}

	@Override
	public String nextUrl(URL baseUrl) {
		String nextUrl = "";
		URL urls = null;
		Node nextUrlNode = TemplateUtil.getNode(root, nextPageXpath);
		if (nextUrlNode != null) {
			String urlTarget = nextUrlNode.getTextContent();
			int index = urlTarget.indexOf("loadData('");
			if (index >= 0) {
				index = index + "loadData('".length();
				int end = urlTarget.indexOf("'", index + 1);
				nextUrl = urlTarget.substring(index, end);
			} else {
				try {
					urls = URLUtil.resolveURL(baseUrl, urlTarget);
					nextUrl = formatSafeUrl(urls.toString());
				} catch (Exception e) {
					LOG.error("->nextUrl error url=" + baseUrl + ",info=" + e);
				}
			}
		}
		if (isSafeUrl(nextUrl)) {
			return formatSafeUrl(nextUrl);
		}
		return "";
	}

	@Override
	public List<String> nextUrlList(URL baseUrl) {
		List<String> list = new ArrayList<String>();
		Set<String> set = new LinkedHashSet<String>();
		URL urls = null;
		String urlTarget = "";
		String url = "";
		for (String xpath : outLinkXpath) {
			NodeList nodeList = TemplateUtil.getNodeList(root, xpath);
			if (nodeList.getLength() > 0) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					try {
						urlTarget = nodeList.item(i).getTextContent();
						urls = URLUtil.resolveURL(baseUrl, urlTarget);
						url = urls.toString();
						if (set.contains(url) || !isSafeUrl(url)) {
							continue;
						}
						url = formatSafeUrl(url);
						set.add(url);
					} catch (Exception e) {
						LOG.error("->nextUrlList error,url=" + url + ",info=" + e);
					}
				}
			}
		}
		list.addAll(set);
		return list;
	}

	public boolean isSafeUrl(String url) {
		if (urlFilter(url)) {
			LOG.info("safeUrl ++ " + url);
			return true;
		} else {
			for (String seed : seeds) {
				if (seed.equals(url)) {
					LOG.info("safeUrl ++ " + url);
					return true;
				}
			}
			LOG.info("invalidUrl -- " + url);
			return false;
		}
	}

	@Deprecated
	public boolean isSafeUrl_0901(String url) {
		if (StrUtil.isEmpty(url)) {
			return false;
		}
		url = url.toLowerCase();
		if (url.startsWith("javascript") || !url.contains("amazon.cn") || url.startsWith("https")) {
			LOG.info("->isSafeUrl -- " + url);
			return false;
		}
		if (url.contains("kaidian.amazon.cn") || url.contains("widget.weibo.com")
				|| url.contains("profile.amazon") || url.contains("action=sign-in")
				|| url.contains("/ap/signin") || url.contains("ap/register")
				|| url.contains("mn/article") || url.contains("/images/")
				|| url.contains("/dp/image") || url.contains("images-amazon.com")
				|| url.contains("completion.amazon.com") || url.contains("kindle")
				|| url.contains("books") || url.contains("music") || url.contains("games")
				|| url.contains("kfapps") || url.contains("feature.html")
				|| url.contains("redirect.html") || url.contains("sign-in.html")
				|| url.contains("gp/search/other") || url.contains("gp/mas")
				|| url.contains("gp/press") || url.contains("gp/voting")
				|| url.contains("gp/registry") || url.contains("gp/yourstore")
				|| url.contains("gp/subscribe-and-save") || url.contains("gp/css")
				|| url.contains("gp/cart") || url.contains("/gp/gc") || url.contains("/gp/aw")
				|| url.contains("/bestsellers/") || url.contains("/careers/")
				|| url.contains("/article/") || url.contains("/forum/") || url.contains("/help/")
				|| url.contains("/mobile-apps") || url.contains("fiona/manage")
				|| url.contains("/clouddrive") || url.contains("/review/")
				|| url.contains("product-reviews") || url.contains("/offer-listing/")
				|| url.contains("/sim/") || url.contains("/customer-media/")
				|| url.contains("kfapps") || url.contains("wishlist")
				|| url.contains("kindle-store") || url.contains("associates.amazon")
				|| url.contains("field-keywords")) {
			LOG.info(this.getClass().getSimpleName() + "->isSafeUrl -- " + url);
			return false;
		}
		LOG.info(this.getClass().getSimpleName() + "->isSafeUrl ++ " + url);
		return true;
	}

	// URL格式化去重
	public String formatSafeUrl(String url) {
		if (StrUtil.isEmpty(url)) {
			return url;
		}
		// 未知错误导致无效URL，尝试恢复处理
		if (url.split("http").length > 2 && url.indexOf("/dp/") >= 0) {
			int index = url.indexOf("/dp/");
			boolean end = url.endsWith("\"") || url.endsWith("'") || url.endsWith("”")
					|| url.endsWith("’");
			url = url.substring(index + 5, end ? url.length() - 1 : url.length());
			// http://www.amazon.cn/dp/product-description/‘http:/www.amazon.cn/%E6%9E%81%E7%9D%BF%E5%93%A5%E6%96%AF%E8%BE%BE%E9%BB%8E%E5%8A%A0%E7%B2%BE%E9%80%89%E5%92%96%E5%95%A1%E8%B1%86250G/dp/B005CECFTI/ref=sr_1_10
			if (!url.startsWith("http") && url.contains("http")) {
				url = url.substring(url.indexOf("http"));
			}
			if (url.indexOf("joyo.com") >= 0) {
				// http://www.amazon.cn/%E8%80%83%E7%A0%94%E2%80%A2%E6%B3%95%E8%AF%AD%E4%BA%8C%E5%A4%96%E8%80%83%E5%89%8D%E5%86%B2%E5%88%BA-%E6%96%BD%E5%A9%89%E4%B8%BD/dp/“http:/www.joyo.com/detail/product.asp?asin=B0019VM03Y”
				// http:/www.joyo.com/detail/product.asp?asin=B0019VM03Y
				// http://www.amazon.cn/dp/B0019VM03Y
				url = url.replaceAll("joyo.com", "amazon.cn");
				if (url.indexOf("product.asp?asin") >= 0) {
					String pid = url.substring(url.indexOf("=") + 1);
					url = itemBase + pid;
				}
			}
			return formatSafeUrl(url);
		}
		if (url.indexOf("/mn/") >= 0) {
			// http://www.amazon.cn/%E9%AB%98%E9%9C%B2%E6%B4%81%E5%81%A5%E7%99%BD%E9%98%B2%E8%9B%80%E7%89%99%E8%86%8F-200%E5%85%8B-2/dp/‘http:/www.amazon.cn/mn/detailApp?asin=B005DJWYL6’
			// http:/www.amazon.cn/mn/detailApp?asin=B005IEXGFE
			// http://www.amazon.cn/dp/B005IEXGFE
			if (url.indexOf("detailApp?asin") >= 0) {
				String pid = url.substring(url.indexOf("=") + 1);
				return itemBase + pid;
			}
		}
		// http://www.amazon.cn/dp/www.amazon.cn/dp/B005I2F6QS
		// http://www.amazon.cn/Thomas-Jefferson-Author-of-America-Hitchens-Christopher/dp/h/dp/0060598956
		// http://www.amazon.cn/The-Grand-Design-%E5%8F%B2%E8%92%82%E8%8A%AC%E2%80%A2%E9%9C%8D%E9%87%91/dp/product-description/h/dp/0553380168
		if (url.split("/dp/").length > 2) {
			url = url.substring(0, url.indexOf("/dp/")) + url.substring(url.lastIndexOf("/dp/"));
			return formatSafeUrl(url);
		}
		// http://www.amazon.cn/%E7%8E%A9%E5%85%B7/dp/ASIN/B00ARBOUGW
		// http://www.amazon.cn/%E7%8E%A9%E5%85%B7/dp/ASIN/B00B6XZZ6E
		// http://www.amazon.cn/gp/product/ASIN/B007CCD9TS
		if (url.contains("/ASIN/")) {
			url = url.replaceAll("/ASIN/", "/");
			return formatSafeUrl(url);
		}

		// 商品页格式化去重
		Matcher m = null;
		if (isProductPage(url)) {
			url = url.replaceAll("/product-description/", "/");
			String pid = "";
			m = itemFlag1.matcher(url);
			if (m.find()) {
				pid = m.group(1);
				if (pid != null && pid.length() < 6) {
					return "";
				}
				return itemBase + pid;
			}
			m = itemFlag2.matcher(url);
			if (m.find()) {
				pid = m.group(1);
				if (pid != null && pid.length() < 6) {
					return "";
				}
				return itemBase + pid;
			}
		}
		// "http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a124912071"
		// "http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a665002051%2cp_4%3asony%20%e7%b4%a2%e5%b0%bc"
		// "http://www.amazon.cn/b/ref=sr_aj?node=51878071&ajr=0"
		// "http://www.amazon.cn/%E6%89%8B%E6%9C%BA-%E9%80%9A%E8%AE%AF/b/ref=sd_allcat_wi_?ie=UTF8&node=664978051"
		// "http://www.amazon.cn/%e7%94%b5%e5%ad%90%e4%b9%a6-%e5%b0%91%e5%84%bf/b?ie=utf8&node=143276071"
		// 分类列表页格式化去重
		m = listFlag1.matcher(url);
		if (m.find()) {
			String cid = "n:" + m.group(1);
			String page = "";
			m = listFlagP.matcher(url);
			if (m.find()) {
				page = "&page=" + m.group(1);
			}
			cid = "rh=" + URLEncoder.encode(cid);
			return listBase + cid + page;
		}
		m = listFlag2.matcher(url);
		if (m.find()) {
			String rh = m.group(1);
			rh = URLDecoder.decode(rh);
			String cid = getLastN(rh);
			String page = "";
			m = listFlagP.matcher(url);
			if (m.find()) {
				page = "&page=" + m.group(1);
			}
			cid = "rh=" + URLEncoder.encode(cid);
			return listBase + cid + page;
		}

		// http://www.amazon.cn/gp/product/ B00B0TW6T8
		if (url.indexOf(" ") >= 0) {
			String[] tmp = url.trim().split(" ");
			url = "";
			for (String u : tmp) {
				url += u;
			}
			// 递归处理
			return formatSafeUrl(url);
		}
		return url;
	}

	// 私有方法，处理url格式化需要 /b/ 或 /b?
	private String formatBurl(String url) {
		if (StrUtil.isEmpty(url)) {
			return url;
		}
		if (url.indexOf("?") == -1) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		String[] params = param.split("&");
		Map<String, String> pmap = new TemplateUtil().formMap(params, "=");
		String base = "http://www.amazon.cn/s/?";
		StringBuffer sb = new StringBuffer();

		if (pmap.get("node") != null) {
			sb.append("rh=");
			sb.append(URLEncoder.encode("n:" + pmap.get("node")));
			if (pmap.get("page") != null) {
				sb.append("&page=");
				sb.append(pmap.get("page"));
			}
		} else {
			return url;
		}
		return (base + sb.toString());
	}

	// 私有方法，处理url格式化需要 /s/ 或 /s?
	private String formatSurl(String url) {
		if (StrUtil.isEmpty(url)) {
			return url;
		}
		if (url.indexOf("?") == -1) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		String[] params = param.split("&");
		Map<String, String> pmap = new TemplateUtil().formMap(params, "=");

		String base = "http://www.amazon.cn/s/?";
		StringBuffer sb = new StringBuffer();
		if (pmap.get("rh") != null) {
			String n = getLastN(pmap.get("rh"));
			sb.append("rh=");
			sb.append(URLEncoder.encode(n));
			if (pmap.get("page") != null) {
				sb.append("&page=");
				sb.append(pmap.get("page"));
			}
			url = base + sb.toString();
		}
		return url;
	}

	// 私有方法，处理url格式化需要 ：rh获取最后一个n属性
	private String getLastN(String rh) {
		String n = "";
		String[] params = rh.split(",");
		for (String tmp : params) {
			String[] ns = tmp.split(":");
			if (ns[0].equals("n")) {
				n = ns[0] + ":" + ns[1];
			}
		}
		return n;
	}

	@Override
	protected String dealCategory(String brand, String classic, Product product) {
		return classic;
	}

	/**
	 * 商品的短名称：品牌+型号
	 */
	@Override
	public void setShortName() {
		Node shortNameNode = TemplateUtil.getNode(root, shortNameXpath);
		if (shortNameNode != null) {
			String shortName = shortNameNode.getTextContent().trim();
			if (shortName != null && !"".equals(shortName)) {
				if (keywordsFilter.size() > 0) {
					keywordsFilter.add("【.*?】");
					keywordsFilter.add("\\(.*?\\)");
					for (String keyFilter : keywordsFilter) {
						shortName = shortName.replaceAll(keyFilter, "");
					}
				}
				product.setShortName(shortName);
			} else {
				LOG.info(url + "-->Product ShortName Is Error!");
			}
		} else {
			LOG.info(url + "-->Product ShortName Is Error!");
		}
	}

	// 原始分类
	public void set_Category() {
		NodeList categoryNodeList = TemplateUtil.getNodeList(root, catGoryXpath);
		if (categoryNodeList.getLength() > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < categoryNodeList.getLength(); i++) {
				if (i != 0) {
					sb.append("[xm99]");
				}
				String href = categoryNodeList.item(i).getTextContent().trim();
				href = href.substring(href.lastIndexOf("=") + 1, href.length())
						.replaceAll("[n%3A]", "").replaceAll("[\\s]{1,}", "");
				sb.append(href);
			}
			product.setCategory(sb.toString());
		} else {
			LOG.info("未能正常解析商品原始分类！  " + url);
		}
	}

	public static void main(String[] args) throws Exception {
		String url = "";
		ParserAmazonNew ama = new ParserAmazonNew();

		// testInvalidUrlFormat(ama);
		testUrlDecode(ama);

		url = "http://www.amazon.cn/PQI-%E5%8A%B2%E6%B0%B8-Intelligent-Drive-U821V-16GB-%E7%81%B0%E8%89%B2-%E4%BC%98%E7%9B%98/dp/B00A0AE3FS";
		url = "http://www.amazon.cn/ZOMEI-%E5%8D%93%E7%BE%8E-CANON-270/dp/B007T4RBAW";
		url = "http://www.amazon.cn/ONDA-%E6%98%82%E8%BE%BE-VP80-3D-4G-GPS-MP4%E6%92%AD%E6%94%BE%E5%99%A8-%E9%BB%91%E8%89%B2/dp/B007EHPA7U";
		url = "http://www.amazon.cn/%E6%85%A7%E4%B9%90%E5%AE%B6L40%E4%BA%8C%E5%B1%82%E4%B9%A6%E6%9F%9C12051-1/dp/B008KYFMW4";
		// Product p = parser.testParse(url);
		// System.out.println(p);
	}

	// 测试URL-Decode，分析具体url参数信息及格式规范等
	private static void testUrlDecode(ParserAmazonNew ama) throws Exception {
		String[] urls = new String[] {
				"http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a124912071",
				"http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a665002051%2cp_4%3asony%20%e7%b4%a2%e5%b0%bc",
				"http://www.amazon.cn/s/ref=sr_ex_n_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051&bbn=888465051&ie=UTF8&qid=1377489481",
				"http://www.amazon.cn/s/ref=sr_nr_p_4_0?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_4%3AKingston+%E9%87%91%E5%A3%AB%E9%A1%BF&bbn=888465051&ie=UTF8&qid=1377499967&rnid=2016119051",
				"http://www.amazon.cn/s/ref=sr_nr_p_36_5?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_36%3A2045273051&bbn=888465051&ie=UTF8&qid=1377499967&rnid=2045267051",
				"http://www.amazon.cn/b/ref=sr_aj?node=51878071&ajr=0",
				"http://www.amazon.cn/%E6%89%8B%E6%9C%BA-%E9%80%9A%E8%AE%AF/b/ref=sd_allcat_wi_?ie=UTF8&node=664978051",
				"http://www.amazon.cn/%e7%94%b5%e5%ad%90%e4%b9%a6-%e5%b0%91%e5%84%bf/b?ie=utf8&node=143276071",
				"http://www.amazon.cn/s/ref=lp_51878071_pg_3?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071&page=3&ie=UTF8&qid=1377500398",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_n_feature_keywords_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_n_feature_keywords_browse-bin%3A191965071&bbn=51878071&ie=UTF8&qid=1377500182&rnid=191947071",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_4_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_4%3ASamsung+%E4%B8%89%E6%98%9F&bbn=51878071&ie=UTF8&qid=1377500222&rnid=2016119051",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_36_5?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_36%3A2045273051&bbn=51878071&ie=UTF8&qid=1377500287&rnid=2045267051",
				"http://www.amazon.cn/s?ie=UTF8&field-keywords=longbo&index=toys&search-type=ss",
				"http://www.amazon.cn/s?ie=UTF8&page=1&rh=n%3A2078652051%2Cp_n_binding_browse-bin%3A2038564051%2Cn%3A2045366051%2Cn%3A!658391051%2Cn%3A658390051",
				"http://www.amazon.cn/s?ie=UTF8&page=1&rh=n%3A658409051%2Cp_n_age_range%3A2111929051",
				"http://www.amazon.cn/s/ref=lp_51878071_pg_2/478-6929103-7000512?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071&page=2&ie=UTF8&qid=1378087568" };
		for (String tmp : urls) {
			if (!StrUtil.isEmpty(tmp) && ama.isSafeUrl(tmp)) {
				System.out.println("\n1\t" + tmp);
				String d = URLDecoder.decode(tmp);
				System.out.println("2\t" + d);
				tmp = ama.formatSafeUrl(tmp);
				System.out.println("3\t" + tmp);
			}
		}
		System.out.println("读文件");
		// 读文件，测试
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File("e:/data/nutch/amazon-url-sb.txt")));
			String tmp = reader.readLine();
			while (!StrUtil.isEmpty(tmp)) {
				System.out.println("\n1\t" + tmp);
				String d = URLDecoder.decode(tmp);
				System.out.println("2\t" + d);
				tmp = ama.formatSafeUrl(tmp);
				System.out.println("3\t" + tmp);
				tmp = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

	}

	// url拼接错误导致无效url，格式化处理测试
	private static void testInvalidUrlFormat(ParserAmazonNew ama) {
		String url = "http://www.amazon.cn/%E8%80%83%E7%A0%94%E2%80%A2%E6%B3%95%E8%AF%AD%E4%BA%8C%E5%A4%96%E8%80%83%E5%89%8D%E5%86%B2%E5%88%BA-%E6%96%BD%E5%A9%89%E4%B8%BD/dp/“http:/www.joyo.com/detail/product.asp?asin=B0019VM03Y”";
		url = "http://www.amazon.cn/%E8%80%83%E7%A0%94%E2%80%A2%E6%B3%95%E8%AF%AD%E4%BA%8C%E5%A4%96%E8%80%83%E5%89%8D%E5%86%B2%E5%88%BA-%E6%96%BD%E5%A9%89%E4%B8%BD/dp/“http:/www.joyo.com/detail/product.asp?asin=B0019VM03Y”";
		url = "http://www.amazon.cn/dp/product-description/B001UE7G8O";
		url = "http://www.amazon.cn/dp/”http:/www.amazon.cn/gp/product/B00260GJ1Q";
		url = "http://www.amazon.cn/dp/”http:/www.amazon.cn/dp/B00862G62U\"";
		url = "http://www.amazon.cn/gp/product/ B00B0TW6T8";
		url = "http://www.amazon.cn/dp/”http:/www.amazon.cn/SunSonny-%E6%A3%AE%E6%9D%BE%E5%B0%BC-SR-2000-2-4G%E6%97%A0%E7%BA%BF%E7%9E%AC%E5%8A%A8-%E5%B7%A7%E5%85%8B%E5%8A%9B%E6%97%B6%E5%B0%9A%E9%94%AE%E9%BC%A0%E5%A5%97%E8%A3%85-%E7%99%BD%E8%89%B2/dp/B0085R4MQI/ref=sr_1_6";
		url = "http://www.amazon.cn/dp/‘http:/www.amazon.cn/dp/B007CVV6GW’";
		url = "http://www.amazon.cn/dp/‘http:/www.amazon.cn/茱莉牛奶味小熊饼饼干240g/dp/B004GGT29E/’";
		url = "http://www.amazon.cn/dp/product-description/‘http:/www.amazon.cn/%E6%9E%81%E7%9D%BF%E5%93%A5%E6%96%AF%E8%BE%BE%E9%BB%8E%E5%8A%A0%E7%B2%BE%E9%80%89%E5%92%96%E5%95%A1%E8%B1%86250G/dp/B005CECFTI/ref=sr_1_10";
		url = "http://www.amazon.cn/dp/www.amazon.cn/dp/B005I2F6QS";
		url = "http://www.amazon.cn/dp/www.harrypotter.com";
		url = "http://www.amazon.cn/Thomas-Jefferson-Author-of-America-Hitchens-Christopher/dp/h/dp/0060598956";
		url = "http://www.amazon.cn/%E6%85%A7%E4%B9%90%E5%AE%B6%E7%AB%B9%E6%89%98%E7%9B%9833010/dp/ASIN/B007CCDAVK";
		url = "http://www.amazon.cn/gp/product/ASIN/B007CCD9TS";
		url = "http://www.amazon.cn/The-Accidental-Billionaires-Mezrich-Ben/dp/0790739739";
		url = "http://www.amazon.cn/dp/0679442952";
		url = "http://www.amazon.cn/%E9%AB%98%E9%9C%B2%E6%B4%81%E5%81%A5%E7%99%BD%E9%98%B2%E8%9B%80%E7%89%99%E8%86%8F-200%E5%85%8B-2/dp/‘http:/www.amazon.cn/mn/detailApp?asin=B005DJWYL6’";
		// url = "http://www.amazon.cn/mn/detailApp/ref=sr_1_4?asin=B003Q97NM0";
		// url =
		// "http://www.amazon.cn/%E8%BF%9B%E5%8F%A3%E5%8E%9F%E7%89%88%E5%9B%BE%E4%B9%A6/b/ref=sd_allcat_books_l3_b2045366051/480-1315718-0182144?ie=UTF8&node=2045366051";
		// url =
		// "http://www.amazon.cn/%E5%B0%91%E5%84%BF%E5%9B%BE%E4%B9%A6/b/ref=sd_allcat_books_l3_b658409051/480-1315718-0182144?ie=UTF8&node=658409051";
		// url =
		// "http://www.amazon.cn/dp/B007OZO03M/ref=sd_allcat_kindle_l3_ebook_kindle_p/477-8027306-4324958";
		// url =
		// "http://www.amazon.cn/s/ref=sd_allcat_books_l3_sbook0_2_years/479-6178736-2615222?ie=UTF8&page=1&rh=n%3A658409051%2Cp_n_age_range%3A2111929051";
		// url =
		// "http://www.amazon.cn/s/ref=dp_brlad_entry/475-1791749-7702256?ie=UTF8&page=1&rh=n%3A2134641051";
		// url =
		// "http://www.amazon.cn/s/ref=sr_pg_56/480-1139296-4201955?rh=n%3A831780051&page=56&ie=UTF8&qid=1378036912";
		// url =
		// "http://www.amazon.cn/gp/product/B009Z7KHSE/ref=gb1h_img_c-3_8392_A3DHRNZDZIHNJ1?smid=A1AJ19PSB66TGU&pf_rd_m=A1AJ19PSB66TGU&pf_rd_t=101&pf_rd_s=center-3&pf_rd_r=0E534A9ZRQFN48V647ZA&pf_rd_i=664978051&pf_rd_p=66588392";
		// url = "http://www.amazon.cn/gp/ product/B00B0TW6T8";
		// url = "http://www.amazon.cn/%E7%8E%A9%E5%85%B7/dp/ASIN/B00B6XZZ6E";

		System.out.println("begin url=\n" + url);
		url = ama.formatSafeUrl(url);
		System.out.println("format url=\n" + url);
		boolean b = ama.isProductPage(url);
		System.out.println("product page=" + b);
		b = ama.isSafeUrl(url);
		System.out.println("safe url=" + b);
	}
}