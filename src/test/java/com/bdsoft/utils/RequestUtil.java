package com.bdsoft.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;

public class RequestUtil {

	private static final Log LOG = LogFactory.getLog(RequestUtil.class);

	/**
	 * 调用request.getConnectByHttpUrl方法 通过neko工具，获取符合w3c标准的Document
	 * 
	 * @param url
	 * @param charset2
	 * @return DocumentFragment
	 */
	public static DocumentFragment getDocumentFragment(String url, String charset) {

		HTMLDocumentImpl doc = new HTMLDocumentImpl();
		doc.setErrorChecking(false);
		DocumentFragment document = doc.createDocumentFragment();
		DocumentFragment frag = doc.createDocumentFragment();
		DOMFragmentParser parser = new DOMFragmentParser();
		BufferedReader in = null;
		try {
			//
			parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
			parser.setFeature(
					"http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",
					true);
			parser.setFeature("http://cyberneko.org/html/features/report-errors",
					LOG.isTraceEnabled());

			in = RequestUtil.getConnectByHttpUrl(url, charset);

			InputSource input = new InputSource(in);
			parser.parse(input, frag);
			document.appendChild(frag);

			while (true) {
				frag = doc.createDocumentFragment();
				parser.parse(input, frag);
				if (!frag.hasChildNodes())
					break;
				document.appendChild(frag);
			}
		} catch (Exception e) {
			e.printStackTrace();
			document = null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				// //TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return document;
	}

	public static DocumentFragment getDocumentFragmentByString(String message, String charset) {

		HTMLDocumentImpl doc = new HTMLDocumentImpl();
		doc.setErrorChecking(false);
		DocumentFragment document = doc.createDocumentFragment();
		DocumentFragment frag = doc.createDocumentFragment();
		DOMFragmentParser parser = new DOMFragmentParser();
		BufferedReader in = null;
		try {
			//
			parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
			parser.setFeature(
					"http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",
					true);
			parser.setFeature("http://cyberneko.org/html/features/report-errors",
					LOG.isTraceEnabled());

			in = new BufferedReader(new StringReader(message));
			InputSource input = new InputSource(in);
			parser.parse(input, frag);
			document.appendChild(frag);

			while (true) {
				frag = doc.createDocumentFragment();
				parser.parse(input, frag);
				if (!frag.hasChildNodes())
					break;
				document.appendChild(frag);
			}
		} catch (Exception e) {
			e.printStackTrace();
			document = null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	/**
	 * get方式发送请求数据
	 * 
	 * @param url
	 * @param charset
	 * @return StringBuilder
	 */
	public static StringBuilder getOfHttpURLConnection(String url, String charset) {
		HttpURLConnection uc = null;
		StringBuilder sb = null;
		BufferedReader br = null;
		String code = "";
		try {

			sb = new StringBuilder();
			URL u = new URL(url);
			uc = (HttpURLConnection) u.openConnection();

			uc.setConnectTimeout(1000);
			uc.setReadTimeout(1000);
			uc.setDoOutput(true);
			uc.setRequestMethod("GET");
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="
					+ charset);

			uc.setRequestProperty("accept",
					"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			uc.setRequestProperty(
					"user-agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10");

			uc.setRequestProperty("accept-language", "zh-CN,zh;q=0.8");
			uc.setRequestProperty("accept-charset", "GBK,utf-8;q=0.7,*;q=0.3");

			String line = null;

			br = new BufferedReader(new InputStreamReader(uc.getInputStream(), charset));

			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception e) {
			if (uc != null) {
				try {
					code = uc.getResponseCode() + "";
				} catch (Exception e2) {
					LOG.error("getOfHttpURLConnection got http-code error");
				}
			}
			LOG.error("getOfHttpURLConnection error,http-code=" + code + ",error=" + e);
		} finally {

			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb;
	}

	/**
	 * get方式发送请求数据 @
	 * 
	 * @param url
	 * @param charset
	 * @return StringBuilder
	 */
	public static BufferedReader getConnectByHttpUrl(String url, String charset) {

		BufferedReader br = null;
		try {

			URL u = new URL(url);
			HttpURLConnection uc = null;
			uc = (HttpURLConnection) u.openConnection();

			uc.setConnectTimeout(20000);
			uc.setReadTimeout(20000);
			uc.setRequestMethod("GET");
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=GBK");

			uc.setRequestProperty("accept",
					"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			uc.setRequestProperty(
					"user-agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10");
			uc.setRequestProperty("accept-language", "zh-CN,zh;q=0.8");
			uc.setRequestProperty("accept-charset", "GBK,utf-8;q=0.7,*;q=0.3");

			String contentType = uc.getContentType();
			String[] conEmt = null;
			if (contentType != null) {
				conEmt = contentType.toLowerCase().split("charset=");
			}
			if (conEmt != null && conEmt.length >= 2) {
				charset = conEmt[1].trim();
			}
			br = new BufferedReader(new InputStreamReader(uc.getInputStream(), charset));

		} catch (Exception ex) {
			System.out.println(url + "==" + ex.getStackTrace());
			return null;
		}
		return br;
	}

	public static Document getSafeDocument(String url, String charset, Map<String, String> fm) {
		Document document = null;
		DOMParser parser = new DOMParser();
		BufferedReader br = null;
		try {
			URL u = new URL(url);
			HttpURLConnection uc = null;
			uc = (HttpURLConnection) u.openConnection();

			uc.setConnectTimeout(20000);
			uc.setReadTimeout(20000);
			uc.setRequestMethod("GET");
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="
					+ charset);

			uc.setRequestProperty("accept",
					"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			uc.setRequestProperty(
					"user-agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10");
			uc.setRequestProperty("accept-language", "zh-CN,zh;q=0.8");
			uc.setRequestProperty("accept-charset", "GBK,utf-8;q=0.7,*;q=0.3");

			// String contentType = uc.getContentType();
			// String[] conEmt = null;
			// if (contentType != null) {
			// conEmt = contentType.toLowerCase().split("charset=");
			// }
			// if (conEmt != null && conEmt.length >= 2) {
			// charset = conEmt[1].trim();
			// }
			// 过滤fm参数
			if (fm != null && fm.size() > 0) {
				String src = new String(parseStream(uc.getInputStream()), charset);
				for (Entry<String, String> en : fm.entrySet()) {
					src = src.replaceAll(en.getKey(), en.getValue());
				}
				java.io.InputStream in = new ByteArrayInputStream(src.getBytes());
				br = new BufferedReader(new InputStreamReader(in));
			} else {
				br = new BufferedReader(new InputStreamReader(uc.getInputStream(), charset));
			}

			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
			parser.setFeature("http://xml.org/sax/features/namespaces", false);

			parser.parse(new InputSource(br));
			document = parser.getDocument();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("getDocument exception!");
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	public static byte[] parseStream(java.io.InputStream ins) throws Exception {
		int len = -1;
		byte[] data = null;
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream outs = null;
		try {
			outs = new ByteArrayOutputStream();
			while ((len = ins.read(buffer)) != -1) {
				outs.write(buffer, 0, len);
			}
			outs.flush();
			data = outs.toByteArray();
		} finally {
			if (outs != null) {
				outs.close();
			}
		}
		return data;
	}

	/**
	 * 调用request.getConnectByHttpUrl方法 通过neko工具，获取符合w3c标准的Document
	 * 
	 * @param url
	 * @param charset2
	 * @return Document
	 */
	public static Document getDocument(String url, String charset) {
		Document document = null;
		DOMParser parser = new DOMParser();
		BufferedReader br = null;
		try {
			URL u = new URL(url);
			HttpURLConnection uc = null;
			uc = (HttpURLConnection) u.openConnection();

			uc.setConnectTimeout(20000);
			uc.setReadTimeout(20000);
			uc.setRequestMethod("GET");
			uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=GBK");

			uc.setRequestProperty("accept",
					"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
			uc.setRequestProperty(
					"user-agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.552.224 Safari/534.10");
			uc.setRequestProperty("accept-language", "zh-CN,zh;q=0.8");
			uc.setRequestProperty("accept-charset", "GBK,utf-8;q=0.7,*;q=0.3");

			String contentType = uc.getContentType();
			String[] conEmt = null;
			if (contentType != null) {
				conEmt = contentType.toLowerCase().split("charset=");
			}
			if (conEmt != null && conEmt.length >= 2) {
				charset = conEmt[1].trim();
			}
			// System.out.println(charset);
			br = new BufferedReader(new InputStreamReader(uc.getInputStream(), charset));

			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
			parser.setFeature("http://xml.org/sax/features/namespaces", false);

			parser.parse(new InputSource(br));
			document = parser.getDocument();
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException("getDocument exception!");
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// if(Pattern.compile("^[A-Za-z0-9]\\.?[A-Za-z0-9|\\-]\\.{1}[A-Za-z]+").matcher("javascript:regist();").find()){
		// System.out.println("1OK!!!");
		// }
		// if(Pattern.compile("^[A-Za-z0-9]+[\\/+|\\.+]+[A-Za-z0-9]+").matcher("products/1320-5019-5020.html").find()){
		// System.out.println("2OK!!!");
		// }
		//
		// if(Pattern.compile("\\(|\\,|\\)|\\;|\\+|\\=").matcher("str.substring(0,(str.length-6));str1+=RndNum(6);document.getElementById(eleId).href=str1;}function").find()){
		// System.out.println("3OK!!!");
		// }
		//
		// Document sb =
		// RequestUtil.getDocument("http://www.360buy.com/product/497851.html","UTF-8");
		StringBuilder sb = RequestUtil.getOfHttpURLConnection(
				"http://s.etao.com/item/8208098.html", "GBK");

		LOG.info("\n\n\n" + sb.toString());

		// int lenth = sb.length()/20000;
		// for(int i =0 ;i< lenth;i++){
		// System.out.println(sb.substring(i*20000, (i+1)*20000));
		// }

		String url = "http://www.360buy.com/product/1000394618.html";
		if (Pattern.compile("360buy\\.com/product/\\d+\\.html").matcher(url).find()
				|| Pattern.compile("mvd\\.360buy\\.com/\\d+\\.html").matcher(url).find()
				|| Pattern.compile("book\\.360buy\\.com/\\d+\\.html").matcher(url).find()
				|| Pattern.compile("product\\.dangdang\\.com/product\\.aspx").matcher(url).find()) {// 匹配京东、当当网详情页面

			System.out.println("4OK!!!");
		}
	}

}
