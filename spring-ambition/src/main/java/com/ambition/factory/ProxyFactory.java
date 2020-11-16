package com.ambition.factory;

import com.ambition.handler.ProxyInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * @author Elewin
 * @date 2020-01-08 10:04 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class ProxyFactory {

	@SuppressWarnings("unchecked")
	public <T> T getMapper(Class<T> clazz) {

		// JDK 动态代理
		ClassLoader classLoader = ProxyFactory.class.getClassLoader();
		Class[] classes = new Class[]{clazz};
		ProxyInvocationHandler handler = new ProxyInvocationHandler();

		/**
		 * ClassLoader loader    类加载器
		 * Class<?>[] interfaces 被代理的类
		 * InvocationHandler h   代理逻辑
		 **/
		Object instance = Proxy.newProxyInstance(classLoader, classes, handler);

		return (T) instance;
	}

}
