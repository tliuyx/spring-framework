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

import java.beans.PropertyDescriptor;

/**
 * The central interface of Spring's low-level JavaBeans infrastructure.
 *
 * <p>Typically not used directly but rather implicitly via a
 * {@link org.springframework.beans.factory.BeanFactory} or a
 * {@link org.springframework.validation.DataBinder}.
 *
 * <p>Provides operations to analyze and manipulate standard JavaBeans:
 * the ability to get and set property values (individually or in bulk),
 * get property descriptors, and query the readability/writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
 *
 * <p>A BeanWrapper's default for the "extractOldValueForEditor" setting
 * is "false", to avoid side effects caused by getter method invocations.
 * Turn this to "true" to expose present property values to custom editors.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 * 
 * BeanWrapper是Spring低级JavaBeans基础架构的中心接口。
 * 
 * 通常不直接使用，而是通过BeanFactory或DataBinder隐式使用。
 * 
 * 提供操作来分析和操作标准JavaBeans：
 * 获取和设置属性值（单独或批量）、获取属性描述符、
 * 查询属性的可读性/可写性。
 * 
 * 此接口支持嵌套属性，允许无限深度地设置子属性的属性。
 * 
 * BeanWrapper的"extractOldValueForEditor"设置默认为"false"，
 * 以避免getter方法调用引起的副作用。
 * 将其设置为"true"可将当前属性值暴露给自定义编辑器。
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor {

	/**
	 * Specify a limit for array and collection auto-growing.
	 * <p>Default is unlimited on a plain BeanWrapper.
	 * @since 4.1
	 * 
	 * 指定数组和集合自动增长的限制。
	 * 在普通BeanWrapper上默认是无限制的。
	 */
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

	/**
	 * Return the limit for array and collection auto-growing.
	 * @since 4.1
	 * 
	 * 返回数组和集合自动增长的限制。
	 */
	int getAutoGrowCollectionLimit();

	/**
	 * Return the bean instance wrapped by this object.
	 * 
	 * 返回由此对象包装的bean实例。
	 */
	Object getWrappedInstance();

	/**
	 * Return the type of the wrapped bean instance.
	 * 
	 * 返回包装的bean实例的类型。
	 */
	Class<?> getWrappedClass();

	/**
	 * Obtain the PropertyDescriptors for the wrapped object
	 * (as determined by standard JavaBeans introspection).
	 * @return the PropertyDescriptors for the wrapped object
	 * 
	 * 获取包装对象的PropertyDescriptors（通过标准JavaBeans内省确定）。
	 * 返回包装对象的PropertyDescriptors
	 */
	PropertyDescriptor[] getPropertyDescriptors();

	/**
	 * Obtain the property descriptor for a specific property
	 * of the wrapped object.
	 * @param propertyName the property to obtain the descriptor for
	 * (may be a nested path, but not an indexed/mapped property)
	 * @return the property descriptor for the specified property
	 * @throws InvalidPropertyException if there is no such property
	 * 
	 * 获取包装对象特定属性的属性描述符。
	 * propertyName：要获取描述符的属性（可以是嵌套路径，但不是索引/映射属性）
	 * 返回指定属性的属性描述符
	 * 如果没有这样的属性则抛出InvalidPropertyException
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

}
