package com.ambition.study;

/**
 * @author Elewin
 * @date 2020-01-27 9:14 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class BaseTest {

	static class Super{

		void print(){
			System.out.println("我是父类");
		}
	}

	static class Sub extends Super{

		void eat() {
			System.out.println("吃东西");
		}
	}

	public static void main(String[] args) {
		Super sub = new Sub();
		sub.print();
	}
}
