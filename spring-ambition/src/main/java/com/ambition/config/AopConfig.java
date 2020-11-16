package com.ambition.config;

import com.ambition.service.Calculate;
import com.ambition.service.CalculateImpl;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Elewin
 * @date 2020-05-03 4:17 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Configuration
//@ComponentScan(value = "com.ambition")
/**
 * Spring AOP 默认使用 JDK 动态代理
 *
 * proxyTargetClass = true 时则代理目标对象时强制使用 CGLIB 代理
 * @see DefaultAopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
 *
 * exposeProxy = true 暴露代理对象，这样就可以使用 AopContext.currentProxy() 方法获取当前代理的对象
 * @see AopContext#currentProxy
 * @see JdkDynamicAopProxy#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
 *
 * 可以实现在方法里面调方法，或者用 this 关键字调方法，而无法被代理的情况
 **/
//@EnableAspectJAutoProxy(proxyTargetClass = false, exposeProxy = true)
public class AopConfig {

}
