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

package org.springframework.aop.aspectj.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Helper for retrieving @AspectJ beans from a BeanFactory and building
 * Spring Advisors based on them, for use with auto-proxying.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see AnnotationAwareAspectJAutoProxyCreator
 */
public class BeanFactoryAspectJAdvisorsBuilder {

	private static final Log logger = LogFactory.getLog(BeanFactoryAspectJAdvisorsBuilder.class);

	private final ListableBeanFactory beanFactory;

	private final AspectJAdvisorFactory advisorFactory;

	@Nullable
	private volatile List<String> aspectBeanNames;

	private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();

	private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();


	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory) {
		this(beanFactory, new ReflectiveAspectJAdvisorFactory(beanFactory));
	}

	/**
	 * Create a new BeanFactoryAspectJAdvisorsBuilder for the given BeanFactory.
	 * @param beanFactory the ListableBeanFactory to scan
	 * @param advisorFactory the AspectJAdvisorFactory to build each Advisor with
	 */
	public BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
		this.beanFactory = beanFactory;
		this.advisorFactory = advisorFactory;
	}


	/**
	 * Look for AspectJ-annotated aspect beans in the current bean factory,
	 * and return to a list of Spring AOP Advisors representing them.
	 * <p>Creates a Spring Advisor for each AspectJ advice method.
	 * @return the list of {@link org.springframework.aop.Advisor} beans
	 * @see #isEligibleBean
	 */
	@SuppressWarnings("NullAway")
	public List<Advisor> buildAspectJAdvisors() {
		// 缓存中保存已解析过的切面 Bean 名称，避免在容器启动过程中重复全量扫描
		List<String> aspectNames = this.aspectBeanNames;

		if (aspectNames == null) {
			// 第一次调用时进入同步块，完成全量扫描与缓存构建
			synchronized (this) {
				aspectNames = this.aspectBeanNames;
				if (aspectNames == null) {
					List<Advisor> advisors = new ArrayList<>();
					aspectNames = new ArrayList<>();
					// 步骤1：从 BeanFactory 中获取所有候选 Bean 名称
					String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
							this.beanFactory, Object.class, true, false);
					for (String beanName : beanNames) {
						// 步骤2：根据 isEligibleBean 过滤不参与 AOP 的 Bean
						if (!isEligibleBean(beanName)) {
							continue;
						}
						// We must be careful not to instantiate beans eagerly as in this case they
						// would be cached by the Spring container but would not have been weaved.
						// 步骤3：只获取类型信息而不实例化 Bean，避免过早创建单例
						Class<?> beanType = this.beanFactory.getType(beanName, false);
						if (beanType == null) {
							continue;
						}
						// 步骤4：判断当前 Bean 是否为 @Aspect 切面类
						if (this.advisorFactory.isAspect(beanType)) {
							try {
								AspectMetadata amd = new AspectMetadata(beanType, beanName);
								if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
									// 步骤5：针对单例切面创建 BeanFactoryAspectInstanceFactory
									MetadataAwareAspectInstanceFactory factory =
											new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
									// 步骤6：从切面中提取所有 Advisor
									List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
									// 步骤7：根据 Bean 作用域将 Advisor 或切面工厂缓存起来
									if (this.beanFactory.isSingleton(beanName)) {
										this.advisorsCache.put(beanName, classAdvisors);
									}
									else {
										this.aspectFactoryCache.put(beanName, factory);
									}
									advisors.addAll(classAdvisors);
								}
								else {
									// Per target or per this.
									// 步骤8：处理非单例切面（如 perthis/pertarget），仅缓存工厂
									if (this.beanFactory.isSingleton(beanName)) {
										throw new IllegalArgumentException("Bean with name '" + beanName +
												"' is a singleton, but aspect instantiation model is not singleton");
									}
									MetadataAwareAspectInstanceFactory factory =
											new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
									this.aspectFactoryCache.put(beanName, factory);
									advisors.addAll(this.advisorFactory.getAdvisors(factory));
								}
								// 记录当前切面 Bean 名称，后续可以直接从缓存中恢复 Advisor
								aspectNames.add(beanName);
							}
							catch (IllegalArgumentException | IllegalStateException | AopConfigException ex) {
								if (logger.isDebugEnabled()) {
									logger.debug("Ignoring incompatible aspect [" + beanType.getName() + "]: " + ex);
								}
							}
						}
					}
					this.aspectBeanNames = aspectNames;
					return advisors;
				}
			}
		}

		if (aspectNames.isEmpty()) {
			return Collections.emptyList();
		}
		// 非首次调用：根据 aspectBeanNames 列表从缓存中恢复 Advisor，避免重复解析 @Aspect
		List<Advisor> advisors = new ArrayList<>();
		for (String aspectName : aspectNames) {
			List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
			if (cachedAdvisors != null) {
				advisors.addAll(cachedAdvisors);
			}
			else {
				MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
				advisors.addAll(this.advisorFactory.getAdvisors(factory));
			}
		}
		return advisors;
	}

	/**
	 * Return whether the aspect bean with the given name is eligible.
	 * @param beanName the name of the aspect bean
	 * @return whether the bean is eligible
	 */
	protected boolean isEligibleBean(String beanName) {
		return true;
	}

}
