package com.bdsoft.nutch.count.crawldb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bdsoft.hadoop.BDHadoop;
import com.bdsoft.nutch.count.CrawlDatum;
import com.bdsoft.nutch.count.RandomWriteFile;
import com.bdsoft.nutch.count.TimerC;

public class CountCrawlDB {

	public static void main(String[] args) throws Exception {
		// hadoop解压缩
		String path, depath, opath;
		Date start = new Date();
		path = BDHadoop.BASE_PATH + "part-00000";
		// depath = BDHadoop.decompsDeflate(path);
		// TimerC.cost("解压缩", start);

		// url提取
		depath = BDHadoop.BASE_PATH + "6crawldb_dump0901_jdyixun-2";
		opath = BDHadoop.BASE_PATH + "jd_item_url_7";
		// String host = "item.jd.com";
		// readCrawlDBDumpByPost(depath, opath, host);

		Pattern p1 = Pattern.compile(".*(http://item.jd.com/([\\d]+).html).*");
		Pattern p2 = Pattern
				.compile(".*(http://www.360buy.com/product/([\\d]+).html).*");
		Pattern[] pats = new Pattern[] { p1, p2 };
		String flag = ".*(jd.com|360buy.com).*";
		takeUrlByReg(flag, pats, 2, depath, opath);
		TimerC.cost("提取crawldb-url", start);

		// 分析crawldb-dump
		// start = new Date();
		// depath = BDHadoop.BASE_PATH + "crawldb0124-dump0816-1";
		// opath = BDHadoop.BASE_PATH + "175crawldb-dump0816-coo8";
		// readCrawlDBDumpRandom(depath, opath);
		// TimerC.cost("分析crawldb-dump", start);
	}

	public static void takeUrlByReg(String flag, Pattern[] pats, int group,
			String src, String dest) {
		if (!src.endsWith("txt")) {
			src += ".txt";
		}
		if (!dest.endsWith("txt")) {
			dest += ".txt";
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(src));
			String line = br.readLine();
			int i = 0;
			Set<String> pick = new HashSet<String>();
			while (line != null) {
				if (line.startsWith("http")) {
					if (line.matches(flag)) {
						String url = line.split("Version:")[0].trim();
						for (Pattern pat : pats) {
							Matcher m = pat.matcher(url);
							if (m.find()) {
								i++;
								System.out.println("pick > " + url + "\t"
										+ pick.size());
								String tmp = m.group(group);
								pick.add(tmp);
								if (i >= 5000) {
									data2file(pick, dest);
									pick = new HashSet<String>();
									i = 0;
								}
								break;
							}
						}
					}
				}
				line = br.readLine();
			}
			if (pick.size() > 0) {
				data2file(pick, dest);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static void data2file(Set<String> data, String dest) {
		System.out.println("开始输出...");
		String flag = "#ID#";
		String base = "http://item.jd.com/" + flag + ".html";
		StringBuffer sb = new StringBuffer();
		int li = 0;
		for (String d : data) {
			String url = base.replaceAll(flag, d);
			sb.append(url);
			sb.append("\r\n");
			li++;
			if (li >= 1000) {
				RandomWriteFile.writeFile(dest, sb.toString().getBytes());
				System.out.println("输出 > " + li);
				li = 0;
				sb = new StringBuffer();
			}
		}
		if (li > 0) {
			RandomWriteFile.writeFile(dest, sb.toString().getBytes());
			System.out.println("输出 > " + li);
		}
		System.out.println("输出结束。");
	}

	public static void readCrawlDBDumpByPost(String src, String dest,
			String host) {
		if (!src.endsWith("txt")) {
			src += ".txt";
		}
		if (!dest.endsWith("txt")) {
			dest += ".txt";
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(src));
			String line = br.readLine();
			long i = 0l;
			StringBuffer sb = new StringBuffer();
			while (line != null) {
				if (line.startsWith("http")) {
					String url = line.split("Version:")[0].trim();
					if (url.indexOf(host) >= 0) {
						i++;
						sb.append(url + "\r\n");
						System.out.print(i + "-");
						if (i == 100) {
							RandomWriteFile.writeFile(dest, sb.toString()
									.getBytes());
							i = 0l;
							sb = new StringBuffer();
							System.out.println();
						}
					}
				}
				line = br.readLine();
			}
			if (sb != null) {
				RandomWriteFile.writeFile(dest, sb.toString().getBytes());
			}
			// System.out.println("累计url：" + cds.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * 从Crawldb-dump中提取url
	 * 
	 * @param path
	 */
	public static void readCrawlDBDumpRandom(String src, String dest) {
		if (!src.endsWith("txt")) {
			src += ".txt";
		}
		List<CrawlDatum> cds = new ArrayList<CrawlDatum>();
		BufferedReader br = null;
		// 随机抽取
		int i = 0;
		int c = 10000000; // 单文件url总数
		int n = 700000; // 需要提取url数
		if (!dest.endsWith("txt")) {
			dest += ".txt";
		}
		try {
			br = new BufferedReader(new FileReader(src));
			// 随机抽取
			RandomURL ru = new RandomURL(n, c, dest);
			String line = br.readLine();
			while (line != null) {
				// System.out.println(line);
				if (line.startsWith("http")) {
					i++;
					CrawlDatum cd = new CrawlDatum();
					String url = line.split("Version:")[0].trim();
					cd.setUrl(url);
					line = br.readLine();
					String status = line.split(" ")[1];
					cd.setStatus(status);
					line = br.readLine();
					if (line == null)
						break;
					String time = line.split("time:")[1].trim();
					cd.setFetchTime(time);
					line = br.readLine();
					if (line == null)
						break;
					line = br.readLine();
					if (line == null)
						break;
					line = br.readLine();
					if (line == null)
						break;
					line = br.readLine();
					if (line == null)
						break;
					String score = line.split(":")[1].trim();
					cd.setScore(score);
					line = br.readLine();
					if (line == null)
						break;
					line = br.readLine();
					if (line == null)
						break;
					String meta = line.substring(line.indexOf(":")).trim();
					cd.setMeta(meta);
					// System.out.println(cd.toString());
					// System.out.println(cd.getUrl());
					// cds.add(cd);
					if (!ru.addUrl(i, cd)) {
						break;
					}
				}
				line = br.readLine();
			}
			// System.out.println("累计url：" + cds.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
