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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Resource} implementation for class path resources. Uses either a
 * given {@link ClassLoader} or a given {@link Class} for loading resources.
 *
 * <p>Supports resolution as {@code java.io.File} if the class path
 * resource resides in the file system, but not for resources in a JAR.
 * Always supports resolution as {@code java.net.URL}.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 28.12.2003
 * @see ClassLoader#getResourceAsStream(String)
 * @see ClassLoader#getResource(String)
 * @see Class#getResourceAsStream(String)
 * @see Class#getResource(String)
 * 
 * ClassPathResource是Resource接口的实现类，用于加载类路径资源。
 * 它使用给定的ClassLoader或Class来加载资源。
 * 
 * 主要特点：
 * 1. 如果类路径资源位于文件系统中，则支持解析为java.io.File，但不支持JAR中的资源
 * 2. 始终支持解析为java.net.URL
 * 3. 可以通过ClassLoader或Class加载资源
 */
public class ClassPathResource extends AbstractFileResolvingResource {

	/**
	 * Internal representation of the original path supplied by the user,
	 * used for creating relative paths and resolving URLs and InputStreams.
	 */
	private final String path;

	private final String absolutePath;

	@Nullable
	private final ClassLoader classLoader;

	@Nullable
	private final Class<?> clazz;


	/**
	 * Create a new {@code ClassPathResource} for {@code ClassLoader} usage.
	 * <p>A leading slash will be removed, as the {@code ClassLoader} resource
	 * access methods will not accept it.
	 * <p>The default class loader will be used for loading the resource.
	 * @param path the absolute path within the class path
	 * @see ClassUtils#getDefaultClassLoader()
	 * 
	 * 为ClassLoader使用创建一个新的ClassPathResource。
	 * 前导斜杠将被移除，因为ClassLoader资源访问方法不接受它。
	 * 将使用默认类加载器加载资源。
	 */
	public ClassPathResource(String path) {
		this(path, (ClassLoader) null);
	}

	/**
	 * Create a new {@code ClassPathResource} for {@code ClassLoader} usage.
	 * <p>A leading slash will be removed, as the {@code ClassLoader} resource
	 * access methods will not accept it.
	 * <p>If the supplied {@code ClassLoader} is {@code null}, the default class
	 * loader will be used for loading the resource.
	 * @param path the absolute path within the class path
	 * @param classLoader the class loader to load the resource with
	 * @see ClassUtils#getDefaultClassLoader()
	 * 
	 * 为ClassLoader使用创建一个新的ClassPathResource。
	 * 前导斜杠将被移除，因为ClassLoader资源访问方法不接受它。
	 * 如果提供的ClassLoader为null，将使用默认类加载器加载资源。
	 */
	public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
		Assert.notNull(path, "Path must not be null");
		String pathToUse = StringUtils.cleanPath(path);
		// 移除前导斜杠，因为ClassLoader资源访问方法不接受它
		if (pathToUse.startsWith("/")) {
			pathToUse = pathToUse.substring(1);
		}
		this.path = pathToUse;
		this.absolutePath = pathToUse;
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
		this.clazz = null;
	}

	/**
	 * Create a new {@code ClassPathResource} for {@code Class} usage.
	 * <p>The path can be relative to the given class, or absolute within
	 * the class path via a leading slash.
	 * <p>If the supplied {@code Class} is {@code null}, the default class
	 * loader will be used for loading the resource.
	 * <p>This is also useful for resource access within the module system,
	 * loading a resource from the containing module of a given {@code Class}.
	 * See {@link ModuleResource} and its javadoc.
	 * @param path relative or absolute path within the class path
	 * @param clazz the class to load resources with
	 * @see ClassUtils#getDefaultClassLoader()
	 * @see ModuleResource
	 * 
	 * 为Class使用创建一个新的ClassPathResource。
	 * 路径可以相对于给定的类，或者通过前导斜杠在类路径内绝对定位。
	 * 如果提供的Class为null，将使用默认类加载器加载资源。
	 * 这对于模块系统内的资源访问也很有用，可以从给定Class的包含模块加载资源。
	 */
	public ClassPathResource(String path, @Nullable Class<?> clazz) {
		Assert.notNull(path, "Path must not be null");
		this.path = StringUtils.cleanPath(path);

		String absolutePath = this.path;
		if (clazz != null && !absolutePath.startsWith("/")) {
			// 如果clazz不为null且路径不以"/"开头，则拼接包路径
			absolutePath = ClassUtils.classPackageAsResourcePath(clazz) + "/" + absolutePath;
		}
		else if (absolutePath.startsWith("/")) {
			// 移除前导斜杠
			absolutePath = absolutePath.substring(1);
		}
		this.absolutePath = absolutePath;

		this.classLoader = null;
		this.clazz = clazz;
	}


	/**
	 * Return the <em>absolute path</em> for this resource, as a
	 * {@linkplain StringUtils#cleanPath(String) cleaned} resource path within
	 * the class path.
	 * <p>The path returned by this method does not have a leading slash and is
	 * suitable for use with {@link ClassLoader#getResource(String)}.
	 * 
	 * 返回此资源的绝对路径，作为类路径内清理后的资源路径。
	 * 此方法返回的路径没有前导斜杠，适用于ClassLoader#getResource(String)。
	 */
	public final String getPath() {
		return this.absolutePath;
	}

	/**
	 * Return the {@link ClassLoader} that this resource will be obtained from.
	 * 
	 * 返回此资源将从中获取的ClassLoader。
	 */
	@Nullable
	public final ClassLoader getClassLoader() {
		return (this.clazz != null ? this.clazz.getClassLoader() : this.classLoader);
	}


	/**
	 * This implementation checks for the resolution of a resource URL.
	 * @see ClassLoader#getResource(String)
	 * @see Class#getResource(String)
	 * 
	 * 此实现检查资源URL的解析。
	 */
	@Override
	public boolean exists() {
		return (resolveURL() != null);
	}

	/**
	 * This implementation checks for the resolution of a resource URL upfront,
	 * then proceeding with {@link AbstractFileResolvingResource}'s length check.
	 * @see ClassLoader#getResource(String)
	 * @see Class#getResource(String)
	 * 
	 * 此实现首先检查资源URL的解析，然后继续进行AbstractFileResolvingResource的长度检查。
	 */
	@Override
	public boolean isReadable() {
		URL url = resolveURL();
		return (url != null && checkReadable(url));
	}

	/**
	 * Resolves a {@link URL} for the underlying class path resource.
	 * @return the resolved URL, or {@code null} if not resolvable
	 * 
	 * 解析底层类路径资源的URL。
	 * 返回解析后的URL，如果无法解析则返回null
	 */
	@Nullable
	protected URL resolveURL() {
		try {
			if (this.clazz != null) {
				// 通过Class加载资源
				return this.clazz.getResource(this.path);
			}
			else if (this.classLoader != null) {
				// 通过ClassLoader加载资源
				return this.classLoader.getResource(this.absolutePath);
			}
			else {
				// 通过系统ClassLoader加载资源
				return ClassLoader.getSystemResource(this.absolutePath);
			}
		}
		catch (IllegalArgumentException ex) {
			// Should not happen according to the JDK's contract:
			// see https://github.com/openjdk/jdk/pull/2662
			return null;
		}
	}

	/**
	 * This implementation opens an {@link InputStream} for the underlying class
	 * path resource, if available.
	 * @see ClassLoader#getResourceAsStream(String)
	 * @see Class#getResourceAsStream(String)
	 * @see ClassLoader#getSystemResourceAsStream(String)
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		InputStream is;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else if (this.classLoader != null) {
			is = this.classLoader.getResourceAsStream(this.absolutePath);
		}
		else {
			is = ClassLoader.getSystemResourceAsStream(this.absolutePath);
		}
		if (is == null) {
			throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}

	/**
	 * This implementation returns a URL for the underlying class path resource,
	 * if available.
	 * @see ClassLoader#getResource(String)
	 * @see Class#getResource(String)
	 */
	@Override
	public URL getURL() throws IOException {
		URL url = resolveURL();
		if (url == null) {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	/**
	 * This implementation creates a {@code ClassPathResource}, applying the given
	 * path relative to the path used to create this descriptor.
	 * @see StringUtils#applyRelativePath(String, String)
	 */
	@Override
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return (this.clazz != null ? new ClassPathResource(pathToUse, this.clazz) :
				new ClassPathResource(pathToUse, this.classLoader));
	}

	/**
	 * This implementation returns the name of the file that this class path
	 * resource refers to.
	 * @see StringUtils#getFilename(String)
	 */
	@Override
	@Nullable
	public String getFilename() {
		return StringUtils.getFilename(this.absolutePath);
	}

	/**
	 * This implementation returns a description that includes the absolute
	 * class path location.
	 */
	@Override
	public String getDescription() {
		return "class path resource [" + this.absolutePath + "]";
	}


	/**
	 * This implementation compares the underlying class path locations and
	 * associated class loaders.
	 * @see #getPath()
	 * @see #getClassLoader()
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ClassPathResource that &&
				this.absolutePath.equals(that.absolutePath) &&
				ObjectUtils.nullSafeEquals(getClassLoader(), that.getClassLoader())));
	}

	/**
	 * This implementation returns the hash code of the underlying class path location.
	 * @see #getPath()
	 */
	@Override
	public int hashCode() {
		return this.absolutePath.hashCode();
	}

}
