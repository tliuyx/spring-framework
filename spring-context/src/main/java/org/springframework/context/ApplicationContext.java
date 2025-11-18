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

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

/**
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 * 
 * ApplicationContext是为应用程序提供配置的中心接口。
 * 在应用程序运行时是只读的，但如果实现支持，可以重新加载。
 * 
 * ApplicationContext提供：
 * 1. 访问应用程序组件的Bean工厂方法。继承自ListableBeanFactory。
 * 2. 以通用方式加载文件资源的能力。继承自ResourceLoader接口。
 * 3. 向注册的监听器发布事件的能力。继承自ApplicationEventPublisher接口。
 * 4. 解析消息的能力，支持国际化。继承自MessageSource接口。
 * 5. 从父上下文继承。后代上下文中的定义总是优先。
 *    这意味着，例如，整个Web应用程序可以使用单个父上下文，
 *    而每个servlet都有自己的子上下文，独立于任何其他servlet。
 * 
 * 除了标准BeanFactory生命周期功能外，ApplicationContext实现还会检测并调用
 * ApplicationContextAware beans以及ResourceLoaderAware、
 * ApplicationEventPublisherAware和MessageSourceAware beans。
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the unique id of this application context.
	 * @return the unique id of the context, or {@code null} if none
	 * 
	 * 返回此应用程序上下文的唯一ID。
	 * 返回上下文的唯一ID，如果没有则返回null
	 */
	@Nullable
	String getId();

	/**
	 * Return a name for the deployed application that this context belongs to.
	 * @return a name for the deployed application, or the empty String by default
	 * 
	 * 返回此上下文所属的已部署应用程序的名称。
	 * 返回已部署应用程序的名称，默认为空字符串
	 */
	String getApplicationName();

	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context (never {@code null})
	 * 
	 * 返回此上下文的友好名称。
	 * 返回此上下文的显示名称（永不为null）
	 */
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 * 
	 * 返回此上下文首次加载的时间戳。
	 * 返回此上下文首次加载的时间戳（毫秒）
	 */
	long getStartupDate();

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or {@code null} if there is no parent
	 * 
	 * 返回父上下文，如果没有父上下文且这是上下文层次结构的根，则返回null。
	 * 返回父上下文，如果没有父上下文则返回null
	 */
	@Nullable
	ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * <p>This is not typically used by application code, except for the purpose of
	 * initializing bean instances that live outside the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * {@link AutowireCapableBeanFactory} interface too. The present method mainly
	 * serves as a convenient, specific facility on the ApplicationContext interface.
	 * <p><b>NOTE: As of 4.2, this method will consistently throw IllegalStateException
	 * after the application context has been closed.</b> In current Spring Framework
	 * versions, only refreshable application contexts behave that way; as of 4.2,
	 * all application context implementations will be required to comply.
	 * @return the AutowireCapableBeanFactory for this context
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (for example, if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 * 
	 * 为此上下文暴露AutowireCapableBeanFactory功能。
	 * 应用程序代码通常不使用此功能，除非是为了初始化位于应用程序上下文外部的bean实例，
	 * 对其应用Spring bean生命周期(全部或部分)。
	 * 或者，ConfigurableApplicationContext接口暴露的内部BeanFactory也提供对
	 * AutowireCapableBeanFactory接口的访问。当前方法主要作为ApplicationContext接口上
	 * 方便、特定的工具。
	 * 
	 * 注意：从4.2版本开始，应用程序上下文关闭后此方法将始终抛出IllegalStateException。
	 * 在当前Spring框架版本中，只有可刷新的应用程序上下文表现如此；从4.2版本开始，
	 * 所有应用程序上下文实现都将被要求遵守。
	 * 
	 * 返回此上下文的AutowireCapableBeanFactory
	 * 如果上下文不支持AutowireCapableBeanFactory接口，
	 * 或者尚未持有可自动装配的bean工厂（例如，如果从未调用过refresh()），
	 * 或者上下文已经关闭，则抛出IllegalStateException
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
