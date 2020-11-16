package com.ambition.registrar;

import com.ambition.processor.AmbitionBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Elewin
 * @date 2020-01-15 1:45 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class AmbitionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * 可以实现往 Spring 容器中动态注册一个类
	 **/
	@Override
	public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
										@NonNull BeanDefinitionRegistry registry) {

		BeanDefinitionBuilder builder =
				BeanDefinitionBuilder.genericBeanDefinition(AmbitionBeanPostProcessor.class);
		GenericBeanDefinition definition = (GenericBeanDefinition) builder.getBeanDefinition();
		// 设置动态注册的类
		definition.setBeanClass(AmbitionBeanPostProcessor.class);
		// 委托容器进行注册
		registry.registerBeanDefinition("ambitionBeanPostProcessor", definition);
	}
}
