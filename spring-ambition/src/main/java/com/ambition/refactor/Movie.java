package com.ambition.refactor;

/**
 * @author Elewin
 * @date 2020-03-14 3:32 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class Movie {

	public static final int CHILDRENS = 2;
	public static final int REGULAR = 0;
	public static final int NEW_RELEASE = 1;

	private String title;

	// private int priceCode;

	Price price;

	public Movie(String title, int priceCode) {
		this.title = title;
		setPriceCode(priceCode);
	}

	public int getPriceCode() {
		return price.getPriceCode();
	}

	public void setPriceCode(int priceCode) {
		switch (priceCode) {
			case REGULAR:
				price = new RegularPrice();
				break;
			case CHILDRENS:
				price = new ChildrensPrice();
				break;
			case NEW_RELEASE:
				price = new NewReleasePrice();
				break;
			default:
				break;
		}
	}

	public String getTitle() {
		return title;
	}

	/**
	 * 【第二次移动后】
	 * */
	public double getCharge(int daysRented) {
		/*double result = 0;
		switch (getPriceCode()) {
			case REGULAR:
				result += 2;
				if (daysRented > 2) {
					result += (daysRented - 2) * 1.5;
				}
				break;
			case NEW_RELEASE:
				result += daysRented * 3;
				break;
			case CHILDRENS:
				result += 1.5;
				if (daysRented > 3) {
					result += (daysRented - 3) * 1.5;
				}
				break;
			default:
				break;
		}

		return result;*/

		return price.getCharge(daysRented);
	}

	/*int getFrequentRenterPoints(int daysRented) {

		if ((getPriceCode() == Movie.NEW_RELEASE) && daysRented > 1) {
			return 2;
		}

		return 1;
	}*/
}
