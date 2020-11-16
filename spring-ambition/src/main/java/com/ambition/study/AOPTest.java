package com.ambition.study;

import com.ambition.config.AopConfig;
import com.ambition.service.Calculate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Elewin
 * @date 2020-02-01 4:19 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class AOPTest {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AopConfig.class);
		context.refresh();

		Calculate calculate = context.getBean(Calculate.class);

		int result = calculate.add(1, 2);
		System.out.println("运算结果：" + result);
	}

}
