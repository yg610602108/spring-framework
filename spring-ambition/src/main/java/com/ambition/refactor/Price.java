package com.ambition.refactor;

/**
 * @author Elewin
 * @date 2020-03-16 3:56 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public abstract class Price {

	abstract int getPriceCode();

	/**
	 * 【第三次移动后】
	 * */
	abstract double getCharge(int daysRented); /*{
		double result = 0;
		switch (getPriceCode()) {
			case Movie.REGULAR:
				result += 2;
				if (daysRented > 2) {
					result += (daysRented - 2) * 1.5;
				}
				break;
			case Movie.NEW_RELEASE:
				result += daysRented * 3;
				break;
			case Movie.CHILDRENS:
				result += 1.5;
				if (daysRented > 3) {
					result += (daysRented - 3) * 1.5;
				}
				break;
			default:
				break;
		}

		return result;
	}*/

	int getFrequentRenterPoints(int daysRented) {
		return 1;
	}
}
