/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.junit5.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.common.list.ListSupplierDelegate;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.common.service.ServiceConfiguration;
import org.osgi.test.common.service.ServiceConfigurationKey;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * A JUnit 5 Extension to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;ExtendWith(ServiceExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectService
 * 	Foo foo;
 *
 * 	&#64;Test
 * 	public void test() {
 * 		// use foo
 * 	}
 * }
 * </pre>
 */
public class ServiceExtension extends InjectingExtension<InjectService> {

	public ServiceExtension() {
		super(InjectService.class);
	}

	@Override
	protected Object fieldValue(Field field, ExtensionContext extensionContext) {
		assertValidFieldCandidate(field);
		InjectService injectService = field.getAnnotation(supported);
		TargetType targetType = TargetType.of(field);
		return resolveReturnValue(targetType, injectService, extensionContext);
	}

	@Override
	protected Object parameterValue(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Optional<InjectService> injectService = parameterContext.findAnnotation(supported);
		final Parameter parameter = parameterContext.getParameter();

		TargetType targetType = TargetType.of(parameter);

		return resolveReturnValue(targetType, injectService.get(), extensionContext);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {

		if (!parameterContext.isAnnotated(supported)) {
			return false;
		}

		Parameter parameter = parameterContext.getParameter();
		Class<?> memberType = parameter.getType();
		Type genericMemberType = parameter.getParameterizedType();
		Type serviceType = genericMemberType;

		if (List.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) genericMemberType).getActualTypeArguments()[0];
		} else if (ServiceAware.class.equals(memberType) && (genericMemberType instanceof ParameterizedType)) {
			serviceType = ((ParameterizedType) genericMemberType).getActualTypeArguments()[0];
		}
		// The service type must be a raw type
		if (serviceType instanceof Class || serviceType instanceof WildcardType) {
			return true;
		}
		throw new ParameterResolutionException("Can only resolve @" + supported.getSimpleName()
			+ " parameter for services with non-generic types, service type was: " + serviceType.getTypeName());
	}

	void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())) {
			throw new ExtensionConfigurationException(
				"@" + supported.getSimpleName() + " field ["
				+ field.getName() + "] must not be final or private.");
		}
	}

	public static <S> ServiceConfiguration<S> getServiceConfiguration(Class<S> serviceType, String format,
		String[] args, int cardinality, long timeout, ExtensionContext extensionContext) {
		@SuppressWarnings("unchecked")
		ServiceConfiguration<S> serviceConfiguration = getStore(extensionContext)
			.getOrComputeIfAbsent(new ServiceConfigurationKey<>(serviceType, format, args, cardinality, timeout),
				key -> new CloseableServiceConfiguration<>(
					new ServiceConfiguration<>(key).init(BundleContextExtension.getBundleContext(extensionContext))),
				CloseableServiceConfiguration.class)
			.get();
		return serviceConfiguration;
	}

	static Object resolveReturnValue(TargetType targetType, InjectService injectService,
		ExtensionContext extensionContext) throws ParameterResolutionException {

		Type serviceType = targetType.getType();

		if (targetType.matches(List.class) || targetType.matches(ServiceAware.class)) {
			Optional<Type> o = targetType.getFirstGenericTypes();
			if (o.isPresent()) {
				serviceType = (o.get());
			}
		}

		// supportsParameter() If Jupiter does the right thing then this method
		// should not be called without serviceType being a class
		assert serviceType instanceof Class || serviceType instanceof WildcardType;

		if (serviceType instanceof WildcardType) {
			serviceType = Object.class;

		}
		if (!injectService.service()
			.equals(Object.class)) {
			serviceType = injectService.service();
		}

		ServiceConfiguration<?> configuration = getServiceConfiguration((Class<?>) serviceType, injectService.filter(),
			injectService.filterArguments(), injectService.cardinality(), injectService.timeout(), extensionContext);

		if (targetType.matches(List.class) && targetType.hasParameterizedTypes()) {
			return new ListSupplierDelegate<>(configuration::getServices);
		} else if (targetType.matches(ServiceAware.class) && targetType.hasParameterizedTypes()) {
			return configuration;
		}

		return configuration.getService();
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext.getStore(Namespace.create(ServiceExtension.class, extensionContext.getUniqueId()));
	}

	public static class CloseableServiceConfiguration<S> implements CloseableResource {

		private final ServiceConfiguration<S> serviceConfiguration;

		CloseableServiceConfiguration(ServiceConfiguration<S> serviceConfiguration) {
			this.serviceConfiguration = serviceConfiguration;
		}

		@Override
		public void close() throws Exception {
			get().close();
		}

		public ServiceConfiguration<S> get() {
			return serviceConfiguration;
		}

		@Override
		public String toString() {
			return get().toString();
		}
	}
}
