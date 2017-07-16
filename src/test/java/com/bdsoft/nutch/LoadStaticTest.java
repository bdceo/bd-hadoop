package com.bdsoft.nutch;

import java.util.HashMap;
import java.util.Map;

public class LoadStaticTest {

	public static void main(String[] args) {
		System.out.println(C1.map.size());
		System.out.println(C2.map2.size());
		
		C2 c2 = new C2();
		c2.init();
		
		System.out.println(C1.map.size());
		System.out.println(C2.map2.size());
		
	}

}

class C1 {
	public static Map<String, String> map = C2.map2;
}

class C2 {
	public static Map<String, String> map2 = new HashMap<String, String>();

	public void init() {
		map2.put("1", "bdceo");
		map2.put("2", "bdcoo");
		map2.put("3", "bdcto");
	}

}
