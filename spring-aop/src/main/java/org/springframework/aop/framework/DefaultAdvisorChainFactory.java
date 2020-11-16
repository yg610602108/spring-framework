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

package org.springframework.aop.framework;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.*;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple but definitive way of working out an advice chain for a Method,
 * given an {@link Advised} object. Always rebuilds each advice chain;
 * caching can be provided by subclasses.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0.3
 */
@SuppressWarnings("serial")
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	/**
	 * 增强器转换为拦截器
	 *
	 * TODO 拦截器是有顺序的？
	 **/
	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config,
																	Method method,
																	@Nullable Class<?> targetClass) {

		// This is somewhat tricky... We have to process introductions first,
		// but we need to preserve order in the ultimate list.
		/**
		 * This is somewhat tricky... We have to process introductions first,
		 * but we need to preserve order in the ultimate list.
		 *
		 * 这有点棘手...我们必须先处理 introductions，但是我们需要将顺序保持在最终列表中。
		 **/

		// 获取 DefaultAdvisorAdapterRegistry 实例
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
		// 获取通知
		Advisor[] advisors = config.getAdvisors();
		// 拦截器集合
		List<Object> interceptorList = new ArrayList<>(advisors.length);
		// 目标类
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());

		Boolean hasIntroductions = null;

		for (Advisor advisor : advisors) {
			/**
			 * 是 PointcutAdvisor 切点通知类
			 *
			 * BeanFactoryTransactionAttributeSourceAdvisor 符合条件
			 * DefaultPointcutAdvisor 符合条件
			 * InstantiationModelAwarePointcutAdvisorImpl 符合条件
			 **/
			if (advisor instanceof PointcutAdvisor) {
				// Add it conditionally.
				PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
				/**
				 * 代理配置以预先过【默认为False】 或者 切面与目标类匹配
				 *
				 * 事务的类过滤器是 TransactionAttributeSourceClassFilter
				 **/
				if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
					/**
					 * 获取方法匹配器
					 *
					 * 事务的是
					 * 		TransactionAttributeSourcePointcut
					 *
					 * AOP 的是
					 * 		TrueMethodMatcher
					 * 		AspectJExpressionPointcut
					 **/
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					boolean match;
					/**
					 * TrueMethodMatcher 不是 IntroductionAwareMethodMatcher 的子类
					 * AspectJExpressionPointcut 是 IntroductionAwareMethodMatcher 的子类
					 * TransactionAttributeSourcePointcut 不是 IntroductionAwareMethodMatcher 的子类
					 **/
					if (mm instanceof IntroductionAwareMethodMatcher) {
						if (hasIntroductions == null) {
							hasIntroductions = hasMatchingIntroductions(advisors, actualClass);
						}
						match = ((IntroductionAwareMethodMatcher) mm).matches(method, actualClass, hasIntroductions);
					}
					else {
						match = mm.matches(method, actualClass);
					}

					// 匹配
					if (match) {
						// 转换为拦截器
						MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
						if (mm.isRuntime()) {
							// Creating a new object instance in the getInterceptors() method
							// isn't a problem as we normally cache created chains.
							for (MethodInterceptor interceptor : interceptors) {
								// 封装为 InterceptorAndDynamicMethodMatcher
								interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
							}
						}
						else {
							interceptorList.addAll(Arrays.asList(interceptors));
						}
					}
				}
			}
			// 是 IntroductionAdvisor 类型
			else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					Interceptor[] interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(Arrays.asList(interceptors));
				}
			}
			else {
				Interceptor[] interceptors = registry.getInterceptors(advisor);
				interceptorList.addAll(Arrays.asList(interceptors));
			}
		}

		return interceptorList;
	}

	/**
	 * Determine whether the Advisors contain matching introductions.
	 */
	private static boolean hasMatchingIntroductions(Advisor[] advisors, Class<?> actualClass) {
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(actualClass)) {
					return true;
				}
			}
		}
		return false;
	}

}
