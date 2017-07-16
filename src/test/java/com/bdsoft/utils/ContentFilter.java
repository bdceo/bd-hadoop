package com.bdsoft.utils;

import java.util.regex.Pattern;

public class ContentFilter  {
	private static String tagetPhrase = "[\\S]*加载[\\S]*" +
			"|正\\s*[\\S]*加载中[\\S]*" + "|请稍候[\\s\\S]*加载中[\\S]*" + "|纠错";

	public static String contentFilt(String string) {
		string=string.replaceAll(tagetPhrase, "");
//		prt(string);
		return string;		
	}
	public static void main(String[] args){
		String phrase="您好！ 正 我们请稍候， 是经国家的统一的。 在加载中。。。。！！";
		//String phrase = "诺基亚 Nokia C2 07 触控+键盘娱乐互联手机 2 6寸QVGA显示屏 内置诺基亚高速上网浏览器 支持JAVA软件扩展 音乐播放 200万像素照相机 小巧外观 双色可选";
		phrase=ContentFilter.contentFilt(phrase);
//		phrase=phrase.replaceAll("[\\S]*加载中[\\S]*", "");
		prt(phrase);
		phrase = "华为Honor荣耀 U8860 5秒急速开机 1 4GHz高速处理器 1930毫安强劲电池 4英寸FWVGA高清大屏 专业防刮玻璃屏 800万像素 安卓2 3操作系统 内置云服务";
		if(Pattern.compile("平板电脑[\\s\\S]*手机|平板电脑[\\s\\S]*").matcher(phrase).find()){
			prt("14");
		}
		if(Pattern.compile("手机[\\s\\S]*像素[\\s\\S]*|处理器[\\s\\S]*像素[\\s\\S]*|像素[\\s\\S]*").matcher(phrase).find()){
			prt("10");
		}
	}
	private static void prt(String phrase) {
		System.out.println(phrase);
		
	}
	
	public static String bookWordFormat(String mparams) {
		String mparams1 = mparams;
		//
		mparams1 = mparams1.replaceAll("作[ 　\\s ]+者", "作者")
				.replaceAll("出[　\\s ]+版[　\\s ]+社", "出版社")
				.replaceAll("ＩＳＢＮ", "ISBN")
				.replaceAll("I\\s*S\\s*B\\s*N", "ISBN")
				.replaceAll("版[　\\s ]*次", "版次")
				.replaceAll("字[　\\s ]*数", "字数")
				.replaceAll("页[　\\s ]*数", "页数")
				.replaceAll("装[　\\s ]+帧", "装帧")
				.replaceAll("开[　\\s ]+本", "开本")
				.replaceAll("纸[　\\s ]+张", "纸张")
				.replaceAll("印[　\\s ]+次", "印次")
				.replaceAll("包[　\\s ]+装", "包装")
				.replaceAll("介[　\\s ]+质", "介质")
				.replaceAll("地[　\\s ]+区", "地区")
				.replaceAll("条[　\\s ]+形[　\\s ]+码", "条形码")
				.replaceAll("碟[ \\s]+片[ \\s]+数", "碟片数");
				
		return mparams1;
	}
	


}
