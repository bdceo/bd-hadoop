package com.bdsoft.nutch;

import java.net.InetAddress;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.nutch.util.URLUtil;

public class NutchSrcTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String os = System.getProperty("os.name");
		System.out.println(os);

		Pattern pat = Pattern.compile("");
		String id = "urlnormalizer-basic";
		System.out.println(pat.matcher(id).matches());
		
		

	}

	// Net相关API测试
	public static void test1022() throws Exception {
		String url = "http://channel.jd.com/chaoshi.html";
		url = "http://www.douban.com";
		URL ju = new URL(url);
		System.out.println("URL > " + url);
		System.out.println("file=" + ju.getFile());
		System.out.println("ref=" + ju.getRef());
		System.out.println("path=" + ju.getPath());

		// 根据主机名，获取ip地址
		InetAddress ia = InetAddress.getByName(ju.getHost());
		String domain = URLUtil.getDomainName(url);
		String host = ia.getHostName();
		String ip = ia.getHostAddress();
		System.out.println("--------------------------------");
		System.out.println("URL > " + url);
		System.out.println("域名 > " + domain);
		System.out.println("主机 > " + host);
		System.out.println("IP > " + ip);
		System.out.println("--------------------------------");

		int hash = domain.hashCode();
		System.out.println("domain-hash = " + hash);
		hash = host.hashCode();
		System.out.println("host-hash = " + hash);
		hash = ip.hashCode();
		System.out.println("ip-hash = " + hash);

		hash = host.hashCode();
		hash ^= 0;
		System.out.println("^= hash = " + hash);

		int rn = hash & Integer.MAX_VALUE;
		System.out.println("hash & MAX_INT = " + hash);
		rn = rn % 1;
		System.out.println("rn = " + rn);
	}

}
