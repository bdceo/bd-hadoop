package com.bdsoft.nutch.parser.url;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.bdsoft.utils.StrUtil;

public class UrlFormat4amazon {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String str = ", :";
		str = URLEncoder.encode(str);
		System.out.println("encode=" + str);
		String[] urls = new String[] {
				"http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a124912071",
				"http://www.amazon.cn/s?ie=utf8&page=1&rh=n%3a665002051%2cp_4%3asony%20%e7%b4%a2%e5%b0%bc",
				"http://www.amazon.cn/s/ref=sr_ex_n_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051&bbn=888465051&ie=UTF8&qid=1377489481",
				"http://www.amazon.cn/s/ref=sr_nr_p_4_0?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_4%3AKingston+%E9%87%91%E5%A3%AB%E9%A1%BF&bbn=888465051&ie=UTF8&qid=1377499967&rnid=2016119051",
				"http://www.amazon.cn/s/ref=sr_nr_p_72_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_72%3A2039714051&bbn=888465051&ie=UTF8&qid=1377499967&rnid=664973051",
				"http://www.amazon.cn/s/ref=sr_nr_p_36_5?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_36%3A2045273051&bbn=888465051&ie=UTF8&qid=1377499967&rnid=2045267051",
				"http://www.amazon.cn/s/ref=sr_nr_p_8_0?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_8%3A78600071&bbn=888465051&ie=UTF8&qid=1377499967&rnid=78599071",
				"http://www.amazon.cn/s/ref=sr_nr_p_6_0?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_6%3AA1AJ19PSB66TGU&bbn=888465051&ie=UTF8&qid=1377499967&rnid=51327071",
				"http://www.amazon.cn/s/ref=sr_pg_3?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cp_n_availability%3A2054683051&page=3&bbn=888465051&ie=UTF8&qid=1377500117",
				"http://www.amazon.cn/b/ref=sr_aj?node=51878071&ajr=0",
				"http://www.amazon.cn/%E6%89%8B%E6%9C%BA-%E9%80%9A%E8%AE%AF/b/ref=sd_allcat_wi_?ie=UTF8&node=664978051",
				"http://www.amazon.cn/%e7%94%b5%e5%ad%90%e4%b9%a6-%e5%b0%91%e5%84%bf/b?ie=utf8&node=143276071",
				"http://www.amazon.cn/s/ref=lp_51878071_pg_3?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071&page=3&ie=UTF8&qid=1377500398",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_n_feature_keywords_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_n_feature_keywords_browse-bin%3A191965071&bbn=51878071&ie=UTF8&qid=1377500182&rnid=191947071",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_4_1?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_4%3ASamsung+%E4%B8%89%E6%98%9F&bbn=51878071&ie=UTF8&qid=1377500222&rnid=2016119051",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_72_2?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_72%3A2039715051&bbn=51878071&ie=UTF8&qid=1377500235&rnid=664973051",
				"http://www.amazon.cn/s/ref=lp_51878071_nr_p_36_5?rh=n%3A2016116051%2Cn%3A%212016117051%2Cn%3A888465051%2Cn%3A51878071%2Cp_36%3A2045273051&bbn=51878071&ie=UTF8&qid=1377500287&rnid=2045267051" };
		for (String url : urls) {
			if (!StrUtil.isEmpty(url)) {
				str = URLDecoder.decode(url);
				System.out.println("\ndecode=" + str);
				if (str.indexOf("/s?") >= 0 || str.indexOf("/s/") >= 0) {
					url = formatSurl(str);
					System.out.println("format=" + url);
				} else if (str.indexOf("/b?") >= 0 || str.indexOf("/b/") >= 0) {
					url = formatBurl(str);
					System.out.println("format=" + url);
				}
			}
		}

	}

	public static String formatBurl(String url) {
		if(StrUtil.isEmpty(url)){
			return url;
		}
		if(url.indexOf("?") == -1){
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		System.out.println(param);
		String[] params = param.split("&");
		Map<String, String> pmap = formMap(params, "=");
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

	public static String formatSurl(String url) {
		if(StrUtil.isEmpty(url)){
			return url;
		}
		if(url.indexOf("?") == -1){
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		System.out.println(param);
		String[] params = param.split("&");
		Map<String, String> pmap = formMap(params, "=");

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
		} else {
			return url;
		}
		return (base + sb.toString());
	}

	public static String getLastN(String rh) {
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

	public static Map<String, String> formMap(String[] str, String flag) {
		Map<String, String> pmap = new HashMap<String, String>();
		for (String tmp : str) {
			String[] kv = tmp.split(flag);
			if (kv.length == 2) {
				pmap.put(kv[0], kv[1]);
			}
		}
		return pmap;
	}

}
