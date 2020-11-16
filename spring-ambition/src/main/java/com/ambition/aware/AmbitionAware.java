package com.ambition.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-04-08 6:33 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class AmbitionAware implements BeanFactoryAware {

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		System.out.println(this.getClass().getName() + "被回调了");
	}
}
