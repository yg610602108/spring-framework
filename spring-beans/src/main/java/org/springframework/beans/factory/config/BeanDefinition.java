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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;
import org.springframework.lang.Nullable;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 *
 * <p>This is just a minimal interface: The main intention is to allow a
 * {@link BeanFactoryPostProcessor} to introspect and modify property values
 * and other bean metadata.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	/**
	 * 标准单例作用域的作用域标识符：singleton
	 *
	 * Scope identifier for the standard singleton scope: "singleton".
	 * <p>Note that extended bean factories might support further scopes.
	 * @see #setScope
	 */
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

	/**
	 * 标准原型作用域的作用域标识符：prototype
	 *
	 * Scope identifier for the standard prototype scope: "prototype".
	 * <p>Note that extended bean factories might support further scopes.
	 * @see #setScope
	 */
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;

	/**
	 * 角色提示，指示 BeanDefinition 是应用程序的主要部分
	 * 通常对应于用户定义的 Bean
	 *
	 * Role hint indicating that a {@code BeanDefinition} is a major part
	 * of the application. Typically corresponds to a user-defined bean.
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * 角色提示，指示 BeanDefinition 是某些较大配置的支持部分
	 * 通常是外部配置
	 *
	 * Role hint indicating that a {@code BeanDefinition} is a supporting
	 * part of some larger configuration, typically an outer
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 * {@code SUPPORT} beans are considered important enough to be aware
	 * of when looking more closely at a particular
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition},
	 * but not when looking at the overall configuration of an application.
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * 角色提示，指示 BeanDefinition 正在提供完全后台的角色，与最终用户无关
	 * 注册完全属于 ComponentDefinition 内部工作一部分的 Bean 时使用此提示
	 *
	 * Role hint indicating that a {@code BeanDefinition} is providing an
	 * entirely background role and has no relevance to the end-user. This hint is
	 * used when registering beans that are completely part of the internal workings
	 * of a {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 */
	int ROLE_INFRASTRUCTURE = 2;

	/*
	 * Modifiable attributes
	 * 可以修改的属性
	 **/

	/**
	 * 设置此 BeanDefinition 的父定义的名称【Bean 的合并时需要用到】
	 *
	 * Set the name of the parent definition of this bean definition, if any.
	 */
	void setParentName(@Nullable String parentName);

	/**
	 * 返回此 BeanDefinition 的父定义的名称
	 *
	 * Return the name of the parent definition of this bean definition, if any.
	 */
	@Nullable
	String getParentName();

	/**
	 * 指定此 BeanDefinition 的 Bean 类名称
	 *
	 * Specify the bean class name of this bean definition.
	 * <p>The class name can be modified during bean factory post-processing,
	 * typically replacing the original class name with a parsed variant of it.
	 * @see #setParentName
	 * @see #setFactoryBeanName
	 * @see #setFactoryMethodName
	 */
	void setBeanClassName(@Nullable String beanClassName);

	/**
	 * 返回此 BeanDefinition 的 Bean 类名称
	 *
	 * Return the current bean class name of this bean definition.
	 * <p>Note that this does not have to be the actual class name used at runtime, in
	 * case of a child definition overriding/inheriting the class name from its parent.
	 * Also, this may just be the class that a factory method is called on, or it may
	 * even be empty in case of a factory bean reference that a method is called on.
	 * Hence, do <i>not</i> consider this to be the definitive bean type at runtime but
	 * rather only use it for parsing purposes at the individual bean definition level.
	 * @see #getParentName()
	 * @see #getFactoryBeanName()
	 * @see #getFactoryMethodName()
	 */
	@Nullable
	String getBeanClassName();

	/**
	 * 设置 Bean 的目标作用域
	 *
	 * Override the target scope of this bean, specifying a new scope name.
	 * @see #SCOPE_SINGLETON
	 * @see #SCOPE_PROTOTYPE
	 */
	void setScope(@Nullable String scope);

	/**
	 * 返回 Bean 当前目标作用域的名称，如果未知，则返回空
	 *
	 * Return the name of the current target scope for this bean,
	 * or {@code null} if not known yet.
	 */
	@Nullable
	String getScope();

	/**
	 * 设置是否应延迟初始化此 Bean
	 *
	 * Set whether this bean should be lazily initialized.
	 * <p>If {@code false}, the bean will get instantiated on startup by bean
	 * factories that perform eager initialization of singletons.
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * 返回此 Bean 是否应延迟初始化，即不要在启动时急于实例化
	 * 仅适用于单例 Bean
	 *
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 */
	boolean isLazyInit();

	/**
	 * 设置该 Bean 依赖于初始化的 Bean 的名称
	 * BeanFactory 将确保首先初始化这些 Bean
	 *
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized first.
	 */
	void setDependsOn(@Nullable String... dependsOn);

	/**
	 * 返回此 Bean 依赖的 Bean 名称
	 *
	 * Return the bean names that this bean depends on.
	 */
	@Nullable
	String[] getDependsOn();

	/**
	 * 设置此 Bean 是否适合自动连接到其他 Bean
	 * 设置是否作为自动装配的候选对象
	 *
	 * 当有多个候选对象时，@Autowire 注解手动装配会报错
	 * 但是根据 byType 自动装配不会报错，会装配不上，为空
	 *
	 * XML 配置文件中有一个属性 autowire-candidate="default | false | true" 可以指定是否将该 Bean 作为注入的候选对象
	 * 还有一个属性 primary="false | true" 可以指定当有多个候选对象时，该候选对象为主要的候选对象
	 *
	 * Set whether this bean is a candidate for getting autowired into some other bean.
	 * <p>Note that this flag is designed to only affect type-based autowiring.
	 * It does not affect explicit references by name, which will get resolved even
	 * if the specified bean is not marked as an autowire candidate. As a consequence,
	 * autowiring by name will nevertheless inject a bean if the name matches.
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * 返回此 Bean 是否适合自动连接到其他 Bean
	 *
	 * Return whether this bean is a candidate for getting autowired into some other bean.
	 */
	boolean isAutowireCandidate();

	/**
	 * 设置此 Bean 是否为自动装配的主要候选对象
	 *
	 * Set whether this bean is a primary autowire candidate.
	 * <p>If this value is {@code true} for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 */
	void setPrimary(boolean primary);

	/**
	 * 返回此 Bean 是否为自动装配的主要候选对象
	 *
	 * Return whether this bean is a primary autowire candidate.
	 */
	boolean isPrimary();

	/**
	 * 指定要使用的 FactoryBean
	 *
	 * XML 配置文件中有一个属性为 factory-bean 可以指定要使用的 FactoryBean
	 *
	 * Specify the factory bean to use, if any.
	 * This the name of the bean to call the specified factory method on.
	 * @see #setFactoryMethodName
	 */
	void setFactoryBeanName(@Nullable String factoryBeanName);

	/**
	 * 返回要使用的 FactoryBean 名称
	 *
	 * Return the factory bean name, if any.
	 */
	@Nullable
	String getFactoryBeanName();

	/**
	 * 指定工厂方法
	 * 将使用构造函数参数调用此方法，如果未指定，则不使用任何参数
	 * 该方法将在指定的工厂 Bean（如果有）上被调用，否则将作为本地 Bean 类上的静态方法被调用
	 *
	 * 可以在指定的工厂方法里面返回另一个对象，此时这个对象就会被 Spring 实例化
	 *
	 * XML 配置文件中有一个属性为 factory-method 可以指定工厂方法
	 *
	 * Specify a factory method, if any. This method will be invoked with
	 * constructor arguments, or with no arguments if none are specified.
	 * The method will be invoked on the specified factory bean, if any,
	 * or otherwise as a static method on the local bean class.
	 * @see #setFactoryBeanName
	 * @see #setBeanClassName
	 */
	void setFactoryMethodName(@Nullable String factoryMethodName);

	/**
	 * 返回工厂方法
	 *
	 * Return a factory method, if any.
	 */
	@Nullable
	String getFactoryMethodName();

	/**
	 * 返回此 Bean 的构造函数参数值
	 *
	 * Return the constructor argument values for this bean.
	 * <p>The returned instance can be modified during bean factory post-processing.
	 * @return the ConstructorArgumentValues object (never {@code null})
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * 如果为此 Bean 定义了构造函数参数值，则返回
	 *
	 * Return if there are constructor argument values defined for this bean.
	 * @since 5.0.2
	 */
	default boolean hasConstructorArgumentValues() {
		return !getConstructorArgumentValues().isEmpty();
	}

	/**
	 * 返回要应用到 Bean 的新实例的属性值
	 *
	 * Return the property values to be applied to a new instance of the bean.
	 * <p>The returned instance can be modified during bean factory post-processing.
	 * @return the MutablePropertyValues object (never {@code null})
	 */
	MutablePropertyValues getPropertyValues();

	/**
	 * 返回是否有为此 Bean 定义的属性值
	 *
	 * Return if there are property values values defined for this bean.
	 * @since 5.0.2
	 */
	default boolean hasPropertyValues() {
		return !getPropertyValues().isEmpty();
	}

	/**
	 * 设置初始化方法的名称
	 *
	 * Set the name of the initializer method.
	 * @since 5.1
	 */
	void setInitMethodName(@Nullable String initMethodName);

	/**
	 * 返回初始化方法的名称
	 *
	 * Return the name of the initializer method.
	 * @since 5.1
	 */
	@Nullable
	String getInitMethodName();

	/**
	 * 设置销毁方法的名称
	 *
	 * Set the name of the destroy method.
	 * @since 5.1
	 */
	void setDestroyMethodName(@Nullable String destroyMethodName);

	/**
	 * 返回销毁方法的名称
	 *
	 * Return the name of the destroy method.
	 * @since 5.1
	 */
	@Nullable
	String getDestroyMethodName();

	/**
	 * 设置此 BeanDefinition 的角色提示
	 * 角色提示提供了框架和工具，并指出特定的 BeanDefinition 的作用和重要性
	 *
	 * Set the role hint for this {@code BeanDefinition}. The role hint
	 * provides the frameworks as well as tools with an indication of
	 * the role and importance of a particular {@code BeanDefinition}.
	 * @since 5.1
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	void setRole(int role);

	/**
	 * 获取 BeanDefinition 的角色提示
	 * 角色提示提供了框架和工具，并指出特定的 BeanDefinition 的作用和重要性
	 *
	 * Get the role hint for this {@code BeanDefinition}. The role hint
	 * provides the frameworks as well as tools with an indication of
	 * the role and importance of a particular {@code BeanDefinition}.
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	int getRole();

	/**
	 * 设置此 BeanDefinition 的可读描述
	 * 对应注解
	 * @see org.springframework.context.annotation.Description
	 *
	 * Set a human-readable description of this bean definition.
	 * @since 5.1
	 */
	void setDescription(@Nullable String description);

	/**
	 * 返回此 BeanDefinition 的可读描述
	 *
	 * Return a human-readable description of this bean definition.
	 */
	@Nullable
	String getDescription();

	/*
	 * Read-only attributes
	 * 只读属性
	 **/

	/**
	 * 返回这是否是一个 Singleton
	 * 若是，则对所有调用都返回同一个共享的实例
	 *
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * 返回这是否是一个 Prototype
	 * 若是，则对每个调用都返回一个独立的实例
	 *
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * @since 3.0
	 * @see #SCOPE_PROTOTYPE
	 */
	boolean isPrototype();

	/**
	 * 返回此 bean 是否为抽象的，即不打算实例化
	 *
	 * 提供统一的规范，规范子类的行为
	 *
	 * Return whether this bean is "abstract", that is, not meant to be instantiated.
	 */
	boolean isAbstract();

	/**
	 * 返回此 BeanDefinition 所来自的资源的描述
	 * 为了在发生错误的情况下显示上下文
	 *
	 * Return a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 */
	@Nullable
	String getResourceDescription();

	/**
	 * 返回原始的 BeanDefinition，如果没有，则返回空
	 *
	 * 如果有的话，允许获取修饰的 BeanDefinition
	 *
	 * Return the originating BeanDefinition, or {@code null} if none.
	 * Allows for retrieving the decorated bean definition, if any.
	 * <p>Note that this method returns the immediate originator. Iterate through the
	 * originator chain to find the original BeanDefinition as defined by the user.
	 */
	@Nullable
	BeanDefinition getOriginatingBeanDefinition();

}
