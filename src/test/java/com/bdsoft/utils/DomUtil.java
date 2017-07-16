package com.bdsoft.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bdsoft.hot.bean.UrlQueue;

/**
 * Dom tree 相关的函数方法
 * @author yangchao
 *
 */
public class DomUtil {
	private static final Log logger = LogFactory.getLog(DomUtil.class);
	

	public static UrlQueue valuation(UrlQueue queue, String uurl) {
		UrlQueue queue1 = null;
		try {
			queue1 = (UrlQueue) queue.clone();
		} catch (CloneNotSupportedException e) {
			// XXX Auto-generated catch block
			e.printStackTrace();
		}
		queue1.setUrl(uurl);
		queue1.setUrlcode(uurl.hashCode() + "");

		return queue1;
	}

	
	
	/**
	 * 给定Node返回该Node中HTML代码。
	 * 注意：HTML代码是网站原有格式，使用时，需要进行必要的转换，例如去除空格或者回车，一般去除回车即可，因为属性中可能包含空格，不应该去除属性中的空格
	 * Node的attribute属性可以配置过滤器进行过滤
	 * @param node
	 * @param filter 包含在数组中的标签是需要生成的。
	 * @return
	 */
	public static String nodeToString(Node node,String... filter){
		List<String> filterList = Arrays.asList(filter);
		StringBuffer str = new StringBuffer();
		if(node.getNodeName().equalsIgnoreCase("#text")){
			return node.getTextContent();
		}else{
			str.append("<"+node.getNodeName());
			//添加属性
			NamedNodeMap attrbutes = node.getAttributes();
			if(attrbutes!=null){
				for(int i=0;i<attrbutes.getLength();i++){
					Node n = attrbutes.item(i);
					//标签在filter中是需要生成的。attributeList中是不需要生成的
					if(filterList.contains(n.getNodeName().toLowerCase()) ||  !attributeFilterList.contains(n.getNodeName().toLowerCase()) ){
						str.append(" ").append(n.getNodeName()).append("='").append(n.getNodeValue()).append("'");
					}
				}
			}
			str.append(">");
			//添加子节点
			NodeList nodeList = node.getChildNodes();
			if(nodeList!=null && nodeList.getLength()>0){
				for(int i=0;i<nodeList.getLength();i++){
					Node n = nodeList.item(i);
					str.append(nodeToString(n));
				}
			}
			str.append("</"+node.getNodeName()+">");
		}
		return str.toString();
	}
	
	/**
	 * 属性过滤器，用于nodeToString方法，通过给定的Node，返回该Node下的html代码
	 */
	static List<String> attributeFilterList = new ArrayList<String>();
	static{
		attributeFilterList.add("style");
		attributeFilterList.add("face");
		attributeFilterList.add("border");
		attributeFilterList.add("cellspacing");
		attributeFilterList.add("cellpadding");
		attributeFilterList.add("width");
		attributeFilterList.add("heigh");
	}
	
	
	/**
	 * getlinks from the webpage and filter the javaScript
	 * 
	 * @param base
	 * @param node
	 * @return ArrayList
	 * @throws MalformedURLException
	 */
	public static Set<String> getLinks(String parent_url, NodeList nodeList) {
		Set<String> outlinks = new HashSet<String>();

		int len = nodeList.getLength();
		for (int i = 0; i < len; i++) {
			Node node = nodeList.item(i);
			String tmpUrl = node.getTextContent().trim();
			tmpUrl = picUrlFilter(tmpUrl);
			tmpUrl = autoCompleteUrl(parent_url, tmpUrl);
			if (tmpUrl == null) {
				continue;
			}
			outlinks.add(tmpUrl);
		}
		return outlinks;
	}
	
	
	/**
	 * 过滤掉以javascript|^#|\\.gif|\\.jpg|\\.png|\\.swf结尾的所有url
	 * @param url
	 * @return
	 */
	public static String picUrlFilter(String url){
		
		String tmpUrl="";
		if (Pattern.compile("javascript|^#|\\.gif|\\.jpg|\\.png|\\.swf")
				.matcher(tmpUrl).find()) {
			tmpUrl=null;
		}else {
			tmpUrl=url;
		}
		
		return tmpUrl;		
	}

	
	/**
	 * 根据来源url补全url
	 * @param parentUrl
	 * @param tmpUrl
	 * @return tmpUrl
	 */
	public static String autoCompleteUrl(String parentUrl, String tmpUrl) {		
		
		if (!tmpUrl.startsWith("http")) {
			URL u = null;
			try {
				u = new URL(parentUrl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			if (tmpUrl.startsWith("/") && tmpUrl.length() > 1) {
				String host=u.getHost();
				tmpUrl = "http://" + host + tmpUrl;
			} else if (tmpUrl.length() > 1) {
				String startUrl = getStartUrl(parentUrl);
				tmpUrl = startUrl + tmpUrl;
			} else {
				tmpUrl = null;
			}
		}
		return tmpUrl;
	}

	protected static String getStartUrl(String url) {
		url = url.substring(url.indexOf("//") + 2);
		if (url.contains("/")) {
			url = "http://" + url.substring(0, url.lastIndexOf("/")) + "/";
		} else {
			url = "http://" + url + "/";
		}
		return url;
	}

	
	public static NodeList getNodeList(Document doc, String Xpath) {
		NodeList nodeList = null;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(
				new com.bdsoft.hot.bean.XMNamespaceContext("http://www.w3.org/1999/xhtml"));
		XPathExpression expr;
		try {
			expr = xpath.compile(Xpath);
			nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			logger.error("节点解析错误=="+ Xpath,e);
		}
		return nodeList;
	}

	
	public static Node getNode(Document doc, String Xpath) {
		Node node = null;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new com.bdsoft.hot.bean.XMNamespaceContext("http://www.w3.org/1999/xhtml"));
		// products = XPathAPI.selectNodeList(doc, productsXpath);
		XPathExpression expr = null;
		try {
			expr = xpath.compile(Xpath);
			node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			logger.error("节点解析错误=="+ Xpath,e);
		}
		return node;
	}

	public static Node getNode(DocumentFragment documentFragment, String pathString) {
		Node node = null;
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new com.bdsoft.hot.bean.XMNamespaceContext("http://www.w3.org/1999/xhtml"));
		XPathExpression expr = null;
		try {
			expr = xpath.compile(pathString);
			node = (Node) expr.evaluate(documentFragment, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return node;
	}
	
	
	/**
	 * unicodeToString while reverse the chinese's Unicode to chinese
	 * but this method use the pattern to do, it will cost many time;
	 * @param str
	 * @return
	 */
	public static String unicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}
	
	
	/**
	 * unicodeToString while reverse the chinese's Unicode to chinese
	 * use StringBuilder and String[] to do;
	 * @param str
	 * @return
	 */
	public static StringBuilder unicode2String(String str){
		String[] strings = str.split("\\\\u");
		StringBuilder sBu=new StringBuilder();
		int i =0;
		while (i <strings.length) {
			if(strings[i].isEmpty()|strings[i].matches("\\W+")){
				sBu.append(strings[i]);
				i++;continue;	
			}
			if(strings[i].length()==4){
				sBu.append((char) Integer.parseInt(strings[i],16));
				i++;
			}else if(strings[i].length()>4){
				sBu.append((char) Integer.parseInt(strings[i].substring(0, 4),16)+strings[i].substring(4));
				i++;
			}else {
				sBu.append(strings[i]);
				i++;continue;	
			}
		}
		return sBu;
	}
	
	public static String unicode3String(String str){
		String[] strings = str.split("\\\\u");
		String sBu="";
		int i =0;
		while (i <strings.length) {
			if(strings[i].length()==4){
				sBu +=((char) Integer.parseInt(strings[i],16));
				i++;
			}else if(strings[i].length()>4){
				sBu +=((char) Integer.parseInt(strings[i].substring(0, 4),16)+strings[i].substring(4));
				i++;
			}else {
				sBu +=(strings[i]);
				i++;continue;	
			}
		}
		return sBu.toString();
	}
	
	public static String unicode4String(String str) {
		String[] strings = str.split("\\\\u");
		StringBuffer sBu = new StringBuffer();
		int i =0;
		while (i <strings.length) {
			if(strings[i].length()==4){
				sBu.append((char) Integer.parseInt(strings[i],16));
				i++;
			}else if(strings[i].length()>4){
				sBu.append((char) Integer.parseInt(strings[i].substring(0, 4),16)+strings[i].substring(4));
				i++;
			}else {
				sBu.append(strings[i]);
				i++;continue;	
			}
		}
		return sBu.toString();
	}
	
	
}
