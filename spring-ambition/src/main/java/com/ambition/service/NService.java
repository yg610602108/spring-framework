package com.ambition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-01-29 1:10 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component(value = "nService")
//@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
public class NService {

//	@Autowired
	private MService mService;

	public NService() {
		System.out.println("nService create mService : " + mService);
	}
}
