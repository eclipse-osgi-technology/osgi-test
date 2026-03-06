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

package org.osgi.test.junit5.featurelauncher;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import org.osgi.test.common.service.ServiceAware;

/**
 * Provides access to the running OSGi framework state during a test.
 * <p>
 * Each capture instance owns its own service proxy factory, so services
 * obtained through it are tracked independently. When {@link #close()} is
 * called, only the services obtained through this instance are released.
 * <p>
 * This allows per-method scoping: the extension creates a new capture per test
 * method that shares the same bridge but tracks services independently.
 */
public class FeatureLaunchCapture {

	private final JdiFrameworkBridge bridge;
	private final List<JdiServiceAware<?>> serviceAwares = new ArrayList<>();

	FeatureLaunchCapture(JdiFrameworkBridge bridge) {
		this.bridge = bridge;
	}

	/**
	 * Gets the current FrameworkDTO from the running VM (suspend, extract, resume).
	 */
	public FrameworkDTO frameworkDTO() {
		return bridge.takeFrameworkDTOFromRunningVm();
	}

	/**
	 * Gets the current FrameworkStartLevelDTO from the running VM (suspend,
	 * extract, resume).
	 */
	public FrameworkStartLevelDTO frameworkStartLevelDTO() {
		return bridge.takeFrameworkStartLevelDTOFromRunningVm();
	}

	/**
	 * Returns a {@link ServiceAware} for the given service interface. The returned
	 * instance lazily resolves services from the remote VM and tracks them for
	 * cleanup when this capture is closed.
	 *
	 * @param <T>              the service interface type
	 * @param serviceInterface the service interface class
	 * @return a ServiceAware providing access to the remote service(s)
	 */
	public <T> ServiceAware<T> getServiceAware(Class<T> serviceInterface) {
		return getServiceAware(serviceInterface, null);
	}

	/**
	 * Returns a {@link ServiceAware} for the given service interface and LDAP
	 * filter. The returned instance lazily resolves services from the remote VM and
	 * tracks them for cleanup when this capture is closed.
	 *
	 * @param <T>              the service interface type
	 * @param serviceInterface the service interface class
	 * @param filter           LDAP filter on service properties, or null
	 * @return a ServiceAware providing access to the matching remote service(s)
	 */
	public <T> ServiceAware<T> getServiceAware(Class<T> serviceInterface, String filter) {
		JdiServiceAware<T> sa = new JdiServiceAware<>(serviceInterface, filter, 5000, 1, bridge);
		serviceAwares.add(sa);
		return sa;
	}

	/**
	 * Returns a proxy for the system bundle's {@code BundleContext}. The proxy
	 * transparently delegates all method calls to the remote VM.
	 *
	 * @return the BundleContext proxy
	 */
	public BundleContext getBundleContext() {
		return bridge.resolveBundleContextProxy();
	}

	/**
	 * Resolves a {@link BundleDTO} by its numeric ID from the current framework
	 * snapshot.
	 *
	 * @param bundleId the bundle ID
	 * @return the BundleDTO
	 * @throws IllegalStateException if no bundle with that ID exists
	 */
	public BundleDTO resolveBundleDTO(long bundleId) {
		return frameworkDTO().bundles.stream().filter(b -> b.id == bundleId).findFirst()
				.orElseThrow(() -> new IllegalStateException("Bundle with id " + bundleId + " not found"));
	}

	/**
	 * Resolves a {@link BundleDTO} by its symbolic name from the current framework
	 * snapshot.
	 *
	 * @param symbolicName the bundle symbolic name
	 * @return the BundleDTO
	 * @throws IllegalStateException if no bundle with that symbolic name exists
	 */
	public BundleDTO resolveBundleDTO(String symbolicName) {
		return frameworkDTO().bundles.stream().filter(b -> symbolicName.equals(b.symbolicName)).findFirst().orElseThrow(
				() -> new IllegalStateException("Bundle with symbolic name '" + symbolicName + "' not found"));
	}

	/**
	 * Resolves a bundle by its numeric ID and returns it. The returned object can
	 * be passed to service method calls that require a {@code Bundle} parameter.
	 *
	 * @param bundleId the bundle ID
	 * @return the bundle proxy
	 */
	public Bundle resolveBundle(long bundleId) {
		return bridge.resolveBundleProxy(bundleId);
	}

	/**
	 * Resolves a bundle by its symbolic name and returns it. The returned object
	 * can be passed to service method calls that require a {@code Bundle}
	 * parameter.
	 *
	 * @param symbolicName the bundle symbolic name
	 * @return the bundle proxy
	 */
	public Bundle resolveBundle(String symbolicName) {
		return bridge.resolveBundleProxy(symbolicName);
	}

	/**
	 * Releases all service references held by this capture instance.
	 */
	public void close() {
		serviceAwares.forEach(JdiServiceAware::close);
		serviceAwares.clear();
	}
}
