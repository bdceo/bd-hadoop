/**
 * FileDecompressor.java
 * com.bdsoft.hadoop.compress
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.compress;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;

/**
 * 通过 CompressionCodecFactory 推断 CompressionCodec
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-24
 * @version  1.0.0
 */
public class FileDecompressor {

	/**
	 * 根据文件扩展名选取 codec 解压缩文件
	 * 
	 * >% hadoop FileDecompressor file.gz
	*/
	public static void main(String[] args) throws Exception {
		String uri = args[0];
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(new URI(uri), conf);

		Path inputPath = new Path(uri);
		CompressionCodecFactory factory = new CompressionCodecFactory(conf);
		CompressionCodec codec = factory.getCodec(inputPath);
		if (codec == null) {
			System.err.println("No codec found for :" + uri);
			System.exit(1);
		}

		String outputUri = CompressionCodecFactory.removeSuffix(uri, codec.getDefaultExtension());
		InputStream in = null;
		OutputStream out = null;

		try {
			in = codec.createInputStream(fs.open(inputPath));
			out = fs.create(new Path(outputUri));

			IOUtils.copyBytes(in, out, conf);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeStream(in);
			IOUtils.closeStream(out);
		}

	}

}
