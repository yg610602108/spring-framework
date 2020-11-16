package com.ambition.study;

import com.ambition.config.AmbitionConfig;
import com.ambition.service.AService;
import com.ambition.service.BService;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Elewin
 * @date 2020-01-29 1:12 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class CircularReferenceTest {

	public static void main(String[] args) {

		/**
		 * Spring 默认是支持循环依赖的
		 * {@link AbstractAutowireCapableBeanFactory#allowCircularReferences}
		 * 前提是相互引用的两个 Bean 不能全是原型的，且不能用构造方法注入
		 *
		 * Spring 解决循环依赖的方式
		 * 通过两次调用两次重载的 getSingleton() 方法，将早期对象暴露到缓存中，
		 * 当解析 A 对象时，发现其依赖 B 对象，然后获取并创建 B 对象
		 * 当填充 B 对象的属性时，发现其依赖 A 对象，然后去创建 A 对象
		 * 返回完整的 B 对象后，再去填充 A 对象的属性
		 *
		 * @see AbstractAutowireCapableBeanFactory#doCreateBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])
		 *
		 * 为什么用构造方法注入不能解决循环依赖？
		 * 因为使用构造方法注入在暴露到缓存之前，只要缓存中没有就获取不到
		 * 同理全是原型的也不行，没有一个会暴露到缓存中
		 *
		 * @see AbstractAutowireCapableBeanFactory#createBeanInstance(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])
		 *
		 * 有两种方式关闭循环依赖：
		 * 1、获取 DefaultListableBeanFactory 或 AbstractAutowireCapableBeanFactory 后将 allowCircularReferences 设置为 false
		 * {@link AbstractAutowireCapableBeanFactory#setAllowCircularReferences(boolean)}
		 * 2、修改源码
		 **/

		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(AmbitionConfig.class);
//		context.register(AmbitionConfig.class);

//		AbstractAutowireCapableBeanFactory beanFactory =
//				(AbstractAutowireCapableBeanFactory) context.getBeanFactory();
//		// 关闭循环依赖
//		beanFactory.setAllowCircularReferences(Boolean.FALSE);

//		context.refresh();

		AService aService = context.getBean(AService.class);
		System.out.println(aService);

		BService bService = context.getBean(BService.class);
		System.out.println(bService);
	}

}
