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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
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
	protected boolean supportsType(TargetType targetType, ExtensionContext extensionContext) {
		return true;
	}

	private Type extractServiceType(TargetType targetType, InjectService injectService)
		throws ParameterResolutionException {
		Type valueType;
		Type upperBoundType = null;
		if (targetType.matches(List.class) || targetType.matches(ServiceAware.class)) {
			if (targetType.hasParameterizedTypes()) {
				valueType = targetType.getFirstGenericTypes()
					.get();
				if (valueType instanceof WildcardType) {
					final WildcardType wild = (WildcardType) valueType;
					upperBoundType = (wild.getUpperBounds().length > 0) ? wild.getUpperBounds()[0] : Object.class;
				}
			} else {
				valueType = Object.class;
			}
		} else {
			valueType = targetType.getGenericType();
		}
		if (upperBoundType == null) {
			upperBoundType = valueType;
		}
		if (!(upperBoundType instanceof Class)) {
			throw new ParameterResolutionException(String.format(
				"Element %s has an unsupported type %s for annotation @%s. Service must have non-generic type.",
				targetType.getName(), upperBoundType.getTypeName(), annotation().getSimpleName()));
		}
		final Class<?> upperBoundClass = (Class<?>) upperBoundType;

		final Class<?> serviceClass = injectService.service();
		if (serviceClass.equals(annotation())) {
			return upperBoundClass;
		}

		if (!upperBoundClass.isAssignableFrom(serviceClass)) {
			throw new ParameterResolutionException(String.format(
				"Element %s has service type %s for annotation @%s but field expects %s.", targetType.getName(),
				serviceClass.getName(), annotation().getSimpleName(), valueType.getTypeName()));
		}
		if (valueType instanceof WildcardType) {
			final WildcardType wild = (WildcardType) valueType;
			if (wild.getLowerBounds().length > 0) {
				final Type lowerBoundType = wild.getLowerBounds()[0];
				if (!(lowerBoundType instanceof Class)) {
					throw new ParameterResolutionException(String.format(
						"Element %s has an unsupported lower bound %s for annotation @%s. Service must have non-generic type.",
						targetType.getName(), lowerBoundType.getTypeName(), annotation().getSimpleName()));
				}
				if (!serviceClass.isAssignableFrom((Class<?>) lowerBoundType)) {
					throw new ParameterResolutionException(String.format(
						"Element %s has service type %s for annotation @%s but field expects %s.", targetType.getName(),
						serviceClass.getName(), annotation().getSimpleName(), valueType.getTypeName()));
				}
			}
		}

		return serviceClass;
	}

	@Override
	protected Object resolveValue(TargetType targetType, InjectService injectService, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		final Type serviceType = extractServiceType(targetType, injectService);

		ServiceConfiguration<?> configuration = getServiceConfiguration((Class<?>) serviceType, injectService.filter(),
			injectService.filterArguments(), injectService.cardinality(), injectService.timeout(), extensionContext);

		if (targetType.matches(ServiceAware.class)) {
			return configuration;
		}
		if (targetType.matches(List.class)) {
			return new ListSupplierDelegate<>(configuration::getServices);
		}
		return configuration.getService();
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
