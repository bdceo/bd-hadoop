package com.bdsoft.hadoop.proxy;

public class PDQueryStatusImpl implements PDQueryStatus {

	@Override
	public PDFileStatus getFileStatus(String filename) {
		PDFileStatus fs = new PDFileStatus(filename);
		System.out.println("PDQueryStatusImpl Method getFileStatus Called, return: " + fs);
		return fs;
	}

}
