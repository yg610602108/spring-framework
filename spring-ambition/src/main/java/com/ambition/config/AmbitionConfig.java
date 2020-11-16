package com.ambition.config;

import com.ambition.annotation.AmbitionScan;
import com.ambition.registrar.AmbitionBeanDefinitionRegistrar;
import com.ambition.selector.AmbitionImportSelector;
import com.ambition.service.AService;
import com.ambition.service.BService;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.context.annotation.*;

/**
 * @author Elewin
 * @date 2020-01-03 1:27 PM
 * @version 1.0
 */

/**
 * 不加 @Configuration 注解是一个普通的 Bean
 *
 * 加了 @Configuration 注解就是一个代理 Bean
 * 且里面定义的 @Bean 一定是单例的，没加则不一定是单例
 **/
@Configuration
//@AmbitionScan("com.ambition")
@ComponentScan(value = "com.ambition")
/**
 * 加载配置文件构建配置类
 **/
//@ImportResource(value = "classpath:spring.xml")
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
/**
 * 开启事务管理
 **/
//@EnableTransactionManagement
//@Import(value = {
//		BService.class,
//		AmbitionImportSelector.class
//})
public class AmbitionConfig {

//	@Bean
//	@Scope(proxyMode = ScopedProxyMode.INTERFACES)
//	public BService getB() {
//		System.out.println(getA());
//		return new BService();
//	}

	/**
	 * 如果没有加 @Configuration 注解
	 * 则 "aService create" 会打印两遍
	 * 但是加了 @Configuration 注解就只会打印一遍
	 *
	 * 说明 getB() 方法的代码被修改了
	 * AmbitionConfig 类已经变成了一个代理对象
	 * {@link ConfigurationClassEnhancer.BeanMethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.springframework.cglib.proxy.MethodProxy)}
	 *
	 * @Configuration 注解的作用是为了产生一个 CGLIB 代理对象
	 * 保证 @Bean 方法产生的对象只实例化一次
	 * 不会因为互相调用而实例化多次
	 **/
//	@Bean(name = "getAAA")
//	public AService getA() {
//		System.out.println("aService create");
//		return new AService();
//	}

}
