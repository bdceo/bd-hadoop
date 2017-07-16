package com.bdsoft.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import com.bdsoft.hadoop.BaseConfig;

/**
 * 利用通配符和PathFilter对象，将本地多种格式的文件上传至 HDFS，并过滤掉 zip文本格式以外的文件
 */
public class CopyFromLocalDemo {

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		// 源文件路径，指定文件模式
		String path = "d:/home/webuser/logs/*";
		String dest = BaseConfig.HDFS_PATH + "/bdceo/logs/";
		listAndUpload(path, dest, conf);

	}

	/**
	 * 过滤原路径并上传
	 * 
	 * @param src
	 *            原路径
	 * @param dest
	 *            目标路径
	 * @param conf
	 *            默认配置
	 */
	public static void listAndUpload(String src, String dest, Configuration conf)
			throws Exception {
		FileSystem fs = JavaApi4HDFS.getFileSystem(conf);

		// 获取windows/linux本地文件系统
		LocalFileSystem lfs = FileSystem.getLocal(conf);

		// 获取本地文件目录
		FileStatus[] files = lfs.globStatus(new Path(src),
				new RegexAcceptPathFileter("^.*log$"));
		// 转换成文件路径
		Path[] paths = FileUtil.stat2Paths(files);

		// 循环遍历，准备上传
		Path out = new Path(dest);
		if (!fs.exists(out)) {
			JavaApi4HDFS.rmr(dest, conf);
		}
		for (Path p : paths) {
			fs.copyFromLocalFile(p, out);
			System.out.println("copy file to hdfs > " + p.getName());
		}
		System.out.println("copy finish!");
	}

}

