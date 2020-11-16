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

package org.springframework.context.annotation;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Utilities for processing {@link Bean}-annotated methods.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
abstract class BeanAnnotationHelper {

	private static final Map<Method, String> beanNameCache = new ConcurrentReferenceHashMap<>();

	private static final Map<Method, Boolean> scopedProxyCache = new ConcurrentReferenceHashMap<>();

	public static boolean isBeanAnnotated(Method method) {
		return AnnotatedElementUtils.hasAnnotation(method, Bean.class);
	}

	/**
	 * 获取 beanMethod 对应的 beanName
	 **/
	public static String determineBeanNameFor(Method beanMethod) {
		// 先从 Map<Method, String> beanNameCache 中由 beanMethod 获取对应的 beanName
		String beanName = beanNameCache.get(beanMethod);
		// beanMethod 第一次进来一般没有，因为 put 操作在 get 之后
		if (beanName == null) {
			/**
			 * By default, the bean name is the name of the @Bean-annotated method
			 *
			 * 默认情况下，beanName 是 @Bean 注释方法的名称
			 **/
			beanName = beanMethod.getName();
			/**
			 * Check to see if the user has explicitly set a custom bean name...
			 *
			 * 检查用户是否已显式设置自定义 beanName
			 * 获取用户设置的 @Bean 注解值
			 **/
			AnnotationAttributes bean = AnnotatedElementUtils.findMergedAnnotationAttributes(
							beanMethod,
							Bean.class,
							false,
							false
					);
			// 如果显式设置了自定义的名称，则用第一个值作为 beanMethod 的 beanName
			if (bean != null) {
				String[] names = bean.getStringArray("name");
				if (names.length > 0) {
					beanName = names[0];
				}
			}
			// 存入 Map<Method, String> beanNameCache 中
			beanNameCache.put(beanMethod, beanName);
		}

		return beanName;
	}

	/**
	 * 校验是否标注了 @Scope 注解，且 {@link Scope#proxyMode()} 值不是 {@link ScopedProxyMode#NO}
	 **/
	public static boolean isScopedProxy(Method beanMethod) {
		// 先从 Map<Method, Boolean> scopedProxyCache 中由 beanMethod 获取对应的 scopedProxy
		Boolean scopedProxy = scopedProxyCache.get(beanMethod);
		// beanMethod 第一次进来一般没有，因为 put 操作在 get 之后
		if (scopedProxy == null) {
			// 获取用户设置的 @Scope 注解值
			AnnotationAttributes scope = AnnotatedElementUtils.findMergedAnnotationAttributes(
							beanMethod,
							Scope.class,
							false,
							false
					);
			/**
			 * 标注了 @Scope 注解 且 {@link Scope#proxyMode()} 值不是 {@link ScopedProxyMode#NO}
			 **/
			scopedProxy = (scope != null && scope.getEnum("proxyMode") != ScopedProxyMode.NO);
			// 存入 Map<Method, Boolean> scopedProxyCache 中
			scopedProxyCache.put(beanMethod, scopedProxy);
		}

		return scopedProxy;
	}

}
