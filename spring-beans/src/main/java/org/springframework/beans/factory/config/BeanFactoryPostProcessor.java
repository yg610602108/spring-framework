/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * 允许自定义修改应用程序上下文的 BeanDefinition，以适应上下文基础 BeanFactory 的 Bean 属性值
 * 应用程序上下文可以在其 BeanDefinition 中自动检测 BeanFactoryPostProcessor Bean，并在创建任何其他 Bean 之前应用它们
 * 对于针对系统管理员的自定义配置文件很有用，这些文件覆盖了在应用程序上下文中配置的 Bean 属性
 * 请参阅 PropertyResourceConfigurer 及其具体实现，以获取可解决此类配置需求的即用型解决方案
 * BeanFactoryPostProcessor 可以与 BeanDefinition 进行交互并对其进行修改，但不能与 Bean 实例进行交互
 * 这样做可能会导致 Bean 实例化过早，从而违反了容器并造成了意外的副作用
 * 如果需要 Bean 实例交互，请考虑改为实现 BeanPostProcessor
 *
 * Allows for custom modification of an application context's bean definitions,
 * adapting the bean property values of the context's underlying bean factory.
 *
 * <p>Application contexts can auto-detect BeanFactoryPostProcessor beans in
 * their bean definitions and apply them before any other beans get created.
 *
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context.
 *
 * <p>See PropertyResourceConfigurer and its concrete implementations
 * for out-of-the-box solutions that address such configuration needs.
 *
 * <p>A BeanFactoryPostProcessor may interact with and modify bean
 * definitions, but never bean instances. Doing so may cause premature bean
 * instantiation, violating the container and causing unintended side-effects.
 * If bean instance interaction is required, consider implementing
 * {@link BeanPostProcessor} instead.
 *
 * @author Juergen Hoeller
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {

	/**
	 * 标准初始化后，修改应用程序上下文的内部 BeanFactory
	 * 所有 BeanDefinition 都将被加载，但尚未实例化任何 Bean
	 * 这甚至可以覆盖或添加属性，甚至可以用于初始化 Bean
	 *
	 * 可以用来干预 BeanFactory 的初始化过程
	 *
	 * CGLIB 的作用点
	 *
	 * 执行时机：所有的 BeanDefinition 信息已经加载到容器中，但是 Bean 实例还没有被初始化
	 * 可以实现完成扫描之后对 Spring 功能进行扩展
	 *
	 * Spring 会先执行
	 * {@link BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)}
	 * 再执行
	 * {@link BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)}
	 * {@see org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.List)}
	 *
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * @param beanFactory the bean factory used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
