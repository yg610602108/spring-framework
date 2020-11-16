/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() { }

	/**
	 * 执行所有的 BeanDefinitionRegistryPostProcessor 的 postProcessBeanDefinitionRegistry 方法
	 *
	 * 1、外部程序通过 API 添加的 BeanDefinitionRegistryPostProcessor
	 * {@link AbstractApplicationContext#addBeanFactoryPostProcessor(org.springframework.beans.factory.config.BeanFactoryPostProcessor)}
	 *
	 * 2、Spring 中内置的 BeanFactoryPostProcessor
	 * 最重要的是 ConfigurationClassPostProcessor 这个类
	 * {@link AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)}
	 * {@link DefaultListableBeanFactory#registerBeanDefinition(java.lang.String, org.springframework.beans.factory.config.BeanDefinition)}
	 *
	 * 3、外部程序往 BeanDefinitionMap 中添加的
	 * {@link DefaultListableBeanFactory#registerBeanDefinition(java.lang.String, org.springframework.beans.factory.config.BeanDefinition)}
	 *
	 * 再执行父接口 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
	 **/
	public static void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory,
													   List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
		/**
		 * Invoke BeanDefinitionRegistryPostProcessors first, if any.
		 *
		 * 如果有的话，首先调用 BeanDefinitionRegistryPostProcessors
		 * 存放所有的 BeanDefinitionRegistryPostProcessors
		 **/
		Set<String> processedBeans = new HashSet<>();

		/**
		 * 如果是 BeanDefinitionRegistry 的子类
		 *
		 * beanFactory 是 DefaultListableBeanFactory
		 * DefaultListableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry
		 */
		if (beanFactory instanceof BeanDefinitionRegistry) {
			// 转换成 BeanDefinitionRegistry
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			/**
			 * 存放实现了 BeanFactoryPostProcessor 的集合
			 **/
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			/**
			 * 存放所有需要回调父类 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法的 BeanDefinitionRegistryPostProcessor 集合
			 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)}
			 *
			 * BeanDefinitionRegistryPostProcessor 继承了 BeanFactoryPostProcessor
			 * 存放所有的 BeanDefinitionRegistryPostProcessor
			 */
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/**
			 * 区分 BeanDefinitionRegistryPostProcessor 和 BeanFactoryPostProcessor，初始化集合元素
			 *
			 * 此时还没有任何元素，只有通过 API 添加了 BeanFactoryPostProcessor 才会执行
			 * {@link AbstractApplicationContext#addBeanFactoryPostProcessor(org.springframework.beans.factory.config.BeanFactoryPostProcessor)}
			 * 这种情形很少，一般是通过注解来添加一个 BeanFactoryPostProcessor
			 * 所以下面这个循环很少会执行
			 *
			 * Spring 最开始执行的是外部程序通过 API 提供的 BeanFactoryPostProcessor
			 * 直接转换成 BeanDefinitionRegistryPostProcessor，调用其 postProcessBeanDefinitionRegistry 方法
			 **/
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				/**
				 * 实现了 BeanDefinitionRegistryPostProcessor
				 *
				 * 这里只有一个 ConfigurationClassPostProcessor
				 *
				 * 一般外部扩展的都是 BeanFactoryPostProcessor
				 *
				 * public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor
				 * 这里 Spring 需要保证自己内置的先执行，为了和外部扩展的区分，进行了判断
				 **/
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					/**
					 * 转换为 BeanDefinitionRegistryPostProcessor
					 **/
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					/**
					 * 直接调用 postProcessBeanDefinitionRegistry 方法
					 **/
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					/**
					 * 执行完后添加到集合中，稍后执行其父类的方法
					 **/
					registryProcessors.add(registryProcessor);
				}
				// 实现了 BeanFactoryPostProcessor
				else {
					/**
					 * 直接添加到外部扩展的集合中，稍后执行其父类的方法
					 **/
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			/**
			 * 需要保留所有未初始化的常规 bean，以使 BeanFactoryPostProcessor 适用于这些 Bean
			 * 将实现 PriorityOrdered，Ordered 和其余的 BeanDefinitionRegistryPostProcessors 分开执行
			 *
			 * 存放当前正在注册的 BeanDefinitionRegistryPostProcessor
			 * 临时变量，分别找到实现 PriorityOrdered、Ordered 和其他的 BeanDefinitionRegistryPostProcessor
			 * 执行其 postProcessBeanDefinitionRegistry 方法
			 */
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			/**
			 * 根据类型从 List<String> beanDefinitionNames 和 Set<String> manualSingletonNames 中获取名称
			 *
			 * 获取 BeanDefinitionRegistryPostProcessor bean 名称
			 *
			 * 此时至少包含了一个 ConfigurationClassPostProcessor
			 * ConfigurationClassPostProcessor 实现了 BeanDefinitionRegistryPostProcessor
			 * 名称为 internalConfigurationAnnotationProcessor
			 *
			 * @see AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
			 */
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);

			/**
			 * ConfigurationClassPostProcessor 最重要的类
			 */
			for (String ppName : postProcessorNames) {
				/**
				 * 首先，调用实现 PriorityOrdered 的 BeanDefinitionRegistryPostProcessors
				 *
				 * ConfigurationClassPostProcessor 实现了 PriorityOrdered
				 */
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					/**
					 * 获取该 Bean，并添加到集合中
					 *
					 * beanFactory.getBean() 方法做了两件事
					 * 首先从容器中获取，获取到则返回
					 * 否则会实例化这个 Bean
					 **/
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// 合并
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 调用 BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry 方法【重要】
			 * 扫描配置类并注册所有符合条件的 BeanDefinition
			 *
			 * 集合中的元素包含 ConfigurationClassPostProcessor
			 * {@link ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)}
			 *
			 * 此时 Spring 已经加载完所有辅助初始化的内部类，开始构建和解析配置类
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			/**
			 * 执行完成，清空当前注册的处理器集合数据
			 *
			 * 至少清除包含的 ConfigurationClassPostProcessor
			 **/
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			/**
			 * 获取 BeanDefinitionRegistryPostProcessor BeanName
			 * 下面的代码理论上不会执行，只是 Spring 确保初始化过程中没有新的类被添加进来
			 **/
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			/**
			 * 接下来，调用实现 Ordered 的 BeanDefinitionRegistryPostProcessors
			 */
			for (String ppName : postProcessorNames) {
				// 不在 processedBeans 中且实现了 Ordered 的 BeanDefinitionRegistryPostProcessors
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					// 获取该 bean，并添加到集合中
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// 排序
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// 合并
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 调用 BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry 方法
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			// 执行完成，清空集合数据
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			/**
			 * 最后，调用所有剩余的 BeanDefinitionRegistryPostProcessors
			 * 直到不再出现其他 BeanDefinitionRegistryPostProcessors
			 */
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				// 获取 BeanDefinitionRegistryPostProcessor 名称
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					// 不在 processedBeans 中的 BeanDefinitionRegistryPostProcessors
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						/**
						 * 已经添加的 BeanDefinitionRegistryPostProcessor 可能还会产生新的 BeanDefinitionRegistryPostProcessor
						 * {@link BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)}
						 * 有了 BeanDefinitionRegistry 变量可以往 beanDefinitionMap 中添加新的元素
						 * 所以将 reiterate 置为 true 表示再找一遍
						 **/
						reiterate = true;
					}
				}
				// 排序
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				// 合并
				registryProcessors.addAll(currentRegistryProcessors);
				/**
				 * 调用 BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry 方法
				 */
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				// 执行完成，清空集合数据
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			/**
			 * 执行所有 BeanDefinitionRegistryPostProcessor 的 postProcessBeanFactory 方法
			 *
			 * 对 full 属性值的配置类进行 CGLIB 增强
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			/**
			 * 执行所有 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
			 **/
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}
		else {
			// Invoke factory processors registered with the context instance.
			/**
			 * 调用在上下文实例中注册的工厂处理器
			 */
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		/**
		 * 获取 BeanFactoryPostProcessor 名称
		 * 此时至少有两个元素：
		 *      org.springframework.context.annotation.internalConfigurationAnnotationProcessor
		 *      org.springframework.context.event.internalEventListenerProcessor
		 *
		 * 需要保留所有未初始化的常规 bean，以使 bean 工厂后处理器适用于这些 bean
		 */
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		/**
		 * 分隔实现了 PriorityOrdered，Ordered 和其余的 BeanFactoryPostProcessors
		 */
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			// 处理过则跳过
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			// 实现了 PriorityOrdered
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			// 实现了 Ordered
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			// 普通类
			else {
				// org.springframework.context.event.internalEventListenerProcessor 是普通类
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		/**
		 * 首先，调用实现 PriorityOrdered 的 BeanFactoryPostProcessors
		 */
		// 排序
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		/**
		 * 调用 BeanFactoryPostProcessor#postProcessBeanFactory 方法
		 **/
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		/**
		 * 接下来，调用实现 Ordered 的 BeanFactoryPostProcessors
		 */
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		// 排序
		sortPostProcessors(orderedPostProcessors, beanFactory);
		/**
		 * 调用 BeanFactoryPostProcessor#postProcessBeanFactory 方法
		 **/
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		/**
		 * 最后，调用所有其他 BeanFactoryPostProcessors
		 */
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		/**
		 * 调用 BeanFactoryPostProcessor#postProcessBeanFactory 方法
		 **/
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		/**
		 * 清除缓存的合并 bean 定义，因为后处理器可能已经修改了原始元数据，例如，替换值中的占位符
		 */
		beanFactory.clearMetadataCache();
	}

	/**
	 * 注册 BeanPostProcessor
	 **/
	public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
												  AbstractApplicationContext applicationContext) {

		// 获取所有的 BeanPostProcessor
		String[] postProcessorNames = beanFactory.getBeanNamesForType(
				BeanPostProcessor.class,
				true,
				false
		);

		/**
		 * Register BeanPostProcessorChecker that logs an info message when
		 * a bean is created during BeanPostProcessor instantiation, i.e. when
		 * a bean is not eligible for getting processed by all BeanPostProcessors.
		 *
		 * 注册 BeanPostProcessorChecker
		 * 当在 BeanPostProcessor 实例化期间创建 Bean 时，即当某个 Bean 不适合所有 BeanPostProcessor处理时，记录一条信息消息
		 *
		 * 已经注册的 + 下面注册的一个 BeanPostProcessorChecker + 待注册的
		 **/
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		// 注册 BeanPostProcessorChecker
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		/**
		 * Separate between BeanPostProcessors that implement PriorityOrdered, Ordered, and the rest.
		 *
		 * 将实现 PriorityOrdered，Ordered 的 BeanPostProcessor 与其余的分开
		 **/
		// 实现 PriorityOrdered 的 BeanPostProcessor
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 内部的 BeanPostProcessor
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		// 实现 Ordered 的 BeanPostProcessor
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 其余的 BeanPostProcessor
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();

		for (String ppName : postProcessorNames) {
			// 实现了 PriorityOrdered
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				// 创建实现 PriorityOrdered 的 BeanPostProcessor
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				// 如果实现了 MergedBeanDefinitionPostProcessor，则加入到内部的 BeanPostProcessor 集合中
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			// 实现了 Ordered
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			// 其余的
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		/**
		 * First, register the BeanPostProcessors that implement PriorityOrdered.
		 *
		 * 首先，注册实现 PriorityOrdered 的 BeanPostProcessor
		 **/
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		/**
		 * Next, register the BeanPostProcessors that implement Ordered.
		 *
		 * 接下来，注册实现 Ordered 的 BeanPostProcessor
		 **/
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		/**
		 * Now, register all regular BeanPostProcessors.
		 *
		 * 现在，注册所有常规 BeanPostProcessor
		 **/
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		/**
		 * Finally, re-register all internal BeanPostProcessors.
		 *
		 * 最后，重新注册所有内部 BeanPostProcessor
		 **/
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		/**
		 * Re-register post-processor for detecting inner beans as ApplicationListeners,
		 * moving it to the end of the processor chain (for picking up proxies etc).
		 *
		 * 重新注册后置处理器以将内部 bean 检测为 ApplicationListener，将其移至处理器链的末尾（用于拾取代理等）
		 **/
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors,
										   ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors,
			BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			/**
			 * 老版 MyBatis 借助了这个方法进行扫描
			 * 然后借助了 Spring 对于 @Import 注解的处理
			 *
			 * 新版 MyBatis 是直接实现了 BeanDefinitionRegistryPostProcessor 进行更底层的扩展
			 **/
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors,
			ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
												   List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory,
										int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}

	}

}
