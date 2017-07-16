package com.bdsoft.nutch.count.crawldb;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.bdsoft.nutch.count.CrawlDatum;

public class RandomURL {

	private int need;
	private int max = 1000;
	private int cap;
	private int count = 0;
	private int modbase = 1;
	private Map<Integer, CrawlDatum> urlMap;
	private String path;
	private int total = 0;

	public RandomURL(int need, int cap, String path) {
		this.need = need;
		this.cap = cap;
		int tmp = (int) (this.cap / this.need);
		do {
			this.modbase = new Random().nextInt(100 - tmp);
		} while (this.modbase < tmp || this.modbase > (tmp * 3));
		System.out.println(modbase);
		this.urlMap = new HashMap<Integer, CrawlDatum>();
		this.path = path;
	}

	// 随机策略：
	// 累计4千万url，分4个文件存，要求输出3百万url
	// 平均一个文件输出70万url，4*70=280
	// 其余20万从历史segments中提取，异常抓取循环中
	public boolean addUrl(int i, CrawlDatum cd) {
		// 随机策略
		Random rd = new Random();
		int rc = rd.nextInt(this.cap);// 1000 0000
		int rn = rd.nextInt(this.need);// 70 0000
		if ((rc + rn) % this.modbase == 0) {
			urlMap.put(cd.getUrl().hashCode(), cd);
			total++;
			System.out.println("add ok " + modbase + "_" + i + "_" + total);
		}
		if (urlMap.size() >= this.max) {
			export2file();
			count++;
			if ((count * max) >= this.need) {
				System.out.println("累计<" + need + ">url已提取");
				return false;
			}
		}
		return true;
	}

	private void export2file() {
		if (this.path == null) {
			urlMap.clear();
			return;
		}
		RandomAccessFile ranFile = null;
		try {
			ranFile = new RandomAccessFile(new File(path), "rw");
			long pos = ranFile.length();
			ranFile.seek(pos);
			for (Entry<Integer, CrawlDatum> en : urlMap.entrySet()) {
				String url = en.getValue().getUrl();
				ranFile.write(url.getBytes());
				ranFile.write("\r\n".getBytes());
			}
			System.out.println("write to file " + count + "/" + pos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			urlMap.clear();
			if (ranFile != null) {
				try {
					ranFile.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
