package com.ambition.refactor;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Elewin
 * @date 2020-03-14 3:38 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class Customer {

	private String name;

	private Vector<Rental> rentals = new Vector<Rental>();

	public Customer(String name) {
		this.name = name;
	}

	public void addRental(Rental rental) {
		rentals.addElement(rental);
	}

	public String getName() {
		return name;
	}

	/**
	 * 代码块越小，代码的功能就愈容易管理，代码的处理和移动也就越轻松
	 *
	 * 找出函数内的局部变量和参数
	 * 不会被修改的变量可以当成参数传入新的函数
	 * 会被修改的变量可以当作方法的返回值
	 **/
	public String statement() {

		/**
		 * 优化临时变量
		 **/
		// double totalAmount = 0;
		// int frequentRenterPoints = 0;

		Enumeration _rentals = rentals.elements();

		String result = "Rental Record for " + getName() + "\n";

		while (_rentals.hasMoreElements()) {
			// double thisAmount;
			Rental each = (Rental) _rentals.nextElement();

			/**
			 * 【重构前】switch 语句提炼到独立函数中比较好
			 **/
			/*switch (each.getMovie().getPriceCode()) {
				case Movie.REGULAR:
					thisAmount += 2;
					if (each.getDaysRented() > 2) {
						thisAmount += (each.getDaysRented() - 2) * 1.5;
					}
					break;
				case Movie.NEW_RELEASE:
					thisAmount += each.getDaysRented() * 3;
					break;
				case Movie.CHILDRENS:
					thisAmount += 1.5;
					if (each.getDaysRented() > 3) {
						thisAmount += (each.getDaysRented() - 3) * 1.5;
					}
					break;
				default:
					break;
			}*/
			/**
			 * 【重构后】
			 **/
			// thisAmount = amountFor(each);
			/**
			 * 【移动后】
			 **/
			// thisAmount = each.getCharge();

			/**
			 * 【重构前】
			 **/
			/*frequentRenterPoints++;
			if ((each.getMovie().getPriceCode() == Movie.NEW_RELEASE) && each.getDaysRented() > 1) {
				frequentRenterPoints++;
			}*/
			/**
			 * 【重构后】
			 **/
			// frequentRenterPoints += each.getFrequentRenterPoints();

			result += "\t" + each.getMovie().getTitle() + "\t"
					+ String.valueOf(each.getMovie().getCharge(each.getDaysRented())) + "\n";
			// totalAmount += thisAmount;
		}

		result += "Amount owed is " + String.valueOf(getTotalCharge()) + "\n";
		result += "You earned " + String.valueOf(getFrequentRenterPoints()) + " frequent renter points";

		return result;
	}

	/**
	 * 【移动前】这个函数使用了 Rental 类的信息，却没有使用 Customer 类的信息
	 * 绝大多数情况下，函数应该放在它所使用的数据的所属对象内
	 *
	 * 好的代码应该清楚表达出自己的功能，变量名称是代码清晰的关键
	 **/
	/*private double amountFor(Rental rental) {
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

	/**
	 * 通过一个中间函数来避免修改对原有程序的影响
	 **/
	private double amountFor(Rental rental) {
		return rental.getMovie().getCharge(rental.getDaysRented());
	}

	private double getTotalCharge() {

		double result = 0;

		Enumeration<Rental> rental = rentals.elements();
		while (rental.hasMoreElements()) {
			Rental element = rental.nextElement();
			result += element.getMovie().getCharge(element.getDaysRented());
		}

		return result;
	}

	private int getFrequentRenterPoints () {

		int result = 0;

		Enumeration<Rental> rental = rentals.elements();
		while (rental.hasMoreElements()) {
			Rental element = rental.nextElement();
			result += element.getFrequentRenterPoints();
		}

		return result;
	}
}
