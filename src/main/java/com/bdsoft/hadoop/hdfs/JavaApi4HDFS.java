package com.bdsoft.hadoop.hdfs;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;

import com.bdsoft.hadoop.BaseConfig;

/**
 * hadoop提供的java-api操作hdfs
 */
public class JavaApi4HDFS {

	/**
	 * 测试入口
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		getFileSystem(conf);

		String path = BaseConfig.HDFS_PATH + "/testapi";
		mkdir(path, conf);
		rmr(path, conf);

		path = BaseConfig.HDFS_PATH + "/bdceo";
		listAllFiles(path, conf);

		path = "d:/home/amerge.txt";
		String dest = BaseConfig.HDFS_PATH + "/bdceo/";
		copyToHDFS(path, dest, conf);

		path = BaseConfig.HDFS_PATH + "/bdceo/wordcount/words.txt";
		dest = "d:/home/";
		getFile(path, dest, conf);

		getHDFSNodes(conf);

		getFileLocat(path, conf);
	}

	/**
	 * 获取文件块位置信息
	 * 
	 * @param path
	 *            文件路径
	 * @param conf
	 *            默认配置
	 */
	public static void getFileLocat(String path, Configuration conf)
			throws Exception {
		FileSystem fs = getFileSystem(conf);

		// 文件路径
		Path p = new Path(path);

		// 文件详情
		FileStatus fstatus = fs.getFileStatus(p);
		
		// 文件块信息
		BlockLocation[] locats = fs.getFileBlockLocations(fstatus, 0,
				fstatus.getLen());

		for (int i = 0; i < locats.length; i++) {
			String msg = String.format("block_%d_location:%s", i,
					locats[i].getHosts()[0]);
			System.out.println(msg);
		}

		fs.close();
	}

	/**
	 * 获取hdfs集群节点信息
	 */
	public static void getHDFSNodes(Configuration conf) throws Exception {
		FileSystem fs = getFileSystem(conf);

		// 获取分布式文件系统
		DistributedFileSystem dfs = (DistributedFileSystem) fs;

		// 获取所有节点
		DatanodeInfo[] dns = dfs.getDataNodeStats();
		for (int i = 0; i < dns.length; i++) {
			String msg = String.format("DataNode_%d_Name:%s", i,
					dns[i].getHostName());
			System.out.println(msg);
		}

		fs.close();
	}

	/**
	 * 下载文件
	 * 
	 * @param src
	 *            hdfs源文件路径
	 * @param dest
	 *            下载粗存放路径，windows的绝对路径
	 * @param conf
	 *            默认配置
	 */
	public static void getFile(String src, String dest, Configuration conf)
			throws Exception {
		FileSystem fs = getFileSystem(conf);

		// hdfs源文件路径
		Path sp = new Path(src);
		// windows绝对路径
		Path dp = new Path(dest);

		fs.copyToLocalFile(false, sp, dp);

		fs.close();
		System.out.println("get hdfs file to local ok.");
	}

	/**
	 * 上传文件
	 * 
	 * @param src
	 *            源文件路径：windows的绝对路径
	 * @param dest
	 *            hdfs目标目录
	 * @param conf
	 *            默认配置
	 */
	public static void copyToHDFS(String src, String dest, Configuration conf)
			throws Exception {
		FileSystem fs = getFileSystem(conf);

		// 源文件，如果是windows需要时绝对磁盘路径 d:/home/xxx
		Path sp = new Path(src);

		// 目标目录，必须是hdfs
		Path dp = new Path(dest);

		fs.copyFromLocalFile(false, true, sp, dp);

		fs.close();
		System.out.println("copy local file to hdfs ok.");
	}

	/**
	 * 获取目录下所有文件
	 * 
	 * @param path
	 *            目录
	 * @param conf
	 *            默认配置
	 */
	public static void listAllFiles(String path, Configuration conf)
			throws Exception {
		FileSystem fs = getFileSystem(conf);

		// 列出目录内容
		FileStatus[] status = fs.listStatus(new Path(path));

		// 获取目录所有文件路径信息
		Path[] paths = FileUtil.stat2Paths(status);

		for (Path p : paths) {
			System.out.println(p);
		}

		fs.close();
	}

	/**
	 * 删除目录或文件
	 * 
	 * @param path
	 *            目录
	 * @param conf
	 *            默认配置
	 */
	public static void rmr(String path, Configuration conf) throws Exception {
		FileSystem fs = getFileSystem(conf);

		// fs.delete(new Path(path)); 方法已过期
		fs.delete(new Path(path), true);

		fs.close();
	}

	/**
	 * 创建目录
	 * 
	 * @param path
	 *            绝对目录
	 * @param conf
	 *            默认配置
	 */
	public static void mkdir(String path, Configuration conf) throws Exception {
		FileSystem fs = getFileSystem(conf);

		fs.mkdirs(new Path(path));

		fs.close();
	}

	/**
	 * 获取文件系统
	 */
	public static FileSystem getFileSystem(Configuration conf) throws Exception {
		// 如果放到 hadoop 集群上面运行，获取文件系统可以直接使用 FileSystem.get(conf)
		FileSystem fs = FileSystem.get(conf);
		// System.out.println(fs.getName());

		// 如果本地测试，需要使用此种方式获取文件系统， 指定的文件系统地址
		URI uri = new URI(BaseConfig.HDFS_PATH);
		FileSystem hdfs = FileSystem.get(uri, conf);
		// System.out.println(hdfs.getName());

		return hdfs;
	}

}
