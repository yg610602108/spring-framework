package com.ambition.study;

import com.ambition.annotation.AmbitionScan;
import com.ambition.config.AmbitionConfig;
import com.ambition.scanner.AmbitionScanner;
import com.ambition.service.AService;
import com.ambition.service.BService;
import com.ambition.service.YService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * @author Elewin
 * @date 2020-01-01 10:37 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class IOCTest {

	public static void main(String[] args) {

		/**
		 * 两种自动注入方式（根据类型注入和根据名称注入）【推断构造方法时会选择构造参数最多的进行注入】
		 * 四种手动注入模型（no、byType、byName、Construct）
		 *
		 * @Autowired 是 springframework 包下面的，由 AutowiredAnnotationBeanPostProcessor 解析
		 * @Resource 是 javax 包下面的，由 CommonAnnotationBeanPostProcessor 解析
		 *
		 * 原型是在每次 getBean 的时候才会实例化
		 * 单例是在容器初始化的时候实例化并放到一个单例缓存池中
		 *
		 *
		 * Spring 中 Bean 的生命周期回调
		 * Spring 中实现 Bean 的生命周期回调有三种方法：
		 * 1、加 @PostConstruct 或者 @PreDestroy 注解
		 * 2、实现 InitializingBean 或者 DisposableBean，并重写其中的 afterPropertiesSet() 或者 destroy()
		 * 3、定制配置的 init() 或者 destroy() 方法
		 * Spring 回调的顺序是
		 * 1、@PostConstruct -> afterPropertiesSet() -> init()
		 * 2、@PreDestroy -> destroy() -> destroy()
		 **/

		AnnotationConfigApplicationContext context =
		 		new AnnotationConfigApplicationContext(AmbitionConfig.class);

//		System.out.println(context.getBean(AmbitionConfig.class));
//		System.out.println(context.getBean(BService.class));
//		System.out.println(context.getBean(AService.class));
//		System.out.println(context.getBean(AService.class));

		// AmbitionScanner scanner = new AmbitionScanner(context);
		// scanner.addIncludeFilter(new AnnotationTypeFilter(AmbitionScan.class));
		// int scan = scanner.scan("com.ambition");
		// System.out.println("scan --->" + scan);


		// context.scan("com.ambition");

		// System.out.println(context.getBean(AmbitionConfig.class));

		// context.start();

		// context.stop();

		/**
		 * 只能注册被依赖的对象，不能注册依赖的对象
		 **/
		// context.getBeanFactory().registerSingleton("xService", new XService());

		// context.getBean(YService.class).getY();

		// System.out.println(context.getBean("&ambitionBeanFactory"));

//		ClassPathXmlApplicationContext xmlContext =
//				new ClassPathXmlApplicationContext("classpath:spring.xml");

		BeanDefinition beanDefinition =
				context.getBeanDefinition("yService");
		// 打印的是类文件在磁盘上的路径
		System.out.println(beanDefinition.getSource());
//
		YService yService = (YService) context.getBean("yService");

		yService.getY();

		context.close();
	}
}
