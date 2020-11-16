package com.ambition.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-01-14 5:05 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class DestroyMethodBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		BeanDefinition xService = beanFactory.getBeanDefinition("xService");
		/**
		 * 使用 AbstractBeanDefinition.INFER_METHOD 则销毁方法名必须是 close 或者 shutdown
		 **/
		xService.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
	}
}
