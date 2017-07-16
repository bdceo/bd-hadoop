package com.bdsoft.hadoop.nio;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class NIOServer {

	private static final int TIMEOUT = 1000;
	private static final int PORT = 12112;

	public static void main(String[] args) {
		try {
			Selector selector = Selector.open();

			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(PORT));

			// 注册到selector
			SelectionKey sk = server.register(selector, SelectionKey.OP_ACCEPT);
			sk.attach(new Acceptor());

			while (true) {
				if (selector.select(TIMEOUT) == 0) {
					System.out.println("wait connect @ " + new Date().toLocaleString());
					continue;
				}

				Iterator<SelectionKey> keysIte = selector.selectedKeys().iterator();
				while (keysIte.hasNext()) {
					SelectionKey skey = keysIte.next();
					keysIte.remove();

					if (skey.isAcceptable()) {
						// 服务器接收以后，返回客户端连接
						SocketChannel client = ((ServerSocketChannel) skey.channel()).accept();
						client.configureBlocking(false);
						// 将客户端注册到选择器
						SelectionKey ckey = client.register(selector, SelectionKey.OP_READ);
						// 交由具体的连接处理类，处理请求和响应
						NIOServerConnection csCon = new NIOServerConnection(ckey);
						ckey.attach(csCon);
					}

					if (skey.isReadable()) {
						NIOServerConnection con = (NIOServerConnection) skey.attachment();
						con.handleRequest();
					}

					if (skey.isValid() && skey.isWritable()) {
						NIOServerConnection con = (NIOServerConnection) skey.attachment();
						con.handleResponse();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class Acceptor implements Runnable {

	public void run() {

	}
}
