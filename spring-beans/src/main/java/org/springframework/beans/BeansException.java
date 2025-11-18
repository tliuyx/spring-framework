/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.beans;

import org.springframework.core.NestedRuntimeException;
import org.springframework.lang.Nullable;

/**
 * Abstract superclass for all exceptions thrown in the beans package
 * and subpackages.
 *
 * <p>Note that this is a runtime (unchecked) exception. Beans exceptions
 * are usually fatal; there is no reason for them to be checked.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * 
 * BeansException是beans包及其子包中抛出的所有异常的抽象超类。
 * 
 * 注意这是一个运行时（未检查）异常。Beans异常通常是致命的；
 * 没有理由让它们成为检查型异常。
 */
@SuppressWarnings("serial")
public abstract class BeansException extends NestedRuntimeException {

	/**
	 * Create a new BeansException with the specified message.
	 * @param msg the detail message
	 * 
	 * 使用指定消息创建新的BeansException。
	 */
	public BeansException(String msg) {
		super(msg);
	}

	/**
	 * Create a new BeansException with the specified message
	 * and root cause.
	 * @param msg the detail message
	 * @param cause the root cause
	 * 
	 * 使用指定消息和根本原因创建新的BeansException。
	 */
	public BeansException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
