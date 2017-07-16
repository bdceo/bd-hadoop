package com.bdsoft.hadoop.rpc;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class IPCQueryServer {

	public static int IPC_PORT = 4042;// 服务器监听
	public static long IPC_VER = 5473L; // 协议版本

	public static void main(String[] args) {
		try {
			ConsoleAppender append = new ConsoleAppender(new PatternLayout(
					PatternLayout.TTCC_CONVERSION_PATTERN));
			append.setThreshold(Level.DEBUG);
			BasicConfigurator.configure();

			IPCQueryStatus ipcQS = new IPCQueryStatusImpl();
//			Server server = RPC.getServer(ipcQS, "localhost", IPC_PORT,
//					new Configuration());
//
//			server.start();
//			System.out.println("服务器启动");
//			System.in.read();
//
//			server.stop();
//			System.out.println("服务器停止");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
