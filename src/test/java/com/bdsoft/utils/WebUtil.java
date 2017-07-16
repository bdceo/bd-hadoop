package com.bdsoft.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WebUtil {

    public static String getXmlFormat(Object infomation){
    	
    	  return getXmlFormat(String.valueOf(infomation));
    }
    
    public static String getXmlFormat(String infomation)
    {
        String tmpInfo = infomation != null ? infomation : "";
        try
        {
            //tmpInfo = tmpInfo.replaceAll("<br/>", " ");
            //tmpInfo = tmpInfo.replaceAll("<br>", " ");
           
        	return    tmpInfo.replaceAll("&ldquo;", "“")
                             .replaceAll("&rdquo;", "”")
                             .replaceAll("&", "&amp;")
                             .replaceAll("&nbsp;", "")
                             .replaceAll("<", "&lt;")
                             .replaceAll(">", "&gt;")
                             .replaceAll("\"", "&quot;")
                             .replaceAll("'", "&apos;");

        }
        catch(Exception ex) {
        	 ex.printStackTrace();
        }
        return tmpInfo;
        
    }
    
    public static String formatUrl(String infomation){
    	
        infomation = infomation != null ? infomation : "";
//        try
//        {
//            tmpInfo = tmpInfo.replaceAll("/", "x2F00");
//            tmpInfo = tmpInfo.replaceAll("&", "x2600");
//            tmpInfo = tmpInfo.replaceAll("#", "x2700");
//            tmpInfo = tmpInfo.replaceAll("\\+", "x2800");
//        }
//        catch(Exception exception) { }
        return urlFormat(infomation);
    }

    public static String unFormatUrl(String infomation){
    	
    	  infomation = infomation != null ? infomation : "";
//        try
//        {
//            tmpInfo = tmpInfo.replaceAll("x2F00", "/");
//            tmpInfo = tmpInfo.replaceAll("x2600", "&");
//            tmpInfo = tmpInfo.replaceAll("x2800", "+");
//            tmpInfo = tmpInfo.replaceAll("x2700", "#");
//            tmpInfo = tmpInfo.replaceAll("\\\\x2F", "/");
//            tmpInfo = tmpInfo.replaceAll("\\\\x26", "&");
//        }
//        catch(Exception exception) { }
        return urlUnFormat(infomation.
        		replaceAll("\\\\x2F", "/").replaceAll("\\\\x26", "&"));

    }
    
    public static String urlGoogleFilter(String infomation){
    	
        String tmpInfo = infomation != null ? infomation : "";
        try
        {
            tmpInfo = tmpInfo.replaceAll("\\\\x2F", "/");
            tmpInfo = tmpInfo.replaceAll("\\\\x26", "&");
            tmpInfo = tmpInfo.replaceAll("amp;", "");
        }
        catch(Exception exception) { }
        return tmpInfo;    	
    }
    
    public static String getRealUrlFromGoogle(String url){
    	
    	   //System.out.println("#########################>>>>:"+url);
    	   String[] urls = url.trim().split("\\s");
    	   for(int i=0;i<urls.length;i++){
    		   if(urls[i].toLowerCase().indexOf("href")!=-1){
    			   url = urls[i];
    			   break;
    		   }
    	   }
    	   
    	   int st = url.indexOf("http");//目的是找到真正的有效地址
    	   if(st==-1){return urlUnFormat(url);}
    	   
    	   String url2 = "";
    	   int sst = url.indexOf("?");
    	   if(sst>st){// http://xxxx/x?...//开头带http，并且带问号
    		   int ssst = url.lastIndexOf("http");
    		   if(ssst==st){//http://xxx/x?xx=xxxxx 后面无http
    	    	   url2 = url.substring(st);
    	    	   //String[] urls = url2.split("\\s");
    	    	   //url2 = urls[0].replaceAll("\"", "").replaceAll("'", "");
    	    	   //return urlUnFormat(url2);
    		   }else{// if(ssst>st) http://xxx/x?xx=httpxxxxx 后面带http
    			   url2 = url.substring(ssst);
    			   int st2 = url2.indexOf("&");
    			   if(st2!=-1){// http://xxx/x?xx=httpxxxxx&xxx=xxxxx 主http带有另外的参数
    				   url2 = url2.substring(0, st2);
        	    	   //String[] urls = url2.split("\\s");
        	    	   //url2 = urls[0].replaceAll("\"", "").replaceAll("'", "");
    				   //return urlUnFormat(url2);
    			   }//else{// http://xxx/x?xx=httpxxxxx 主http没带有另外的参数
    				   //url2 = url2.substring(0);
        	    	   //String[] urls = url2.split("\\s");
        	    	   //url2 = urls[0].replaceAll("\"", "").replaceAll("'", "");
    				   //return urlUnFormat(url2);
    			   //}
    		   }
    	   }else if(sst!=-1){// xxxx/x?...//开头不带http，但带问号
    		   int ssst = url.lastIndexOf("http");
    		   if(ssst==st){// xxx/x?xx=httpxxxxx 后面带http
    			   url2 = url.substring(ssst);
    			   int st2 = url2.indexOf("&");
    			   if(st2!=-1){// xxx/x?xx=httpxxxxx&xxx=xxxxx 主http带有另外的参数
    				   url2 = url2.substring(0, st2);
    			   }//else{// xxx/x?xx=httpxxxxx 主http没带有另外的参数
    				   //url2 = url2.substring(0, st2);
    			   //}
    		   }else{
    			   url2 = url.substring(ssst);
    		   }
    	   }else if(sst==-1){
    		   url2 = url.substring(st);
    	   }
    	   
    	   urls = url2.split("\\s");
    	   url2 = urls[0].replaceAll("\"", "").replaceAll("'", "");  
    	   return urlUnFormat(url2); 
    }
    
    public static String keywordFilter(String keyword)
    {
        return keyword.replaceAll("\\\\", "").replaceAll("\\^", "").replaceAll("\\$", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\?", "").replaceAll("\\+", "").replaceAll("\\*", "").replaceAll("\\|", "").replaceAll("\\&", "").replaceAll("\\#", "").replaceAll("\\%", "").replaceAll("\\/", "");
    }
    
    public static String getRealProductPicFromYou(String url){
    	
    	   if(url.indexOf("url=")==-1){return url;}
    	   url = url.substring(url.indexOf("url=")+4);
    	   url = url.substring(0, url.indexOf("&"));
    	   //System.out.println("有dao图片地址："+url);
    	   return url.replaceAll("%25", "%")
    	             .replaceAll("%3A", ":")
    	             .replaceAll("%2F", "/")
    	             .replaceAll("%3F", "?")
    	             .replaceAll("%26", "&")
    	             .replaceAll("%3D", "=");
    }
    
    public static String urlUnFormat(String url){
    	
 	   return   url.replaceAll("%25", "%")
			       .replaceAll("%3A", ":")
			       .replaceAll("%2F", "/")
			       .replaceAll("%3F", "?")
			       .replaceAll("%26", "&")
			       .replaceAll("%3D", "=")
                   .replaceAll("%23", "#")
                   .replaceAll("%2B", "+");
    }
    
    public static String urlFormat(String url){
    	
  	   return   url.replaceAll("%", "%25")
 			       .replaceAll(":", "%3A")
 			       .replaceAll("/", "%2F")
 			       .replaceAll("\\?", "%3F")
 			       .replaceAll("&", "%26")
 			       .replaceAll("=", "%3D")
  	               .replaceAll("#", "%23")
  	               .replaceAll("\\+", "%2B");
     }
    
	public static String getStrDateTimeFromLong(long time,String format){
		
		String strDateTime = null;
		try{
			
			java.util.Date dDate = new Date(time);
			
			strDateTime = new SimpleDateFormat(
					format).format(dDate);
	     }catch(Exception ex){
	    	 ex.printStackTrace();
	     }
		
	     return strDateTime;
	}
	
	/**
	 * 解析元素
	 * @param before_tag
	 * @param after_tag
	 * @param end_tag
	 * @param sb
	 * @return
	 */
	public static String getHtmlElementVal(String before_tag, String after_tag, String end_tag, StringBuilder sb){
		    if (sb == null){
		        return null;
		    }
            
		    String[] tags = before_tag.split(Constant.NULLCHAR);
		    int et = -1;
		    int endt = -1;
		    for(String tag:tags){
		    	   //System.out.println(tag);
		    	   String[] subs = tag.split("@");
		    	   et=-1;
		    	   for(String sub:subs){
		    		   
		    		   if(et!=-1&&(sb.indexOf(sub)==-1||et<sb.indexOf(sub))){
		    			   //et = sb.indexOf(sub)+sub.length();
		    			   break;
		    		   }else{
		    			   et = sb.indexOf(sub);
		    			   if(et!=-1){
		    				   et +=sub.length();
		    			   }
		    			   endt = sb.indexOf(end_tag);
		    		   }
		    	   }
	    		   
	    		   if (et == -1||et>endt){
	    			   return null;
	    		    }
	    		   sb.delete(0, et);
		    }
		    
		    String[] after_list = after_tag.split("@");
		    String val = null;
		    et = -1;
		    for(String tag:after_list){
		    	
		    	if(et!=-1&&(sb.indexOf(tag)==-1||et<sb.indexOf(tag))){
		    		//et = sb.indexOf(tag);
		    		break;
		    	}else{
		    		et = sb.indexOf(tag);
		    		endt = sb.indexOf(end_tag);
		    	}
		    	if (et == -1||et>endt) return null;
		    }
		    val = sb.substring(0, et);
		    sb.delete(0, et);
		    return val;
	 }
	


	/**
	 * 获取一个参数规格表
	 * @param doc
	 * @param mparasXpath
	 * @return 
	 */
	public static String getMParas(Document doc, String mparasXpath) {
		Node node;
		NodeList nodeList;
		nodeList = DomUtil.getNodeList(doc, mparasXpath);
		String mparams = "";
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			if (node.getNodeName().toLowerCase().equals("script")
					| node.getNodeName().toLowerCase().equals("style")) {
				continue;
			}
			if (node.hasChildNodes()) {
				NodeList childNodeList = node.getChildNodes();
				int len1 = childNodeList.getLength();
				if (i > 0) {
					mparams += Constant.XMFLAG;
				}
				if (len1 > 1) {
					int flag = 0;
					for (int j = 0; j < len1; j++) {
						String str = "";
						Node node1 = childNodeList.item(j);
						if (node1 != null
								&& !node1.getNodeName().toLowerCase()
										.equals("script")
								&& !node1.getNodeName().toLowerCase()
										.equals("style")) {
							str = node1.getTextContent().replaceAll("[　\\s ]+",
									" ");
							str = str.replaceAll("：", ":");
						} else {
							continue;
						}
						if (str == null | isBlankString(str)) {
							flag += 1;
							continue;
						}
						if (j == flag) {
							mparams += " ";
							mparams += str;
							mparams += Constant.XMFLAG;
						} else {
							mparams += " ";
							mparams += str;
							mparams += Constant.XMFLAG;
						}
					}
				} else {
					Node child = childNodeList.item(0);
					if (child.getNodeName().toLowerCase().equals("script")
							| child.getNodeName().toLowerCase().equals("style")) {
						continue;
					}
					mparams += " ";
					mparams += childNodeList.item(0).getTextContent().replaceAll("：", ":")
							.replaceAll("[ \n|\r|\t|\\s]+", " ").trim();
					mparams += Constant.XMFLAG;
				}
			} else {
				mparams += node.getTextContent().replaceAll("[ \n|\r|\t|\\s]+"," ").replaceAll("：", ":");
				mparams += Constant.XMFLAG;
			}
		}
		return  mparams;
	}

	/**
	 * 逆向匹配对应关系,假设有value，则必定有key，有key不一定有value
	 * @param mparams
	 * @param keysSplitFlag, keys分割符
	 * @return
	 */
	public static String formatKeyValueString(String mparams,String keysSplitFlag) {
		mparams = DataFormatUtil.filterEXPCharater_1(mparams);
		String[] strings= mparams.replaceAll("[\\s ]+", " ").split(keysSplitFlag);
		StringBuilder mString = new StringBuilder();
		if(checkColonCount(strings)){
			return KVTogether(strings, mString);
		}else{
			return KVSplit(strings, mString);
		}
	}

	private static boolean checkColonCount(String[] strings) {
		int colonCount=0;
		int lenth=0;
		for(int i=0;i<strings.length;i++){
			if(strings[i].contains(":")){
				colonCount++;
				lenth++;
			}else if(!strings[i].isEmpty()){
				lenth++;
			}
		}
		if(lenth==colonCount){
			return true;
		}else{
			return false;
		}
	}

	
	/**
	 * key-value is together,split with splitFlag 
	 * @param strings
	 * @param mString
	 * @return
	 */
	private static String KVTogether(String[] strings, StringBuilder mString) {
		for (int i = 0; i < strings.length; i++) {
			strings[i] = DataFormatUtil.filterEXPCharater_1(strings[i]).trim();
			mString.append(strings[i]+"  ");
		}
		return mString.toString().trim();
	}

	/**
	 * key-value is split by [xm99],
	 * @param strings
	 * @param mString
	 * @return
	 */
	private static String KVSplit(String[] strings, StringBuilder mString) {
		if(strings.length==1){
			return DataFormatUtil.filterEXPCharater_1(strings[0]);
		}
		for (int i = strings.length-1; i >= 0; i--) {
			strings[i] = DataFormatUtil.filterEXPCharater_1(strings[i]);
			if(i==0){
				mString.insert(0, strings[i].trim() + ":" + "  ");
				continue;
			}
			if(strings[i].isEmpty()&i>0){
				continue;
			}
			
			if (!strings[i - 1].isEmpty()&i>0) {
				mString.insert(0, "  "+strings[i - 1].trim() + ":" + strings[i].trim());
			} else if(i-1!=0){
				mString.insert(0, "  "+strings[i].trim() + ":" + "  ");
			}else{
				mString.insert(0, "  "+strings[i-1].trim() + ":" + "  ");
			}
			i--;
		}
		return mString.toString().replaceAll("：", ":").replaceAll("::", ":");
	}
	
	
	
	/**
	 * 将冒号替换为“=”
	 * @param mparams
	 * @return
	 */
	public static StringBuilder formatMparamsColon(String mparams,String splitFlag) {
		String[] strings=mparams.split(splitFlag);
		StringBuilder mString = new StringBuilder();
		for(int i=0;i<strings.length;i++){
			if(strings[i].isEmpty()){continue;}
			if(strings[i].matches(".*[^\\d]+[：:]+.*")){
				strings[i]=strings[i].replaceFirst("[：:]+", ":");
				mString.append(strings[i]).append("  ");
			}else{
				mString.append(strings[i]).append("  ");
			}
		}
		return mString;
	}
	
	
	/**
	 * 分行获取节点下的所有文本节点内容
	 * @param node
	 * @return
	 */
	public static String getTextHelper( Node node) {
		StringBuffer sb = new StringBuffer(); 
		NodeWalker walker = new NodeWalker(node);

		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();
			short nodeType = currentNode.getNodeType();

			if ("script".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if ("style".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if (nodeType == Node.COMMENT_NODE) {
				walker.skipChildren();
			}
			if (nodeType == Node.TEXT_NODE) {
				// cleanup and trim the value
				String text = currentNode.getNodeValue();
				text = text.replaceAll("\\s+", " ");
				text = text.trim();
				if (text.length() > 0) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(text);
				}
			}
		}
		return sb.toString();
	}
	
	
	
	
	
	public static boolean isBlankString(String str) {
		return str.isEmpty() | str.matches("[ \\s　]+");
	}	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<?> randomList(List<?> list){
		try{
			Set<?> set = new HashSet(list);
			list = new ArrayList(set);
		}catch (Exception e) {
			// TODO: handle exception
		}
		return list;		
	}


	/**
	 * 按指定字符分割参数表，并将将只有key没有value的段补充“ ”占位，
	 * @param mparams 
	 * @param KVSplitFlag key-value之间的分隔符
	 * @param splitFlag key-value对之间的分隔符
	 * @return
	 */
	public static String formatMParams(String mparams,String KVSplitFlag,String splitFlag) {
		String[] strings = mparams.split(splitFlag);
		StringBuilder mString = new StringBuilder();
		if(strings.length==1){
			return strings[0];
		}
		for(int i = 0; i < strings.length; i++) {
			if(strings[i].contains(":")){
				if(i==0)
					if (strings[i].endsWith(":")) {
						mString.append(strings[i].replaceFirst(":", Constant.XMFLAG)+" ");
					}else
					mString.append(strings[i].replaceFirst(":", Constant.XMFLAG));
				else if(strings[i].endsWith(":"))
					mString.append(Constant.XMFLAG + strings[i].replaceFirst(":", Constant.XMFLAG) + " ");
				else
					mString.append(Constant.XMFLAG + strings[i].replaceFirst(":", Constant.XMFLAG));
			}else {
				mString.append(strings[i]);
			}
		}
		return mString.toString();
	}
	
	
	
	public static String formatBookMparams(String mStr){
		String rs ="";
		String[] strings=mStr.toString().split("\\s*\\[xm99\\]\\s*");
		for(int i=0;i<strings.length;i++){
//			是否为空
			if(strings[i].isEmpty()){
//				结果内容长度>6 && 结果内容的结尾不为[xm99],在结果末尾加上[xm99];否则反之;
				if(rs.length()>6 && !rs.substring(rs.length()-6, rs.length()).toString().equals(Constant.XMFLAG)){
					rs+=(Constant.XMFLAG );
				}
				continue;
			}
//			不为空，是否到末尾，到了，结果直接加上末尾内容；
			if(i==strings.length-1){
				if(!strings[i].contains("=")||rs.isEmpty()||(rs.length()>6 && rs.substring(rs.length()-6, rs.length()).toString().equals(Constant.XMFLAG)))
					rs += strings[i];
				else if(rs.length()>6 && !rs.substring(rs.length()-6, rs.length()).toString().equals(Constant.XMFLAG)){
					rs += Constant.XMFLAG + strings[i];
				}
			}else if(strings[i].endsWith("=")){
				if(strings[i+1].isEmpty()){
					if(rs.isEmpty()||(rs.length()>6 && Constant.XMFLAG.equals(rs.substring(rs.length()-6, rs.length()))))
						rs += strings[i] + "null";
					else {
						rs += Constant.XMFLAG + strings[i] + "null";
					}
				}else if (strings[i+1].contains("=")&&(rs.isEmpty()||rs.length()>6 && Constant.XMFLAG.equals(rs.substring(rs.length()-6, rs.length())))){
					rs += strings[i] + "null";
				}else{
					if(rs.isEmpty()||(rs.length()>6 && Constant.XMFLAG.equals(rs.substring(rs.length()-6, rs.length())))){
						rs += strings[i] + strings[i + 1];
						i++;
					 }else
						rs += Constant.XMFLAG + strings[i]+"null";
				}
				
//			不是末尾，同时i的内容含"=" 且不是“=”结尾 	
			}else if(strings[i].contains("=")){
				if(rs.isEmpty()||(rs.length()>6 && rs.substring(rs.length()-6, rs.length()).toString().equals(Constant.XMFLAG))){
					rs += strings[i];
				}else{
					rs+=Constant.XMFLAG + strings[i];
				}
//			i的内容不含“=”， 	
			}else{
					rs += " " + strings[i];
			}
		}
		return rs;
		
	}
	
	
	
	
}
