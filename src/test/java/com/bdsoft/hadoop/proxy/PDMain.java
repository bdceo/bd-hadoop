package com.bdsoft.hadoop.proxy;

import java.lang.reflect.Proxy;

public class PDMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PDQueryStatus proxy = create(new PDQueryStatusImpl());
		PDFileStatus fs = proxy.getFileStatus("d:/download/aaa.tx");
		System.out.println("Main call back:" + fs);

	}

	public static PDQueryStatus create(PDQueryStatus qs) {
		PDInvocationHandler ih = new PDInvocationHandler(qs);
		Class<?>[] is = new Class<?>[] { PDQueryStatus.class };

		Object proxy = Proxy.newProxyInstance(qs.getClass().getClassLoader(),
				is, ih);

		Class<?> p2 = Proxy.getProxyClass(qs.getClass().getClassLoader(), is);
		System.out.println("Proxy class name:" + p2.getCanonicalName());
		System.out.println("Proxy's super class name:"
				+ p2.getClass().getName());
		System.out.println();

		return (PDQueryStatus) proxy;
	}
}
