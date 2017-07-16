package com.bdsoft.hadoop.hia;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.BitSet;
import java.util.Random;

import org.apache.hadoop.io.Writable;

// Bloom Filter java版实现，依赖于BitSet和子实现的hash算法
public class BloomFilter<E> implements Writable {

	private BitSet bf;
	private int bitArraySize = 100000000;
	private int numHashFunc = 6;

	public BloomFilter() {
		bf = new BitSet(bitArraySize);
	}

	public void add(E obj) {
		int[] indexes = getHashIndexes(obj);
		for (int index : indexes) {
			bf.set(index);
		}
	}

	public boolean contains(E obj) {
		int[] indexes = getHashIndexes(obj);
		for (int index : indexes) {
			if (bf.get(index) == false) {
				return false;
			}
		}
		return true;
	}

	public int[] getHashIndexes(E obj) {
		int[] indexes = new int[numHashFunc];

		long seed = 0;
		byte[] digest;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(obj.toString().getBytes());
			digest = md.digest();

			for (int i = 0; i < 6; i++) {
				seed = seed ^ (((long) digest[i] & 0xff)) << (8 * i);
			}

			Random gen = new Random(seed);
			for (int i = 0; i < numHashFunc; i++) {
				indexes[i] = gen.nextInt(bitArraySize);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return indexes;
	}

	// 合并两个过滤器
	public void union(BloomFilter<E> other) {
		bf.or(other.bf);
	}

	public void write(DataOutput out) throws IOException {
		int byteArraySize = (int) (bitArraySize / 8);
		byte[] byteArray = new byte[byteArraySize];
		for (int i = 0; i < byteArraySize; i++) {
			byte nextElement = 0;
			for (int j = 0; j < 8; j++) {
				if (bf.get(8 * i + j)) {
					nextElement |= 1 << j; // 或等于，按位计算，得到一个字节，用于输出
				}
			}
			byteArray[i] = nextElement;
		}
		out.write(byteArray);
	}

	public void readFields(DataInput in) throws IOException {
		int byteArraySize = (int) (bitArraySize / 8);
		byte[] byteArray = new byte[byteArraySize];

		in.readFully(byteArray);

		for (int i = 0; i < byteArraySize; i++) {
			byte nextByte = byteArray[i];
			for (int j = 0; j < 8; j++) {
				if (((int) nextByte & (1 << j)) != 0) {
					bf.set(8 * i + j);
				}
			}
		}

	}

}
