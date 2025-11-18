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

package org.springframework.context;

import java.util.Locale;

import org.springframework.lang.Nullable;

/**
 * Strategy interface for resolving messages, with support for the parameterization
 * and internationalization of such messages.
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li>{@link org.springframework.context.support.ResourceBundleMessageSource}: built
 * on top of the standard {@link java.util.ResourceBundle}, sharing its limitations.
 * <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource}:
 * highly configurable, in particular with respect to reloading message definitions.
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 * 
 * MessageSource是解析消息的策略接口，支持消息的参数化和国际化。
 * 
 * Spring提供了两个开箱即用的生产实现：
 * 1. ResourceBundleMessageSource：基于标准的ResourceBundle构建，共享其局限性
 * 2. ReloadableResourceBundleMessageSource：高度可配置，特别是在重新加载消息定义方面
 */
public interface MessageSource {

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 * @param code the message code to look up, for example, 'calculator.noRateSet'.
	 * MessageSource users are encouraged to base message names on qualified class
	 * or package names, avoiding potential conflicts and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none
	 * @param defaultMessage a default message to return if the lookup fails
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message if the lookup was successful, otherwise
	 * the default message passed as a parameter (which may be {@code null})
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see java.text.MessageFormat
	 * 
	 * 尝试解析消息。如果未找到消息则返回默认消息。
	 * code：要查找的消息代码，例如'calculator.noRateSet'
	 * MessageSource用户被鼓励基于限定类或包名称来命名消息，以避免潜在冲突并确保最大清晰度
	 * args：将填充到消息中参数的参数数组（消息中的参数看起来像"{0}"、"{1,date}"、"{2,time}"），如果没有则为null
	 * defaultMessage：如果查找失败要返回的默认消息
	 * locale：进行查找的语言环境
	 * 返回：如果查找成功则返回解析的消息，否则返回作为参数传递的默认消息（可能为null）
	 */
	@Nullable
	String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, @Nullable Locale locale);

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * @param code the message code to look up, for example, 'calculator.noRateSet'.
	 * MessageSource users are encouraged to base message names on qualified class
	 * or package names, avoiding potential conflicts and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message (never {@code null})
	 * @throws NoSuchMessageException if no corresponding message was found
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see java.text.MessageFormat
	 * 
	 * 尝试解析消息。如果找不到消息则视为错误。
	 * code：要查找的消息代码，例如'calculator.noRateSet'
	 * MessageSource用户被鼓励基于限定类或包名称来命名消息，以避免潜在冲突并确保最大清晰度
	 * args：将填充到消息中参数的参数数组（消息中的参数看起来像"{0}"、"{1,date}"、"{2,time}"），如果没有则为null
	 * locale：进行查找的语言环境
	 * 返回：解析的消息（永不为null）
	 * 如果未找到相应消息则抛出NoSuchMessageException
	 */
	String getMessage(String code, @Nullable Object[] args, @Nullable Locale locale) throws NoSuchMessageException;

	/**
	 * Try to resolve the message using all the attributes contained within the
	 * {@code MessageSourceResolvable} argument that was passed in.
	 * <p>NOTE: We must throw a {@code NoSuchMessageException} on this method
	 * since at the time of calling this method we aren't able to determine if the
	 * {@code defaultMessage} property of the resolvable is {@code null} or not.
	 * @param resolvable the value object storing attributes required to resolve a message
	 * (may include a default message)
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message (never {@code null} since even a
	 * {@code MessageSourceResolvable}-provided default message needs to be non-null)
	 * @throws NoSuchMessageException if no corresponding message was found
	 * (and no default message was provided by the {@code MessageSourceResolvable})
	 * @see MessageSourceResolvable#getCodes()
	 * @see MessageSourceResolvable#getArguments()
	 * @see MessageSourceResolvable#getDefaultMessage()
	 * @see java.text.MessageFormat
	 * 
	 * 尝试使用传入的MessageSourceResolvable参数中包含的所有属性来解析消息。
	 * 注意：我们必须在此方法上抛出NoSuchMessageException，因为在调用此方法时
	 * 我们无法确定resolvable的defaultMessage属性是否为null。
	 * resolvable：存储解析消息所需属性的值对象（可能包括默认消息）
	 * locale：进行查找的语言环境
	 * 返回：解析的消息（永不为null，因为即使是MessageSourceResolvable提供的默认消息也必须非null）
	 * 如果未找到相应消息（且MessageSourceResolvable未提供默认消息）则抛出NoSuchMessageException
	 */
	String getMessage(MessageSourceResolvable resolvable, @Nullable Locale locale) throws NoSuchMessageException;

}
