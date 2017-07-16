package com.bdsoft.hadoop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;

public class PDInvocationHandler implements InvocationHandler {

	private PDQueryStatus target;

	public PDInvocationHandler(PDQueryStatus obj) {
		this.target = obj;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String msg = MessageFormat.format("Calling method {0}({1})",
				method.getName(), Arrays.toString(args));
		System.out.println(msg);

		Object ret = method.invoke(target, args);

		msg = MessageFormat.format("Method {0} returned with {1}",
				method.getName(), ret.toString());
		System.out.println(msg);

		return ret;
	}

}
