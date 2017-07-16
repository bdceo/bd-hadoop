package com.bdsoft.hadoop.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIQueryStatusServer {

	public static void main(String[] args) {
		try {
			RMIQueryStatus rmiQS = new RMIQueryStatusImpl();

			LocateRegistry.createRegistry(12090);
			Naming.rebind(RMIQueryStatus.RMI_URL, rmiQS);

			System.out.println("RMI server is ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
