package com.ambition.refactor;

/**
 * @author Elewin
 * @date 2020-03-14 3:36 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class Rental {

	private Movie movie;

	private int daysRented;

	public Rental(Movie movie, int daysRented) {
		this.movie = movie;
		this.daysRented = daysRented;
	}

	public Movie getMovie() {
		return movie;
	}

	public int getDaysRented() {
		return daysRented;
	}

	/**
	 * 【第一次移动后】
	 *
	 * 最好不要在另一个对象的属性基础上运用 switch 语句
	 * 如果不得不使用，也应该在对象自己的数据上使用
	 * */
	/*public double getCharge(Rental rental) {
		double result = 0;
		switch (rental.getMovie().getPriceCode()) {
			case Movie.REGULAR:
				result += 2;
				if (rental.getDaysRented() > 2) {
					result += (rental.getDaysRented() - 2) * 1.5;
				}
				break;
			case Movie.NEW_RELEASE:
				result += rental.getDaysRented() * 3;
				break;
			case Movie.CHILDRENS:
				result += 1.5;
				if (rental.getDaysRented() > 3) {
					result += (rental.getDaysRented() - 3) * 1.5;
				}
				break;
			default:
				break;
		}

		return result;
	}*/

	public double getCharge() {
		return movie.getCharge(daysRented);
	}

	/**
	 * 【重构后】
	 **/
	int getFrequentRenterPoints() {

		/*if ((getMovie().getPriceCode() == Movie.NEW_RELEASE)
				&& getDaysRented() > 1) {
			return 2;
		}

		return 1;*/

		return movie.price.getFrequentRenterPoints(daysRented);
	}
}
