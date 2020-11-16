package com.ambition.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * 在 Spring 还未完成扫描之前，可以对扫描结果进行干预
 *
 * @author Elewin
 * @date 2020-04-07 12:49 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class AmbitionBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
			throws BeansException {
		System.out.println(this.getClass().getName());
		System.out.println("Spring 扫描的 BeanDefinition 数量：" + registry.getBeanDefinitionCount());

		RootBeanDefinition definition = new RootBeanDefinition();
		registry.registerBeanDefinition("bService", definition);

	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		System.out.println(this.getClass().getName());
		System.out.println("Spring 扫描的 BeanDefinition 数量：" + beanFactory.getBeanDefinitionCount());
	}
}
