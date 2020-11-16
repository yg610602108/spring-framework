package com.ambition.service;

import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Elewin
 * @date 2020-05-04 6:36 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Service
public class PayServiceImpl implements PayService {

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void pay() {

		System.out.println("准备支付");

		((PayService) AopContext.currentProxy()).deduction();

		System.out.println(1 / 0);

		System.out.println("支付完成");
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	@Override
	public void deduction() {

		System.out.println("准备扣减");

		//System.out.println(1 / 0);

		System.out.println("扣减完成");
	}

}
