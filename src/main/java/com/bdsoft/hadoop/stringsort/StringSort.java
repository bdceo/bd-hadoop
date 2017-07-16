package com.bdsoft.hadoop.stringsort;

import java.util.Arrays;

public class StringSort {

	public static void main(String[] args) {
		/*
		 * a b abc def xyz silent enlist listen tinsel inlets hello world hi
		 * parth end
		 */
		String[] arr = { "a", "b", "abc", "def", "xyz", "silent", "enlist",
				"listen", "tinsel", "inlets", "hello", "world", "hi", "parth",
				"end" };

		Arrays.sort(arr);

		for (String str : arr) {
			System.out.print(str + "\t");
		}
	}
}
