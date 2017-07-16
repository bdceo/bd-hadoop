package com.bdsoft.hadoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.io.compress.Compressor;
import org.apache.hadoop.io.compress.zlib.BuiltInZlibDeflater;
import org.apache.hadoop.util.ReflectionUtils;

public class HadoopAPI {

	public static void main(String[] args) throws Exception {
		// compress("org.apache.hadoop.io.compress.DefaultCodec");
		// File file = new File("d:/download/aaa.txt.deflat");
		// decompress(file);

		String s = "hrpc";
		System.out.println(s.getBytes().length);
	}

	public static void compress(String method) throws Exception {
		File fileIn = new File("d:/download/aaa.txt");
		InputStream in = new FileInputStream(fileIn);

		Class<?> codecClass = Class.forName(method);
		Configuration conf = new Configuration();
		CompressionCodec codec = (CompressionCodec) ReflectionUtils
				.newInstance(codecClass, conf);

		File fileOut = new File("d:/download/aaa.txt"
				+ codec.getDefaultExtension());
		fileOut.delete();
		OutputStream out = new FileOutputStream(fileOut);

		CompressionOutputStream cout = codec.createOutputStream(out);
		IOUtils.copyBytes(in, cout, 4096, false);

		in.close();
		cout.close();
	}

	public static void decompress(File file) throws Exception {
		Configuration conf = new Configuration();
		CompressionCodecFactory factory = new CompressionCodecFactory(conf);

		CompressionCodec codec = factory.getCodec(new Path(file.getName()));

		File fileOut = new File(file.getName() + ".txt");
		InputStream in = codec.createInputStream(new FileInputStream(file));
		OutputStream out = new FileOutputStream(fileOut);

		IOUtils.copyBytes(in, out, 4096, false);

		in.close();
		out.close();
	}

	static final int compressorOutputBufferSize = 100;

	public static void compressor() throws Exception {
		// 读取待压缩内容到输入流
		File fileIn = new File("d:/download/aaa.txt");
		InputStream in = new FileInputStream(fileIn);
		int dataLength = in.available();
		byte[] inbuf = new byte[dataLength];
		in.read(inbuf, 0, dataLength);
		in.close();

		byte[] outbuf = new byte[compressorOutputBufferSize];

		Compressor compressor = new BuiltInZlibDeflater();

		int step = 100;
		int inputPos = 0;
		int putcount = 0;
		int getcount = 0;
		int compressedlen = 0;

		while (inputPos < dataLength) {
			int len = (dataLength - inputPos >= step) ? step
					: (dataLength - inputPos);
			compressor.setInput(inbuf, inputPos, len);
			putcount++;

			while (!compressor.needsInput()) {
				compressedlen = compressor.compress(outbuf, 0,
						compressorOutputBufferSize);
				if (compressedlen > 0) {
					getcount++;
				}
			}
			inputPos += step;
		}

		compressor.finish();

		while (!compressor.finished()) {
			getcount++;
			compressor.compress(outbuf, 0, compressorOutputBufferSize);
		}
		System.out.println("Compress " + compressor.getBytesRead()
				+ " bytes into " + compressor.getBytesWritten());
		System.out.println("put " + putcount + " times and get " + getcount
				+ " times");

		compressor.end();
	}

}
