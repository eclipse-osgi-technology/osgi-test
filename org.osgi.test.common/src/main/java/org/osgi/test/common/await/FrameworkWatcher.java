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

package org.osgi.test.common.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.common.event.EventRecorder;
import org.osgi.test.common.event.EventRecorders;
import org.osgi.test.common.event.EventTimeoutException;
import org.osgi.test.common.event.TimedEvent;

/**
 * Waits for the OSGi framework to become quiet, i.e. no bundle or service
 * events are fired for a specified period. Each wait arms an
 * {@link EventRecorder} for its duration: a synchronous bundle listener, an
 * {@code AllServiceListener}, or both, depending on the method called.
 */
public class FrameworkWatcher implements AwaitCalm {

	private final BundleContext ctx;

	public FrameworkWatcher(BundleContext ctx) {
		this.ctx = ctx;
	}

	private void validateTimeouts(Duration quietPeriod, Duration timeout) {
		if (quietPeriod.isNegative() || quietPeriod.isZero()) {
			throw new IllegalArgumentException("The quiet period duration must be positive");
		}
		if (timeout.compareTo(quietPeriod) <= 0) {
			throw new IllegalArgumentException("The timeout must be longer than the quiet period");
		}
	}

	private static <E extends EventObject> List<TimedEvent<E>> waitForQuiet(EventRecorder<E> recorder,
		Duration quietPeriod, Duration timeout) throws InterruptedException, AwaitCalmTimeoutException {
		try (EventRecorder<E> r = recorder) {
			return r.waitForQuiet(quietPeriod, timeout);
		} catch (EventTimeoutException e) {
			throw new AwaitCalmTimeoutException(quietPeriod, timeout, asEventObjects(e.getEvents()));
		}
	}

	@SuppressWarnings("unchecked")
	private static List<TimedEvent<EventObject>> asEventObjects(List<TimedEvent<?>> events) {
		List<TimedEvent<EventObject>> result = new ArrayList<>(events.size());
		for (TimedEvent<?> event : events) {
			result.add((TimedEvent<EventObject>) event);
		}
		return result;
	}

	@Override
	public List<TimedEvent<EventObject>> waitForQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException {
		validateTimeouts(quietPeriod, timeout);
		return waitForQuiet(EventRecorders.bundleAndServiceEvents(ctx), quietPeriod, timeout);
	}

	@Override
	public List<TimedEvent<BundleEvent>> waitForBundleQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException {
		validateTimeouts(quietPeriod, timeout);
		return waitForQuiet(EventRecorders.bundleEvents(ctx, EventRecorders.ALL_TYPES, true), quietPeriod, timeout);
	}

	@Override
	public List<TimedEvent<ServiceEvent>> waitForServiceQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException {
		try {
			return waitForServiceQuiet(quietPeriod, timeout, null);
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException(e); // cannot happen without a filter
		}
	}

	@Override
	public List<TimedEvent<ServiceEvent>> waitForServiceQuiet(Duration quietPeriod, Duration timeout, String filter)
		throws InterruptedException, AwaitCalmTimeoutException, InvalidSyntaxException {
		validateTimeouts(quietPeriod, timeout);
		return waitForQuiet(EventRecorders.allServiceEvents(ctx, EventRecorders.ALL_TYPES, filter), quietPeriod,
			timeout);
	}
}
