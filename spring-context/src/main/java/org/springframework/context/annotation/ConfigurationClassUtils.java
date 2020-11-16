/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for identifying {@link Configuration} classes.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
abstract class ConfigurationClassUtils {

	public static final String CONFIGURATION_CLASS_FULL = "full";

	public static final String CONFIGURATION_CLASS_LITE = "lite";

	public static final String CONFIGURATION_CLASS_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "configurationClass");

	private static final String ORDER_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "order");

	private static final Log logger = LogFactory.getLog(ConfigurationClassUtils.class);

	private static final Set<String> candidateIndicators = new HashSet<>(8);

	static {
		candidateIndicators.add(Component.class.getName());
		candidateIndicators.add(ComponentScan.class.getName());
		candidateIndicators.add(Import.class.getName());
		candidateIndicators.add(ImportResource.class.getName());
	}

	/**
	 * Spring 如何判定一个 BeanDefinition 是一个基于配置类的 BeanDefinition？
	 *
	 * 判断 BeanDefinition 的类型，类型是固定的，由 Spring 的 API 写死的
	 * 当我们传入一个配置类时，Spring 直接实例化了一个 AnnotatedGenericBeanDefinition
	 * {@link AnnotatedBeanDefinitionReader#register(Class[])}
	 * class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition
	 *
	 *
	 * 检查给定的 BeanDefinition 是否是一个候选配置类
	 * 或在配置/组件类中声明的嵌套组件类，也要自动注册，并进行相应标记
	 *
	 * Check whether the given bean definition is a candidate for a configuration class
	 * (or a nested component class declared within a configuration/component class,
	 * to be auto-registered as well), and mark it accordingly.
	 * @param beanDef the bean definition to check
	 * @param metadataReaderFactory the current factory in use by the caller
	 * @return whether the candidate qualifies as (any kind of) configuration class
	 */
	public static boolean checkConfigurationClassCandidate(BeanDefinition beanDef,
														   MetadataReaderFactory metadataReaderFactory) {

		String className = beanDef.getBeanClassName();
		if (className == null || beanDef.getFactoryMethodName() != null) {
			return false;
		}

		// 获取 BeanDefinition 的注解元数据
		AnnotationMetadata metadata;
		/**
		 * 判断是否是 AnnotatedBeanDefinition 的子类并且类的全类名一致
		 * 即不能是一个内部类或者导入的类？
		 *
		 * AnnotatedGenericBeanDefinition 和 ScannedGenericBeanDefinition 都是 AnnotatedBeanDefinition 的子类
		 * 这意味着被注解标注的和被扫描到的都可能是一个候选配置类
		 **/
		if (beanDef instanceof AnnotatedBeanDefinition
				&& className.equals(((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
			// Can reuse the pre-parsed metadata from the given BeanDefinition...
			// 复用来自给定 BeanDefinition 的预先解析的元数据
			metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
		}
		/**
		 * 是 AbstractBeanDefinition 的子类并且指定 Bean 类
		 **/
		else if (beanDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
			/**
			 * Check already loaded Class if present...
			 * since we possibly can't even load the class file for this Class.
			 *
			 * 检查已经加载的类（如果存在）
			 * 因为我们甚至无法加载该类的类文件
			 **/
			Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
			if (BeanFactoryPostProcessor.class.isAssignableFrom(beanClass)
					|| BeanPostProcessor.class.isAssignableFrom(beanClass)
					|| AopInfrastructureBean.class.isAssignableFrom(beanClass)
					|| EventListenerFactory.class.isAssignableFrom(beanClass)) {
				return false;
			}
			metadata = AnnotationMetadata.introspect(beanClass);
		}
		/**
		 * 既不是 AnnotatedBeanDefinition 的子类也不是 AbstractBeanDefinition 的子类
		 **/
		else {
			try {
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
				metadata = metadataReader.getAnnotationMetadata();
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not find class file for introspecting configuration annotations: " +
							className, ex);
				}
				return false;
			}
		}

		Map<String, Object> config = metadata.getAnnotationAttributes(Configuration.class.getName());
		/**
		 * 校验 proxyBeanMethods 属性值是否满足
		 **/
		if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
			/**
			 * 设置 configurationClass 属性值为 full
			 *
			 * 这一步很重要
			 **/
			beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
		}
		/**
		 * 校验配置应该具有的特征
		 *
		 * 不是接口
		 * 加了 @Component @ComponentScan @Import @ImportResource 注解
		 * 有被 @Bean 注解标注的方法
		 **/
		else if (config != null || isConfigurationCandidate(metadata)) {
			/**
			 * 设置 configurationClass 属性值为 lite
			 *
			 * 这一步很重要
			 **/
			beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
		}
		else {
			return false;
		}

		// It's a full or lite configuration candidate... Let's determine the order value, if any.
		/**
		 * 获取 @Order 注解值
		 **/
		Integer order = getOrder(metadata);
		if (order != null) {
			// 设置 order 属性值为注解值
			beanDef.setAttribute(ORDER_ATTRIBUTE, order);
		}

		return true;
	}

	/**
	 * 检查给定的元数据中是否有配置类候选对象
	 * 或在配置/组件类中声明的嵌套组件类
	 *
	 * Check the given metadata for a configuration class candidate
	 * (or nested component class declared within a configuration/component class).
	 * @param metadata the metadata of the annotated class
	 * @return {@code true} if the given class is to be registered for
	 * configuration class processing; {@code false} otherwise
	 */
	public static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
		// Do not consider an interface or an annotation...
		// 是接口
		if (metadata.isInterface()) {
			return false;
		}

		// Any of the typical annotations found?
		/**
		 * 是否能找到任何典型的注解
		 * @Component
		 * @ComponentScan
		 * @Import
		 * @ImportResource
		 **/
		for (String indicator : candidateIndicators) {
			if (metadata.isAnnotated(indicator)) {
				return true;
			}
		}

		// Finally, let's look for @Bean methods...
		/**
		 * 最后寻找被 @Bean 注解标注的方法
		 **/
		try {
			return metadata.hasAnnotatedMethods(Bean.class.getName());
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to introspect @Bean methods on class [" + metadata.getClassName() + "]: " + ex);
			}
			return false;
		}
	}

	/**
	 * Determine the order for the given configuration class metadata.
	 * @param metadata the metadata of the annotated class
	 * @return the {@code @Order} annotation value on the configuration class,
	 * or {@code Ordered.LOWEST_PRECEDENCE} if none declared
	 * @since 5.0
	 */
	@Nullable
	public static Integer getOrder(AnnotationMetadata metadata) {
		Map<String, Object> orderAttributes = metadata.getAnnotationAttributes(Order.class.getName());
		return (orderAttributes != null ? ((Integer) orderAttributes.get(AnnotationUtils.VALUE)) : null);
	}

	/**
	 * Determine the order for the given configuration class bean definition,
	 * as set by {@link #checkConfigurationClassCandidate}.
	 * @param beanDef the bean definition to check
	 * @return the {@link Order @Order} annotation value on the configuration class,
	 * or {@link Ordered#LOWEST_PRECEDENCE} if none declared
	 * @since 4.2
	 */
	public static int getOrder(BeanDefinition beanDef) {
		Integer order = (Integer) beanDef.getAttribute(ORDER_ATTRIBUTE);
		return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
	}

}
