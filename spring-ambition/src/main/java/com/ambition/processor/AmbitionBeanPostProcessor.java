package com.ambition.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor 后置处理器在 Bean 初始化之前执行
 *
 * 通过实现此接口，可插手 Bean 实例化的过程
 * Spring 会将此接口的实现类加入到 List<BeanPostProcessor> 集合中
 *
 * 最重要的类【动态注册类】
 * ImportBeanDefinitionRegistrar
 * ImportSelector
 *
 * @author Elewin
 * @date 2020-04-16 9:19 PM
 * @version 1.0
 */
public class AmbitionBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {

		System.out.println("ambitionBeanPostProcessor create");
		return null;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return null;
	}

}
