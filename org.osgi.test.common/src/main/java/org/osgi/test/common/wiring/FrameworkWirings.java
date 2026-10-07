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
package org.osgi.test.common.wiring;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.test.common.event.EventRecorders;
import org.osgi.test.common.event.EventTimeoutException;
import org.osgi.test.common.event.ManualEventRecorder;
import org.osgi.test.common.event.TimedEvent;

/**
 * Refreshing bundles in a test.
 * <p>
 * {@link FrameworkWiring#refreshBundles(Collection, org.osgi.framework.FrameworkListener...)}
 * returns at once and does its work on another thread, so a test that goes on
 * right away sees the old wiring. {@link #refreshBundles(BundleContext, Bundle...)}
 * waits for the {@link FrameworkEvent#PACKAGES_REFRESHED} event of
 * <em>this</em> refresh, which the framework delivers to the listener handed
 * to the refresh, and not for any refresh that happens to finish in the
 * meantime.
 */
public final class FrameworkWirings {

	/**
	 * The time {@link #refreshBundles(BundleContext, Bundle...)} waits for a
	 * refresh to finish: one minute, which is what the OSGi TCKs allowed.
	 */
	public static final Duration DEFAULT_REFRESH_TIMEOUT = Duration.ofMinutes(1);

	private FrameworkWirings() {}

	/**
	 * Refresh the bundles and wait up to {@link #DEFAULT_REFRESH_TIMEOUT}
	 * until the framework reports this refresh as done.
	 *
	 * @param context a bundle context of the framework
	 * @param bundles the bundles to refresh; none means all bundles with a
	 *            removal pending
	 * @return the {@link FrameworkEvent#PACKAGES_REFRESHED} event of this
	 *         refresh
	 * @throws EventTimeoutException if the refresh has not finished when the
	 *             default timeout expires
	 * @throws IllegalStateException if the waiting thread is interrupted; the
	 *             interrupt flag is set again
	 */
	public static FrameworkEvent refreshBundles(BundleContext context, Bundle... bundles)
		throws EventTimeoutException {
		return refreshBundles(context, DEFAULT_REFRESH_TIMEOUT, asCollection(bundles));
	}

	/**
	 * Refresh the bundles and wait up to {@link #DEFAULT_REFRESH_TIMEOUT}
	 * until the framework reports this refresh as done.
	 *
	 * @param context a bundle context of the framework
	 * @param bundles the bundles to refresh; {@code null} means all bundles
	 *            with a removal pending
	 * @return the {@link FrameworkEvent#PACKAGES_REFRESHED} event of this
	 *         refresh
	 * @throws EventTimeoutException if the refresh has not finished when the
	 *             default timeout expires
	 * @throws IllegalStateException if the waiting thread is interrupted; the
	 *             interrupt flag is set again
	 */
	public static FrameworkEvent refreshBundles(BundleContext context, Collection<Bundle> bundles)
		throws EventTimeoutException {
		return refreshBundles(context, DEFAULT_REFRESH_TIMEOUT, bundles);
	}

	/**
	 * Refresh the bundles and wait until the framework reports this refresh as
	 * done.
	 *
	 * @param context a bundle context of the framework
	 * @param timeout the maximum time to wait for the refresh to finish
	 * @param bundles the bundles to refresh; none means all bundles with a
	 *            removal pending
	 * @return the {@link FrameworkEvent#PACKAGES_REFRESHED} event of this
	 *         refresh
	 * @throws EventTimeoutException if the refresh has not finished when
	 *             {@code timeout} expires
	 * @throws IllegalStateException if the waiting thread is interrupted; the
	 *             interrupt flag is set again
	 */
	public static FrameworkEvent refreshBundles(BundleContext context, Duration timeout, Bundle... bundles)
		throws EventTimeoutException {
		return refreshBundles(context, timeout, asCollection(bundles));
	}

	/**
	 * Refresh the bundles and wait until the framework reports this refresh as
	 * done.
	 *
	 * @param context a bundle context of the framework
	 * @param timeout the maximum time to wait for the refresh to finish
	 * @param bundles the bundles to refresh; {@code null} means all bundles
	 *            with a removal pending
	 * @return the {@link FrameworkEvent#PACKAGES_REFRESHED} event of this
	 *         refresh
	 * @throws EventTimeoutException if the refresh has not finished when
	 *             {@code timeout} expires
	 * @throws IllegalStateException if the waiting thread is interrupted; the
	 *             interrupt flag is set again
	 */
	public static FrameworkEvent refreshBundles(BundleContext context, Duration timeout, Collection<Bundle> bundles)
		throws EventTimeoutException {
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(timeout, "timeout");
		FrameworkWiring wiring = context.getBundle(Constants.SYSTEM_BUNDLE_ID)
			.adapt(FrameworkWiring.class);
		if (wiring == null) {
			throw new IllegalStateException("The system bundle does not adapt to FrameworkWiring");
		}
		try (ManualEventRecorder<FrameworkEvent> refresh = EventRecorders.manual()) {
			wiring.refreshBundles(bundles, refresh::record);
			List<TimedEvent<FrameworkEvent>> events = refresh
				.waitFor(event -> event.getType() == FrameworkEvent.PACKAGES_REFRESHED, timeout);
			return events.get(events.size() - 1)
				.event();
		} catch (InterruptedException e) {
			Thread.currentThread()
				.interrupt();
			throw new IllegalStateException("Interrupted while waiting for the refresh to finish", e);
		}
	}

	@SafeVarargs
	private static <T> Collection<T> asCollection(T... items) {
		if (items == null || items.length == 0) {
			return null;
		}
		return Arrays.asList(items);
	}
}
