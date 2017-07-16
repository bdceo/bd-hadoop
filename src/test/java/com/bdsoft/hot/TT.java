package com.bdsoft.hot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.bdsoft.hbase.BaseDao;
import com.bdsoft.hbase.BaseDaoImpl;
import com.bdsoft.hot.bean.Product;
import com.bdsoft.hot.bean.UrlQueue;
import com.bdsoft.utils.Constant;
import com.bdsoft.utils.ContentFilter;
import com.bdsoft.utils.DataFormatUtil;
import com.bdsoft.utils.DomUtil;
import com.bdsoft.utils.Formatter;
import com.bdsoft.utils.RequestUtil;
import com.bdsoft.utils.StrUtil;
import com.bdsoft.utils.WebUtil;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class TT {

	public static Document getDoc(String file, String code) {
		DOMParser parser = new DOMParser();
		try {
			parser.setProperty(
					"http://cyberneko.org/html/properties/default-encoding",
					code);
			parser.setFeature("http://xml.org/sax/features/namespaces", false);
			FileInputStream fi = new FileInputStream(new File(file));
			BufferedReader br = new BufferedReader(new InputStreamReader(fi,
					code));
			parser.parse(new InputSource(br));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parser.getDocument();
	}

	public static void showTime(String timestamp) {
		Date date = new Date();
		System.out.println("当前时间【" + date.toLocaleString() + "】\t"
				+ date.getTime());
		if (StrUtil.isEmpty(timestamp)) {
			timestamp = date.getTime() + "";
		}
		long time = Long.parseLong(timestamp);
		date = new Date(time);
		System.out.println("测试时间【" + date.toLocaleString() + "】\t"
				+ date.getTime());
	}

	public static void main(String[] args) throws Exception {
		String timestamp = "1380451112528";
		showTime(timestamp);
		System.exit(1);

		// t051303();

		// Date now = new Date();
		// long time = now.getTime();
		// Date tmp = new Date(time + 60 * 1000);
		// System.out.println(now.toString());
		// System.out.println(tmp.toLocaleString());
		// System.out.println(now.after(tmp));

		// Random rd = new Random();
		// for (int i = 0; i < 100; i++) {
		// System.out.print(rd.nextInt(1000) + "  ");
		// }

		// String xpath =
		// "//DIV[@class='t-time']/descendant::LI[@class='fore2']"
		// + "|//SPAN[contains(text(),'小时')]";
		// String file = "src/com/xm/datamin/hot/html/src.html";
		// Document doc = getDoc(file, "utf-8");
		// Pattern pattern = Pattern
		// .compile("(?:^http://.+\\.jd\\.com)/([0-9]{5,14})(.html)$");
		// Node d = DomUtil.getNode(doc, xpath);
		// System.out.println(d == null);
		// String text = d.getTextContent();
		// System.out.println(text.trim());
		//
		// NodeList nodes = DomUtil.getNodeList(doc, xpath);
		// int len = nodes.getLength();
		// System.out.println("nodes = " + nodes.getLength());
		// if (nodes.getLength() > 0) {
		// for (int i = 0; i < len; i++) {
		// Node node = nodes.item(i);
		// System.out.print((i + 1) + " / " + node.getNodeName() + "\t");
		// System.out.println("值 = " + node.getTextContent());
		// System.out.println("\t"
		// + pattern.matcher(node.getTextContent()).find());
		// }
		// }
	}

	public static void t0718() {
	}

	public static void t070201() {
		String file = "src/com/xm/datamin/hot/src.html";
		Document doc = getDoc(file, "utf-8");
		String xpath = "//H1";
		Node d = DomUtil.getNode(doc, xpath);
		System.out.println(d == null);
		String text = d.getTextContent();
		System.out.println(text.trim());
	}

	public static void t062603() {
		String xpath = "//DIV[@class='tabc']/UL[@class='prolist cls']/descendant::A/@href";
		String file = "src/com/xm/datamin/hot/src.html";
		Document doc = getDoc(file, "utf-8");
		String value = WebUtil.getMParas(doc, xpath);
		System.out.println(value);
		value = WebUtil.formatKeyValueString(value, Constant.XMTAG_R);
		System.out.println(value);
	}

	public static void t062602() {
		String url = "http://item.51buy.com/item-343006.html";
		System.out.println(url);
		String code = "utf-8";
		Map<String, String> map = new HashMap<String, String>();
		Document document = RequestUtil.getDocument(url, code);
		JSONObject productObject = Formatter.getJson(document, "//SCRIPT",
				"varitemInfo=", "p_char_id", "itemInfo");
		if (productObject != null) {
			map.put("productName", productObject.getString("name"));
			map.put("pid", productObject.getString("pid"));
			map.put("p_char_id", productObject.getString("p_char_id"));
		}
		String pid = null;
		Pattern productFlag = Pattern
				.compile("http://.+51buy.com/item-([\\d]{4,10}).html.*?");
		Matcher ma1 = productFlag.matcher(url.toLowerCase());
		if (ma1.find()) {
			pid = ma1.group(1);
		}

		String mparas = "";
		String jsonUrl = "http://item.51buy.com/json.php?mod=item&act=parameters&pid="
				+ pid;
		System.out.println(jsonUrl);
		Document doc = RequestUtil.getSafeDocument(jsonUrl, "GBK", null);
		mparas = WebUtil.getMParas(doc, "//TABLE/descendant::TR").replaceAll(
				"<.*?>", "");
		// mparas = mparas.replaceAll("\"}", "").replaceAll("[\\s]{1,}", "");
		if (mparas != null && !"".equals(mparas)) {
			mparas = WebUtil.formatKeyValueString(mparas, Constant.XMTAG_R);
			mparas = mparas.replaceAll("\"}", "");
			System.out.println(mparas);
		}
		// String str = "dd    dd";
		// str = str.replaceAll("[\\s]{1,}", "");
		// System.out.println(str);
	}

	public static void t0626() {
		String url = "http://item.51buy.com/item-343006.html";
		String code = "utf-8";
		Map<String, String> map = new HashMap<String, String>();
		Document document = RequestUtil.getDocument(url, code);
		JSONObject productObject = Formatter.getJson(document, "//SCRIPT",
				"varitemInfo=", "p_char_id", "itemInfo");
		if (productObject != null) {
			map.put("productName", productObject.getString("name"));
			map.put("pid", productObject.getString("pid"));
			map.put("p_char_id", productObject.getString("p_char_id"));
		}

		String mparamUrl = "http://item.51buy.com/json.php?mod=item&act=introduce&pid="
				+ map.get("pid");
		System.out.println(mparamUrl);
		String message = RequestUtil
				.getOfHttpURLConnection(mparamUrl, "GB2312").toString();
		if (!message.equals("")) {
			JSONObject jsonObj = JSONObject.fromObject(message);
			String data = jsonObj.getString("data");
			data = data.replaceAll("<[^>]*>", " ")
					.replaceAll("[\\s|&nbsp;]{3,}", "")
					.replaceAll(" var[^>]*}", "").replaceAll("&times;", "×");
			data = DataFormatUtil.filterSpaceCharater(data).replaceAll(
					"\\s{3,}", "  ")
					+ "\n";
			data = ContentFilter.contentFilt(data).trim();
			System.out.println(data);
		}
	}

	public static void t0618() {
		try {
			// 1,遍历瑜臣给的pid
			String pid = "1005216096";
			BaseDao dao = new BaseDaoImpl();
			// 1.1,加载商品详情，获取商品url
			Map<String, String> data = dao.queryOne("products", pid);
			String productUrl = data.get("product_url");
			System.out.println(productUrl);

			// 2, 加载商品网页源文件信息，以供解析：xpath，jsoup
			productUrl = "http://cn.amazon.www/%e9%9f%a9%e5%9b%bd%e5%86%9c%e5%8d%8f%e8%9c%82%e8%9c%9c%e6%9f%9a%e5%ad%90%e8%8c%b61kg/dp/b00575n078";
			data = dao.queryOne("productContent", productUrl);
			String src = data.get("contenehtml");

			// 2.1,jsoup解析获取分类信息

			// 3，存储结构设计

			// 4，输出文件

			// 5，导入mysql
			System.out.println(src);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void t053001() {
		String file = "src/com/xm/datamin/hot/src.html";
		Document document = getDoc(file, "GBK");
		String contentDetailXpath = "//TABLE[@id='newTb']/TBODY/TR";
		NodeList nodeList = DomUtil.getNodeList(document, contentDetailXpath);
		String content = "";
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				String string = WebUtil.getTextHelper(node);
				if (string.isEmpty() || string.matches("[ \\s ]+")) {
					continue;
				}
				string = string.replaceAll("<.*?>", " ");
				content += DataFormatUtil.filterSpaceCharater(string)
						.replaceAll("\\s{3,}", "  ") + "\n";
			}
			content = ContentFilter.contentFilt(content).trim();
		}
		System.out.println(content);
	}

	public static void t052901() {
		String feedOrderXpath = "//DIV[@class='Cr_2']/TABLE//descendant::TR[TD[3 and contains(text(),'￥')]]/TD[1]";
		String feedUrlXpath = "//DIV[@class='Cr_2']/TABLE//descendant::TR[TD[3 and contains(text(),'￥')]]/TD[2]/A[1]/@href";
		String feedDaysXpath = "//DIV[@class='Cr_2']/TABLE//descendant::TR[TD[3 and contains(text(),'￥')]]/TD[6]";

		String file = "src/com/xm/datamin/hot/src.html";
		String xpath = "//DIV[@class='Cr_2']/TABLE//descendant::TR[TD[3 and contains(text(),'￥')]]";
		Document doc = getDoc(file, "utf-8");

		Node d = DomUtil.getNode(doc, xpath);
		System.out.println(d == null);
		String text = d.getTextContent();
		System.out.println(text.trim());

		NodeList nodes = DomUtil.getNodeList(doc, xpath);
		NodeList n1 = DomUtil.getNodeList(doc, feedOrderXpath);
		NodeList n2 = DomUtil.getNodeList(doc, feedUrlXpath);
		NodeList n3 = DomUtil.getNodeList(doc, feedDaysXpath);

		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());

		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);
				System.out.print((i + 1) + " / " + node.getNodeName() + "\t");
				System.out.println("值 = " + node.getTextContent());
				System.out.println(n1.item(i).getTextContent() + "\t"
						+ n2.item(i).getTextContent() + "\t"
						+ n3.item(i).getTextContent());
			}
		}
	}

	public static void t052403() {
		String file = "src/com/xm/datamin/hot/src.html";
		String xpath = "//DD[@id='mParam']/UL[@class='main_param_list']/LI";
		Document doc = getDoc(file, "gb2312");
		String value = WebUtil.getMParas(doc, xpath);
		if (value != null && !"".equals(value)) {
			value = WebUtil.formatKeyValueString(value, Constant.XMTAG_R);
			value = DataFormatUtil.formatMParams(value);
			System.out.println(value);
		}
	}

	public static void t052402() {
		UrlQueue url = new UrlQueue();
		url.setUrl("http://detail.zol.com.cn/cell_phone/index335843.shtml");
		String tmp = "/336/335843/param.shtml";
		tmp = DomUtil.autoCompleteUrl(url.getUrl(), tmp);
		System.out.println(tmp);
	}

	public static void t052401() {
		String file = "src/com/xm/datamin/hot/src.html";
		String xpath = "//UL[@class='nav']";
		Document doc = getDoc(file, "gbk");

		String value = WebUtil.getMParas(doc, xpath);
		if (value != null && !"".equals(value)) {
			value = WebUtil.formatKeyValueString(value, Constant.XMTAG_R);
			System.out.println(value.trim());
		}
	}

	public static void t052001() {
		String str = "http://www.amazon.cn/Haier-%E6%B5%B7%E5%B0%94ZQD90SV-90%E5%8D%87%E5%B5%8C%E5%85%A5%E5%BC%8F%E6%B6%88%E6%AF%92%E6%9F%9C/dp/B003ICXUWM/ref=br_lf_m_195638_1_1_img/476-6472403-9610349";
		// str =
		// "http://www.amazon.cn/gp/product/B00BBRJMY6/ref=s9_topn_se_g107_ir17/475-2464056-7364151?pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=auto-no-results-center-1&pf_rd_r=FD4CB0C02A29422580E1&pf_rd_t=101&pf_rd_p=58429932&pf_rd_i=665002051%2C664978051";
		System.out.println(str);
		Pattern productFlag = Pattern
				.compile("(http\\://www\\.amazon\\.cn)/([a-z0-9%-]+)/(dp|product)/([a-z0-9]+/).*?");
		Matcher ma1 = productFlag.matcher(str.toLowerCase());
		System.out.println(ma1.find());
		if (str.indexOf("?") > 0) {
			str = str.substring(0, str.indexOf("?"));
			System.out.println(str);
		} else if (str.indexOf("/dp/") > 0) {
			String host = str.substring(0, str.indexOf("cn/") + 2);
			String prod = str.substring(str.indexOf("/dp/"));
			str = host + prod;
			System.out.println(str);
		}
	}

	public static void t051601() {
		String file = "src/com/xm/datamin/hot/src.html";
		// String url = "http://51ibaby.com/group_buy.php";//
		// //*[@id="special_offer"]/dd[1]
		String xpath = "//DIV[@class='s9hl']/A/@href";
		xpath = "//DIV[@class='content']//descendant::A/@href"
				+ " | //DIV[@class='s9hl']/A/@href"
				+ " | //DIV[@class='newaps']//descendant::A/@href"
				+ " | //DIV[@id='atfResults']//descendant::A/@href";

		Document doc = getDoc(file, "gbk");

		NodeList nodes = DomUtil.getNodeList(doc, xpath);
		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);
				System.out.println((i + 1) + " / " + node.getNodeName());
				System.out.println(node.getTextContent());

			}
		}
	} 
	
	public static void t051303() {
		String ct = "gbk";
		String url = "http://item.51buy.com/json.php?mod=item&act=parameters&pid=136411&callback=jQuery15108017042595893145_1368431549827&_=1368431647991";

		// String src = RequestUtil.getOfHttpURLConnection(url, ct).toString();
		// java.io.InputStream in = new ByteArrayInputStream(src.getBytes());
		// BufferedReader br = new BufferedReader(new InputStreamReader(in));
		// DOMParser parser = new DOMParser();
		// parser.setProperty(
		// "http://cyberneko.org/html/properties/default-encoding",
		// ct);
		// parser.setFeature("http://xml.org/sax/features/namespaces", false);
		// parser.parse(new InputSource(br));
		// Document doc = parser.getDocument();

		Document doc = RequestUtil.getSafeDocument(url, ct, null);

		// System.exit(1);
		// Document doc = RequestUtil.getDocument(url, ct);

		String mparas = WebUtil.getMParas(doc, "//TABLE/descendant::TR")
				.replaceAll("<.*?>", "");
		mparas = WebUtil.formatKeyValueString(mparas, "\\[xm99\\]");
		System.out.println(mparas);
	}

	public static void t051302() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = "2013-05-13 00:00:00";
		System.out.println(str);
		Date d = df.parse(str);
		System.out.println(d.getTime());

		long t = d.getTime();
		t = t - Constant.ONE_DAY * 7;
		System.out.println(t);

		d = new Date(t);
		System.out.println(d.toLocaleString());

		d = new Date();
		System.out.println(d.getTime());
		t = d.getTime() - 1000 * 60 * 60;
		System.out.println(t);
	}

	public void t051301() throws Exception {
		String dstr = "2013-05-13 11:12:21";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Product p = new Product();
		p.setGroupbuyEndTime(df.parse(dstr).getTime());
		long dif = p.getGroupbuyEndTime() - System.currentTimeMillis();
		long c = dif / Constant.ONE_DAY;
		System.out.println(c);
		if (c > 7) {
			c -= 7;
			dif = p.getGroupbuyEndTime() - Constant.ONE_DAY * c;
			p.setGroupbuyEndTime(dif);
			System.out.println(new Date(dif).toLocaleString());
		}
	}

	public void t051002() {
		byte[] buffer = new byte[] { (byte) 0x1c };
		String n = new String(buffer);
		System.out.println("[" + n + "]");
		System.out.println(1);
	}

	public void t051001() {
		Date d = new Date(System.currentTimeMillis());
		System.out.println(d.toLocaleString());

		String str = "1394121600000";
		d = new Date(Long.parseLong(str));
		System.out.println(d.toLocaleString());

		str = "652274";
		long ts = Long.parseLong(str) * 1000;
		ts += System.currentTimeMillis();
		d = new Date(ts);
		System.out.println(d.toLocaleString());

		ts = System.currentTimeMillis() + 4 * 60 * 3600;
		d = new Date(ts);
		System.out.println(d.toLocaleString());
	}

	public void t050902() {
		String file = "src/com/xm/datamin/hot/src.html";
		String url = "http://51ibaby.com/group_buy.php";// //*[@id="special_offer"]/dd[1]
		String xpath = "(//DL[@id='special_offer'])[1]/descendant::DIV[@class='v_order']/A/@href";

		Document doc = getDoc(file, "utf-8");

		NodeList nodes = DomUtil.getNodeList(doc, xpath);
		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);
				System.out.println(i + " / " + node.getNodeName());
				System.out.println(node.getTextContent());

			}
		}
	}

	public void t050901() {
		String file = "src/com/xm/datamin/hot/src.html";
		String url = "http://bj.jumei.com/";
		// String xpath =
		// "//DIV[@id='toady_deal_list']/descendant::A[@class='deal_link']/@href";
		String xpath = "//*[@id=\"toady_deal_list\"]//descendant::A[@class='deal_link']/@href";

		Map<String, String> fm = new HashMap<String, String>();
		fm.put("textarea", "div");
		Document document = RequestUtil.getSafeDocument(url, "utf-8", fm);
		// Document document = getDoc(file);
		NodeList nodes = DomUtil.getNodeList(document, xpath);
		Node node = DomUtil.getNode(document, xpath);

		System.out.println((nodes == null) + "/" + (node == null));
		int len = nodes.getLength();
		System.out.println("nodes = " + nodes.getLength());
		if (nodes.getLength() > 0) {
			for (int i = 0; i < len; i++) {
				node = nodes.item(i);
				System.out.println(i + " / " + node.getNodeName());
				System.out.println(node.getTextContent());

			}
		}
	}

	public void t050802() {
		String file = "src/errjumei.html";
		Document doc = getDoc(file, "utf-8");
		String xpath = "//SPAN[@class='newdeal_deal_price en']";
		Node node = DomUtil.getNode(doc, xpath);
		System.out.println(node == null);

		String price = node.getTextContent();
		price = DataFormatUtil.formatPrice(price);
		System.out.println(price);
	}

	public void t050801() {
		String xpath = "//SPAN[@class='newdeal_deal_price en']";
		List<String> list = new ArrayList<String>();
		String url = "http://bj.jumei.com/i/deal/bj130508p792.html?from=home_index_muti_p1_pos78";
		list.add(url);
		url = "http://bj.jumei.com/i/deal/bj130508p66044.html?from=home_index_muti_p1_pos15";
		list.add(url);
		url = "http://bj.jumei.com/i/deal/bj130508p51782.html?from=home_index_muti_p1_pos33";
		list.add(url);
		url = "http://bj.jumei.com/i/deal/bj130508p296.html?from=home_index_muti_p1_pos32";
		list.add(url);
		url = "http://bj.jumei.com/i/deal/bj130508p18795.html?from=home_index_muti_p1_pos149";
		list.add(url);
		// url =
		// "http://bj.jumei.com/i/deal/bj130508p10935.html?from=home_index_muti_p1_pos138";//
		// 跳转首页

		for (String u : list) {
			System.out.println("zhuaqu = " + u);

			String content = RequestUtil.getOfHttpURLConnection(url, "utf-8")
					.toString();
			Document doc = RequestUtil.getDocument(url, "utf-8");

			Node node = DomUtil.getNode(doc, xpath);
			System.out.println(node == null);
			if (node == null) {
				System.out.println(doc.getTextContent());
				System.out.println(doc.toString());
				System.out.println(content);
				break;
			}
			System.out.println(node.getTextContent());

			String price = node.getTextContent();
			price = DataFormatUtil.formatPrice(price);
			System.out.println(price);
		}

	}

	public void t0507() {
		String url = "http://www.17mh.com/koubei/1475.html";
		url = "http://www.17mh.com/estee_lauder/jmg-30944.html";
		// url = "http://www.17mh.com/coppertone/fs-30928.html";
		// url = "http://www.17mh.com/missha/bbs-30932.html";

		Pattern productFlag = Pattern
				.compile("(?:^http\\://www\\.17mh\\.com/)([^koubei])([_a-zA-Z0-9]+/)([a-zA-Z0-9-]+)(.html).*?");
		Matcher ma = productFlag.matcher(url);
		System.out.println(ma.find());
	}

	public void t051003() throws Exception {
		// |目标路径|资源路径|目标图片后缀名|目标图片名称|像素宽|像素高|
		int w = Integer.parseInt(Constant.pic_big_width);
		int h = Integer.parseInt(Constant.pic_big_height);
		float per = Constant.per;
		String fpath = "D:/srcpic/";
		String fname = "4";
		String ext = Constant.ext;
		String file = fpath + fname + ext;
		String ofile = fpath + fname + "_big" + ext;

		FileInputStream fi = new FileInputStream(new File(file));
		Image src = javax.imageio.ImageIO.read(fi);
		int old_w = src.getWidth(null);
		int old_h = src.getHeight(null);
		int new_w = 0;
		int new_h = 0;

		double w2 = (old_w * 1.00) / (w * 1.00);
		double h2 = (old_h * 1.00) / (h * 1.00);

		BufferedImage oldpic;
		if (old_w > old_h) {
			oldpic = new BufferedImage(old_w, old_w, BufferedImage.TYPE_INT_RGB);
		} else {
			if (old_w < old_h) {
				oldpic = new BufferedImage(old_h, old_h,
						BufferedImage.TYPE_INT_RGB);
			} else {
				oldpic = new BufferedImage(old_w, old_h,
						BufferedImage.TYPE_INT_RGB);
			}
		}
		Graphics2D g = oldpic.createGraphics();
		g.setColor(Color.white);
		if (old_w > old_h) {
			g.fillRect(0, 0, old_w, old_w);
			g.drawImage(src, 0, (old_w - old_h) / 2, old_w, old_h, Color.white,
					null);
		} else {
			if (old_w < old_h) {
				g.fillRect(0, 0, old_h, old_h);
				g.drawImage(src, (old_h - old_w) / 2, 0, old_w, old_h,
						Color.white, null);
			} else {
				// g.fillRect(0,0,old_h,old_h);
				g.drawImage(
						src.getScaledInstance(old_w, old_h, Image.SCALE_SMOOTH),
						0, 0, null);
			}
		}
		g.dispose();
		src = oldpic;
		if (old_w > w)
			new_w = (int) Math.round(old_w / w2);
		else
			new_w = old_w;
		if (old_h > h)
			new_h = (int) Math.round(old_h / h2);
		else
			new_h = old_h;
		BufferedImage tag = new BufferedImage(new_w, new_h,
				BufferedImage.TYPE_INT_RGB);
		tag.getGraphics().drawImage(
				src.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0, 0,
				null);
		FileOutputStream newimage = new FileOutputStream(ofile); // 输出到文件流
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
		JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(tag);
		jep.setQuality(per, true);
		encoder.encode(tag, jep);
		newimage.close();
	}

}
