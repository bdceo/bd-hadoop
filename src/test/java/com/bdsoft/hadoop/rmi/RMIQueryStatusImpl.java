package com.bdsoft.hadoop.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIQueryStatusImpl extends UnicastRemoteObject implements
		RMIQueryStatus {

	private static final long serialVersionUID = -2448436315946266716L;

	protected RMIQueryStatusImpl() throws RemoteException {
		super();
	}

	@Override
	public RMIFileStatus getFileStatus(String fileName) throws RemoteException {
		RMIFileStatus fs = new RMIFileStatus(fileName);
		return fs;
	}

}
