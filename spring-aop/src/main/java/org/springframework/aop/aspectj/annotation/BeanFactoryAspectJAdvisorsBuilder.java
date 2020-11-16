/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper for retrieving @AspectJ beans from a BeanFactory and building
 * Spring Advisors based on them, for use with auto-proxying.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AnnotationAwareAspectJAutoProxyCreator
 */
public class BeanFactoryAspectJAdvisorsBuilder {

	private final ListableBeanFactory beanFactory;

	private final AspectJAdvisorFactory advisorFactory;

	@Nullable
	private volatile List<String> aspectBeanNames;

	/**
	 * Key 是我们写的切面类 beanName，Value 是对应的切面增强处理类【不是我们写的类】
	 **/
	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();

	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {

		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
	}

	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 * @param advisorFactory the AspectJAdvisorFactory to build each Advisor with
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory,
											 AspectJAdvisorFactory advisorFactory) {

		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}

	/**
	 * 在当前的 beanFactory 中查找带有 AspectJ 注解的 Aspect bean，然后返回代表它们的 Spring AOP Advisor 列表
	 * 为每个 AspectJ 通知方法创建一个 Spring Advisor
	 *
	 * Look for AspectJ-annotated aspect beans in the current bean factory,
	 * and return to a list of Spring AOP Advisors representing them.
	 * <p>Creates a Spring Advisor for each AspectJ advice method.
	 * @return the list of {@link org.springframework.aop.Advisor} beans
	 * @see #isEligibleBean
	 */
	public List<Advisor> buildAspectJAdvisors() {
		// 第一次会缓存，第二次直接获取
		List<String> aspectNames = this.aspectBeanNames;
		// 缓存未构建，则进行同步获取
		if (aspectNames == null) {

			synchronized (this) {

				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					List<Advisor> advisors = new ArrayList<>();
					aspectNames = new ArrayList<>();

					// 获取容器中所有组件的 beanName
					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
							this.beanFactory, Object.class, true, false);

					for (String beanName : beanNames) {
						// 不合格则跳过
						if (!isEligibleBean(beanName)) {
							continue;
						}
						/**
						 * We must be careful not to instantiate beans eagerly as in this case they
						 * would be cached by the Spring container but would not have been weaved.
						 *
						 * 我们必须注意不要急于实例化 Bean，因为在这种情况下，它们将由 Spring 容器缓存，但不会被编织
						 **/
						Class<?> beanType = this.beanFactory.getType(beanName);
						if (beanType == null) {
							continue;
						}
						// 是切面【具有 @Aspect 注解并且不是由 ajc 编译】
						if (this.advisorFactory.isAspect(beanType)) {
							aspectNames.add(beanName);
							AspectMetadata amd = new AspectMetadata(beanType, beanName);
							// AspectJ支持的不同子句（切面实例化模型）是单例的
							if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
								MetadataAwareAspectInstanceFactory factory =
										new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
								/**
								 * 获取 AspectJ 注解对应的切面增强处理类【不是我们写的类】
								 **/
								List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
								// 是单例则存入 Map<String, List<Advisor>> 缓存
								if (this.beanFactory.isSingleton(beanName)) {
									this.advisorsCache.put(beanName, classAdvisors);
								}
								// 否则存入 Map<String, MetadataAwareAspectInstanceFactory> 缓存
								else {
									this.aspectFactoryCache.put(beanName, factory);
								}

								advisors.addAll(classAdvisors);
							}
							else {
								// Per target or per this.
								if (this.beanFactory.isSingleton(beanName)) {
									throw new IllegalArgumentException("Bean with name '" + beanName +
											"' is a singleton, but aspect instantiation model is not singleton");
								}
								MetadataAwareAspectInstanceFactory factory =
										new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
								this.aspectFactoryCache.put(beanName, factory);
								advisors.addAll(this.advisorFactory.getAdvisors(factory));
							}
						}
					}
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}

		List<Advisor> advisors = new ArrayList<>();
		for (String aspectName : aspectNames) {
			// 从缓存中获取
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) {
				advisors.addAll(cachedAdvisors);
			}
			else {
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}

		return advisors;
	}

	/**
	 * 返回具有给定名称的 Aspect Bean 是否合格
	 *
	 * Return whether the aspect bean with the given name is eligible.
	 * @param beanName the name of the aspect bean
	 * @return whether the bean is eligible
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
