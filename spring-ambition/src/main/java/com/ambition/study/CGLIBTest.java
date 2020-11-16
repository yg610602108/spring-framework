package com.ambition.study;

import com.ambition.interceptor.CglibInterceptor;
import com.ambition.service.AService;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;

/**
 * @author Elewin
 * @date 2020-01-23 2:12 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class CGLIBTest {

	public static void main(String[] args) {
		Enhancer enhancer = new Enhancer();
		// 设置父类
		enhancer.setSuperclass(AService.class);
		// 是否使用工厂
		enhancer.setUseFactory(false);
		// 名字生成策略
		enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
		// 设置回调
		enhancer.setCallback(new CglibInterceptor());

		AService service = (AService) enhancer.create();
	}
}
