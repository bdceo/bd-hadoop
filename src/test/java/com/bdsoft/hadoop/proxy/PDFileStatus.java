package com.bdsoft.hadoop.proxy;

import java.util.Date;

public class PDFileStatus {
	
	private String filename;
	private long time;

	public PDFileStatus(String filename) {
		this.filename = filename;
		this.time = (new Date()).getTime();
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "File: " + filename + " Create at " + (new Date(time));
	}
}
