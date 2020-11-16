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

package org.springframework.context.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ConfigurationClassEnhancer.EnhancedConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * 比较重要的类
 *
 * 这个类有两个行为
 * {@link BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)}
 * {@link BeanFactoryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)}
 *
 * {@link BeanFactoryPostProcessor} used for bootstrapping processing of
 * {@link Configuration @Configuration} classes.
 *
 * <p>Registered by default when using {@code <context:annotation-config/>} or
 * {@code <context:component-scan/>}. Otherwise, may be declared manually as
 * with any other BeanFactoryPostProcessor.
 *
 * <p>This post processor is priority-ordered as it is important that any
 * {@link Bean} methods declared in {@code @Configuration} classes have
 * their corresponding bean definitions registered before any other
 * {@link BeanFactoryPostProcessor} executes.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 3.0
 */
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
		PriorityOrdered, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

	/**
	 * A {@code BeanNameGenerator} using fully qualified class names as default bean names.
	 * <p>This default for configuration-level import purposes may be overridden through
	 * {@link #setBeanNameGenerator}. Note that the default for component scanning purposes
	 * is a plain {@link AnnotationBeanNameGenerator#INSTANCE}, unless overridden through
	 * {@link #setBeanNameGenerator} with a unified user-level bean name generator.
	 * @since 5.2
	 * @see #setBeanNameGenerator
	 */
	public static final AnnotationBeanNameGenerator IMPORT_BEAN_NAME_GENERATOR = new AnnotationBeanNameGenerator() {
		@Override
		protected String buildDefaultBeanName(BeanDefinition definition) {
			String beanClassName = definition.getBeanClassName();
			Assert.state(beanClassName != null, "No bean class name set");
			return beanClassName;
		}
	};

	private static final String IMPORT_REGISTRY_BEAN_NAME =
			ConfigurationClassPostProcessor.class.getName() + ".importRegistry";


	private final Log logger = LogFactory.getLog(getClass());

	private SourceExtractor sourceExtractor = new PassThroughSourceExtractor();

	private ProblemReporter problemReporter = new FailFastProblemReporter();

	@Nullable
	private Environment environment;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	@Nullable
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

	private boolean setMetadataReaderFactoryCalled = false;

	private final Set<Integer> registriesPostProcessed = new HashSet<>();

	private final Set<Integer> factoriesPostProcessed = new HashSet<>();

	@Nullable
	private ConfigurationClassBeanDefinitionReader reader;

	private boolean localBeanNameGeneratorSet = false;

	/**
	 * Using short class names as default bean names by default.
	 *
	 * 默认情况下，使用短类名作为默认bean名称
	 *
	 * 扫描出来的 BeanName 生成器
	 **/
	private BeanNameGenerator componentScanBeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	/**
	 * Using fully qualified class names as default bean names by default.
	 *
	 * 默认情况下，使用完全限定的类名作为默认bean名称
	 *
	 * 导入进来的 BeanName 生成器
	 **/
	private BeanNameGenerator importBeanNameGenerator = IMPORT_BEAN_NAME_GENERATOR;

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;  // within PriorityOrdered
	}

	/**
	 * Set the {@link SourceExtractor} to use for generated bean definitions
	 * that correspond to {@link Bean} factory methods.
	 */
	public void setSourceExtractor(@Nullable SourceExtractor sourceExtractor) {
		this.sourceExtractor = (sourceExtractor != null ? sourceExtractor : new PassThroughSourceExtractor());
	}

	/**
	 * Set the {@link ProblemReporter} to use.
	 * <p>Used to register any problems detected with {@link Configuration} or {@link Bean}
	 * declarations. For instance, an @Bean method marked as {@code final} is illegal
	 * and would be reported as a problem. Defaults to {@link FailFastProblemReporter}.
	 */
	public void setProblemReporter(@Nullable ProblemReporter problemReporter) {
		this.problemReporter = (problemReporter != null ? problemReporter : new FailFastProblemReporter());
	}

	/**
	 * Set the {@link MetadataReaderFactory} to use.
	 * <p>Default is a {@link CachingMetadataReaderFactory} for the specified
	 * {@linkplain #setBeanClassLoader bean class loader}.
	 */
	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		Assert.notNull(metadataReaderFactory, "MetadataReaderFactory must not be null");
		this.metadataReaderFactory = metadataReaderFactory;
		this.setMetadataReaderFactoryCalled = true;
	}

	/**
	 * Set the {@link BeanNameGenerator} to be used when triggering component scanning
	 * from {@link Configuration} classes and when registering {@link Import}'ed
	 * configuration classes. The default is a standard {@link AnnotationBeanNameGenerator}
	 * for scanned components (compatible with the default in {@link ClassPathBeanDefinitionScanner})
	 * and a variant thereof for imported configuration classes (using unique fully-qualified
	 * class names instead of standard component overriding).
	 * <p>Note that this strategy does <em>not</em> apply to {@link Bean} methods.
	 * <p>This setter is typically only appropriate when configuring the post-processor as a
	 * standalone bean definition in XML, e.g. not using the dedicated {@code AnnotationConfig*}
	 * application contexts or the {@code <context:annotation-config>} element. Any bean name
	 * generator specified against the application context will take precedence over any set here.
	 * @since 3.1.1
	 * @see AnnotationConfigApplicationContext#setBeanNameGenerator(BeanNameGenerator)
	 * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		Assert.notNull(beanNameGenerator, "BeanNameGenerator must not be null");
		this.localBeanNameGeneratorSet = true;
		this.componentScanBeanNameGenerator = beanNameGenerator;
		this.importBeanNameGenerator = beanNameGenerator;
	}

	@Override
	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
		if (!this.setMetadataReaderFactoryCalled) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
		}
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
		if (!this.setMetadataReaderFactoryCalled) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory(beanClassLoader);
		}
	}


	/**
	 * 从注册表中的配置类派生更多的 BeanDefinition
	 *
	 * Derive further bean definitions from the configuration classes in the registry.
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		int registryId = System.identityHashCode(registry);
		if (this.registriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException(
					"postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
		}
		if (this.factoriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException(
					"postProcessBeanFactory already called on this post-processor against " + registry);
		}
		this.registriesPostProcessed.add(registryId);

		processConfigBeanDefinitions(registry);
	}

	/**
	 * Prepare the Configuration classes for servicing bean requests at runtime
	 * by replacing them with CGLIB-enhanced subclasses.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		int factoryId = System.identityHashCode(beanFactory);
		if (this.factoriesPostProcessed.contains(factoryId)) {
			throw new IllegalStateException(
					"postProcessBeanFactory already called on this post-processor against " + beanFactory);
		}
		this.factoriesPostProcessed.add(factoryId);
		if (!this.registriesPostProcessed.contains(factoryId)) {
			// BeanDefinitionRegistryPostProcessor hook apparently not supported...
			// Simply call processConfigurationClasses lazily at this point then.
			processConfigBeanDefinitions((BeanDefinitionRegistry) beanFactory);
		}

		/**
		 * 增强配置类
		 **/
		enhanceConfigurationClasses(beanFactory);
		/**
		 * 可以通过这种方式添加一个 BeanPostProcessor
		 * 除了之前那种方式，还可以通过这种方式添加
		 * {@link AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)}
		 **/
		beanFactory.addBeanPostProcessor(new ImportAwareBeanPostProcessor(beanFactory));
	}

	/**
	 * 基于配置类的注册表构建并验证配置模型
	 *
	 * Build and validate a configuration model based on the registry of
	 * {@link Configuration} classes.
	 */
	public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {

		// 存放候选配置类的 BeanDefinitionHolder 集合
		List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
		/**
		 * 获取容器中注册的所有 BeanDefinition 名称
		 *
		 * 此时包括 AnnotatedBeanDefinitionReader 扫描的 5 个或者 6 个
		 * 和初始化 Spring 上下文环境时传入的一个配置类
		 */
		String[] candidateNames = registry.getBeanDefinitionNames();

		/**
		 * 遍历获取需要解析的类，即获取配置类
		 *
		 * 加了 @Configuration @Pointcut 注解的候选类有 configurationClass 属性值，且值为 full
		 * 不是接口
		 * 加了 @Component @ComponentScan @Import @ImportResource 注解
		 * 有被 @Bean 注解标注的方法 注解的候选类也有 configurationClass 属性值，但值为 lite
		 *
		 * 后面会对 full 属性值的配置类进行 CGLIB 增强
		 * {@see org.springframework.context.annotation.ConfigurationClassPostProcessor#enhanceConfigurationClasses(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)}
		 **/
		for (String beanName : candidateNames) {
			// 获取 BeanDefinition
			BeanDefinition beanDef = registry.getBeanDefinition(beanName);
			/**
			 * 如果 BeanDefinition 中的 configurationClass 属性为 full 或者 lite，则表示已经解析过了，直接跳过
			 *
			 * 因为解析过的话，Spring 会将 configurationClass 属性值设为 full 或者 lite
			 * {@link ConfigurationClassUtils#checkConfigurationClassCandidate(org.springframework.beans.factory.config.BeanDefinition, org.springframework.core.type.classreading.MetadataReaderFactory)}
			 */
			if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
				}
			}
			/**
			 * 否则判断是否是 @Configuration 注解配置类，加了则通过校验，没加再判断是否加了以下注解
			 *
			 * candidateIndicators.add(Component.class.getName());
			 * candidateIndicators.add(ComponentScan.class.getName());
			 * candidateIndicators.add(Import.class.getName());
			 * candidateIndicators.add(ImportResource.class.getName());
			 *
			 * 如果加了 @Configuration 注解，会在后面再解析其他注解；如果没加，只会单独解析相应的注解
			 *
			 * 此时只有传进来的配置类会执行
			 */
			else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
				// 有的话就添加到 BeanDefinitionHolder 集合中【此时传入的配置类会添加进集合中】
				configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
			}
		}

		// Return immediately if no @Configuration classes were found
		// 如果没有发现其他的 @Configuration 类就立即返回
		if (configCandidates.isEmpty()) {
			return;
		}

		// Sort by previously determined @Order value, if applicable
		// 按先前确定的 @Order 值排序
		configCandidates.sort((bd1, bd2) -> {
			int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
			int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
			return Integer.compare(i1, i2);
		});

		// Detect any custom bean name generation strategy supplied through the enclosing application context
		/**
		 * 检测通过封闭的应用程序上下文提供的任何自定义 bean 名称生成策略
		 */
		SingletonBeanRegistry sbr = null;
		/**
		 * 如果 BeanDefinitionRegistry 是 SingletonBeanRegistry 的子类
		 *
		 * 传入的是 DefaultListableBeanFactory 是 SingletonBeanRegistry 的子类
		 */
		if (registry instanceof SingletonBeanRegistry) {
			sbr = (SingletonBeanRegistry) registry;
			// 没有自定义的 name
			if (!this.localBeanNameGeneratorSet) {
				/**
				 * 获取 BeanNameGenerator
				 *
				 * SingletonBeanRegistry 中是否包含 beanName 为 internalConfigurationBeanNameGenerator 的对象
				 **/
				BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
						AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
				// 有则使用 ConfigurationBeanNameGenerator，没有则使用默认的 AnnotationBeanNameGenerator
				if (generator != null) {
					this.componentScanBeanNameGenerator = generator;
					this.importBeanNameGenerator = generator;
				}
			}
		}

		if (this.environment == null) {
			this.environment = new StandardEnvironment();
		}

		/**
		 * Parse each @Configuration class
		 *
		 * 实例化 ConfigurationClassParser 对象，用于解析 @Configuration 类
		 **/
		ConfigurationClassParser parser = new ConfigurationClassParser(
				this.metadataReaderFactory,
				this.problemReporter,
				this.environment,
				this.resourceLoader,
				this.componentScanBeanNameGenerator,
				registry
		);

		/**
		 * 定义两个集合
		 * candidates 集合用于将之前加入的候选配置类进行去重，因为可能有重复的
		 * alreadyParsed 用于存放已解析过的配置类
		 */
		Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
		// 因为用 do-while 循环进行解析，所以初始容量为 configCandidates.size()
		Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
		do {
			/**
			 * 解析配置类【重要】
			 *
			 * 只注册包扫描器扫描到的 BeanDefinition
			 */
			parser.parse(candidates);
			/**
			 * 验证配置类
			 *
			 * 主要验证 @Configuration 的 @Bean 方法是否是静态和可覆盖的
			 * 静态则跳过验证
			 * @Configuration 类中的实例 @Bean 方法必须可以覆盖才能容纳 CGLIB
			 */
			parser.validate();

			/**
			 * 去重
			 *
			 * 此时加了注解的普通类已经注册完成，包括 @Configuration 配置类
			 * @Bean 方法定义的类不在里面，
			 * @Import 引入的类在里面
			 * 可以认为注册了所有加了 @Component 注解的组件
			 */
			Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
			// 移除所有已解析的
			configClasses.removeAll(alreadyParsed);

			/**
			 * Read the model and create bean definitions based on its content
			 * 读取模型并根据其内容创建 BeanDefinition
			 **/
			if (this.reader == null) {
				// 实例化 ConfigurationClassBeanDefinitionReader 阅读器，用于创建 BeanDefinitions
				this.reader = new ConfigurationClassBeanDefinitionReader(
						registry,
						this.sourceExtractor,
						this.resourceLoader,
						this.environment,
						this.importBeanNameGenerator,
						parser.getImportRegistry()
				);
			}
			/**
			 * 加载 BeanDefinitions 存入集合中【重要】
			 *
			 * 注册 @Bean，ImportSelector，@ImportResource，ImportBeanDefinitionRegistrar 的 BeanDefinition
			 **/
			this.reader.loadBeanDefinitions(configClasses);
			// 全部标记为已处理
			alreadyParsed.addAll(configClasses);
			// 清空候选者集合，后面保存经过校验存在是配置类的候选者 并且 没有处理过
			candidates.clear();

			/**
			 * 处理后工厂内的 BeanDefinition 数量大于处理前的数量
			 *
			 * 判断是否已解析完，candidateNames.length 为 6 或者 7
			 **/
			if (registry.getBeanDefinitionCount() > candidateNames.length) {
				// 处理后 BeanFactory 中 beanDefinitionNames 数据
				String[] newCandidateNames = registry.getBeanDefinitionNames();
				// 处理前 BeanFactory 中 beanDefinitionNames 数据
				Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));

				/**
				 * alreadyParsed + oldCandidateNames = newCandidateNames
				 */
				Set<String> alreadyParsedClasses = new HashSet<>();

				// 已解析的 @Component 注解组件类添加到 alreadyParsedClasses 集合中
				for (ConfigurationClass configurationClass : alreadyParsed) {
					alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
				}

				/**
				 * 循环遍历处理后的 beanDefinitionNames
				 */
				for (String candidateName : newCandidateNames) {
					/**
					 * 过滤出处理后的候选者中存在于处理前的候选者，即 alreadyParsed 中候选者
					 *
					 * 为什么不直接遍历 alreadyParsed 集合，而是通过这种方式？
					 * 可能是 Spring 怕当前正在解析的时候，又手动添加了新的需要解析的类
					 **/
					if (!oldCandidateNames.contains(candidateName)) {
						BeanDefinition bd = registry.getBeanDefinition(candidateName);
						// 再次检查 BeanDefinition 是否是配置类的候选者 并且 没有处理过
						if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory)
								&& !alreadyParsedClasses.contains(bd.getBeanClassName())) {
							candidates.add(new BeanDefinitionHolder(bd, candidateName));
						}
					}
				}
				// 标记所有候选者已处理完成
				candidateNames = newCandidateNames;
			}
		}
		// 全部解析完结束循环
		while (!candidates.isEmpty());

		/**
		 * Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
		 * 将 ImportRegistry 注册为 bean 以支持 ImportAware 的 @Configuration 类
		 */
		if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
			sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
		}

		// 如果是 CachingMetadataReaderFactory 的子类，这里会进入判断
		if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
			// Clear cache in externally provided MetadataReaderFactory; this is a no-op
			// for a shared cache since it'll be cleared by the ApplicationContext.
			// 清除外部提供的 MetadataReaderFactory 缓存
			// 这是一个无操作的共享缓存，因为它会通过ApplicationContext中被清除
			((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
		}
	}

	/**
	 * Post-processes a BeanFactory in search of Configuration class BeanDefinitions;
	 * any candidates are then enhanced by a {@link ConfigurationClassEnhancer}.
	 * Candidate status is determined by BeanDefinition attribute metadata.
	 * @see ConfigurationClassEnhancer
	 */
	public void enhanceConfigurationClasses(ConfigurableListableBeanFactory beanFactory) {
		// 存放配置类的 Map
		Map<String, AbstractBeanDefinition> configBeanDefs = new LinkedHashMap<>();
		// 遍历所有找到的 beanDefinitionNames
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			// 获取 BeanDefinition
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
			// 获取 configurationClass 属性值
			Object configClassAttr = beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE);

			MethodMetadata methodMetadata = null;
			if (beanDef instanceof AnnotatedBeanDefinition) {
				methodMetadata = ((AnnotatedBeanDefinition) beanDef).getFactoryMethodMetadata();
			}
			/**
			 * 配置类（完整或精简版）或配置类派生的 @Bean 方法
			 *
			 * AbstractBeanDefinition 的子类有
			 * 1、GenericBeanDefinition
			 * 2、AnnotatedGenericBeanDefinition
			 * 3、ScannedGenericBeanDefinition
			 **/
			if ((configClassAttr != null || methodMetadata != null) && beanDef instanceof AbstractBeanDefinition) {
				// Configuration class (full or lite) or a configuration-derived @Bean method
				// -> resolve bean class at this point...
				AbstractBeanDefinition abd = (AbstractBeanDefinition) beanDef;
				if (!abd.hasBeanClass()) {
					try {
						/**
						 * 解析 beanClass
						 **/
						abd.resolveBeanClass(this.beanClassLoader);
					}
					catch (Throwable ex) {
						throw new IllegalStateException(
								"Cannot load configuration class: " + beanDef.getBeanClassName(), ex);
					}
				}
			}
			// 如果是完整版配置类
			if (ConfigurationClassUtils.CONFIGURATION_CLASS_FULL.equals(configClassAttr)) {
				/**
				 * 如果不是 AbstractBeanDefinition 的子类则抛异常
				 **/
				if (!(beanDef instanceof AbstractBeanDefinition)) {
					throw new BeanDefinitionStoreException("Cannot enhance @Configuration bean definition '" +
							beanName + "' since it is not stored in an AbstractBeanDefinition subclass");
				}
				else if (logger.isInfoEnabled() && beanFactory.containsSingleton(beanName)) {
					logger.info("Cannot enhance @Configuration bean definition '" + beanName +
							"' since its singleton instance has been created too early. The typical cause " +
							"is a non-static @Bean method with a BeanDefinitionRegistryPostProcessor " +
							"return type: Consider declaring such methods as 'static'.");
				}
				// 添加到集合中
				configBeanDefs.put(beanName, (AbstractBeanDefinition) beanDef);
			}
		}

		if (configBeanDefs.isEmpty()) {
			// nothing to enhance -> return immediately
			return;
		}

		// 实例化 ConfigurationClassEnhancer，用于增强配置类
		ConfigurationClassEnhancer enhancer = new ConfigurationClassEnhancer();
		// 遍历所有的配置类
		for (Map.Entry<String, AbstractBeanDefinition> entry : configBeanDefs.entrySet()) {
			AbstractBeanDefinition beanDef = entry.getValue();
			// If a @Configuration class gets proxied, always proxy the target class
			beanDef.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
			// Set enhanced subclass of the user-specified bean class
			Class<?> configClass = beanDef.getBeanClass();
			// 增强类
			Class<?> enhancedClass = enhancer.enhance(configClass, this.beanClassLoader);
			if (configClass != enhancedClass) {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Replacing bean definition '%s' existing class '%s' with " +
							"enhanced class '%s'", entry.getKey(), configClass.getName(), enhancedClass.getName()));
				}

				beanDef.setBeanClass(enhancedClass);
			}
		}
	}

	private static class ImportAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

		private final BeanFactory beanFactory;

		public ImportAwareBeanPostProcessor(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public PropertyValues postProcessProperties(@Nullable PropertyValues pvs, Object bean, String beanName) {
			// Inject the BeanFactory before AutowiredAnnotationBeanPostProcessor's
			// postProcessProperties method attempts to autowire other configuration beans.
			if (bean instanceof EnhancedConfiguration) {
				((EnhancedConfiguration) bean).setBeanFactory(this.beanFactory);
			}
			return pvs;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			if (bean instanceof ImportAware) {
				ImportRegistry ir = this.beanFactory.getBean(IMPORT_REGISTRY_BEAN_NAME, ImportRegistry.class);
				AnnotationMetadata importingClass = ir.getImportingClassFor(ClassUtils.getUserClass(bean).getName());
				if (importingClass != null) {
					((ImportAware) bean).setImportMetadata(importingClass);
				}
			}
			return bean;
		}

	}

}
