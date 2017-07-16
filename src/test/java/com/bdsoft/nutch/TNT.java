package com.bdsoft.nutch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.nutch.util.URLUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bdsoft.nutch.count.RandomWriteFile;
import com.bdsoft.nutch.parser.ParserAmazonNew;
import com.bdsoft.nutch.parser.ParserSuning;
import com.bdsoft.nutch.parser.ProductDealUtil;
import com.bdsoft.nutch.parser.TemplateUtil;
import com.bdsoft.utils.DomUtil;
import com.bdsoft.utils.HtmlCompressor;
import com.bdsoft.utils.NetUtil;
import com.bdsoft.utils.RequestUtil;
import com.bdsoft.utils.StrUtil;
import com.google.gson.Gson;

interface IT {
}

public class TNT {

	private static String CHARCODE = "utf-8";

	// 对URL进行解码
	public static String decode(String url) throws Exception {
		return URLDecoder.decode(url);
	}

	public static String encode(String url) throws Exception {
		return URLEncoder.encode(url);
	}

	// 指定xpath，解析节点
	public static void printNodes3(String url, String xpath) throws Exception {
		Map<String, String> safeConvert = new HashMap<String, String>();
		safeConvert.put("textarea", "div");
		org.w3c.dom.Document doc = RequestUtil.getSafeDocument(url, CHARCODE,
				safeConvert);
		NodeList nodes = DomUtil.getNodeList(doc, xpath);
		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);
				System.out.print((i + 1) + " / " + node.getNodeName() + "\t");
				String txt = node.getTextContent();
				System.out.println("值 = " + txt);
			}
		}
		System.out.println("nodes = " + nodes.getLength());
	}

	// 指定xpath，解析节点
	public static void printNodes2(String url, String xpath) throws Exception {
		Set<String> urls = new TreeSet<String>();
		ParserAmazonNew par = new ParserAmazonNew();
		URL base = new URL("http://www.amazon.cn");
		org.w3c.dom.Document doc = RequestUtil.getDocument(url, CHARCODE);
		NodeList nodes = DomUtil.getNodeList(doc, xpath);
		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);
				System.out.print((i + 1) + " / " + node.getNodeName() + "\t");
				String txt = node.getTextContent();
				System.out.println("值 = " + txt);
				try {
					URL cu = URLUtil.resolveURL(base, txt);
					url = cu.toString();
					// url = decode(url);
					url = par.formatSafeUrl(url);
					System.out.println(url);
					urls.add(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("nodes = " + nodes.getLength());
		System.out.println("size = " + urls.size());
	}

	// 指定xpath，解析节点
	public static void printNodes(String url, String xpath) throws Exception {
		org.w3c.dom.Document doc = RequestUtil.getDocument(url, CHARCODE);
		NodeList nodes = DomUtil.getNodeList(doc, xpath);
		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);
				System.out.print((i + 1) + " / " + node.getNodeName() + "\t");
				String txt = node.getTextContent();
				System.out.println("值 = " + txt);
			}
		}
		System.out.println("nodes = " + nodes.getLength());
	}

	// 验证正则
	public static boolean checkReg(String reg, String str) {
		Pattern p1 = Pattern.compile(reg);
		return p1.matcher(str).matches();
	}

	// 获取正则分组信息
	public static String takeGroup(String reg, String str, int index) {
		Pattern p1 = Pattern.compile(reg);
		Matcher m1 = p1.matcher(str);
		if (m1.find()) {
			return m1.group(index);
		}
		return "no match group";
	}

	public static void main(String[] arsg) throws Exception {
		String xpath = "";
		String url = "http://www.amazon.cn/s/ref=sr_pg_2/480-1139296-4201955?rh=n%3A831780051&page=2&ie=UTF8&qid=1378036779";
		// url = Formatter.reverseUrl(url);
		// System.out.println(url);

		url = "http://item.jd.com/16014830.html";
		xpath = "//A[contains(@href,'jdvote/skucheck')]/@href";
		// printNodes(url, xpath);
		// printNodes2(url, xpath);
		// printNodes3(url, xpath);

		// t0905AmazonUrl404();
		// t0904LefengReg();
		// t0830SuningReg();
		// t0829DangdangReg();
		// t0901AmazonReg();
		// Set<String> catUrls = t0903SuningCat();
		// t0903SuningCatSum(catUrls);
		// t0903JumeiReg();
		// t0927Reg();
		t1011Jd();

		// url =
		// "http://www.gome.com.cn/p/json?module=async_search&paramJson=%7B%22pageNumber%22%3A%2211%22%2C%22facets%22%3A%7B%7D%2C%22envReq%22%3A%7B%22catId%22%3A%22cat15965733%22%2C%22catalog%22%3A%22homeStore%22%2C%22siteId%22%3A%22homeSite%22%2C%22shopId%22%3A%22%22%2C%22regionId%22%3A%2211011400%22%2C%22ip%22%3A%22123.116.109.176%22%2C%22sessionId%22%3A%22%22%2C%22pageName%22%3A%22list%22%2C%22et%22%3A%22%22%2C%22XSearch%22%3Afalse%2C%22startDate%22%3A0%2C%22endDate%22%3A0%2C%22pageNumber%22%3A1%2C%22pageSize%22%3A36%2C%22state%22%3A4%2C%22weight%22%3A0%2C%22more%22%3A0%2C%22sale%22%3A0%2C%22instock%22%3A0%2C%22question%22%3A%22%22%2C%22categories%22%3A%7B%7D%7D%7D";
		// url = decode(url);
		// System.out.println(url);
		// url =
		// "{\"pageNumber\":\"11\",\"envReq\":{\"catId\":\"cat15965733\",\"catalog\":\"homeStore\",\"siteId\":\"homeSite\",\"pageName\":\"list\",\"pageSize\":36}}";
		// url = "http://www.gome.com.cn/p/json?paramJson=" + encode(url);
		// System.out.println(url);

		// int ia[] = new int[1];
		// System.out.println(ia[0]);
		// ia[0]++;
		// System.out.println(ia[0]);

		// URL base = new
		// URL("http://mall.jumei.com/products/0-1-0-11-1.html?from=top_category");
		// URL next = URLUtil.resolveURL(base,
		// "../product_1209.html?from=mall_list_result_p1_pos39");
		// System.out.println(next.toString());

		// url = "http://www.amazon.cn/一品玉新疆巴旦木400g/dp/B003QMM6JC/";
		// URL base = new URL(url);
		// String domain = URLUtil.getDomainName(base);
		// String host = base.getHost();
		// System.out.println("domain="+domain + "\nhost=" + host);

		// Object o = test0806();
		// System.out.println(o == null);

		String uri = "file:///";
		URI u = URI.create(uri);
//		System.out.println(u.getPath());
	}
	
	public static void t1011Jd(){
		String url = "http://book.360buy.com/10000072.html";
		String reg = ".*(http://item.jd.com/([\\d]+).html).*";
		Pattern pat = Pattern.compile(reg);
		Matcher m = pat.matcher(url);
		
		String flag = ".*(jd.com|360buy.com).*";
		System.out.println(url.matches(flag));
		
		System.out.println(url);
		if(m.find()){
			String take = m.group(1);
			System.out.println(take);
			String id = m.group(2);
			System.out.println(id);
			List<String> list = new ArrayList<String>();
			for(int i=0;i<3000000;i++){
//				list.add(id + new Random().nextInt(100000));
			}
			System.out.println(list.size());
		}
		
		
	}

	public static void t0927Reg() {
		String str = "";
		Properties pro = new Properties();
		pro.setProperty("name", "nutch/${hadoop.log.dir}/history/user");

		Pattern varPat = Pattern.compile("\\$\\{[^\\}\\$\u0020]+\\}");
		Matcher match = varPat.matcher(str);

		str = pro.getProperty("name");
		match.reset(str);
		System.out.println(!match.find());

		String var = match.group();
		var = var.substring(2, var.length() - 1); // remove ${ .. }
		System.out.println(var);

		System.out.println(match.start());
		System.out.println(match.end());
	}

	public static void t0905AmazonUrl404() throws Exception {
		File f = new File("e:/count/20130904201439_url_amazon");
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		int ok = 0, bad = 0;
		String charset = "utf-8";
		Set<String> okUrl = new HashSet<String>();
		Set<String> badUrl = new HashSet<String>();
		while (!StrUtil.isEmpty(line)) {
			System.out.println("待校验URL=" + line);
			try {
				NetUtil.getPageContent(line, charset);
				ok++;
				okUrl.add(line);
				if (ok > 0 && ok % 100 == 0) {
					t0905AamzonUrlSave("e:/count/20130904201439_url_amazon_ok",
							okUrl);
					okUrl.clear();
				}
			} catch (Exception e) {
				bad++;
				badUrl.add(line);
				if (bad > 0 && bad % 100 == 0) {
					t0905AamzonUrlSave(
							"e:/count/20130904201439_url_amazon_bad", badUrl);
					badUrl.clear();
				}
			}
			line = br.readLine();
			System.out.println("累计校验：" + (ok + bad) + "个URL，OK-" + ok
					+ "个，BAD-" + bad + "个");
			// Thread.sleep(50);
		}
		t0905AamzonUrlSave("e:/count/20130904201439_url_amazon_ok", okUrl);
		t0905AamzonUrlSave("e:/count/20130904201439_url_amazon_bad", badUrl);
		System.out.println("累计校验：" + (ok + bad) + "个URL，OK-" + ok + "个，BAD-"
				+ bad + "个。 时间：" + new Date().toLocaleString());
	}

	public synchronized static void t0905AamzonUrlSave(String path,
			Set<String> urls) {
		StringBuffer sb = new StringBuffer();
		for (String url : urls) {
			sb.append(url);
			sb.append("\r\n");
		}
		RandomWriteFile.writeFile(path, sb.toString().getBytes());
	}

	public static void t0904LefengReg() {
		String reg = "http://product.lefeng.com/product/([\\d]+).html.*?";
		String url = "http://product.lefeng.com/coat/206854.html";
		url = "http://product.lefeng.com/product/183901.html?wt.ct=link&wt.s_pg=navi&wt.s_pf=jlynn&biid=34592";

		// reg =
		// "http://s.lefeng.com/(?:directory|coat|sweater)/([_\\d%A-Za-z]+).html.*";
		// url =
		// "http://s.lefeng.com/directory/26000_0_0_0_0_0_0_0_2.html#list";
		// url = "http://s.lefeng.com/coat/26024.html";
		// url = "http://s.lefeng.com/sweater/26044.html";

		// reg = "http://product.lefeng.com/.*productId=([\\d]+).*";
		// url =
		// "http://product.lefeng.com/goods/productDetail_coat.jsp?productId=189275&wt.ct=link&wt.s_pg=navi&wt.s_pf=huanxing&biid=43933";

		reg = "http://track.lefeng.com/.*productid=([\\d]+).*";
		url = "http://track.lefeng.com/vrm_track_jumpok.html?productid=5930&productstatus=0&uuid=8d548b0b-3e24-4ca9-8dc2-dcceccacc731_53303&userid=0&";

		System.out.println("reg =\t" + reg);
		System.out.println("url =\t" + url);
		System.out.println("match?\t" + checkReg(reg, url));
		String tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + takeGroup(reg, url, 1));
		String[] ps = tmp.split("_");
		System.out.println(ps.length);
		if (ps.length == 9) {
			System.out.println(ps[0] + "_0_0_0_0_0_0_0_" + ps[8]);
		}
		reg = "http://s.lefeng.com/(?:directory|coat|sweater)/([\\d]+)([_\\d]+)?.html.*";
		System.out.println("group\t" + takeGroup(reg, url, 2));
	}

	public static void t0903JumeiReg() {
		String reg = "http://mall.jumei.com/products/([\\d-]+).html.*";
		String url = "http://mall.jumei.com/products/0-34-0-11-1.html?from=top_category";
		url = "http://mall.jumei.com/products/0-260-0.html";
		url = "http://mall.jumei.com/products/0-6-";

		// reg =
		// "http://mall.jumei.com/products/([\\d-]+).html.*new_catid=([\\d]+).*";
		// url =
		// "http://mall.jumei.com/products/0-60-0-11-1.html?from=top_category_hover&new_catid=280";
		// url =
		// "http://mall.jumei.com/products/0-232-0-11-1.html?from=top_category_hover&new_catid=445";
		// url = "http://mall.jumei.com/Kose/products/18-0-13-11-1.html";

		reg = "http://mall.jumei.com/([a-zA-Z]+)/(product_[\\d]+).html.*";
		// reg = "http://mall.jumei.com/(product_[\\d]+).html.*";
		url = "http://mall.jumei.com/aaskincare/product_53834.html";
		// url = "http://mall.jumei.com/product_2086.html";

		System.out.println("reg =\t" + reg);
		System.out.println("url =\t" + url);
		boolean b = checkReg(reg, url);
		System.out.println("match?\t" + b);
		String tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);
		// String[] params = tmp.split("-");
		// System.out.println(params.length);

		reg = "http://mall.jumei.com/([a-zA-Z]+)/(product_[\\d]+).html.*";
		tmp = takeGroup(reg, url, 2);
		System.out.println("group\t" + tmp);
	}

	public static void t0903SuningCatSum(Set<String> catUrls) throws Exception {
		Pattern listFlag1 = Pattern
				.compile("http://search.suning.com/emall/pcd.do\\?.*ci=([\\d]+).*");
		Pattern listFlag2 = Pattern
				.compile("http://search.suning.com/emall/strd.do\\?.*ci=([\\d]+).*");
		Pattern listFlag3 = Pattern
				.compile("http://search.suning.com/emall/trd.do\\?.*ci=([\\d]+).*");
		int size = catUrls.size(), i = 0;
		String code = "utf-8";
		long psum = 0;
		// trd-20; pcd-48; strd-48
		for (String url : catUrls) {
			i++;
			System.out.println("抓取分类：(" + i + "/" + size + ")" + url);
			String src = NetUtil.getPageContent(url, code);
			Document doc = Jsoup.parse(src);
			int page = 0;
			String pageInfo = "";
			if (listFlag1.matcher(url).matches()) {
				page = 48;
				Element tmp = doc.getElementById("pageTotal");
				pageInfo = tmp.text();
			} else if (listFlag2.matcher(url).matches()) {
				page = 48;
				Element tmp = doc.getElementById("pageTotal");
				pageInfo = tmp.text();
			} else if (listFlag3.matcher(url).matches()) {
				page = 20;
				Element tmp = doc.getElementsByClass("pageCount").get(0);
				pageInfo = tmp.text();
				pageInfo = pageInfo.split("\\/")[1];
			}
			int total = Integer.parseInt(pageInfo);
			int sum = total * page;
			System.out.println("\t旗下商品：" + sum);
			psum = psum + sum;
			System.out.println("分类统计，商品总数：" + psum);
		}
	}

	public static Set<String> t0903SuningCat() throws Exception {
		ParserSuning su = new ParserSuning();
		Set<String> catUrls = new HashSet<String>();
		String catUrl = "http://www.suning.com/emall/pgv_10052_10051_1_.html";
		String code = "utf-8";
		String src = NetUtil.getPageContent(catUrl, code);
		Document doc = Jsoup.parse(src);
		// DIV[@class='boxCon']/UL/LI/A/@href
		Elements eles = doc.getElementsByClass("boxCon");
		for (Element ele : eles) {
			Elements eleas = ele.getElementsByTag("a");
			for (Element a : eleas) {
				String href = a.attr("href");
				if (su.isSafeUrl(href)) {
					href = su.formatSafeUrl(href);
					System.out.println(href);
					catUrls.add(href);
				}
			}
		}
		System.out.println("cat size =" + catUrls.size());
		// DIV[@class='listLeft' or @class='listRight']/DL/DD/SPAN/A/@href
		eles = doc.getElementsByClass("listLeft");
		for (Element ele : eles) {
			Elements eleas = ele.getElementsByTag("a");
			for (Element a : eleas) {
				String href = a.attr("href");
				if (su.isSafeUrl(href)) {
					href = su.formatSafeUrl(href);
					System.out.println(href);
					catUrls.add(href);
				}
			}
		}
		System.out.println("cat size =" + catUrls.size());
		eles = doc.getElementsByClass("listRight");
		for (Element ele : eles) {
			Elements eleas = ele.getElementsByTag("a");
			for (Element a : eleas) {
				String href = a.attr("href");
				if (su.isSafeUrl(href)) {
					href = su.formatSafeUrl(href);
					System.out.println(href);
					catUrls.add(href);
				}
			}
		}
		System.out.println("cat size =" + catUrls.size());
		return catUrls;
	}

	public static void t0901AmazonReg() {
		String reg = "(?:^http://www.amazon.cn.*/dp/)([a-zA-Z0-9]+).*?";
		String url = "http://www.amazon.cn/dp/B00960YR3Q/ref=sd_allcat_kindle_l3_pad_kindle_fir/480-1315718-0182144";

		// reg = "http://www.amazon.cn(?:/.*)?/b(?:/|\\?)?.*node=([\\d]+).*";
		// url =
		// "http://www.amazon.cn/%E8%BF%9B%E5%8F%A3%E5%8E%9F%E7%89%88%E5%9B%BE%E4%B9%A6/b/ref=sd_allcat_books_l3_b2045366051/480-1315718-0182144?ie=UTF8&node=2045366051&page=2";
		// url =
		// "http://www.amazon.cn/b/ref=sd_allcat_books_l3_b2088297051/480-1315718-0182144?ie=UTF8&node=2088297051&page=3";
		// url =
		// "http://www.amazon.cn/%e7%94%b5%e5%ad%90%e4%b9%a6-%e5%b0%91%e5%84%bf/b?ie=utf8&node=143276071";
		// url = "http://www.amazon.cn/b/ref=sr_aj?node=51878071&ajr=0";
		// url =
		// "http://www.amazon.cn/%E6%89%8B%E6%9C%BA-%E9%80%9A%E8%AE%AF/b/ref=sd_allcat_wi_?ie=UTF8&node=664978051";
		// url =
		// "http://www.amazon.cn/%e7%94%b5%e5%ad%90%e4%b9%a6-%e5%b0%91%e5%84%bf/b?ie=utf8&node=143276071";
		// url =
		// "http://www.amazon.cn/s/ref=s9_dnav_bw_ir01_s?__mk_zh_CN=%E4%BA%9A%E9%A9%AC%E9%80%8A%E7%BD%91%E7%AB%99&node=658390051,!658391051,658400051,658619051&page=1&sort=salesrank&bbn=658400051&pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=center-2&pf_rd_r=1ECRJMEFETPJS17WT9B4&pf_rd_t=101&pf_rd_p=78920572&pf_rd_i=658400051";
		//
		// reg =
		// "http://www.amazon.cn(?:/.*)?/s(?:/|\\?)?.*rh=([%A-Za-z\\d_\\-!]+).*";
		// url =
		// "http://www.amazon.cn/s/ref=sr_pg_3?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051&page=3&bbn=888465051&ie=UTF8&qid=1378085295";
		// url =
		// "http://www.amazon.cn/s?ie=UTF8&page=1&rh=n%3A2078652051%2Cp_n_binding_browse-bin%3A2038564051%2Cn%3A2045366051%2Cn%3A!658391051%2Cn%3A658390051";

		reg = "(?:^http://www.amazon.cn.*/sim/)([a-zA-Z0-9]+).*?";
		url = "http://www.amazon.cn/%E5%AE%9E%E7%94%A8%E7%A7%91%E6%8A%80%E8%8B%B1%E8%AF%AD%E7%BF%BB%E8%AF%91%E8%A6%81%E4%B9%89-%E9%97%AB%E6%96%87%E5%9F%B9/sim/B001NH3J8O/2";
		url = "http://www.amazon.cn/%E5%AE%9E%E7%94%A8%E7%A7%91%E6%8A%80%E6%9C%AF%E8%AF%AD%E6%89%8B%E5%86%8C-%E9%99%88%E7%91%9E%E8%97%BB/sim/B006LT95K0/2";

		reg = "(?:^http://www.amazon.cn.*[\\u4e00-\\u9fa5]+.*/dp/)([a-zA-Z0-9]+).*?";
		url = "http:/www.amazon.cn/一品玉新疆巴旦木400g/dp/B003QMM6JC/";

		// reg = "[\\u4e00-\\u9fa5]?.*";
		// url = "一品玉新疆巴旦木400g";

		System.out.println("reg =\t" + reg);
		System.out.println("url =\t" + url);
		boolean b = checkReg(reg, url);
		System.out.println("match?\t" + b);
		String tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);

		reg = "http://www.amazon.cn(?:/.*)?/[b|s](?:/|\\?)?.*page=([\\d]+).*";
		tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);
	}

	public static void t0830SuningReg() {
		String reg = "http://www.suning.com/emall/prd_10052_10051_-7_([\\d]+)_.html.*";
		String url = "http://www.suning.com/emall/prd_10052_10051_-7_14862227_.html";

		reg = "http://search.suning.com/emall/pcd.do\\?.*ci=([\\d]+).*";
		url = "http://search.suning.com/emall/pcd.do?ci=20016";
		url = "http://search.suning.com/emall/pcd.do?ci=20016&cp=9";
		url = "http://search.suning.com/emall/pcd.do?ci=250511&tp=_&cityId={cityId}%20&iy=-1&cf=solr_1414_attrId:E99E8BE69EB6E99E8BE69F9C";

		reg = "http://search.suning.com/emall/strd.do\\?.*ci=([\\d]+).*";
		// url = "http://search.suning.com/emall/strd.do?ci=258006";
		url = "http://search.suning.com/emall/strd.do?ci=258006&cp=2";
		url = "http://search.suning.com/emall/strd.do?ci=340033&cityId={cityId}";

		reg = "http://search.suning.com/emall/trd.do\\?.*ci=([\\d]+).*";
		url = "http://search.suning.com/emall/trd.do?ci=262532&st=0&cityId=9173&cp=6";

		System.out.println("reg =\t" + reg);
		System.out.println("url =\t" + url);
		boolean b = checkReg(reg, url);
		System.out.println("match?\t" + b);
		String tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);

		reg = "http://search.suning.com/emall/trd.do\\?.*cp=([\\d]+).*";
		tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);
	}

	public static void t0829DangdangReg() {
		String reg = "http://product.dangdang.com/([\\d]+).html.*";
		String url = "http://product.dangdang.com/9299998.html#ddclick?act=click&pos=9299998_48_1_p&cat=01.07.36.00.00.00&key=&qinfo=&pinfo=933_1_72&minfo=&ninfo=&custid=&permid=&ref=&rcount=&type=&t=1377772625000";

		// reg =
		// "http://product.dangdang.com/Product.aspx\\?product_id=([\\d]+).*";
		// url =
		// "http://product.dangdang.com/product.aspx?product_id=60541092#ddclick?act=click&pos=1022681207_44_1_m&cat=4002069&key=&qinfo=&pinfo=&minfo=3233_1_48&ninfo=&custid=&permid=20130821134103669690026792766851672&ref=&rcount=&type=&t=1377685343000";
		//
		// reg = "http://category.dangdang.com/all/\\?.*category_id=([\\d]+).*";
		// url = "http://category.dangdang.com/all/?category_id=4002069";
		// url =
		// "http://category.dangdang.com/all/?att=1%3A1214&category_id=4002242&xx=1&page_index=3&a=1";
		//
		// reg = "http://category.dangdang.com/cid([\\d]+).*";
		// url = "http://category.dangdang.com/cid4002778.html";
		// url = "http://category.dangdang.com/cid4002778-pg1000.html";
		// url =
		// "http://category.dangdang.com/cid4002778-f0%7C0%7C0%7C1%7C0-pg10.html";
		// url =
		// "http://category.dangdang.com/cid4002291.html#ddclick?act=clickcat&pos=0_0_0_m&cat=4003382&key=&qinfo=&pinfo=&minfo=263_1_48&ninfo=&custid=&permid=&ref=&rcount=&type=&t=1375254499000";
		//
		// reg = "http://category.dangdang.com/list\\?cat=([\\d]+).*";
		// url = "http://category.dangdang.com/list?cat=4002859&store=mt1";

		// reg = "http://category.dangdang.com/cp([\\d]{2}(.[\\d]{2})+).*html";
		// url = "http://category.dangdang.com/cp01.03.56.00.00.00.html";
		// url = "http://category.dangdang.com/cp01.03.56.00.00.00-pg2.html";
		// url =
		// "http://category.dangdang.com/cp01.03.56.00.00.00-srsort_score_desc-pg1.html";
		//
		// reg =
		// "http://category.dangdang.com/all/\\?category_path=([\\d]{2}(.[\\d]{2})+).*";
		// url =
		// "http://category.dangdang.com/all/?category_path=01.03.30.00.00.00";
		// url =
		// "http://category.dangdang.com/all/?category_path=01.03.30.00.00.00&sort_type=sort_sale_amt_desc&page_index=3&a=1";
		//
		// reg =
		// "http://product.dangdang.com/Product.aspx\\?product_id=([\\d]+).*";
		// url =
		// "http://product.dangdang.com/Product.aspx?product_id=1109171702&ref=shop-2-RA";
		//
		// reg = "http://product.dangdang.com/([\\d]+).html.*";
		// url = "http://product.dangdang.com/1109171702.html";
		//
		// reg = "http://shop.dangdang.com/([\\d]+).*";
		// url = "http://shop.dangdang.com/6400";
		//
		// reg =
		// "http://shop.dangdang.com/store_clothes.php\\?.*sid=([\\d]+).*";
		// url = "http://shop.dangdang.com/store_clothes.php?sid=6400";
		// url =
		// "http://shop.dangdang.com/store_clothes.php?inner_cat=all&p=2&sid=6141&sort=sort_sellcount_desc&ref=shop-2-RA";

		// reg = "http://www.dangdang.com/brands/([\\d]+)?.*";
		// url = "http://www.dangdang.com/brands/list.html";
		// url = "http://www.dangdang.com/brands/1052.html";

		// reg = "http://www.dangdang.com/brands/([\\d]+)?.*(page=([\\d]+))?.*";
		// url =
		// "http://www.dangdang.com/brands/11614000000.html?page=2&order=sort_shopid_asc";

		System.out.println("reg =\t" + reg);
		System.out.println("url =\t" + url);
		boolean b = checkReg(reg, url);
		System.out.println("match?\t" + b);
		String tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);
		reg = "http://shop.dangdang.com/store_clothes.php\\?.*p=([\\d]+).*";
		tmp = takeGroup(reg, url, 1);
		System.out.println("group\t" + tmp);
	}

	public static void t0828() throws Exception {
		long psum = 0;
		int csum = 0;
		String catUrl = "http://www.jd.com/allSort.aspx";
		String code = "gb2312";

		String src = NetUtil.getPageContent(catUrl, code);
		Document doc = Jsoup.parse(src);
		Elements eles = doc.getElementsByClass("m");
		int size = eles.size();
		for (Element ele : eles) {
			Elements eles2 = ele.getElementsByClass("mc").get(0)
					.getElementsByTag("dl");
			for (Element ele2 : eles2) {
				Elements eles3 = ele2.getElementsByTag("dd").get(0)
						.getElementsByTag("a");
				for (Element ele3 : eles3) {
					String href = ele3.attr("href");
					if (href.contains("list.jd.com")) {
						System.out.println(ele3.text() + ":" + href);
						csum++;
						src = NetUtil.getPageContent(href, code);
						doc = Jsoup.parse(src);

						Elements eles4 = doc.getElementsByClass("total");
						if (eles4 != null && eles4.size() == 1) {
							String sum = eles4.get(0)
									.getElementsByTag("strong").get(0).text();
							psum += Long.parseLong(sum);
						} else {
							Elements eles5 = doc.getElementsByClass("pagin");
							if (eles5 != null && eles5.size() >= 1) {
								for (Element ele5 : eles5) {
									if (ele5.attr("class").contains("fr")) {
										Elements eles6 = ele5
												.getElementsByTag("a");
										size = eles6.size();
										if (size < 3) {
											continue;
										}
										Element tmp = eles6.get(size - 2);
										String sum = tmp.text();
										psum = psum + (Long.parseLong(sum) - 1)
												* 15;
										break;
									}
								}
							}
						}

						System.out.println("分类:" + csum + ",商品=" + psum);
						System.out.println();
					}
				}
			}
		}
	}

	public static void t08222() {
		String url = "http://item.wgimg.com/det_000000000000000000000047D0AF3DB7";
		// url = "http://item.wgimg.com/det_0000000000000000000000076C544604";
		String data = RequestUtil.getOfHttpURLConnection(url, "utf-8")
				.toString();
		System.out.println("原内容=" + data);

		int s = data.indexOf("{");
		int e = data.lastIndexOf(";");
		data = data.substring(s, e);
		System.out.println("格式化=" + data);

		data = data.replaceAll("\"0\"", "\"detail\"");
		data = data.replaceAll("\"1\"", "\"param\"");
		System.out.println("自定义=" + data);

		Gson gson = new Gson();
		YixunJSON json = gson.fromJson(data, YixunJSON.class);

		// 解参数
		String param = json.getParam();
		// System.out.println(param);
		String xpath = "//TABLE/TBODY//descendant::TR[TD]";
		DocumentFragment doc = RequestUtil.getDocumentFragmentByString(param,
				"utf-8");
		param = TemplateUtil.getMParas(doc, xpath);
		if (param != null && !"".equals(param)) {
			param = TemplateUtil.formatMparams(param);
		}
		System.out.println("参数=" + param);

		// 解详情
		StringBuffer sb = new StringBuffer();
		String detail = json.getDetail();
		// System.out.println(detail);
		xpath = "//DIV[@class='mod_detail_info id_features']";
		doc = RequestUtil.getDocumentFragmentByString(detail, "utf-8");
		NodeList nodeList1 = TemplateUtil.getNodeList(doc, xpath);
		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node1 = nodeList1.item(i);
			TemplateUtil.getTextHelper(sb, node1);
		}
		xpath = "//DIV[@class='mod_detail_info id_link']";
		nodeList1 = TemplateUtil.getNodeList(doc, xpath);
		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node1 = nodeList1.item(i);
			TemplateUtil.getTextHelper(sb, node1);
		}
		System.out.println("\n详情=" + sb.toString());
	}

	public static void t0822() throws Exception {
		String fp = "e:/data/B007T4RBAW-src.html";
		File file = new File(fp);
		if (file.exists()) {
			File bfile = new File(fp.replaceAll("src", "trim"));
			long len = file.length();
			long k = len / 1024;
			System.out.println(len + "/" + k);
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(bfile);

			StringBuffer sb = new StringBuffer();
			byte[] data = new byte[1024];
			while (in.read(data) != -1) {
				sb.append(new String(data));
			}
			String html = sb.toString();
			html = HtmlCompressor.compress(html);

			// com.googlecode.htmlcompressor.compressor.HtmlCompressor hc = new
			// com.googlecode.htmlcompressor.compressor.HtmlCompressor();
			// hc.setRemoveStyleAttributes(true);
			// hc.setRemoveComments(true);
			// html = hc.compress(html);

			out.write(html.getBytes());
			out.flush();

			in.close();
			out.close();

			len = bfile.length();
			k = len / 1024;
			System.out.println(len + "/" + k);
		}
	}

	public static void t0821() {
		String cargoUrl = "http://st.3.cn/gds.html?callback=getStockCallback&skuid=536ADF6E4B42138B30183054EBF77D25&provinceid=1&cityid=72&areaid=4137&townid=0&sortid1=1315&sortid2=1343&sortid3=1354&cd=1_1_1";
		String priceUrl = "http://p.3.cn/prices/get?skuid=J_1019038989&type=1&callback=changeImgPrice2Num";
		priceUrl = "http://item.jd.com/1017642-487.html";
		int ec = 0, sc = 0;
		for (int i = 0; i < 100; i++) {
			try {
				String cargo = RequestUtil.getOfHttpURLConnection(cargoUrl,
						"gbk").toString();
				System.out.println(cargo);
				String price = RequestUtil.getOfHttpURLConnection(priceUrl,
						"gbk").toString();
				System.out.println(price);
				sc++;
				System.out.println("success>" + i);
			} catch (Exception e) {
				ec++;
				System.out.println("falure>" + i);
			}
		}
		System.out.println(sc + "/" + ec);
	}

	public static void t0820() throws Exception {
		String web = "http://www.newegg.com.cn/Product/S2J-20X-02A_028.htm";
		// web = "http://xinyue.jd.com/view_search-30682-239168-5-1-20-14.html";
		// web = "http://item.jd.com/1027779968.html";
		// web = "http://www.jd.com/hotProducts.html";
		web = "http://g-ec4.images-amazon.com/im              <div class=\"emptyClear\"> </div>";
		web = "http://www.51youpin.com/shop/search.php?keywords=%E8%AF%BB%E5%8D%A1%E5%99%A8&f=mainnav";
		web = "http:// http/book.360buy.com/11147297.html";
		// web = "http://lingerie.moonbasa.com&type=1&adsiteid=10000007";
		// web = "http://www.bobobaby.com.cn/";
		web = "http://item.jd.com.com/index-29544.html";
		web = "http://www.amazon.cn/%E8%80%83%E7%A0%94%E2%80%A2%E6%B3%95%E8%AF%AD%E4%BA%8C%E5%A4%96%E8%80%83%E5%89%8D%E5%86%B2%E5%88%BA-%E6%96%BD%E5%A9%89%E4%B8%BD/dp/“http:/www.joyo.com/detail/product.asp?asin=B0019VM03Y”";
		web = " http://desc.gome.com.cn/html/bbchtml/productDesc/descHtml/201307/desc04/A0003768097.html?callback=jianjie&_=1376925092757";
		web = "http://cart.360buy.com/cart/initCart.action?pid=690445&pcount=1&ptype=1&r=635126981395964311";

		String domain = URLUtil.getDomainName(web);
		System.out.println(domain);
		URL url = new URL(web);
		String host = url.getHost();
		System.out.println(host);

		web = "http://item.jd.com/1018838166.html";
		String[] us = web.split("http");
		for (String u : us) {
			System.out.println(u);
		}
	}

	public static void t0813() throws Exception {
		String cls = "org.apache.nutch.crawl.Crawl";
		File file = new File(cls);
		String path = file.toURL().toString();
		System.out.println(path);
	}

	public static Object test0806() {
		Object o = new Object();
		try {
			System.out.println(1 / 1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return o;
	}

	public static void test0805() {
		ParserAmazonNew zon = new ParserAmazonNew();
		String url = "http://www.amazon.cn/gp/redirect.html?ie=UTF8&location=http%3A%2F%2Fwww.amazon.cn%2Fgp%2Ffeature.html%2Fref%3Dnavswms_zcn%3FdocId%3D377588&token=AA7ED3A8C2E38B0F569B620BB51B0DF17FA8410F";
		url = "https://www.amazon.cn/ap/signin?_encoding=UTF8&openid.assoc_handle=cnflex&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.mode=checkid_setup&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.ns.pape=http%3A%2F%2Fspecs.openid.net%2Fextensions%2Fpape%2F1.0&openid.pape.max_auth_age=0&openid.return_to=https%3A%2F%2Fwww.amazon.cn%2Fgp%2Fcss%2Fhomepage.html%3Fie%3DUTF8%26ref_%3Dgno_yam_ya";
		url = "http://www.amazon.cn/gp/digital/fiona/manage";
		url = "http://www.amazon.cn/gp/help/customer/display.html?ie=UTF8&nodeId=200347160";
		zon.isSafeUrl(url);

		url = "http://www.amazon.cn/gp/site-directory/ref=sa_menu_top_fullstore";
		url = zon.formatSafeUrl(url);
		System.out.println(url);
	}

	public static void test0731() {
		String url = "http://category.dangdang.com/cid4002291.html#ddclick?htmlact=clickcat&pos=0_0_0_m&cat=4003382&key=&qinfo=&pinfo=&minfo=263_1_48&ninfo=&custid=&permid=&ref=&rcount=&type=&t=1375254499000";
		url = "http://product.dangdang.com/product.aspx?product_id=22557339#ddclick?act=click&pos=22557339_21_1_p&cat=01.25.16.00.00.00&key=&qinfo=&pinfo=4646_1_48&minfo=&ninfo=&custid=&permid=&ref=&rcount=&type=&t=1375254506000";
		// url = "http://product.dangdang.com/product.aspx?product_id=22557339";
		url = "http://category.dangdang.com/all/?category_path=01.03.30.00.00.00&sort_type=sort_sale_amt_desc";
		String flag = "#ddclick";
		int index = url.indexOf(flag);
		if (index > 0) {
			url = url.substring(0, index);
		}
		flag = "&sort_type";
		index = url.indexOf(flag);
		if (index > 0) {
			url = url.substring(0, index);
		}
		System.out.println(url);
	}

	public static void test0730() {
		String host;
		// host = "http://chat.jd.com/index.action?groupId=14";
		// host = "http://chat7.jd.com/index.action?groupId=14";
		host = "http://chat15.jd.com/index.action?groupId=14";
		host = "http://chat150.jd.com/index.action?groupId=14";
		host = "http://chat150.360buy.com/index.action?groupId=14";
		String base = ".*chat(\\d+)?\\.(jd|360buy)\\.com.*";
		if (host.matches(base)) {
			System.out.println("match");
		}

		if (host.startsWith("javascript")
				|| host.indexOf("cart.jd.com") > 0
				|| host.indexOf("gate.jd.com") > 0
				|| host.indexOf("jd2008.jd.com") > 0
				|| host.indexOf("news.aspx") > 0
				|| host.indexOf("app.360buy.com") > 0
				|| host.indexOf("bbs.360buy.com") > 0
				|| host.indexOf("bbs.jd.com") > 0
				|| host.indexOf("help.jd.com") > 0
				|| host.indexOf("help.360buy.com") > 0
				|| host.indexOf("club.jd.com") > 0
				|| host.indexOf("club.360buy.com") > 0
				|| host.indexOf("market.jd.com") > 0
				|| host.indexOf("e.weibo.com") > 0
				|| host.indexOf("e.weibo.com") > 0
				|| host.indexOf("caipiao.jd.com") > 0
				|| host.indexOf("game.jd.com") > 0
				|| host.indexOf("safe.jd.com") > 0
				|| host.indexOf("c.fa.jd.com") > 0
				// || host.indexOf("book.jd.com") > 0
				// || host.indexOf("e.jd.com") > 0
				|| host.indexOf("cread.e.jd") > 0
				|| host.indexOf("cart.e.jd") > 0
				|| host.indexOf("search.e.jd") > 0

				|| host.indexOf("trip.jd.com") > 0
				|| host.indexOf("chongzhi.jd.com") > 0) {
			System.out.println("match2");
		}
	}

	public static void test0611() {
		String brand = "兄弟";
		String classic = "电脑、办公 > 办公打印 > 一体机 > 兄弟 > 兄弟MFC-7470D";
		classic = classic.replaceAll(">", "[xm99]");
		classic = dealClassic(classic, "");
		System.out.println(classic);
		Map<String, String> res = ProductDealUtil.dealCategory(brand, classic);
		System.out.println(res);
	}

	public static String dealClassic(String classic, String filterStr) {
		List<String> filters = new ArrayList<String>();
		String[] arr = filterStr.split("xm99");
		for (String str : arr) {
			filters.add(str);
		}
		classic = ProductDealUtil.dealClassic(classic, filters);
		return classic;
	}

	class YixunJSON {
		private String detail;
		private String param;

		public YixunJSON() {
		}

		public String getDetail() {
			return detail;
		}

		public void setDetail(String detail) {
			this.detail = detail;
		}

		public String getParam() {
			return param;
		}

		public void setParam(String param) {
			this.param = param;
		}

	}
}
