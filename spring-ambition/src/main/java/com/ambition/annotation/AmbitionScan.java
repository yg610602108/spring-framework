package com.ambition.annotation;

import com.ambition.registrar.AmbitionBeanDefinitionRegistrar;
import org.mybatis.spring.annotation.MapperScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Elewin
 * @date 2020-01-14 11:38 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
//@Import(MapperScannerRegistrar.class)
@Import(AmbitionBeanDefinitionRegistrar.class)
public @interface AmbitionScan {

	String[] value() default {};
}
