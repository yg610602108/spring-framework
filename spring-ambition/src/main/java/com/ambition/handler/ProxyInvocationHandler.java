package com.ambition.handler;

import org.apache.ibatis.annotations.Select;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Elewin
 * @date 2020-01-08 10:28 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class ProxyInvocationHandler implements InvocationHandler {

	/**
	 * Object proxy  : 代理实例
	 * Method method : 被代理对象的方法
	 * Object[] args : 被代理对象的方法的参数
	 **/
	@Override
	public Object invoke(Object proxy,
						 Method method,
						 Object[] args) throws Throwable {

		// 被代理类需要执行的逻辑
		Select annotation = method.getAnnotation(Select.class);
		System.out.println(annotation.value()[0]);

		return method.getReturnType();
	}
}
