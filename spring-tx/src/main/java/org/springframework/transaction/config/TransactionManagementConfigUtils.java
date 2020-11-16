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

package org.springframework.transaction.config;

import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration constants for internal sharing across subpackages.
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 3.1
 */
public abstract class TransactionManagementConfigUtils {

	/**
	 * 内部管理的事务通知的 bean 名称（在 mode == PROXY 时使用）
	 * @see EnableTransactionManagement#mode()
	 *
	 * The bean name of the internally managed transaction advisor (used when mode == PROXY).
	 */
	public static final String TRANSACTION_ADVISOR_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAdvisor";

	/**
	 * 内部管理的事务通知的 bean 名称（在 mode == ASPECTJ 时使用）
	 * @see EnableTransactionManagement#mode()
	 *
	 * The bean name of the internally managed transaction aspect (used when mode == ASPECTJ).
	 */
	public static final String TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAspect";

	/**
	 * AspectJ 事务管理切面的类名称
	 *
	 * The class name of the AspectJ transaction management aspect.
	 */
	public static final String TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.AnnotationTransactionAspect";

	/**
	 * AspectJ 事务管理配置类的名称
	 *
	 * The name of the AspectJ transaction management @{@code Configuration} class.
	 */
	public static final String TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration";

	/**
	 * 内部管理的 JTA 事务通知的 bean 名称（在 mode == ASPECTJ 时使用）
	 *
	 * The bean name of the internally managed JTA transaction aspect (used when mode == ASPECTJ).
	 */
	public static final String JTA_TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalJtaTransactionAspect";

	/**
	 * AspectJ 事务管理切面的类名称
	 *
	 * The class name of the AspectJ transaction management aspect.
	 */
	public static final String JTA_TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.JtaAnnotationTransactionAspect";

	/**
	 * JTA 的 AspectJ 事务管理配置类的名称
	 *
	 * The name of the AspectJ transaction management @{@code Configuration} class for JTA.
	 */
	public static final String JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration";

	/**
	 * 内部管理的 TransactionalEventListenerFactory 的 bean 名称
	 *
	 * The bean name of the internally managed TransactionalEventListenerFactory.
	 */
	public static final String TRANSACTIONAL_EVENT_LISTENER_FACTORY_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionalEventListenerFactory";

}
