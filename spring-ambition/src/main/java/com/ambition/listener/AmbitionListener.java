package com.ambition.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 创建一个事件监听器
 *
 * @author Elewin
 * @date 2020-04-07 12:03 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
//@Component
public class AmbitionListener implements ApplicationListener {

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		System.out.println(AmbitionListener.class.getName() + "监听器接收到了事件 ：" + event);
	}
}
