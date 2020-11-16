package com.ambition.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author Elewin
 * @date 2020-01-03 12:55 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Service(value = "yService")
// 只有在获取 Bean 的时候才会初始化
//@Lazy
public class YService {

//	@Autowired
	private XService xService;

//	public void setxService(XService xService) {
//		this.xService = xService;
//	}

	/**
	 * 有无参构造方法，xService 注入不进来
	 * 没有无参构造方法，xService 可以注入进来
	 **/
	public YService() {
		System.out.println("yService create");
	}

	/**
	 * 这里能注入进来的原因有两个：
	 * 1、自动装配的原因
	 * 2、有且只有一个合理的构造函数
	 *
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBeanInstance
	 *
	 * Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
	 *
	 * if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
	 * 		mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
	 *     return autowireConstructor(beanName, mbd, ctors, args);
	 * }
	 *
	 * ctors = mbd.getPreferredConstructors();
	 * if (ctors != null) {
	 *     return autowireConstructor(beanName, mbd, ctors, null);
	 * }
	 *
	 * 通过看代码可以得知，这里不是自动装配的原因，是 Spring 推断出的构造函数不为空
	 * 这也说明了注解不是自动装配，只是使用了自动装配的技术
	 **/
	public YService(XService xService) {
		this.xService = xService;
		System.out.println("yService create");
	}

	public void getY() {
		System.out.println(xService);
		System.out.println("I am yService");
	}
}
