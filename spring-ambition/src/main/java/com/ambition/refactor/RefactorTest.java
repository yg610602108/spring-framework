package com.ambition.refactor;

import java.util.Arrays;

/**
 * @author Elewin
 * @date 2020-03-14 3:44 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class RefactorTest {

	/**
	 * 如果你发现自己需要为程序添加一个特性，而代码结构使你无法很方便地达成目的
	 * 那就先重构那个程序，使特性的添加比较容易进行，然后再添加特性
	 *
	 * 重构的第一步：
	 * 为即将修改的代码建立一组可靠的测试环境
	 * 好的测试是重构的根本
	 * 花时间建立一个优良的测试机制是完全值得的，因为当你修改程序时，好测试会给你必要的安全保障
	 **/
	/*public static void main(String[] args) {

		Movie movie = new Movie("Science Movie", Movie.NEW_RELEASE);

		Rental rental = new Rental(movie, 5);
BlockingQueue
		Customer customer = new Customer("YangGang");
		customer.addRental(rental);

		String statement = customer.statement();

		System.out.println(statement);
	}*/
	public static void main(String[] args) {


	}

	public static void merge(int[] nums1, int m, int[] nums2, int n) {

		int i = m - 1;
		int j = n - 1;
		int index = m + n -1;

 		while (i >= 0 && j >= 0) {
			if (nums1[i] > nums2[j]) {
				nums1[index--] = nums1[i--];
			} else {
				nums1[index--] = nums2[j--];
			}
		}

		while (j >= 0) {
			nums1[index--] = nums2[j--];
		}

		System.out.println(Arrays.toString(nums1));
	}
}
