package com.ambition.event;

import org.springframework.context.ApplicationEvent;

/**
 * 创建一个事件
 *
 * @author Elewin
 * @date 2020-04-07 12:35 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class AmbitionEvent extends ApplicationEvent {

	/**
	 * Create a new {@code ApplicationEvent}.
	 * @param source the object on which the event initially occurred or with
	 * which the event is associated (never {@code null})
	 */
	public AmbitionEvent(Object source) {
		super(source);
	}
}
