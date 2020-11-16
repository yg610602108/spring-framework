package com.ambition.interceptor;


import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author Elewin
 * @date 2020-01-23 2:20 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class CglibInterceptor implements MethodInterceptor {

	@Override
	public Object intercept(Object o, Method method,
							Object[] objects,
							MethodProxy methodProxy) throws Throwable {
		System.out.println("我代理了父类");
		// 执行父类的方法
		return methodProxy.invokeSuper(o, null);
	}
}
