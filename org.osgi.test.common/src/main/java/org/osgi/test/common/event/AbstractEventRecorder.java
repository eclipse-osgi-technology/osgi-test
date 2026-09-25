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
package org.osgi.test.common.event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import org.osgi.test.common.event.TimedEvent;

/**
 * The recording and waiting logic shared by all recorders. Subclasses adapt it
 * to a framework listener interface and know how to unregister themselves.
 *
 * @param <E> the type of event recorded
 */
abstract class AbstractEventRecorder<E extends EventObject> implements EventRecorder<E> {

	private final ReentrantLock		lock		= new ReentrantLock();
	private final Condition			changed		= lock.newCondition();
	private final List<TimedEvent<E>>	events		= new ArrayList<>();
	private final long				startNanos	= System.nanoTime();
	private long					lastEventNanos	= startNanos;
	private boolean					closed;

	/**
	 * Remove this recorder's listener from the framework. Called once, from
	 * {@link #close()}.
	 */
	protected abstract void unregister();

	/**
	 * Record an event. Called from the listener callback, possibly on a
	 * framework thread and while the framework holds a bundle lock, so this
	 * does nothing but store the event and wake up waiters.
	 *
	 * @param event the event to record
	 */
	protected void record(E event) {
		lock.lock();
		try {
			if (closed) {
				return;
			}
			long now = System.nanoTime();
			events.add(new TimedEvent<>(Duration.ofNanos(now - startNanos), event));
			lastEventNanos = now;
			changed.signalAll();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> events() {
		lock.lock();
		try {
			return Collections.unmodifiableList(new ArrayList<>(events));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> drain() {
		lock.lock();
		try {
			return drainUpTo(events.size());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int size() {
		lock.lock();
		try {
			return events.size();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		lock.lock();
		try {
			events.clear();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isClosed() {
		lock.lock();
		try {
			return closed;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() {
		lock.lock();
		try {
			if (closed) {
				return;
			}
			closed = true;
			changed.signalAll();
		} finally {
			lock.unlock();
		}
		unregister();
	}

	@Override
	public List<TimedEvent<E>> waitForCount(int count, Duration timeout)
		throws InterruptedException, EventTimeoutException {
		return waitForCount(count, timeout, null);
	}

	@Override
	public List<TimedEvent<E>> waitForCount(int count, Duration timeout, Duration giveUpAfterQuiet)
		throws InterruptedException, EventTimeoutException {
		if (count < 0) {
			throw new IllegalArgumentException("The count must not be negative");
		}
		requireTimeout(timeout);
		Long giveUpNanos = giveUpNanos(giveUpAfterQuiet);
		lock.lock();
		try {
			long start = System.nanoTime();
			long deadline = start + timeout.toNanos();
			while (true) {
				checkOpen();
				if (events.size() >= count) {
					return drainUpTo(events.size());
				}
				awaitUntil(start, deadline, giveUpNanos, () -> "at least " + count + " event(s)", timeout);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> waitFor(Predicate<? super E> predicate, Duration timeout)
		throws InterruptedException, EventTimeoutException {
		return waitFor(predicate, timeout, null);
	}

	@Override
	public List<TimedEvent<E>> waitFor(Predicate<? super E> predicate, Duration timeout, Duration giveUpAfterQuiet)
		throws InterruptedException, EventTimeoutException {
		Objects.requireNonNull(predicate, "predicate");
		requireTimeout(timeout);
		Long giveUpNanos = giveUpNanos(giveUpAfterQuiet);
		lock.lock();
		try {
			long start = System.nanoTime();
			long deadline = start + timeout.toNanos();
			int next = 0;
			while (true) {
				checkOpen();
				for (; next < events.size(); next++) {
					if (predicate.test(events.get(next)
						.event())) {
						return drainUpTo(next + 1);
					}
				}
				awaitUntil(start, deadline, giveUpNanos, () -> "an event matching " + predicate, timeout);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> waitForSequence(List<? extends Predicate<? super E>> sequence, Duration timeout)
		throws InterruptedException, EventTimeoutException {
		return waitForSequence(sequence, timeout, null);
	}

	@Override
	public List<TimedEvent<E>> waitForSequence(List<? extends Predicate<? super E>> sequence, Duration timeout,
		Duration giveUpAfterQuiet) throws InterruptedException, EventTimeoutException {
		Objects.requireNonNull(sequence, "sequence");
		requireTimeout(timeout);
		Long giveUpNanos = giveUpNanos(giveUpAfterQuiet);
		if (sequence.isEmpty()) {
			return Collections.emptyList();
		}
		lock.lock();
		try {
			long start = System.nanoTime();
			long deadline = start + timeout.toNanos();
			int next = 0;
			int matched = 0;
			while (true) {
				checkOpen();
				for (; next < events.size() && matched < sequence.size(); next++) {
					if (sequence.get(matched)
						.test(events.get(next)
							.event())) {
						matched++;
					}
				}
				if (matched == sequence.size()) {
					return drainUpTo(next);
				}
				final int done = matched;
				awaitUntil(start, deadline, giveUpNanos, () -> "a sequence of " + sequence.size()
					+ " matching event(s), " + done + " of which occurred so far", timeout);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> waitForQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, EventTimeoutException {
		requireQuietPeriod(quietPeriod);
		requireTimeout(timeout);
		if (timeout.compareTo(quietPeriod) <= 0) {
			throw new IllegalArgumentException("The timeout must be longer than the quiet period");
		}
		lock.lock();
		try {
			long deadline = System.nanoTime() + timeout.toNanos();
			return awaitQuiet(quietPeriod, deadline, timeout);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> waitForCountThenQuiet(int count, Duration quietPeriod, Duration timeout)
		throws InterruptedException, EventTimeoutException {
		if (count < 0) {
			throw new IllegalArgumentException("The count must not be negative");
		}
		requireQuietPeriod(quietPeriod);
		requireTimeout(timeout);
		lock.lock();
		try {
			long start = System.nanoTime();
			long deadline = start + timeout.toNanos();
			while (events.size() < count) {
				checkOpen();
				awaitUntil(start, deadline, null,
					() -> "at least " + count + " event(s) followed by " + quietPeriod + " of quiet", timeout);
			}
			return awaitQuiet(quietPeriod, deadline, timeout);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> collect(int count, Duration timeout) throws InterruptedException {
		if (count < 0) {
			throw new IllegalArgumentException("The count must not be negative");
		}
		requireTimeout(timeout);
		lock.lock();
		try {
			long deadline = System.nanoTime() + timeout.toNanos();
			while (events.size() < count) {
				checkOpen();
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					break;
				}
				changed.awaitNanos(remaining);
			}
			checkOpen();
			return drainUpTo(events.size());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<TimedEvent<E>> collectQuiet(int minCount, Duration quietPeriod, Duration timeout)
		throws InterruptedException {
		if (minCount < 0) {
			throw new IllegalArgumentException("The count must not be negative");
		}
		requireQuietPeriod(quietPeriod);
		requireTimeout(timeout);
		final long quietNanos = quietPeriod.toNanos();
		lock.lock();
		try {
			long deadline = System.nanoTime() + timeout.toNanos();
			while (true) {
				checkOpen();
				long now = System.nanoTime();
				long remaining = deadline - now;
				if (remaining <= 0) {
					break;
				}
				if (events.size() < minCount) {
					changed.awaitNanos(remaining);
					continue;
				}
				long remainingQuiet = quietNanos - (now - lastEventNanos);
				if (remainingQuiet <= 0) {
					break;
				}
				changed.awaitNanos(Math.min(remainingQuiet, remaining));
			}
			checkOpen();
			return drainUpTo(events.size());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Wait, holding the lock, until no event has been recorded for
	 * {@code quietPeriod}. Gives up as soon as the quiet period can no longer
	 * fit before the deadline.
	 */
	private List<TimedEvent<E>> awaitQuiet(Duration quietPeriod, long deadline, Duration timeout)
		throws InterruptedException {
		final long quietNanos = quietPeriod.toNanos();
		while (true) {
			checkOpen();
			long now = System.nanoTime();
			long remainingQuiet = quietNanos - (now - lastEventNanos);
			if (remainingQuiet <= 0) {
				return drainUpTo(events.size());
			}
			if (remainingQuiet > deadline - now) {
				throw timeoutException(quietPeriod + " of quiet", timeout);
			}
			changed.awaitNanos(remainingQuiet);
		}
	}

	/**
	 * Wait, holding the lock, for a change or until the deadline. Throws the
	 * timeout exception once the deadline has passed, or, if
	 * {@code giveUpNanos} is set, once no event has been recorded for that
	 * long since the later of the start of the wait and the last event.
	 */
	private void awaitUntil(long start, long deadline, Long giveUpNanos,
		java.util.function.Supplier<String> expectation, Duration timeout) throws InterruptedException {
		long now = System.nanoTime();
		long remaining = deadline - now;
		if (remaining <= 0) {
			throw timeoutException(expectation.get(), timeout);
		}
		if (giveUpNanos != null) {
			long quietSince = Math.max(start, lastEventNanos);
			long remainingQuiet = giveUpNanos - (now - quietSince);
			if (remainingQuiet <= 0) {
				throw new EventTimeoutException(expectation.get() + ", but gave up: no event for "
					+ Duration.ofNanos(giveUpNanos), timeout, new ArrayList<>(events));
			}
			remaining = Math.min(remaining, remainingQuiet);
		}
		changed.awaitNanos(remaining);
	}

	private static Long giveUpNanos(Duration giveUpAfterQuiet) {
		if (giveUpAfterQuiet == null) {
			return null;
		}
		if (giveUpAfterQuiet.isNegative() || giveUpAfterQuiet.isZero()) {
			throw new IllegalArgumentException("The give-up quiet period must be positive");
		}
		return giveUpAfterQuiet.toNanos();
	}

	private EventTimeoutException timeoutException(String expectation, Duration timeout) {
		return new EventTimeoutException(expectation, timeout, new ArrayList<>(events));
	}

	private void checkOpen() {
		if (closed) {
			throw new IllegalStateException("The EventRecorder is closed");
		}
	}

	private List<TimedEvent<E>> drainUpTo(int count) {
		List<TimedEvent<E>> head = events.subList(0, count);
		List<TimedEvent<E>> result = Collections.unmodifiableList(new ArrayList<>(head));
		head.clear();
		return result;
	}

	private static void requireTimeout(Duration timeout) {
		Objects.requireNonNull(timeout, "timeout");
		if (timeout.isNegative()) {
			throw new IllegalArgumentException("The timeout must not be negative");
		}
	}

	private static void requireQuietPeriod(Duration quietPeriod) {
		Objects.requireNonNull(quietPeriod, "quietPeriod");
		if (quietPeriod.isNegative() || quietPeriod.isZero()) {
			throw new IllegalArgumentException("The quiet period duration must be positive");
		}
	}
}
