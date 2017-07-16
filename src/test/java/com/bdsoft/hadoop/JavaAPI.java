package com.bdsoft.hadoop;


public class JavaAPI {

	public static void main(String[] args) {

		String userHome = System.getProperty("user.home", "i don't know");
		System.out.println(userHome);
		
		
		MyStr ms = new MyStr();
		System.out.println(ms.getClass().getName());
		Class o = String.class;
		System.out.println(o.getName());
	}

}

class MyStr extends JavaAPI{
	public MyStr(){}
}
