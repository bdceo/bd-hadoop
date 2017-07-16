package com.bdsoft.hadoop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.ReflectionUtils;

import com.bdsoft.nutch.count.TimerC;

/**
 * Hadoop 测试压缩解压缩
 * 
 */
public class BDHadoop {

	public static String BASE_PATH = "e:/count/";

	public static String CMP_DEFAULT = "org.apache.hadoop.io.compress.DefaultCodec";
	public static String CMP_SNAPPY = "org.apache.hadoop.io.compress.SnappyCodec";

	public static void main(String[] args) throws Exception {
		Date start = new Date();
		String path = BASE_PATH + "domain_host0705";
		String opath = decompsDeflate(path);
		System.out.println("finash -> " + opath);
		TimerC.cost("解压缩", start);
	}

	public static String decompsDeflate(String path) throws Exception {
		if (!path.endsWith("deflate")) {
			path += ".deflate";
		}
		Class<?> coc = Class.forName(CMP_DEFAULT);
		CompressionCodec codec = (CompressionCodec) ReflectionUtils
				.newInstance(coc, new Configuration());
		return decompress(path, codec);
	}

	public static void compsDeflate(String path, byte[] data) throws Exception {
		Class<?> coc = Class.forName(CMP_DEFAULT);
		CompressionCodec codec = (CompressionCodec) ReflectionUtils
				.newInstance(coc, new Configuration());
		compress(data, path, codec);
	}

	private static void compress(byte[] data, String path,
			CompressionCodec codec) {
		CompressionOutputStream out = null;
		try {
			out = codec
					.createOutputStream(new FileOutputStream(new File(path)));
			out.write(data);
			out.finish();
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	private static String decompress(String path, CompressionCodec codec) {
		String opath = path.replaceAll("deflate", "txt");
		byte[] data = new byte[1024];
		InputStream in = null;
		BufferedInputStream bfin = null;
		BufferedOutputStream bfout = null;
		int i = 1;
		try {
			in = codec.createInputStream(new FileInputStream(new File(path)));
			bfin = new BufferedInputStream(in);
			bfout = new BufferedOutputStream(new FileOutputStream(opath));
			while (bfin.read(data) != -1) {
				i++;
				// System.out.println(new String(data));
				System.out.print(i + "-");
				bfout.write(data);
				if ((i % 100) == 0) {
					System.out.println();
					// break;
				}
			}
			bfout.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				bfin.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			try {
				bfout.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return opath;
	}
}
