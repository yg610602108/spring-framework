/*
 * Copyright 2002-2015 the original author or authors.
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

import org.springframework.aop.SpringProxy;

import java.io.Serializable;
import java.lang.reflect.Proxy;

/**
 * Default {@link AopProxyFactory} implementation, creating either a CGLIB proxy
 * or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true for a given
 * {@link AdvisedSupport} instance:
 * <ul>
 * <li>the {@code optimize} flag is set
 * <li>the {@code proxyTargetClass} flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * <p>In general, specify {@code proxyTargetClass} to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
@SuppressWarnings("serial")
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		// 需要优化【默认为False】 或者 proxyTargetClass属性值为True【默认为False】 或者 没有用户提供的代理接口
		if (config.isOptimize() || config.isProxyTargetClass()
				|| hasNoUserSuppliedProxyInterfaces(config)) {
			// 目标类
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			// 是接口 或者 是代理的类
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				// 使用 JDK 动态代理
				return new JdkDynamicAopProxy(config);
			}
			// 使用 CGLIB 代理
			return new ObjenesisCglibAopProxy(config);
		}
		// 默认使用 JDK 动态代理
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		// 代理的目标接口
		Class<?>[] ifcs = config.getProxiedInterfaces();
		// 没有接口 或者 有接口且是 SpringProxy 类型的接口
		return ifcs.length == 0
				|| (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0]));
	}

}
