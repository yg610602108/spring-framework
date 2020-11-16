package com.ambition.factory;

import com.ambition.service.ZService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-01-07 11:06 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class AmbitionBeanFactory implements FactoryBean {

	@Override
	public Object getObject() throws Exception {
		return new ZService();
	}

	@Override
	public Class<?> getObjectType() {
		return ZService.class;
	}
}
