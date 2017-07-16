/**
 * ParseTVData.java
 * com.bdsoft.hadoop.counter
 * Copyright (c) 2015, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.counter;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 解析机顶盒数据
 * <p>
 *
 * @author   丁辰叶
 * @date	 2015-12-30
 * @version  1.0.0
 */
public class ParseTVData {

	public static List<String> transData(String text) {
		List<String> list = new ArrayList<String>();
		Document doc;
		String rec = "";
		try {
			doc = Jsoup.parse(text);//jsoup解析数据
			Elements content = doc.getElementsByTag("WIC");
			String num = content.get(0).attr("cardNum");//记录编号
			if (num == null || num.equals("")) {
				num = " ";
			}

			String stbNum = content.get(0).attr("stbNum");//机顶盒号
			if (stbNum.equals("")) {
				return list;
			}

			String date = content.get(0).attr("date");//日期

			Elements els = doc.getElementsByTag("A");
			if (els.isEmpty()) {
				return list;
			}

			for (Element el : els) {
				String e = el.attr("e");//结束时间
				String s = el.attr("s");//开始时间
				String sn = el.attr("sn");//频道名称
				rec = stbNum + "@" + date + "@" + sn + "@" + s + "@" + e;
				list.add(rec);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return list;
		}
		return list;
	}
}
