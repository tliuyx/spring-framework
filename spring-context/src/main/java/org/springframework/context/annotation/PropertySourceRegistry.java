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

package org.springframework.context.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.support.PropertySourceDescriptor;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.PropertySourceProcessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Registry of {@link PropertySource} processed on configuration classes.
 *
 * @author Stephane Nicoll
 * @since 6.0
 * @see PropertySourceDescriptor
 */
class PropertySourceRegistry {

	private final PropertySourceProcessor propertySourceProcessor;

	private final List<PropertySourceDescriptor> descriptors;


	public PropertySourceRegistry(PropertySourceProcessor propertySourceProcessor) {
		this.propertySourceProcessor = propertySourceProcessor;
		this.descriptors = new ArrayList<>();
	}


	/**
	 * Process the given <code>@PropertySource</code> annotation metadata.
	 * @param propertySource metadata for the <code>@PropertySource</code> annotation found
	 * @throws IOException if loading a property source failed
	 */
	void processPropertySource(AnnotationAttributes propertySource) throws IOException {
		// 解析@PropertySource注解的各个属性
		String name = propertySource.getString("name");
		if (!StringUtils.hasLength(name)) {
			name = null;
		}
		String encoding = propertySource.getString("encoding");
		if (!StringUtils.hasLength(encoding)) {
			encoding = null;
		}
		String[] locations = propertySource.getStringArray("value");
		Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");
		boolean ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound");

		// 获取自定义PropertySourceFactory类，如果没有指定则使用默认的
		Class<? extends PropertySourceFactory> factoryClass = propertySource.getClass("factory");
		Class<? extends PropertySourceFactory> factoryClassToUse =
				(factoryClass != PropertySourceFactory.class ? factoryClass : null);
		
		// 创建PropertySourceDescriptor，封装所有@PropertySource注解的属性
		PropertySourceDescriptor descriptor = new PropertySourceDescriptor(Arrays.asList(locations),
				ignoreResourceNotFound, name, factoryClassToUse, encoding);
		
		// 委托给PropertySourceProcessor处理实际的属性文件加载
		this.propertySourceProcessor.processPropertySource(descriptor);
		
		// 保存描述符，用于AOT处理
		this.descriptors.add(descriptor);
	}

	public List<PropertySourceDescriptor> getDescriptors() {
		return Collections.unmodifiableList(this.descriptors);
	}

}
