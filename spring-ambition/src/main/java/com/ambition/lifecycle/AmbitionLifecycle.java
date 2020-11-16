package com.ambition.lifecycle;

import org.springframework.context.SmartLifecycle;

/**
 * 容器的生命周期
 *
 * @author Elewin
 * @date 2020-01-07 8:20 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
// @Component
public class AmbitionLifecycle implements /*Lifecycle*/ SmartLifecycle {

	private boolean flag = false;

	@Override
	public void start() {
		System.out.println("start");
		flag = true;
	}

	@Override
	public void stop() {
		System.out.println("stop");
		flag = false;
	}

	@Override
	public boolean isRunning() {
		return flag;
	}

	/**
	 * 自动执行
	 **/
	@Override
	public boolean isAutoStartup() {
		return true;
	}

	/**
	 * 容器停止时会调用这个方法
	 **/
	@Override
	public void stop(Runnable callback) {
		System.out.println("XXX stop");
		// 不调用这个方法，Spring 容器会认为还有事情没做完，会等待 30s 后再停止
		callback.run();
	}

	/**
	 * 相当于执行顺序
	 **/
	@Override
	public int getPhase() {
		return DEFAULT_PHASE;
	}
}
