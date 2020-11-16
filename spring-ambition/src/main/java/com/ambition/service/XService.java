package com.ambition.service;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author Elewin
 * @date 2020-01-03 12:53 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
 @Description("XXX")
// @Service(value = "xService")
// @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
// @Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class XService {

 	/**
 	 * 如果销毁属性使用了 Spring 的默认常量 AbstractBeanDefinition.INFER_METHOD
	 *
	 * 则销毁方法名必须是 close 或者 shutdown
	 *
 	 **/
 	public void shutdown() {
 		System.out.println("xService close");
	}

	public XService() {
		System.out.println("xService create");
	}

	public void getX() {
		System.out.println("I am xService");
	}
}
