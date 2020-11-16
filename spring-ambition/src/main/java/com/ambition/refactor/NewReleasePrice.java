package com.ambition.refactor;

/**
 * @author Elewin
 * @date 2020-03-16 3:57 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class NewReleasePrice extends Price {

	@Override
	int getPriceCode() {
		return Movie.NEW_RELEASE;
	}

	@Override
	double getCharge(int daysRented) {
		return daysRented * 3;
	}

	@Override
	int getFrequentRenterPoints(int daysRented) {
		return (daysRented > 1) ? 2 : 1;
	}
}
