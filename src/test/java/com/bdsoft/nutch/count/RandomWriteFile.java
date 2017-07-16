package com.bdsoft.nutch.count;

import java.io.File;
import java.io.RandomAccessFile;

public class RandomWriteFile {

	public static synchronized void writeFile(String path, byte[] data) {
		if (path == null || data == null) {
			return;
		}
		if (data.length == 0) {
			return;
		}
		RandomAccessFile ranFile = null;
		try {
			ranFile = new RandomAccessFile(new File(path), "rw");
			long pos = ranFile.length();
			ranFile.seek(pos);
			ranFile.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ranFile != null) {
				try {
					ranFile.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
