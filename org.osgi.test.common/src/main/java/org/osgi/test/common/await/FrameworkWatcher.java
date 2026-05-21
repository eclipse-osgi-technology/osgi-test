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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.SynchronousBundleListener;

/**
 * Waits for the OSGi framework to become quiet, i.e. no bundle or service
 * events are fired for a specified period. Implements both
 * {@link SynchronousBundleListener} and {@link AllServiceListener} so the
 * caller controls the scope by choosing which listener to register.
 */
public class FrameworkWatcher implements AwaitCalm {

	private static class Listener implements SynchronousBundleListener, AllServiceListener {

		private final ReentrantLock	lock	= new ReentrantLock();
		private final Condition		quiet	= lock.newCondition();
		private int					eventCount;
		private long				lastEventNanos;

		@Override
		public void bundleChanged(BundleEvent event) {
			onEvent();
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			onEvent();
		}

		private void onEvent() {
			lock.lock();
			try {
				eventCount++;
				lastEventNanos = System.nanoTime();
				quiet.signalAll();
			} finally {
				lock.unlock();
			}
		}

		int doWaitForQuiet(Duration quietPeriod, Duration timeout)
			throws InterruptedException, AwaitCalmTimeoutException {
			final long quietNanos = quietPeriod.toNanos();
			final long deadlineNanos = timeout.toNanos();
			final long start = System.nanoTime();
			long remainingQuiet = quietNanos;
			lock.lock();
			if (eventCount == 0) {
				// If no events have occurred yet then we initialize
				// lastEventNanos to when we started
				lastEventNanos = start;
			}
			try {
				for (long now = System.nanoTime(); (now - start) <= deadlineNanos; now = System.nanoTime()) {
					remainingQuiet = quietNanos - (now - lastEventNanos);
					if (remainingQuiet <= 0) {
						// We have had enough quiet
						return eventCount;
					}
					long elapsed = now - start;
					if (remainingQuiet > (deadlineNanos - elapsed)) {
						// There is no longer enough time for quietPeriod to
						// pass before the deadline
						break;
					}
					if (quiet.awaitNanos(remainingQuiet) <= 0) {
						// No need to re-check the elapsed time
						return eventCount;
					}
				}
			} finally {
				lock.unlock();
			}
			throw new AwaitCalmTimeoutException(quietPeriod, timeout, eventCount);
		}
	}

	private void validateTimeouts(Duration quietPeriod, Duration timeout) {
		if (quietPeriod.isNegative() || quietPeriod.isZero()) {
			throw new IllegalArgumentException("The quiet period duration must be positive");
		}
		if (timeout.compareTo(quietPeriod) <= 0) {
			throw new IllegalArgumentException("The timeout must be longer than the quiet period");
		}
	}

	private final BundleContext ctx;

	public FrameworkWatcher(BundleContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int waitForQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException {
		validateTimeouts(quietPeriod, timeout);
		Listener listener = new Listener();
		ctx.addBundleListener(listener);
		try {
			ctx.addServiceListener(listener);
			try {
				return listener.doWaitForQuiet(quietPeriod, timeout);
			} finally {
				ctx.removeServiceListener(listener);
			}
		} finally {
			ctx.removeBundleListener(listener);
		}
	}

	@Override
	public int waitForBundleQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException {
		validateTimeouts(quietPeriod, timeout);
		Listener listener = new Listener();
		ctx.addBundleListener(listener);
		try {
			return listener.doWaitForQuiet(quietPeriod, timeout);
		} finally {
			ctx.removeBundleListener(listener);
		}
	}

	@Override
	public int waitForServiceQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException {
		validateTimeouts(quietPeriod, timeout);
		Listener listener = new Listener();
		ctx.addServiceListener(listener);
		try {
			return listener.doWaitForQuiet(quietPeriod, timeout);
		} finally {
			ctx.removeServiceListener(listener);
		}
	}

	@Override
	public int waitForServiceQuiet(Duration quietPeriod, Duration timeout, String filter)
		throws InterruptedException, AwaitCalmTimeoutException, InvalidSyntaxException {
		validateTimeouts(quietPeriod, timeout);
		Listener listener = new Listener();
		ctx.addServiceListener(listener, filter);
		try {
			return listener.doWaitForQuiet(quietPeriod, timeout);
		} finally {
			ctx.removeServiceListener(listener);
		}
	}
}
