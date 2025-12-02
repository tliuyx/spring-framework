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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions <i>before</i> regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 *
 * <p>BeanDefinitionRegistryPostProcessor扩展了标准的BeanFactoryPostProcessor SPI，
 * 允许在常规BeanFactoryPostProcessor检测开始<i>之前</i>注册更多的Bean定义。
 * 特别是，BeanDefinitionRegistryPostProcessor可以注册更多的Bean定义，
 * 而这些Bean定义又可以定义BeanFactoryPostProcessor实例。
 *
 * <p>The most notable implementation of this interface is
 * {@link org.springframework.context.annotation.ConfigurationClassPostProcessor},
 * which is responsible for processing {@code @Configuration} classes and
 * registering the bean definitions declared through {@code @Bean} methods.
 *
 * <p>此接口最著名的实现是
 * {@link org.springframework.context.annotation.ConfigurationClassPostProcessor}，
 * 它负责处理{@code @Configuration}类并通过{@code @Bean}方法注册声明的Bean定义。
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 * 
	 * 在标准初始化之后修改应用程序上下文的内部Bean定义注册表。
	 * 所有常规Bean定义都将已加载，但还没有Bean被实例化。
	 * 这允许在下一个后处理阶段开始之前添加更多的Bean定义。
	 * 
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

	/**
	 * Empty implementation of {@link BeanFactoryPostProcessor#postProcessBeanFactory}
	 * since custom {@code BeanDefinitionRegistryPostProcessor} implementations will
	 * typically only provide a {@link #postProcessBeanDefinitionRegistry} method.
	 * @since 6.1
	 */
	@Override
	default void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

}
