package com.bdsoft.nutch.count.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.helper.StringUtil;

import com.bdsoft.hadoop.BDHadoop;
import com.bdsoft.nutch.count.RandomWriteFile;
import com.bdsoft.nutch.count.TimerC;

public class Syslog {

	// nutch运行fetch过程日志分析
	public static void main(String[] args) throws Exception {
		List<String> logs = new ArrayList<String>();
		logs.add("6_syslog_amazon_1010");
		logs.add("7_syslog_amazon_1010");
		String[] tmp = logs.toArray(new String[] {});

		readSyslog(tmp);
	}

	public static void readSyslog(String[] logs) throws Exception {
		if (logs == null) {
			return;
		}
		int len = logs.length;
		for (int i = 0; i < len; i++) {
			String log = logs[i];
			if (!log.matches("^[a-zA-Z]:.*")) {
				log = BDHadoop.BASE_PATH + log;
			}
			System.out.println(log);
			File file = new File(log);
			if (file.exists() && !file.isDirectory()) {
				Date start = new Date();
				countFetch(file);
				TimerC.cost("提取日志文件>" + logs[i], start);
				Thread.sleep(5000);
			}
		}
	}

	public static void countFetch(Object o) {
		String fetchFlag = "fetch cost";
		BufferedReader br = null;
		String path = null;
		try {
			if (o instanceof File) {
				path = ((File) o).getAbsolutePath();
				br = new BufferedReader(new FileReader((File) o));
			} else if (o instanceof String) {
				path = (String) o;
				br = new BufferedReader(new FileReader(path));
			}
			path = path + "_count";
			long lineC = 0L;
			FetchCount count = new FetchCount();
			String line = br.readLine();
			while (!StringUtil.isBlank((line))) {
				if (line.contains(fetchFlag)) {
					System.out.println(line);
					FetchCost fc = formFC(line);
					if (fc != null) {
						// System.out.println(fc);
						count.doCount(fc);
						lineC++;
					}
				}
				line = br.readLine();
			}
			System.out.println("累计提取 > " + lineC);
			if (lineC > 0) {
				count.doAvg(lineC);
				System.out.println(count);
				RandomWriteFile.writeFile(path, count.toString().getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	public static void saveCount() {

	}

	// 提取日志信息
	public static FetchCost formFC(String line) {
		if (StringUtil.isBlank(line)) {
			return null;
		}
		// System.out.println(line);
		Pattern pat = Pattern
				.compile(".*\\s([\\d]+)ms\\.\\s([\\d]+)Kb.\\scode=([\\d]+).*url=(http.*)");
		Matcher mat = pat.matcher(line);
		if (mat.find()) {
			String cost = mat.group(1);
			// System.out.println(cost);
			String kb = mat.group(2);
			// System.out.println(kb);
			String code = mat.group(3);
			// System.out.println(code);
			String url = mat.group(4);
			// System.out.println(url);
			return new FetchCost(cost, kb, code, url);
		} else {
			return null;
		}
	}
}
