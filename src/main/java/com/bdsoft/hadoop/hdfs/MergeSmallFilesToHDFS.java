package com.bdsoft.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import com.bdsoft.hadoop.BaseConfig;

/**
 * hadoop合并小文件并上传到hdfs
 */
public class MergeSmallFilesToHDFS {

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		// hdfs
		FileSystem fs = JavaApi4HDFS.getFileSystem(conf);

		// 本地文件系统
		FileSystem lfs = FileSystem.getLocal(conf);

		// 本地小文件存放根目录
		String path = "d:/home/logs/*";
		FileStatus[] sfsDirs = lfs.globStatus(new Path(path),
				new RegexExcludePathFileter("^.*svn$"));
		Path[] sfsPaths = FileUtil.stat2Paths(sfsDirs);

		FSDataOutputStream out = null;
		FSDataInputStream in = null;

		for (Path sfsPath : sfsPaths) {
			// 将一个文件夹下的所有小文件合并成一个目录名称的大文件
			String mergeFileName = sfsPath.getName().replace("-", "");
			FileStatus[] files = lfs.globStatus(new Path(sfsPath + "/*"),
					new RegexAcceptPathFileter("^.*log$"));
			Path[] paths = FileUtil.stat2Paths(files);

			// 计划存放到hdfs上的合并输出文件
			Path mergeFile = new Path(BaseConfig.HDFS_PATH + "/bdceo/logs/"
					+ mergeFileName + ".log");
			// 打开输出流
			out = fs.create(mergeFile);
			for (Path p : paths) {
				// 打开输入流
				in = lfs.open(p);
				// 合并拷贝
				IOUtils.copyBytes(in, out, 4096, false);
				in.close();
				System.out.println(String.format("将%s合并到%s", p, mergeFile));
			}
			out.close();
		}
		System.out.println("mrge small files finish!");

	}

}