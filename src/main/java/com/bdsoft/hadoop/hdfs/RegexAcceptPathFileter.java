package com.bdsoft.hadoop.hdfs;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;


/**
 * 过滤文件
 */
public class RegexAcceptPathFileter implements PathFilter {

	final String regex;

	public RegexAcceptPathFileter(String reg) {
		this.regex = reg;
	}

	/**
	 * @return true-接受，false-不接受
	 */
	@Override
	public boolean accept(Path path) {
		return path.toString().matches(regex);
	}

}
