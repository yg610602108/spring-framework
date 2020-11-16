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

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.function.Supplier;

/**
 * Standalone application context, accepting annotated classes as input - in particular
 * {@link Configuration @Configuration}-annotated classes, but also plain
 * {@link org.springframework.stereotype.Component @Component} types and JSR-330 compliant
 * classes using {@code javax.inject} annotations. Allows for registering classes one by
 * one using {@link #register(Class...)} as well as for classpath scanning using
 * {@link #scan(String...)}.
 *
 * <p>In case of multiple {@code @Configuration} classes, @{@link Bean} methods defined in
 * later classes will override those defined in earlier classes. This can be leveraged to
 * deliberately override certain bean definitions via an extra {@code @Configuration}
 * class.
 *
 * <p>See @{@link Configuration}'s javadoc for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see #register
 * @see #scan
 * @see AnnotatedBeanDefinitionReader
 * @see ClassPathBeanDefinitionScanner
 * @see org.springframework.context.support.GenericXmlApplicationContext
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	/**
	 * 定义一个读取注解的 BeanDefinition 读取器
	 * 这个类在构造方法中被实例化
	 */
	private final AnnotatedBeanDefinitionReader reader;

	/**
	 * 定义一个扫描类路径下加了注解的 BeanDefinition 扫描器
	 * 这个类在构造方法中被实例化
	 */
	private final ClassPathBeanDefinitionScanner scanner;


	/**
	 * Create a new AnnotationConfigApplicationContext that needs to be populated
	 * through {@link #register} calls and then manually {@linkplain #refresh refreshed}.
	 */
	public AnnotationConfigApplicationContext() {
		/**
		 * 实例化 BeanDefinition 读取器
		 *
		 * 将 Spring 内部的注解后置处理器组件注册到 BeanDefinitionRegistry 中
		 * 最重要的一个后置处理器 ConfigurationClassPostProcessor
		 * BeanName 是 internalConfigurationAnnotationProcessor
		 *
		 * 为什么不直接实例化一个 BeanDefinition，而是实例化一个 AnnotatedBeanDefinitionReader
		 * 封装了一层，首先可以提供一个手动注册的 API 给外部调用
		 * 调用 {@link AnnotatedBeanDefinitionReader#register(Class[])} 方法会实例化一个 AnnotatedGenericBeanDefinition
		 *
		 * 最重要的是配置类没有办法扫描自己，而手动注册不是 Spring 的设计初衷
		 *
		 * 这行代码执行完，BeanFactory 的 beanDefinitionMap 中就会加载 Spring 内置的辅助类
		 * 分别是：
		 * 1、org.springframework.context.annotation.internalConfigurationAnnotationProcessor
		 * ---> ConfigurationClassPostProcessor
		 *
		 * 2、org.springframework.context.event.internalEventListenerFactory
		 * ---> DefaultEventListenerFactory
		 *
		 * 3、org.springframework.context.event.internalEventListenerProcessor
		 * ---> EventListenerMethodProcessor
		 *
		 * 4、org.springframework.context.annotation.internalAutowiredAnnotationProcessor
		 * ---> AutowiredAnnotationBeanPostProcessor
		 *
		 * 5、org.springframework.context.annotation.internalCommonAnnotationProcessor
		 * ---> CommonAnnotationBeanPostProcessor
		 *
		 * 如果支持 JPA，则还有一个
		 * 6、org.springframework.context.annotation.internalPersistenceAnnotationProcessor
		 * ---> PersistenceAnnotationBeanPostProcessor
		 *
		 * 其中最重要的一个是
		 * internalConfigurationAnnotationProcessor -> ConfigurationClassPostProcessor
		 * Spring 在后面需要用到这个类进行辅助初始化，所以这里会先实例化出来
		 *
		 * {@link AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)}
		 */
		this.reader = new AnnotatedBeanDefinitionReader(this);

		/**
		 * 实例化 BeanDefinition 扫描器
		 *
		 * 指定扫描策略
		 *
		 * 但是实际上扫描包的工作并不是这里的 scanner 对象来完成的
		 * 是后面 Spring 又创建的一个新的 ClassPathBeanDefinitionScanner 来完成的
		 * 这里的 scanner 仅仅是为了让程序员能够在外部调用 AnnotationConfigApplicationContext 对象的 scanner 方法
		 * {@see org.springframework.context.annotation.ComponentScanAnnotationParser#parse(org.springframework.core.annotation.AnnotationAttributes, java.lang.String)}
		 *
		 * 【重要】一般供扩展 Spring 扫描功能使用，Spring 内部没有使用这个对象
		 * 例如：MyBatis 就是利用这个扩展了自定义的注解，来实现扫描 @Mapper 注解
		 **/
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * Create a new AnnotationConfigApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		/**
		 * 调用父类的构造方法
		 *
		 * 初始化 ClassLoader 类加载器
		 * 初始化 DefaultListableBeanFactory 的 Bean 工厂
		 **/
		super(beanFactory);

		// 实例化该读取器
		this.reader = new AnnotatedBeanDefinitionReader(this);
		// 实例化该扫描器
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}

	/**
	 * 这个构造方法需要传入一个 @Configuration 注解配置类
	 * 通过注解读取器读取并解析
	 *
	 * Create a new AnnotationConfigApplicationContext, deriving bean definitions
	 * from the given annotated classes and automatically refreshing the context.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link Configuration @Configuration} classes
	 */
	public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
		/**
		 * 由于继承了父类，这里会先去调用父类的构造方法，然后调用自身的构造方法
		 *
		 * this.classLoader = ClassUtils.getDefaultClassLoader();
		 * this.beanFactory = new DefaultListableBeanFactory();
		 *
		 * 其实就是初始化一个 DefaultListableBeanFactory 和 ClassLoader
		 */
		this();

		/**
		 * 将传入的 @Configuration 配置类转换为 BeanDefinition
		 * 并添加到 DefaultListableBeanFactory 工厂的 BeanDefinitionMap 中
		 */
		register(annotatedClasses);

		refresh();
	}

	/**
	 * Create a new AnnotationConfigApplicationContext, scanning for bean definitions
	 * in the given packages and automatically refreshing the context.
	 * @param basePackages the packages to check for annotated classes
	 */
	public AnnotationConfigApplicationContext(String... basePackages) {
		/**
		 * 由于继承了父类，这里会先去调用父类的构造方法，然后调用自身的构造方法
		 *
		 * this.classLoader = ClassUtils.getDefaultClassLoader();
		 * this.beanFactory = new DefaultListableBeanFactory();
		 *
		 * 其实就是初始化一个 DefaultListableBeanFactory 和 ClassLoader
		 */
		this();

		/**
		 * 扫描
		 **/
		scan(basePackages);

		refresh();
	}


	/**
	 * Propagates the given custom {@code Environment} to the underlying
	 * {@link AnnotatedBeanDefinitionReader} and {@link ClassPathBeanDefinitionScanner}.
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}

	/**
	 * Provide a custom {@link BeanNameGenerator} for use with {@link AnnotatedBeanDefinitionReader}
	 * and/or {@link ClassPathBeanDefinitionScanner}, if any.
	 * <p>Default is {@link org.springframework.context.annotation.AnnotationBeanNameGenerator}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
	 * @see ClassPathBeanDefinitionScanner#setBeanNameGenerator
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}

	/**
	 * Set the {@link ScopeMetadataResolver} to use for detected bean classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
	 * and/or {@link #scan(String...)}.
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry
	//---------------------------------------------------------------------

	/**
	 * Register one or more annotated classes to be processed.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link Configuration @Configuration} classes
	 * @see #scan(String...)
	 * @see #refresh()
	 */
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		this.reader.register(annotatedClasses);
	}

	/**
	 * Perform a scan within the specified base packages.
	 * <p>Note that {@link #refresh()} must be called in order for the context
	 * to fully process the new classes.
	 * @param basePackages the packages to check for annotated classes
	 * @see #register(Class...)
	 * @see #refresh()
	 */
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		this.scanner.scan(basePackages);
	}


	//---------------------------------------------------------------------
	// Adapt superclass registerBean calls to AnnotatedBeanDefinitionReader
	//---------------------------------------------------------------------

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
								 @Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		this.reader.registerBean(beanClass, beanName, supplier, customizers);
	}

}
