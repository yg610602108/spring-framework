package com.ambition.study;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Elewin
 * @date 2020-05-22 10:06 PM
 */
public class OOMTest {

	private static User user = null;

	private static class User {

		private int id;

		private String name;

		public User(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return "User{" +
					"id=" + id +
					", name='" + name + '\'' +
					'}';
		}

	}

	public static void main(String[] args) {

		List<Object> list = new ArrayList<>();
		int i = 0;
		int j = 0;
		while (true) {
			list.add(new User(i++, UUID.randomUUID().toString()));
			new User(j--, UUID.randomUUID().toString());
		}
	}

}
