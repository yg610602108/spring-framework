package com.ambition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-01-29 1:09 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component(value = "mService")
//@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
public class MService {

//	@Autowired
	private NService nService;

	public MService() {
		System.out.println("mService create nService : " + nService);
	}
}
