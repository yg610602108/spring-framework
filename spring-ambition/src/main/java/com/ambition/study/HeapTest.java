package com.ambition.study;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Elewin
 * @date 2020-05-07 10:20 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class HeapTest {

	byte[] buffer = new byte[1024 * 100];

	private static class Person {

		private String name;

		private int age;

	}

	/**
	 * 堆：存放对象
	 * ‐Xms2048M  堆的初始大小
	 * ‐Xmx2048M  堆的最大大小
	 * ‐Xmn1024M  新生代的大小
	 *
	 * 默认老年代会分配2/3，新生代会分配1/3
	 * 新生代中，Eden、From Survivor和To Survivor的分配比例为8:1:1
	 **/
	public static void main(String[] args) throws InterruptedException {

		List<Person> list = new ArrayList<>();

		while (Boolean.TRUE) {
			list.add(new Person());
			Thread.sleep(1);
		}
	}

}
