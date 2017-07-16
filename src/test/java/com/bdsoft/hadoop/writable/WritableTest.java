package com.bdsoft.hadoop.writable;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class WritableTest {

	public static void main(String[] args) throws Exception {
		A a = new A(22);

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(baout);

		oout.writeObject(a);
		
		System.out.println("serialize ok");
	}

}

class A implements Serializable {

	private static final long serialVersionUID = 5960130912954713943L;

	private int age;

	public A(int age) {
		this.age = age;
	}
}
