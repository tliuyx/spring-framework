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

package org.springframework.core.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link ResourceLoader} interface.
 *
 * <p>Used by {@link ResourceEditor}, and serves as base class for
 * {@link org.springframework.context.support.AbstractApplicationContext}.
 * Can also be used standalone.
 *
 * <p>Will return a {@link UrlResource} if the location value is a URL,
 * and a {@link ClassPathResource} if it is a non-URL path or a
 * "classpath:" pseudo-URL.
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see FileSystemResourceLoader
 * @see org.springframework.context.support.ClassPathXmlApplicationContext
 * 
 * DefaultResourceLoader是ResourceLoader接口的默认实现，是Spring资源加载的核心类。
 * 
 * 主要功能：
 * 1. 根据资源路径加载不同类型的Resource
 * 2. 支持自定义协议解析器
 * 3. 提供资源缓存机制
 * 
 * 资源加载策略：
 * 1. 以"/"开头的路径：调用getResourceByPath处理
 * 2. 以"classpath:"开头的路径：创建ClassPathResource
 * 3. URL路径：创建UrlResource或FileUrlResource
 * 4. 其他路径：尝试解析为URL，失败则作为普通资源路径处理
 */
public class DefaultResourceLoader implements ResourceLoader {

	@Nullable
	private ClassLoader classLoader;

	private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);

	private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap<>(4);


	/**
	 * Create a new DefaultResourceLoader.
	 * <p>ClassLoader access will happen using the thread context class loader
	 * at the time of actual resource access. For more control, pass
	 * a specific ClassLoader to {@link #DefaultResourceLoader(ClassLoader)}.
	 * @see java.lang.Thread#getContextClassLoader()
	 * 
	 * 创建一个新的DefaultResourceLoader。
	 * ClassLoader访问将在实际资源访问时使用线程上下文类加载器。
	 * 如需更多控制，可以将特定的ClassLoader传递给DefaultResourceLoader(ClassLoader)。
	 */
	public DefaultResourceLoader() {
	}

	/**
	 * Create a new DefaultResourceLoader.
	 * @param classLoader the ClassLoader to load class path resources with, or {@code null}
	 * for using the thread context class loader at the time of actual resource access
	 * 
	 * 创建一个新的DefaultResourceLoader。
	 * classLoader：用于加载类路径资源的ClassLoader，如果为null则在实际资源访问时使用线程上下文类加载器
	 */
	public DefaultResourceLoader(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	/**
	 * Specify the ClassLoader to load class path resources with, or {@code null}
	 * for using the thread context class loader at the time of actual resource access.
	 * <p>The default is that ClassLoader access will happen using the thread context
	 * class loader at the time of actual resource access.
	 * 
	 * 指定用于加载类路径资源的ClassLoader，如果为null则在实际资源访问时使用线程上下文类加载器。
	 * 默认情况下，ClassLoader访问将在实际资源访问时使用线程上下文类加载器。
	 */
	public void setClassLoader(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Return the ClassLoader to load class path resources with.
	 * <p>Will get passed to ClassPathResource's constructor for all
	 * ClassPathResource objects created by this resource loader.
	 * @see ClassPathResource
	 * 
	 * 返回用于加载类路径资源的ClassLoader。
	 * 将传递给由此资源加载器创建的所有ClassPathResource对象的构造函数。
	 */
	@Override
	@Nullable
	public ClassLoader getClassLoader() {
		return (this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Register the given resolver with this resource loader, allowing for
	 * additional protocols to be handled.
	 * <p>Any such resolver will be invoked ahead of this loader's standard
	 * resolution rules. It may therefore also override any default rules.
	 * @since 4.3
	 * @see #getProtocolResolvers()
	 * 
	 * 向此资源加载器注册给定的解析器，允许处理其他协议。
	 * 任何此类解析器都将在该加载器的标准解析规则之前调用，因此也可能覆盖任何默认规则。
	 */
	public void addProtocolResolver(ProtocolResolver resolver) {
		Assert.notNull(resolver, "ProtocolResolver must not be null");
		this.protocolResolvers.add(resolver);
	}

	/**
	 * Return the collection of currently registered protocol resolvers,
	 * allowing for introspection as well as modification.
	 * @since 4.3
	 * @see #addProtocolResolver(ProtocolResolver)
	 * 
	 * 返回当前注册的协议解析器集合，允许进行检查和修改。
	 */
	public Collection<ProtocolResolver> getProtocolResolvers() {
		return this.protocolResolvers;
	}

	/**
	 * Obtain a cache for the given value type, keyed by {@link Resource}.
	 * @param valueType the value type, for example, an ASM {@code MetadataReader}
	 * @return the cache {@link Map}, shared at the {@code ResourceLoader} level
	 * @since 5.0
	 * 
	 * 获取给定值类型的缓存，以Resource为键。
	 * valueType：值类型，例如ASM的MetadataReader
	 * 返回ResourceLoader级别的共享缓存Map
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<Resource, T> getResourceCache(Class<T> valueType) {
		return (Map<Resource, T>) this.resourceCaches.computeIfAbsent(valueType, key -> new ConcurrentHashMap<>());
	}

	/**
	 * Clear all resource caches in this resource loader.
	 * @since 5.0
	 * @see #getResourceCache
	 * 
	 * 清除此资源加载器中的所有资源缓存。
	 */
	public void clearResourceCaches() {
		this.resourceCaches.clear();
	}


	/**
	 * 核心方法：根据位置字符串获取Resource对象
	 * 这是ResourceLoader接口的核心实现方法，根据不同的位置前缀返回不同类型的Resource
	 *
	 * 1. 首先检查是否有自定义的协议解析器可以处理该位置
	 * 2. 如果位置以"/"开头，则调用getResourceByPath方法处理
	 * 3. 如果位置以"classpath:"开头，则创建ClassPathResource
	 * 4. 否则尝试将位置解析为URL，成功则创建UrlResource或FileUrlResource
	 * 5. 如果URL解析失败，则调用getResourceByPath方法处理
	 */
	@Override
	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");

		// 遍历所有自定义协议解析器，看是否有解析器可以处理该位置
		for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
			Resource resource = protocolResolver.resolve(location, this);
			if (resource != null) {
				return resource;
			}
		}

		// 根据位置字符串的前缀决定创建哪种类型的Resource
		if (location.startsWith("/")) {
			// 以"/"开头的路径，调用getResourceByPath处理
			return getResourceByPath(location);
		}
		else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			// 以"classpath:"开头的路径，创建ClassPathResource
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
		}
		else {
			try {
				// Try to parse the location as a URL...
				// 尝试将位置解析为URL
				URL url = ResourceUtils.toURL(location);
				return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
			}
			catch (MalformedURLException ex) {
				// No URL -> resolve as resource path.
				// URL解析失败，作为普通资源路径处理
				return getResourceByPath(location);
			}
		}
	}

	/**
	 * Return a Resource handle for the resource at the given path.
	 * <p>The default implementation supports class path locations. This should
	 * be appropriate for standalone implementations but can be overridden,
	 * for example, for implementations targeted at a Servlet container.
	 * @param path the path to the resource
	 * @return the corresponding Resource handle
	 * @see ClassPathResource
	 * @see org.springframework.context.support.FileSystemXmlApplicationContext#getResourceByPath
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#getResourceByPath
	 * 
	 * 返回给定路径处资源的Resource句柄。
	 * 默认实现支持类路径位置。这对于独立实现是合适的，但可以被重写，
	 * 例如，针对Servlet容器的实现。
	 */
	protected Resource getResourceByPath(String path) {
		return new ClassPathContextResource(path, getClassLoader());
	}


	/**
	 * ClassPathResource that explicitly expresses a context-relative path
	 * through implementing the ContextResource interface.
	 * 
	 * 通过实现ContextResource接口明确表示上下文相关路径的ClassPathResource。
	 * 这是DefaultResourceLoader的内部类，用于表示上下文相关的类路径资源。
	 */
	protected static class ClassPathContextResource extends ClassPathResource implements ContextResource {

		public ClassPathContextResource(String path, @Nullable ClassLoader classLoader) {
			super(path, classLoader);
		}

		@Override
		public String getPathWithinContext() {
			return getPath();
		}

		/**
		 * 创建相对于当前资源的资源
		 * 使用StringUtils.applyRelativePath来处理相对路径的计算
		 */
		@Override
		public Resource createRelative(String relativePath) {
			String pathToUse = StringUtils.applyRelativePath(getPath(), relativePath);
			return new ClassPathContextResource(pathToUse, getClassLoader());
		}
	}

}
