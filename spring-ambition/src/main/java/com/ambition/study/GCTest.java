package com.ambition.study;

/**
 * @author Elewin
 * @date 2020-05-22 8:04 PM
 */
public class GCTest {

	private static final Integer _1M = 1024 * 1024;

	/**
	 * -Xms30M -Xmx30M -XX:+PrintGCDetails -XX:PretenureSizeThreshold=1M
	 **/
	public static void main(String[] args) {

		byte[] allocation1 = new byte[5 * _1M];

		byte[] use = new byte[1 * _1M];

		int length = use.length;
	}

}
