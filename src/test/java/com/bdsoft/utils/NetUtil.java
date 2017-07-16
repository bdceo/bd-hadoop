package com.bdsoft.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class NetUtil {
	
	public static void main(String[] args) throws Exception{
		String url = "http://s.1688.com/company/company_search.htm?keywords=%CC%EC%BD%F2&button_click=top&n=y";
		String html = getPageContent(url, "GB18030");
		System.out.println(html);
	}

	/**
	 * 从指定网络地址下载图片
	 * 
	 * @param url
	 *            图片地址
	 * @return 字节数组
	 * @throws Exception
	 */
	public static byte[] getImgFromNet(String url) throws Exception {
		InputStream ins = null;
		byte[] data = null;
		try {
			URL javaUrl = new URL(url);
			HttpURLConnection http = (HttpURLConnection) javaUrl
					.openConnection();
			http.setRequestMethod("GET");
			http.setConnectTimeout(5 * 1000);
			http.setReadTimeout(10 * 1000);
			http.setRequestProperty("User-Agent", Utils.BROWSER_UA);
			if (http.getResponseCode() == 200) {
				ins = http.getInputStream();
				data = parseStream(ins);
			}
		} finally {
			if (ins != null) {
				ins.close();
			}
		}
		return data;
	}

	/**
	 * 从指定地址获取网页源代码
	 * 
	 * @param url
	 *            网页地址
	 * @param charset
	 *            网页编码-字符集
	 * @return 网页源代码
	 * @throws Exception
	 */
	public static String getPageContent(String url, String charset)
			throws Exception {
		InputStream ins = null;
		try {
			URL javaUrl = new URL(url);
			HttpURLConnection http = (HttpURLConnection) javaUrl
					.openConnection();
			http.setRequestMethod("GET");
			http.setConnectTimeout(5 * 1000);
			http.setReadTimeout(5 * 1000);
			http.setRequestProperty("User-Agent", Utils.BROWSER_UA);
			int code = http.getResponseCode();
			System.out.println("返回码=" + code);
			if (code == 200) {
				ins = http.getInputStream();
				return new String(parseStream(ins), charset);
			} else if (code == 403) {
				throw new Exception("HTTP返回：" + code + ",IP被封，暂时无法访问");
			} else if(code == 404){
				throw new Exception("HTTP返回：" + code + ",页面不存在");
			} else{
				throw new Exception("HTTP返回：" + code);
			} 
		} finally {
			try {
				if (ins != null) {
					ins.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * 从指定地址获取网页源代码
	 * 
	 * @param url
	 *            网页地址
	 * @param http
	 *            httpclient
	 * @return 网页源代码
	 * @throws Exception
	 */
	public static String getPageContent(String url, DefaultHttpClient http)
			throws Exception {
		HttpGet get = new HttpGet(url);
		HttpResponse response = http.execute(get);
		return EntityUtils.toString(response.getEntity());
	}

	/**
	 * 从指定输入流中获取原信息
	 * 
	 * @param ins
	 *            输入流
	 * @param charset
	 *            字符集
	 * @return 原信息
	 * @throws Exception
	 */
	public static String parseContentFromStream(InputStream ins, String charset)
			throws Exception {
		byte[] data = parseStream(ins);
		return new String(data, charset);

	}

	/**
	 * 从指定地址获取网页的输入流【调用者必须对返回的输入流做释放处理！！！】
	 * 
	 * @param url
	 *            地址
	 * @param charset
	 *            网页编码-字符集
	 * @return 输入流
	 * @throws Exception
	 */
	public static InputStream getPageStream(String url, String charset)
			throws Exception {
		URL javaUrl = new URL(url);
		HttpURLConnection http = (HttpURLConnection) javaUrl.openConnection();
		http.setRequestMethod("GET");
		http.setConnectTimeout(5 * 1000);
		http.setReadTimeout(5 * 1000);
		http.setRequestProperty("User-Agent", Utils.BROWSER_UA);
		if (http.getResponseCode() == 200) {
			return http.getInputStream();
		}
		return null;
	}

	/**
	 * 发送POST请求，带参数【调用者必须对返回的输入流做释放处理！！！】
	 * 
	 * @param url
	 *            请求地址
	 * @param param
	 *            参数列表-String拼接
	 * @param charset
	 *            字符集
	 * @return 响应（输入流）
	 * @throws Exception
	 */
	public static InputStream sendPostRequest(String url, String param,
			String charset) throws Exception {
		// 对请求参数编码
		param = URLEncoder.encode(param, charset);
		byte[] data = param.getBytes();

		DataOutputStream outs = null;
		try {
			URL javaUrl = new URL(url);
			HttpURLConnection http = (HttpURLConnection) javaUrl
					.openConnection();
			http.setConnectTimeout(10 * 1000);
			http.setReadTimeout(10 * 1000);
			http.setRequestMethod("POST");
			http.setDoOutput(true);// 发送POST请求必须设置允许输出
			http.setUseCaches(false);// 不适用cache
			http.setRequestProperty("Connection", "Keep-Alive");
			http.setRequestProperty("Charset", charset);
			http.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			http.setRequestProperty("User-Agent", Utils.BROWSER_UA);

			// 发送请求并传递参数
			outs = new DataOutputStream(http.getOutputStream());
			outs.write(data);
			outs.flush();
			if (http.getResponseCode() == 200) {
				return http.getInputStream();
			}
		} finally {
			if (outs != null) {
				outs.close();
			}
		}
		return null;
	}

	/**
	 * 发送POST请求，带参数【调用者必须对返回的输入流做释放处理！！！】
	 * 
	 * @param url
	 *            请求地址
	 * @param param
	 *            参数列表-Map泛型
	 * @param charset
	 *            字符集
	 * @return 响应（输入流）
	 * @throws Exception
	 */
	public static InputStream sendPostRequest(String url,
			Map<String, String> param, String charset) throws Exception {
		// 解析参数列表并做编码处理
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : param.entrySet()) {
			sb.append(entry.getKey()).append("=")
					.append(URLEncoder.encode(entry.getValue(), charset));
			sb.append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		String tmp = URLEncoder.encode(sb.toString(), charset);
		byte[] data = tmp.getBytes();

		DataOutputStream out = null;

		try {
			URL javaUrl = new URL(url);
			HttpURLConnection http = (HttpURLConnection) javaUrl
					.openConnection();
			http.setConnectTimeout(10 * 1000);
			http.setReadTimeout(10 * 1000);
			http.setRequestMethod("POST");
			http.setDoOutput(true);// 发送POST请求必须设置允许输出
			http.setUseCaches(false);// 不适用cache
			http.setRequestProperty("Connection", "Keep-Alive");
			http.setRequestProperty("Charset", charset);
			http.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			http.setRequestProperty("User-Agent", Utils.BROWSER_UA);

			// 发送请求并传递参数
			out = new DataOutputStream(http.getOutputStream());
			out.write(data);
			out.flush();
			if (http.getResponseCode() == 200) {
				return http.getInputStream();
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return null;
	}

	/**
	 * 从输入流中解析数据【此方法不对传入的输入流做释放操作！！！】
	 * 
	 * @param ins
	 *            输入流
	 * @return 元数据
	 * @throws Exception
	 */
	public static byte[] parseStream(InputStream ins) throws Exception {
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
}
