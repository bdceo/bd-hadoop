package com.bdsoft.nutch.count;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class NoClcCount {

	private static Map<String, String> merMap = new HashMap<String, String>();
	private static Map<String, Integer> errMap = new HashMap<String, Integer>();
	private static Map<String, Integer> clcMap = new HashMap<String, Integer>();

	/**
	 * nutch抓取商品，没有原始分类统计
	 */
	public static void main(String[] args) {
		loadMerchant();
		loadErrorKey();
		calcNoClc();
	}

	public static void calcNoClc() {
		String file = "e:/ORICAT.log";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				// 01494401069
				String sid = line.substring(0, 4);
				Object value = merMap.get(sid);
				if (value == null) {
					sid = line.substring(line.length() - 4, line.length());
					if (errMap.get(sid) != null) {
						value = merMap.get(sid);
					}
				}
				if (value != null) {
					value = clcMap.get(sid);
					clcMap.put(sid, value == null ? 1 : ((Integer) value) + 1);
				}
			}
			// 排序
			List<Entry<String, Integer>> tmp = new ArrayList<Map.Entry<String, Integer>>(
					clcMap.entrySet());
			Collections.sort(tmp, new Comparator<Entry<String, Integer>>() {
				public int compare(Entry<String, Integer> o1,
						Entry<String, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});
			for (Entry<String, Integer> en : tmp) {
				System.out.println(merMap.get(en.getKey()) + "：\n\t商户编码="
						+ en.getKey() + "\t无分类商品=" + en.getValue());
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

	public static void loadErrorKey() {
		String file = "e:/ERRORKEY.log";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				// 99491581069#1069#9949158
				String[] errs = line.split("#");
				String key = errs[1];
				if (key == null || "".equals(key)) {
					continue;
				}
				Object value = errMap.get(key);
				errMap.put(key, value == null ? 1 : ((Integer) value) + 1);
			}
			// for (Entry<String, Integer> en : errMap.entrySet()) {
			// System.out.println(en.getKey() + "=" + en.getValue());
			// }
			System.out.println("Pid错误商户数=" + errMap.size());
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

	public static void loadMerchant() {
		try {
			org.dom4j.Document doc = new SAXReader()
					.read("src/test/xm-nutch-template.xml");
			List list = doc.selectNodes("//root/item");
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Element ele = (Element) list.get(i);
				String use = ele.element("isUse").getText();
				if (use != null && "1".equals(use)) {
					String code = ele.element("code").getText();
					String name = ele.element("name").getText();
					merMap.put(code, name);
				}
			}
			// for (Entry<String, String> en : merMap.entrySet()) {
			// System.out.println(en.getKey() + "=" + en.getValue());
			// }
			System.out.println("Nutch商户总数=" + merMap.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
