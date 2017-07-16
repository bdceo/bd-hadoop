package com.bdsoft.hadoop.rmi;

import java.rmi.Naming;

public class RMIQueryStatusClient {

	public static void main(String[] args) {
		try {
			RMIQueryStatus rmiQS = (RMIQueryStatus) Naming
					.lookup(RMIQueryStatus.RMI_URL);

			RMIFileStatus rmiFS = rmiQS.getFileStatus("d:/download/aaa.txt");

			System.out.println("RMI client call result ：\n\t"
					+ rmiFS.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
