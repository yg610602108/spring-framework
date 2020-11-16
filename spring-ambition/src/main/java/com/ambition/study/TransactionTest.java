package com.ambition.study;

import com.ambition.config.TransactionConfig;
import com.ambition.service.PayService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Elewin
 * @date 2020-05-04 5:01 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class TransactionTest {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext();
		context.register(TransactionConfig.class);
		context.refresh();

		PayService pay = context.getBean(PayService.class);
		pay.pay();
	}
}
