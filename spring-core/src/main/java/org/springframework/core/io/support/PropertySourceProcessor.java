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

package org.springframework.core.io.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PlaceholderResolutionException;
import org.springframework.util.ReflectionUtils;

/**
 * Contribute {@link PropertySource property sources} to the {@link Environment}.
 *
 * <p>This class is stateful and merges descriptors with the same name in a
 * single {@link PropertySource} rather than creating dedicated ones.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 6.0
 * @see PropertySourceDescriptor
 */
public class PropertySourceProcessor {

	private static final PropertySourceFactory defaultPropertySourceFactory = new DefaultPropertySourceFactory();

	private static final Log logger = LogFactory.getLog(PropertySourceProcessor.class);


	private final ConfigurableEnvironment environment;

	private final ResourcePatternResolver resourcePatternResolver;

	private final List<String> propertySourceNames = new ArrayList<>();


	public PropertySourceProcessor(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
		this.environment = environment;
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}


	/**
	 * Process the specified {@link PropertySourceDescriptor} against the
	 * environment managed by this instance.
	 * @param descriptor the descriptor to process
	 * @throws IOException if loading the properties failed
	 */
	public void processPropertySource(PropertySourceDescriptor descriptor) throws IOException {
		// 从描述符中获取@PropertySource注解的各个属性
		String name = descriptor.name();
		String encoding = descriptor.encoding();
		List<String> locations = descriptor.locations();
		Assert.isTrue(locations.size() > 0, "At least one @PropertySource(value) location is required");
		boolean ignoreResourceNotFound = descriptor.ignoreResourceNotFound();
		
		// 获取PropertySourceFactory实例，如果没有指定则使用默认的DefaultPropertySourceFactory
		PropertySourceFactory factory = (descriptor.propertySourceFactory() != null ?
				instantiateClass(descriptor.propertySourceFactory()) : defaultPropertySourceFactory);

		// 处理每个位置路径
		for (String location : locations) {
			try {
				// 解析位置路径中的占位符（如${spring.config.location}）
				String resolvedLocation = this.environment.resolveRequiredPlaceholders(location);
				// 获取所有匹配的资源（支持通配符，如classpath*:*.properties）
				for (Resource resource : this.resourcePatternResolver.getResources(resolvedLocation)) {
					// 使用工厂创建PropertySource并添加到环境中
					addPropertySource(factory.createPropertySource(name, new EncodedResource(resource, encoding)));
				}
			}
			catch (RuntimeException | IOException ex) {
				// 处理异常，根据ignoreResourceNotFound决定是否忽略
				// Placeholders not resolvable or resource not found when trying to open it
				if (ignoreResourceNotFound && (ex instanceof PlaceholderResolutionException || isIgnorableException(ex) ||
						isIgnorableException(ex.getCause()))) {
					if (logger.isInfoEnabled()) {
						logger.info("Properties location [" + location + "] not resolvable: " + ex.getMessage());
					}
				}
				else {
					throw ex;
				}
			}
		}
	}

	private void addPropertySource(PropertySource<?> propertySource) {
		String name = propertySource.getName();
		MutablePropertySources propertySources = this.environment.getPropertySources();

		// 检查是否已经添加过同名的PropertySource
		if (this.propertySourceNames.contains(name)) {
			// We've already added a version, we need to extend it
			PropertySource<?> existing = propertySources.get(name);
			if (existing != null) {
				PropertySource<?> newSource = (propertySource instanceof ResourcePropertySource rps ?
						rps.withResourceName() : propertySource);
				// 如果已存在的是CompositePropertySource，直接添加新的PropertySource
				if (existing instanceof CompositePropertySource cps) {
					cps.addFirstPropertySource(newSource);
				}
				else {
					// 否则创建一个新的CompositePropertySource来包装现有的和新的PropertySource
					if (existing instanceof ResourcePropertySource rps) {
						existing = rps.withResourceName();
					}
					CompositePropertySource composite = new CompositePropertySource(name);
					composite.addPropertySource(newSource);
					composite.addPropertySource(existing);
					propertySources.replace(name, composite);
				}
				return;
			}
		}

		// 如果是第一个PropertySource，添加到最后
		if (this.propertySourceNames.isEmpty()) {
			propertySources.addLast(propertySource);
		}
		else {
			// 否则添加到最后一个添加的PropertySource之前，保持@PropertySource注解的顺序
			String lastAdded = this.propertySourceNames.get(this.propertySourceNames.size() - 1);
			propertySources.addBefore(lastAdded, propertySource);
		}
		// 记录已添加的PropertySource名称
		this.propertySourceNames.add(name);
	}


	private static PropertySourceFactory instantiateClass(Class<? extends PropertySourceFactory> type) {
		try {
			return ReflectionUtils.accessibleConstructor(type).newInstance();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to instantiate " + type, ex);
		}
	}

	/**
	 * Determine if the supplied exception can be ignored according to
	 * {@code ignoreResourceNotFound} semantics.
	 */
	private static boolean isIgnorableException(@Nullable Throwable ex) {
		return (ex instanceof FileNotFoundException ||
				ex instanceof UnknownHostException ||
				ex instanceof SocketException);
	}

}
