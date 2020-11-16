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

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * Convenient adapter for programmatic registration of annotated bean classes.
 * This is an alternative to {@link ClassPathBeanDefinitionScanner}, applying
 * the same resolution of annotations but for explicitly registered classes only.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 3.0
 * @see AnnotationConfigApplicationContext#register
 */
public class AnnotatedBeanDefinitionReader {

	private final BeanDefinitionRegistry registry;

	private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private ConditionEvaluator conditionEvaluator;


	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry.
	 * If the registry is {@link EnvironmentCapable}, e.g. is an {@code ApplicationContext},
	 * the {@link Environment} will be inherited, otherwise a new
	 * {@link StandardEnvironment} will be created and used.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @see #AnnotatedBeanDefinitionReader(BeanDefinitionRegistry, Environment)
	 * @see #setEnvironment(Environment)
	 */
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, getOrCreateEnvironment(registry));
	}

	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry and using
	 * the given {@link Environment}.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @param environment the {@code Environment} to use when evaluating bean definition
	 * profiles.
	 * @since 3.1
	 */
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry,
										 Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		/**
		 * 初始化注解解析器
		 **/
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
		/**
		 * 将 Spring 内部的注解后置处理器组件注册到 BeanDefinitionRegistry 中
		 **/
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
	}


	/**
	 * Return the BeanDefinitionRegistry that this scanner operates on.
	 */
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Set the Environment to use when evaluating whether
	 * {@link Conditional @Conditional}-annotated component classes should be registered.
	 * <p>The default is a {@link StandardEnvironment}.
	 * @see #registerBean(Class, String, Class...)
	 */
	public void setEnvironment(Environment environment) {
		this.conditionEvaluator = new ConditionEvaluator(this.registry, environment, null);
	}

	/**
	 * Set the BeanNameGenerator to use for detected bean classes.
	 * <p>The default is a {@link AnnotationBeanNameGenerator}.
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator =
				(beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
	}

	/**
	 * Set the ScopeMetadataResolver to use for detected bean classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}


	/**
	 * Register one or more annotated classes to be processed.
	 * <p>Calls to {@code register} are idempotent; adding the same
	 * annotated class more than once has no additional effect.
	 * @param annotatedClasses one or more annotated classes,
	 * e.g. {@link Configuration @Configuration} classes
	 */
	public void register(Class<?>... annotatedClasses) {
		for (Class<?> annotatedClass : annotatedClasses) {
			registerBean(annotatedClass);
		}
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param annotatedClass the class of the bean
	 */
	public void registerBean(Class<?> annotatedClass) {
		doRegisterBean(annotatedClass, null, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param annotatedClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @since 5.2
	 */
	public void registerBean(Class<?> annotatedClass,
							 @Nullable String name) {
		doRegisterBean(annotatedClass, name, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param annotatedClass the class of the bean
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> annotatedClass,
							 Class<? extends Annotation>... qualifiers) {
		doRegisterBean(annotatedClass, null, qualifiers, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param annotatedClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> annotatedClass,
							 @Nullable String name,
							 Class<? extends Annotation>... qualifiers) {

		doRegisterBean(annotatedClass, name, qualifiers, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param annotatedClass the class of the bean
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> annotatedClass,
								 @Nullable Supplier<T> supplier) {
		doRegisterBean(annotatedClass, null, null, supplier, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param annotatedClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> annotatedClass,
								 @Nullable String name,
								 @Nullable Supplier<T> supplier) {
		doRegisterBean(annotatedClass, name, null, supplier, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param annotatedClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.2
	 */
	public <T> void registerBean(Class<T> annotatedClass,
								 @Nullable String name,
								 @Nullable Supplier<T> supplier,
								 BeanDefinitionCustomizer... customizers) {

		doRegisterBean(annotatedClass, name, null, supplier, customizers);
	}

	/**
	 * 将给定的 Bean 类注册为 Bean，从类声明的注释中派生其元数据
	 *
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param annotatedClass the class of the bean
	 * @param name an explicit name for the bean
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param qualifiers specific qualifier annotations to consider, if any,
	 * in addition to qualifiers at the bean class level
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 */
	private <T> void doRegisterBean(Class<T> annotatedClass,
									@Nullable String name,
									@Nullable Class<? extends Annotation>[] qualifiers,
									@Nullable Supplier<T> supplier,
									@Nullable BeanDefinitionCustomizer[] customizers) {

		// 根据指定的 Bean 创建一个 AnnotatedGenericBeanDefinition
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);
		// 根据 @Conditional 注解确定是否应跳过注册
		if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
			return;
		}

		// 指定创建 Bean 实例的回调方法，此时为 null
		abd.setInstanceSupplier(supplier);
		// 解析类的作用域元数据
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		// 添加类的作用域元数据
		abd.setScope(scopeMetadata.getScopeName());
		/**
		 * 生成 BeanNamePriority
		 * 调用 AnnotationBeanNameGenerator.generateBeanName()
		 */
		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

		/**
		 * 处理类当中的通用注解
		 * 主要处理 @Lazy @DependsOn @Primary @Role @Description 注解
		 * 处理完成之后将值赋给到 AnnotatedGenericBeanDefinition 对应的属性中
		 */
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		/**
		 * 如果在向容器注册 AnnotatedGenericBeanDefinition 时，使用了额外的限定符注解则解析
		 * byName 和 qualifiers 变量是 Annotation 类型的数组，里面不仅存了 @Qualifier 注解
		 * 所以 Spring 遍历这个数组判断是否加了指定注解
		 *
		 * 此时 qualifiers 为 null，if 语句不执行
		 */
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				if (Primary.class == qualifier) {
					// 设置 primary 属性值
					abd.setPrimary(true);
				}
				else if (Lazy.class == qualifier) {
					// 设置 lazyInit 属性值
					abd.setLazyInit(true);
				}
				else {
					/**
					 * 如果使用了除 @Primary 和 @Lazy 以外的其他注解
					 * 则为该 Bean 添加一个根据名字自动装配的限定符
					 */
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}

		/**
		 * 如果存在一个或多个用于自定义工厂的回调
		 *
		 * 此时 customizers 为 null，if 语句不执行
		 */
		if (customizers != null) {
			for (BeanDefinitionCustomizer customizer : customizers) {
				customizer.customize(abd);
			}
		}

		/**
		 * 获取 BeanDefinitionHolder 持有者容器
		 * 里面包含的属性值有 beanName， beanDefinition 和 aliases 别名集合
		 */
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		/**
		 * 解析代理模型
		 */
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		/**
		 * 将最终获取到的 BeanDefinitionHolder 持有者容器中包含的信息注册给 BeanDefinitionRegistry
		 *
		 * AnnotationConfigApplicationContext 在初始化的时候通过调用父类的构造方法，实例化了一个 DefaultListableBeanFactory
		 * 这一步就是把 BeanDefinitionHolder 这个数据结构中包含的信息注册到 DefaultListableBeanFactory 中
		 *
		 * DefaultListableBeanFactory 实现了 BeanDefinitionRegistry
		 *
		 * 此时传入的 @Configuration 配置类已经注册到 DefaultListableBeanFactory 工厂中
		 */
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
	}

	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable) {
			return ((EnvironmentCapable) registry).getEnvironment();
		}
		return new StandardEnvironment();
	}

}
