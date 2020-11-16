package com.ambition.refactor;

/**
 * @author Elewin
 * @date 2020-03-16 3:56 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class ChildrensPrice extends Price {

	@Override
	int getPriceCode() {
		return Movie.CHILDRENS;
	}

	@Override
	double getCharge(int daysRented) {

		double result = 1.5;

		if (daysRented > 3) {
			result += (daysRented - 3) * 1.5;
		}

		return result;
	}
}
