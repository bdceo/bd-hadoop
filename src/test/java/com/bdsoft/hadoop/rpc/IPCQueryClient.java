package com.bdsoft.hadoop.rpc;

import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

public class IPCQueryClient {

	public static void main(String[] args) {
		try {
			System.out.println("协议全名：" + IPCQueryStatus.class.getName());
			System.out.println("协议接口："
					+ IPCQueryStatus.class.getMethod("getFileStatus",
							String.class).getName());

			InetSocketAddress addr = new InetSocketAddress("localhost",
					IPCQueryServer.IPC_PORT);

			// 客户端 获取服务器端调用代理
			IPCQueryStatus proxy = RPC.getProxy(
					IPCQueryStatus.class, IPCQueryServer.IPC_VER, addr,
					new Configuration());

			IPCFileStatus fs = proxy.getFileStatus("d:/download/aaa.txt");
			System.out.println("客户端接收响应：" + fs);

			RPC.stopProxy(proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
