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
import java.util.EventObject;
import java.util.List;
import java.util.function.Predicate;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.test.common.event.TimedEvent;

/**
 * Records framework events from the moment it is created until it is
 * {@link #close() closed}, and lets a test wait for them.
 * <p>
 * A recorder is armed <em>before</em> the action under test, so that
 * synchronously delivered events and events that arrive before the test
 * thread starts waiting are captured as well. The Core specification only
 * delivers events to listeners that were registered before the event was
 * published.
 * <p>
 * Recorded events are kept until they are drained. Every {@code waitFor*}
 * method drains the events it returns, so consecutive waits see consecutive
 * batches of events, in the style of the TCK's {@code getList} and
 * {@code getResults} helpers. {@link #events()} takes a snapshot without
 * draining.
 * <p>
 * The {@link TimedEvent#time() time} of a recorded event is relative to the
 * creation of the recorder.
 * <p>
 * Instances are created by {@link EventRecorders} and are thread safe. The
 * listener callback only records the event and never calls back into the
 * framework, so a synchronous recorder is safe to use while the framework
 * holds a bundle's state change lock.
 *
 * @param <E> the type of event recorded
 */
@ProviderType
public interface EventRecorder<E extends EventObject> extends AutoCloseable {

	/**
	 * @return a snapshot of the events recorded since the last drain, in the
	 *         order they were recorded
	 */
	List<TimedEvent<E>> events();

	/**
	 * Take the recorded events and clear the recorder.
	 *
	 * @return the events recorded since the last drain, in the order they were
	 *         recorded
	 */
	List<TimedEvent<E>> drain();

	/**
	 * @return the number of events recorded since the last drain
	 */
	int size();

	/**
	 * Discard the events recorded since the last drain.
	 */
	void clear();

	/**
	 * @return {@code true} once the recorder has been closed
	 */
	boolean isClosed();

	/**
	 * Wait until at least {@code count} events have been recorded.
	 * <p>
	 * More than {@code count} events may be returned if they arrived before
	 * the wait completed.
	 *
	 * @param count the number of events to wait for, zero returns immediately
	 * @param timeout the maximum time to wait
	 * @return the recorded events, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if fewer than {@code count} events have
	 *             been recorded when {@code timeout} expires
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitForCount(int count, Duration timeout) throws InterruptedException, EventTimeoutException;

	/**
	 * Wait until an event matching {@code predicate} has been recorded.
	 *
	 * @param predicate the condition an event must satisfy
	 * @param timeout the maximum time to wait
	 * @return the recorded events up to and including the first matching
	 *         event, drained; later events stay recorded
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if no matching event has been recorded
	 *             when {@code timeout} expires
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitFor(Predicate<? super E> predicate, Duration timeout)
		throws InterruptedException, EventTimeoutException;

	/**
	 * Wait until events matching the given predicates have been recorded in
	 * the given order. Other events may occur between the matches.
	 *
	 * @param sequence the conditions, in the order the events must occur
	 * @param timeout the maximum time to wait
	 * @return the recorded events up to and including the event matching the
	 *         last predicate, drained; later events stay recorded
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if the sequence has not been completed
	 *             when {@code timeout} expires
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitForSequence(List<? extends Predicate<? super E>> sequence, Duration timeout)
		throws InterruptedException, EventTimeoutException;

	/**
	 * Like {@link #waitForCount(int, Duration)}, but gives up early once no
	 * event at all has been recorded for {@code giveUpAfterQuiet}, measured
	 * from the later of the start of the wait and the last recorded event. A
	 * quiet framework means the expected events will not arrive any more, so
	 * a failing test fails at once instead of waiting for the full timeout.
	 * A passing test is not affected.
	 *
	 * @param count the number of events to wait for
	 * @param timeout the maximum time to wait
	 * @param giveUpAfterQuiet the period of silence after which the wait is
	 *            abandoned, must be positive
	 * @return the recorded events, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if the timeout expires or the framework
	 *             goes quiet before {@code count} events have been recorded
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitForCount(int count, Duration timeout, Duration giveUpAfterQuiet)
		throws InterruptedException, EventTimeoutException;

	/**
	 * Like {@link #waitFor(Predicate, Duration)}, but gives up early once no
	 * event at all has been recorded for {@code giveUpAfterQuiet}, measured
	 * from the later of the start of the wait and the last recorded event.
	 *
	 * @param predicate the condition an event must satisfy
	 * @param timeout the maximum time to wait
	 * @param giveUpAfterQuiet the period of silence after which the wait is
	 *            abandoned, must be positive
	 * @return the recorded events up to and including the first matching
	 *         event, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if the timeout expires or the framework
	 *             goes quiet before a matching event has been recorded
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitFor(Predicate<? super E> predicate, Duration timeout, Duration giveUpAfterQuiet)
		throws InterruptedException, EventTimeoutException;

	/**
	 * Like {@link #waitForSequence(List, Duration)}, but gives up early once
	 * no event at all has been recorded for {@code giveUpAfterQuiet},
	 * measured from the later of the start of the wait and the last recorded
	 * event.
	 *
	 * @param sequence the conditions, in the order the events must occur
	 * @param timeout the maximum time to wait
	 * @param giveUpAfterQuiet the period of silence after which the wait is
	 *            abandoned, must be positive
	 * @return the recorded events up to and including the event matching the
	 *         last predicate, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if the timeout expires or the framework
	 *             goes quiet before the sequence has been completed
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitForSequence(List<? extends Predicate<? super E>> sequence, Duration timeout,
		Duration giveUpAfterQuiet) throws InterruptedException, EventTimeoutException;

	/**
	 * Wait until no event has been recorded for {@code quietPeriod}.
	 * <p>
	 * The quiet period is measured from the last recorded event, or from the
	 * creation of the recorder if no event has been recorded.
	 *
	 * @param quietPeriod the required time during which no event must occur,
	 *            must be positive
	 * @param timeout the maximum time to wait, must be longer than the quiet
	 *            period
	 * @return the recorded events, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if {@code timeout} expires, or no longer
	 *             leaves room for the quiet period, before the quiet period is
	 *             reached
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitForQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, EventTimeoutException;

	/**
	 * Wait until at least {@code count} events have been recorded and then no
	 * further event has been recorded for {@code quietPeriod}.
	 * <p>
	 * This mirrors the TCK's {@code getResults(n)}: it collects the events an
	 * action produces without cutting the batch short, but with a quiet
	 * period and an overall timeout of the caller's choosing.
	 *
	 * @param count the minimum number of events to wait for
	 * @param quietPeriod the required time during which no event must occur
	 *            after the count is reached, must be positive
	 * @param timeout the maximum time to wait overall
	 * @return the recorded events, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws EventTimeoutException if {@code timeout} expires before the
	 *             count and the quiet period are reached
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> waitForCountThenQuiet(int count, Duration quietPeriod, Duration timeout)
		throws InterruptedException, EventTimeoutException;

	/**
	 * Wait until at least {@code count} events have been recorded or
	 * {@code timeout} expires, whichever comes first, and return whatever has
	 * been recorded. Unlike {@link #waitForCount(int, Duration)} this never
	 * fails on timeout, so a test can branch on the number of events, as the
	 * TCK's {@code getList(n, timeout)} allows.
	 *
	 * @param count the number of events to wait for
	 * @param timeout the maximum time to wait
	 * @return the recorded events, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> collect(int count, Duration timeout) throws InterruptedException;

	/**
	 * Wait until at least {@code minCount} events have been recorded and then
	 * no further event for {@code quietPeriod}, or until {@code timeout}
	 * expires, and return whatever has been recorded. This is the TCK's
	 * {@code getResults(n)}: it never fails, and the quiet period decides when
	 * a batch of events is complete.
	 *
	 * @param minCount the number of events to wait for before the quiet
	 *            period starts to count
	 * @param quietPeriod the required time during which no event must occur,
	 *            must be positive
	 * @param timeout the maximum time to wait overall
	 * @return the recorded events, drained
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws IllegalStateException if the recorder is closed
	 */
	List<TimedEvent<E>> collectQuiet(int minCount, Duration quietPeriod, Duration timeout)
		throws InterruptedException;

	/**
	 * Stop recording and remove the listener from the framework. Closing an
	 * already closed recorder has no effect. Threads waiting on the recorder
	 * are woken up and fail with an {@link IllegalStateException}.
	 */
	@Override
	void close();
}
