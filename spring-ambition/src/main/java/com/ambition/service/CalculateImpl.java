package com.ambition.service;

import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-05-03 4:33 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class CalculateImpl implements Calculate {

	@Override
	public int add(int numA, int numB) {
		// System.out.println(1 / 0);
		return numA + numB;
	}

	@Override
	public int reduce(int numA, int numB) {
		return numA - numB;
	}

	@Override
	public int div(int numA, int numB) {
		return numA / numB;
	}

	@Override
	public int multi(int numA, int numB) {
		return numA * numB;
	}

}
