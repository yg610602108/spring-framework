package com.ambition.scanner;

import com.ambition.annotation.AmbitionScan;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Elewin
 * @date 2020-01-14 11:32 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class AmbitionScanner extends ClassPathBeanDefinitionScanner {

	/**
	 * 继承 ClassPathBeanDefinitionScanner 这个类
	 * 可以通过改变属性或者行为，使 Spring 能够扫描我们需要的类
	 * 并转换为 ScannedGenericBeanDefinition 或者 AnnotatedGenericBeanDefinition
	 *
	 * {@link org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#addCandidateComponentsFromIndex(org.springframework.context.index.CandidateComponentsIndex, java.lang.String)}
	 * {@link ClassPathScanningCandidateComponentProvider#scanCandidateComponents(java.lang.String)}
	 *
	 * 应用：可以实现自定义的扫描注解
	 **/

	public AmbitionScanner(BeanDefinitionRegistry registry) {
		super(registry);
	}

	/**
	 * @see ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.core.type.classreading.MetadataReader)
	 **/
	@Override
	public void addIncludeFilter(@NonNull TypeFilter includeFilter) {

		// AnnotationTypeFilter filter = new AnnotationTypeFilter(AmbitionScan.class);

		super.addIncludeFilter(includeFilter);
	}

	/**
	 * 扩展点之一，通过过滤器使其满足条件
	 *
	 * @see ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.core.type.classreading.MetadataReader)
	 **/
	@Override
	protected boolean isCandidateComponent(@NonNull MetadataReader metadataReader) throws IOException {
		return super.isCandidateComponent(metadataReader);
	}

	/**
	 * 扩展点之二，通过判断使其满足条件
	 *
	 * @see ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)
	 *
	 * MyBatis 的扩展代码：
	 * {@code return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();}
	 **/
	@Override
	protected boolean isCandidateComponent(@NonNull AnnotatedBeanDefinition beanDefinition) {
		// 获取注解元数据
		AnnotationMetadata metadata = beanDefinition.getMetadata();

		return metadata.isInterface();
		// return super.isCandidateComponent(beanDefinition);
	}
}
