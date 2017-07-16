package com.bdsoft.hadoop.rpc;

import org.apache.hadoop.ipc.VersionedProtocol;

public interface IPCQueryStatus extends VersionedProtocol {

	IPCFileStatus getFileStatus(String filename);

}
