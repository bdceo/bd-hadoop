package com.bdsoft.nutch.count;

import java.util.Date;

public class TimerC {

	public static void cost(String msg, Date start) {
		System.out.println("\n>>" + msg + "\t耗时 "
				+ (System.currentTimeMillis() - start.getTime()) + "ms.");
	}

}
