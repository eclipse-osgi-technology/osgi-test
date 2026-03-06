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
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.featurelauncher.JdiFrameworkBridge.ServiceLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdi.ThreadReference;

/**
 * A {@link ServiceAware} implementation backed by JDI service proxy lookups.
 * Services are lazily resolved on first access from the remote OSGi framework.
 * <p>
 * This class tracks all obtained service references and releases them on
 * {@link #close()}.
 */
class JdiServiceAware<T> implements ServiceAware<T>, AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(JdiServiceAware.class);

	private final Class<T> serviceType;
	private final String filterString;
	private final long timeout;
	private final int cardinality;
	private final JdiFrameworkBridge bridge;

	private final List<ServiceLookup> lookups = new ArrayList<>();
	private final List<T> services = new ArrayList<>();
	private final List<ServiceReference<T>> references = new ArrayList<>();
	private boolean resolved;
	private int trackingCount;

	JdiServiceAware(Class<T> serviceType, String filter, long timeout, int cardinality, JdiFrameworkBridge bridge) {
		this.serviceType = serviceType;
		this.filterString = filter;
		this.timeout = timeout;
		this.cardinality = cardinality;
		this.bridge = bridge;
	}

	@SuppressWarnings("unchecked")
	private synchronized void ensureResolved() {
		if (resolved) {
			return;
		}
		resolved = true;

		bridge.vm().suspend();
		try {
			ThreadReference thread = bridge.acquireEventSuspendedThread();
			List<ServiceLookup> found = bridge.findAllServiceInstances(serviceType.getName(), filterString, thread);

			for (ServiceLookup lookup : found) {
				lookups.add(lookup);
				T serviceProxy = (T) java.lang.reflect.Proxy.newProxyInstance(serviceType.getClassLoader(),
						new Class<?>[] { serviceType }, new JdiProxyInvocationHandler(bridge, lookup.service()));
				services.add(serviceProxy);

				ServiceReference<T> refProxy = (ServiceReference<T>) java.lang.reflect.Proxy.newProxyInstance(
						ServiceReference.class.getClassLoader(), new Class<?>[] { ServiceReference.class },
						new JdiProxyInvocationHandler(bridge, lookup.serviceReference()));
				references.add(refProxy);
			}
			trackingCount++;
			LOG.debug("Resolved {} services for type '{}'{}", found.size(), serviceType.getName(),
					filterString != null ? " with filter '" + filterString + "'" : "");
		} finally {
			bridge.vm().resume();
		}
	}

	@Override
	public T getService() {
		ensureResolved();
		if (services.isEmpty()) {
			throw new IllegalStateException("Service '" + serviceType.getName() + "' not found"
					+ (filterString != null ? " with filter '" + filterString + "'" : ""));
		}
		return services.get(0);
	}

	@Override
	public T getService(ServiceReference<T> reference) {
		ensureResolved();
		int idx = references.indexOf(reference);
		if (idx < 0) {
			throw new IllegalArgumentException("Unknown ServiceReference: " + reference);
		}
		return services.get(idx);
	}

	@Override
	public List<T> getServices() {
		ensureResolved();
		return Collections.unmodifiableList(services);
	}

	@Override
	public ServiceReference<T> getServiceReference() {
		ensureResolved();
		if (references.isEmpty()) {
			return null;
		}
		return references.get(0);
	}

	@Override
	public List<ServiceReference<T>> getServiceReferences() {
		ensureResolved();
		return Collections.unmodifiableList(references);
	}

	@Override
	public SortedMap<ServiceReference<T>, T> getTracked() {
		ensureResolved();
		SortedMap<ServiceReference<T>, T> map = new TreeMap<>(
				(a, b) -> System.identityHashCode(a) - System.identityHashCode(b));
		for (int i = 0; i < references.size(); i++) {
			map.put(references.get(i), services.get(i));
		}
		return map;
	}

	@Override
	public int getTrackingCount() {
		return trackingCount;
	}

	@Override
	public boolean isEmpty() {
		ensureResolved();
		return services.isEmpty();
	}

	@Override
	public int size() {
		ensureResolved();
		return services.size();
	}

	@Override
	public T waitForService(long timeout) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeout;
		while (System.currentTimeMillis() < deadline) {
			close();
			ensureResolved();
			if (!services.isEmpty()) {
				return services.get(0);
			}
			Thread.sleep(Math.min(500, Math.max(1, deadline - System.currentTimeMillis())));
		}
		return null;
	}

	@Override
	public Class<T> getServiceType() {
		return serviceType;
	}

	@Override
	public Filter getFilter() {
		String filter = filterString != null && !filterString.isEmpty()
				? "(&(objectClass=" + serviceType.getName() + ")" + filterString + ")"
				: "(objectClass=" + serviceType.getName() + ")";
		try {
			return FrameworkUtil.createFilter(filter);
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException("Invalid filter: " + filter, e);
		}
	}

	@Override
	public long getTimeout() {
		return timeout;
	}

	@Override
	public int getCardinality() {
		return cardinality;
	}

	@Override
	public void close() {
		if (lookups.isEmpty()) {
			return;
		}
		bridge.vm().suspend();
		try {
			ThreadReference thread = bridge.acquireEventSuspendedThread();
			for (ServiceLookup lookup : lookups) {
				bridge.ungetService(lookup, thread);
			}
			LOG.debug("Released {} service lookups for {}", lookups.size(), serviceType.getName());
			lookups.clear();
			services.clear();
			references.clear();
			resolved = false;
		} finally {
			bridge.vm().resume();
		}
	}
}
