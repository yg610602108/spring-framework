package com.ambition.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * 在 Spring 还未对 Bean 进行初始化之前，可以对 BeanDefinition 进行自定义修改
 *
 * @author Elewin
 * @date 2020-01-07 7:02 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class AmbitionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	/**
	 * 还没有完成扫描之前扩展 Spring 功能的话可以实现 BeanDefinitionRegistryPostProcessor
	 *
	 * 完成扫描之后扩展 Spring 功能的话可以实现 BeanFactoryPostProcessor
	 *
	 * public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
	 *
	 * BeanDefinitionRegistryPostProcessor 不仅有 BeanFactoryPostProcessor 的行为，还有自己定义的行为
	 *
	 * 这是两个不同的类，执行时机也不一样
	 *
	 * Spring 会先执行
	 * {@link BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)}
	 * 再执行
	 * {@link BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)}
	 **/

	/**
	 * 如果是自动装配，则 Spring 用的注入方式是 setter 或者 constructor
	 * 需要提供 setter 方法或者构造方法
	 * // TODO
	 * 如果是手动装配，则 Spring 用的是反射【field.set】，不需要提供 setter 方法或者构造方法
	 **/
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		// 获取 BeanDefinition
		GenericBeanDefinition beanDefinition =
				(GenericBeanDefinition) beanFactory.getBeanDefinition("yService");
		// 设置注入模型
		beanDefinition.setAutowireMode(2);
		System.out.println(beanDefinition.getAutowireMode());
	}
}
