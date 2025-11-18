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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see UrlResource
 * @see FileUrlResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see ByteArrayResource
 * @see InputStreamResource
 * 
 * Resource接口是Spring资源访问策略的抽象，为应用提供了统一的资源访问方式，
 * 屏蔽了底层资源访问的复杂性。它继承自InputStreamSource接口。
 * 
 * Resource接口的主要作用：
 * 1. 统一资源访问方式：无论是文件、类路径资源、URL资源还是其他类型的资源，
 *    都可以通过统一的接口进行访问
 * 2. 抽象资源操作：定义了资源是否存在、是否可读、获取资源内容等通用操作
 * 3. 支持多种资源类型：为不同类型的资源提供统一的访问接口
 * 
 * 常见实现类：
 * - ClassPathResource：类路径资源
 * - UrlResource：URL资源
 * - FileSystemResource：文件系统资源
 * - ByteArrayResource：字节数组资源
 * - InputStreamResource：InputStream资源
 */
public interface Resource extends InputStreamSource {

	/**
	 * Determine whether this resource actually exists in physical form.
	 * <p>This method performs a definitive existence check, whereas the
	 * existence of a {@code Resource} handle only guarantees a valid
	 * descriptor handle.
	 * 
	 * 确定此资源是否实际存在于物理形式中。
	 * 此方法执行明确的存在性检查，而Resource句柄的存在仅保证有效的描述符句柄。
	 * 
	 * 这个方法与Resource句柄的存在性不同，Resource句柄的存在只表示有一个有效的描述符，
	 * 但实际的物理资源可能存在也可能不存在。
	 */
	boolean exists();

	/**
	 * Indicate whether non-empty contents of this resource can be read via
	 * {@link #getInputStream()}.
	 * <p>Will be {@code true} for typical resource descriptors that exist
	 * since it strictly implies {@link #exists()} semantics as of 5.1.
	 * Note that actual content reading may still fail when attempted.
	 * However, a value of {@code false} is a definitive indication
	 * that the resource content cannot be read.
	 * @see #getInputStream()
	 * @see #exists()
	 * 
	 * 指示是否可以通过getInputStream()读取此资源的非空内容。
	 * 对于存在的典型资源描述符将返回true，因为它严格意味着exists()语义。
	 * 注意，实际内容读取尝试时仍可能失败。
	 * 但返回false则明确表示资源内容无法读取。
	 */
	default boolean isReadable() {
		return exists();
	}

	/**
	 * Indicate whether this resource represents a handle with an open stream.
	 * If {@code true}, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * <p>Will be {@code false} for typical resource descriptors.
	 * 
	 * 指示此资源是否表示一个具有开放流的句柄。
	 * 如果为true，则InputStream无法多次读取，必须读取并关闭以避免资源泄漏。
	 * 对于典型的资源描述符将返回false。
	 */
	default boolean isOpen() {
		return false;
	}

	/**
	 * Determine whether this resource represents a file in a file system.
	 * <p>A value of {@code true} strongly suggests (but does not guarantee)
	 * that a {@link #getFile()} call will succeed.
	 * <p>This is conservatively {@code false} by default.
	 * @since 5.0
	 * @see #getFile()
	 * 
	 * 确定此资源是否表示文件系统中的一个文件。
	 * 返回true强烈建议（但不保证）getFile()调用会成功。
	 * 默认情况下保守地返回false。
	 */
	default boolean isFile() {
		return false;
	}

	/**
	 * Return a URL handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URL,
	 * i.e. if the resource is not available as a descriptor
	 * 
	 * 返回此资源的URL句柄。
	 * 如果资源无法解析为URL（即资源不可用作描述符）则抛出IOException。
	 */
	URL getURL() throws IOException;

	/**
	 * Return a URI handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URI,
	 * i.e. if the resource is not available as a descriptor
	 * @since 2.5
	 * 
	 * 返回此资源的URI句柄。
	 * 如果资源无法解析为URI（即资源不可用作描述符）则抛出IOException。
	 */
	URI getURI() throws IOException;

	/**
	 * Return a File handle for this resource.
	 * <p>Note: This only works for files in the default file system.
	 * @throws UnsupportedOperationException if the resource is a file but cannot be
	 * exposed as a {@code java.io.File}; an alternative to {@code FileNotFoundException}
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as a file
	 * @throws IOException in case of general resolution/reading failures
	 * @see #getInputStream()
	 * 
	 * 返回此资源的File句柄。
	 * 注意：这只适用于默认文件系统中的文件。
	 * 如果资源是文件但无法暴露为java.io.File则抛出UnsupportedOperationException。
	 * 如果资源无法解析为文件则抛出FileNotFoundException。
	 * 出现一般解析/读取失败时抛出IOException。
	 */
	File getFile() throws IOException;

	/**
	 * Return a {@link ReadableByteChannel}.
	 * <p>It is expected that each call creates a <i>fresh</i> channel.
	 * <p>The default implementation returns {@link Channels#newChannel(InputStream)}
	 * with the result of {@link #getInputStream()}.
	 * @return the byte channel for the underlying resource (must not be {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource doesn't exist
	 * @throws IOException if the content channel could not be opened
	 * @since 5.0
	 * @see #getInputStream()
	 * 
	 * 返回ReadableByteChannel。
	 * 期望每次调用都创建一个新的通道。
	 * 默认实现使用getInputStream()的结果返回Channels.newChannel(InputStream)。
	 */
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	/**
	 * Return the contents of this resource as a byte array.
	 * @return the contents of this resource as byte array
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @since 6.0.5
	 * 
	 * 将此资源的内容作为字节数组返回。
	 * 如果资源无法解析为绝对文件路径（即资源在文件系统中不可用）则抛出FileNotFoundException。
	 * 出现一般解析/读取失败时抛出IOException。
	 */
	default byte[] getContentAsByteArray() throws IOException {
		return FileCopyUtils.copyToByteArray(getInputStream());
	}

	/**
	 * Return the contents of this resource as a string, using the specified charset.
	 * @param charset the charset to use for decoding
	 * @return the contents of this resource as a {@code String}
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @since 6.0.5
	 * 
	 * 使用指定的字符集将此资源的内容作为字符串返回。
	 * 如果资源无法解析为绝对文件路径（即资源在文件系统中不可用）则抛出FileNotFoundException。
	 * 出现一般解析/读取失败时抛出IOException。
	 */
	default String getContentAsString(Charset charset) throws IOException {
		return FileCopyUtils.copyToString(new InputStreamReader(getInputStream(), charset));
	}

	/**
	 * Determine the content length for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 * 
	 * 确定此资源的内容长度。
	 * 如果资源无法解析（在文件系统中或作为其他已知的物理资源类型）则抛出IOException。
	 */
	long contentLength() throws IOException;

	/**
	 * Determine the last-modified timestamp for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 * 
	 * 确定此资源的最后修改时间戳。
	 * 如果资源无法解析（在文件系统中或作为其他已知的物理资源类型）则抛出IOException。
	 */
	long lastModified() throws IOException;

	/**
	 * Create a resource relative to this resource.
	 * @param relativePath the relative path (relative to this resource)
	 * @return the resource handle for the relative resource
	 * @throws IOException if the relative resource cannot be determined
	 * 
	 * 创建相对于此资源的资源。
	 * relativePath：相对路径（相对于此资源）
	 * 返回相对资源的资源句柄
	 * 如果无法确定相对资源则抛出IOException
	 */
	Resource createRelative(String relativePath) throws IOException;

	/**
	 * Determine the filename for this resource &mdash; typically the last
	 * part of the path &mdash; for example, {@code "myfile.txt"}.
	 * <p>Returns {@code null} if this type of resource does not
	 * have a filename.
	 * <p>Implementations are encouraged to return the filename unencoded.
	 * 
	 * 确定此资源的文件名，通常是路径的最后一部分，例如"myfile.txt"。
	 * 如果此类型的资源没有文件名则返回null。
	 * 鼓励实现返回未编码的文件名。
	 */
	@Nullable
	String getFilename();

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * <p>Implementations are also encouraged to return this value
	 * from their {@code toString} method.
	 * @see Object#toString()
	 * 
	 * 返回此资源的描述，用于在处理资源时的错误输出。
	 * 也鼓励实现在其toString方法中返回此值。
	 */
	String getDescription();

}
