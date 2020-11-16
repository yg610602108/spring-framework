package com.ambition.study;

/**
 * @author Elewin
 * @date 2020-05-07 9:17 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class StackOverflowTest {

	private static int count = 0;

	private static void redo() {
		count++;
		redo();
	}

	/**
	 * 栈：【-Xss1M】
	 * JVM 栈帧的大小默认是 1M
	 * 值越大，可用空间一定的情况下，可分配的线程越少
	 * 值越小，说明一个线程栈里能分配的栈帧越少，但是JVM能开启更多的线程
	 *
	 * 方法区：存放类的元数据，静态变量，常量，对象的句柄等【JDK1.8+】
	 * ‐XX:=256M    元空间大小
	 * ‐XX:MaxMetaspaceSize=256  最大元空间大小
	 **/
	public static void main(String[] args) {

		try {
			redo();
		}
		catch (Throwable e) {
			// java.lang.StackOverflowError
			e.printStackTrace();
			System.out.println(count);
		}
	}

}
