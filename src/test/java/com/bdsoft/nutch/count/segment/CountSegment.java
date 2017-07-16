package com.bdsoft.nutch.count.segment;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bdsoft.hadoop.BDHadoop;
import com.bdsoft.nutch.count.CrawlDatum;
import com.bdsoft.nutch.count.TimerC;

public class CountSegment {

	public static void main(String[] args) throws Exception {
		Date start = new Date();
		// String path = BDHadoop.BASE_PATH + "20130806064459_fetch_dump";
		// String depath = BDHadoop.decompsDeflate(path);
		// TimerC.cost("解压缩", start);

		start = new Date();
		// 解析segment-dump日志
		String depath = BDHadoop.BASE_PATH + "xxx";
		String opath = BDHadoop.BASE_PATH + "20130806064459_fetch_dump";
		readSegDump(depath, opath);
		TimerC.cost("分析segment-dump", start);
	}

	/**
	 * 从segment-dump中提取url
	 * 
	 * @param path
	 */
	public static void readSegDump(String src, String dest) {
		if (!src.endsWith("txt")) {
			src += ".txt";
		}
		if (!dest.endsWith("txt")) {
			dest += ".txt";
		}
		List<CrawlDatum> cds = new ArrayList<CrawlDatum>();
		BufferedReader br = null;
		BufferedOutputStream bfout = null;
		try {
			br = new BufferedReader(new FileReader(src));
			bfout = new BufferedOutputStream(new FileOutputStream(dest));
			String line = br.readLine();
			while (line != null) {
				// System.out.println(line);
				if (line.startsWith("URL")) {
					String url = line.split(":: ")[1].trim();
					if (url.indexOf("search.suning.com") >= 0) {
						CrawlDatum cd = new CrawlDatum();
						cd.setUrl(url);
						cds.add(cd);
						System.out.println("url ++ " + url);
						bfout.write(url.getBytes());
						bfout.write("\r\n".getBytes());
					}
				}
				line = br.readLine();
			}
			bfout.flush();
			System.out.println("累计url：" + cds.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bfout.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
