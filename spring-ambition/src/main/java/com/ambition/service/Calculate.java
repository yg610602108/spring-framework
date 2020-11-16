package com.ambition.service;

import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-05-03 4:29 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public interface Calculate {

	int add(int numA,int numB);

	int reduce(int numA,int numB);

	int div(int numA,int numB);

	int multi(int numA,int numB);
}
