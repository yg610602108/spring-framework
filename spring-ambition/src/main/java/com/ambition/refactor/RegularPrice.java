package com.ambition.refactor;

/**
 * @author Elewin
 * @date 2020-03-16 3:58 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class RegularPrice extends Price {

	@Override
	int getPriceCode() {
		return Movie.REGULAR;
	}

	@Override
	double getCharge(int daysRented) {

		double result = 2;

		if (daysRented > 2) {
			result  += (daysRented - 2) * 1.5;
		}

		return result;
	}
}
