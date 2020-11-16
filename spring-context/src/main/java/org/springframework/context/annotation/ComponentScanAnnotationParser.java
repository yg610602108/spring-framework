/*
 * Copyright 2002-2018 the original author or authors.
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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.*;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Parser for the @{@link ComponentScan} annotation.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 * @see ClassPathBeanDefinitionScanner#scan(String...)
 * @see ComponentScanBeanDefinitionParser
 */
class ComponentScanAnnotationParser {

	private final Environment environment;

	private final ResourceLoader resourceLoader;

	private final BeanNameGenerator beanNameGenerator;

	private final BeanDefinitionRegistry registry;


	public ComponentScanAnnotationParser(Environment environment,
										 ResourceLoader resourceLoader,
										 BeanNameGenerator beanNameGenerator,
										 BeanDefinitionRegistry registry) {

		this.environment = environment;
		this.resourceLoader = resourceLoader;
		this.beanNameGenerator = beanNameGenerator;
		this.registry = registry;
	}

	public Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan,
										   final String declaringClass) {

		/**
		 * 此时又实例化了一个 ClassPathBeanDefinitionScanner
		 * 并没有使用 AnnotationConfigApplicationContext 构造方法中实例化的 ClassPathBeanDefinitionScanner
		 *
		 * 为什么不用构造方法实例化的 ClassPathBeanDefinitionScanner 呢？
		 * 可能是因为那个只是为了让外部能够调用 API 所提供的，外部不能够进行配置修改
		 * Spring 内部的可以进行修改
		 *
		 * @ComponentScan 提供了 useDefaultFilters、includeFilters 和 excludeFilters 等配置
		 * {@link ClassPathBeanDefinitionScanner#ClassPathBeanDefinitionScanner(org.springframework.beans.factory.support.BeanDefinitionRegistry, boolean, org.springframework.core.env.Environment, org.springframework.core.io.ResourceLoader)}
		 */
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
				this.registry,
				componentScan.getBoolean("useDefaultFilters"),
				this.environment,
				this.resourceLoader
		);

		// 获取 BeanNameGenerator
		Class<? extends BeanNameGenerator> generatorClass = componentScan.getClass("nameGenerator");
		boolean useInheritedGenerator = (BeanNameGenerator.class == generatorClass);
		scanner.setBeanNameGenerator(useInheritedGenerator ?
				this.beanNameGenerator : BeanUtils.instantiateClass(generatorClass));

		// 处理代理模型
		ScopedProxyMode scopedProxyMode = componentScan.getEnum("scopedProxy");
		if (scopedProxyMode != ScopedProxyMode.DEFAULT) {
			scanner.setScopedProxyMode(scopedProxyMode);
		}
		else {
			Class<? extends ScopeMetadataResolver> resolverClass = componentScan.getClass("scopeResolver");
			scanner.setScopeMetadataResolver(BeanUtils.instantiateClass(resolverClass));
		}

		scanner.setResourcePattern(componentScan.getString("resourcePattern"));

		/**
		 * 遍历 @ComponentScan 中的 includeFilters
		 * 获取符合组件扫描的条件的类型
		 * Filter[] includeFilters() default {};
		 */
		for (AnnotationAttributes filter : componentScan.getAnnotationArray("includeFilters")) {
			for (TypeFilter typeFilter : typeFiltersFor(filter)) {
				// 添加包含过滤器
				scanner.addIncludeFilter(typeFilter);
			}
		}
		/**
		 * 遍历 @ComponentScan 中的 excludeFilters
		 * 获取不符合组件扫描的条件的类型
		 * Filter[] excludeFilters() default {};
		 */
		for (AnnotationAttributes filter : componentScan.getAnnotationArray("excludeFilters")) {
			for (TypeFilter typeFilter : typeFiltersFor(filter)) {
				// 添加排除过滤器
				scanner.addExcludeFilter(typeFilter);
			}
		}

		/**
		 * 获取 @ComponentScan 中的 lazyInit
		 * 默认不是懒加载
		 * 如果是懒加载，则修改 BeanDefinitionDefaults 的懒加载属性值为 True，而不是修改 BeanDefinition 的懒加载属性值为 True
		 * 为后面应用默认值提供便利
		 * boolean lazyInit() default false;
		 */
		boolean lazyInit = componentScan.getBoolean("lazyInit");
		if (lazyInit) {
			scanner.getBeanDefinitionDefaults().setLazyInit(true);
		}

		// 去重存储扫描的包路径
		Set<String> basePackages = new LinkedHashSet<>();
		/**
		 * 获取 @ComponentScan 中的 basePackages()
		 * String[] basePackages() default {};
		 */
		String[] basePackagesArray = componentScan.getStringArray("basePackages");
		for (String pkg : basePackagesArray) {
			// 处理逗号、分号、回车和换行符
			String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg),
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
			Collections.addAll(basePackages, tokenized);
		}
		/**
		 * 获取 @ComponentScan 中的 basePackageClasses
		 * Class<?>[] basePackageClasses() default {};
		 */
		for (Class<?> clazz : componentScan.getClassArray("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}

		// 如果没有注明扫描路径，则默认扫描 @Configuration 类所在包及其子包
		if (basePackages.isEmpty()) {
			basePackages.add(ClassUtils.getPackageName(declaringClass));
		}

		scanner.addExcludeFilter(new AbstractTypeHierarchyTraversingFilter(false, false) {
			@Override
			protected boolean matchClassName(String className) {
				return declaringClass.equals(className);
			}
		});
		// 上面都是设置扫描器的属性值，这一步才是真正的扫描
		return scanner.doScan(StringUtils.toStringArray(basePackages));
	}

	private List<TypeFilter> typeFiltersFor(AnnotationAttributes filterAttributes) {
		List<TypeFilter> typeFilters = new ArrayList<>();
		FilterType filterType = filterAttributes.getEnum("type");

		for (Class<?> filterClass : filterAttributes.getClassArray("classes")) {
			switch (filterType) {
				case ANNOTATION:
					Assert.isAssignable(Annotation.class, filterClass,
							"@ComponentScan ANNOTATION type filter requires an annotation type");
					@SuppressWarnings("unchecked")
					Class<Annotation> annotationType = (Class<Annotation>) filterClass;
					typeFilters.add(new AnnotationTypeFilter(annotationType));
					break;
				case ASSIGNABLE_TYPE:
					typeFilters.add(new AssignableTypeFilter(filterClass));
					break;
				case CUSTOM:
					Assert.isAssignable(TypeFilter.class, filterClass,
							"@ComponentScan CUSTOM type filter requires a TypeFilter implementation");
					TypeFilter filter = BeanUtils.instantiateClass(filterClass, TypeFilter.class);
					ParserStrategyUtils.invokeAwareMethods(
							filter, this.environment, this.resourceLoader, this.registry);
					typeFilters.add(filter);
					break;
				default:
					throw new IllegalArgumentException("Filter type not supported with Class value: " + filterType);
			}
		}

		for (String expression : filterAttributes.getStringArray("pattern")) {
			switch (filterType) {
				case ASPECTJ:
					typeFilters.add(new AspectJTypeFilter(expression, this.resourceLoader.getClassLoader()));
					break;
				case REGEX:
					typeFilters.add(new RegexPatternTypeFilter(Pattern.compile(expression)));
					break;
				default:
					throw new IllegalArgumentException("Filter type not supported with String pattern: " + filterType);
			}
		}

		return typeFilters;
	}

}
