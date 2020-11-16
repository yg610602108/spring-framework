package com.ambition.study;

import com.ambition.config.AmbitionConfig;
import com.ambition.event.AmbitionEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Elewin
 * @date 2020-04-07 12:05 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class ListenerTest {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(AmbitionConfig.class);

		// 发布事件
		context.publishEvent(new AmbitionEvent("手动发布了一个事件"));

		context.close();
	}
}
