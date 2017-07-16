package com.bdsoft.utils;

import org.apache.commons.lang.StringUtils;

public class StrUtil extends StringUtils{
	public static boolean isEmpty(String s) {
		return trim(s).equals("") ;
	}
	public static String trim(String s ){
		return s==null ? "" : s.trim();
	}

}
