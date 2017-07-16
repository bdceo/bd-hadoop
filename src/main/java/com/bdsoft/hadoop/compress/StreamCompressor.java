/**
 * StreamCompressor.java
 * com.bdsoft.hadoop.compress
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.compress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * 通过 CompressionCodec 对数据流进行压缩和解压缩
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-24
 * @version  1.0.0
 */
public class StreamCompressor {

	/**
	 * 压缩从标准输入读取的数据，然后将其写到标准输出
	 * <p>
	 * >% echo "Text" | hadoop StreamCompressor org.apache.hadoop.io.compress.GzipCodec | gunzip
	 * >Text
	 */
	public static void main(String[] args) throws Exception {
		String codecClassName = args[0];

		Class<?> codecClass = Class.forName(codecClassName);

		Configuration conf = new Configuration();

		CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);

		CompressionOutputStream out = codec.createOutputStream(System.out);

		IOUtils.copyBytes(System.in, out, conf, false);

		out.flush();
	}

}
