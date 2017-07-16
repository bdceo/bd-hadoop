package com.bdsoft.hadoop.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIQueryStatus extends Remote {
	
	public final String RMI_URL = "rmi://192.168.0.3:12090/query_status";

	RMIFileStatus getFileStatus(String fileName) throws RemoteException;

}
