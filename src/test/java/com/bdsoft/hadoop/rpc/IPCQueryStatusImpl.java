package com.bdsoft.hadoop.rpc;

import java.io.IOException;

import org.apache.hadoop.ipc.ProtocolSignature;

public class IPCQueryStatusImpl implements IPCQueryStatus {

	public IPCQueryStatusImpl() {
	}

	@Override
	public long getProtocolVersion(String protocol, long clientVersion)
			throws IOException {
		System.out.println("协议：" + protocol);
		System.out.println("客户端版本：" + clientVersion);
		return IPCQueryServer.IPC_VER;
	}

	@Override
	public IPCFileStatus getFileStatus(String filename) {
		IPCFileStatus fs = new IPCFileStatus(filename);
		System.out
				.println("IPCQueryStatusImpl Method getFileStatus Called, return: "
						+ fs);
		return fs;
	}
 
	@Override
	public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int clientMethodsHash)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
