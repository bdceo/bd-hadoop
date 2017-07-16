/**
 * PolledStreamCompressor.java
 * com.bdsoft.hadoop.compress
 * Copyright (c) 2016, 北京微课创景教育科技有限公司版权所有.
*/

package com.bdsoft.hadoop.compress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CodecPool;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.io.compress.Compressor;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * 如果使用的是原生代码库并且需要再应用中执行大量压缩和解压缩操作，可以考虑使用 CodecPool，它支持反复使用压缩和解压缩，以分摊创建这些对象的开销。
 * <p>
 *
 * @author   丁辰叶
 * @date	 2016-2-24
 * @version  1.0.0
 */
public class PolledStreamCompressor {

	/**
	 * 使用压缩池对读取自标准输入的数据进行压缩，然后将其写到标准输出
	*/
	public static void main(String[] args) throws Exception {

		String codecClassName = args[0];
		Class<?> codecClass = Class.forName(codecClassName);

		Configuration conf = new Configuration();
		CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
		Compressor compressor = null;

		try {
			compressor = CodecPool.getCompressor(codec);
			CompressionOutputStream out = codec.createOutputStream(System.out, compressor);
			IOUtils.copyBytes(System.in, out, conf);
			out.finish();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CodecPool.returnCompressor(compressor);
		}

	}

}
