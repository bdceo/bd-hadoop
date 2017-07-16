package com.bdsoft.hadoop.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;

/**
 * 客户端连接处理器
 *
 * @author   丁辰叶
 * @date	 2016-7-25
 * @version  1.0.0
 */
public class NIOServerConnection {

	private static int BUFFERSIZE = 64;

	private SelectionKey key;
	private SocketChannel clientChannel;
	private ByteBuffer data;
	private String message = "";

	// 用于处理客户端的请求和响应
	public NIOServerConnection(SelectionKey key) {
		this.key = key;
		this.clientChannel = (SocketChannel) key.channel();
		data = ByteBuffer.allocate(BUFFERSIZE);
	}

	public void handleRequest() throws IOException {
		System.out.println("a request ...");
		int len = clientChannel.read(data);
		if (len == -1) {
			clientChannel.close();
		}
		String msg = new String(data.array(), 0, len);
		data.clear();
		if (msg.equals("\r\n")) {
			if (message.toLowerCase().equals("q")) {
				key.interestOps(SelectionKey.OP_WRITE);
				System.out.println("handle request and prepare response");
				return;
			}
			System.out.println("receive :" + message);
			data.flip();
			clientChannel.write(data);
			data.compact();
			message = "";
			key.interestOps(SelectionKey.OP_READ);
		} else {
			message += msg;
			return;
		}
	}

	public void handleResponse() throws IOException {
		System.out.println("handle response ...");
		data.clear();

		String msg = "copy @ :" + new Date().toLocaleString() + "\n";
		data.put(msg.getBytes("utf-8"));

		data.flip();
		clientChannel.write(data);

		System.out.println(msg);
		if (!data.hasRemaining()) {
			key.interestOps(SelectionKey.OP_READ);
		}

		data.compact();
	}

}
