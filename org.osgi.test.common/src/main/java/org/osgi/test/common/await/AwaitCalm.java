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
import java.util.EventObject;
import java.util.List;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.common.event.TimedEvent;

/**
 * A utility which can be used to await a period of calm in the framework
 */
@ProviderType
public interface AwaitCalm {

	/**
	 * Waits for up to <code>timeout</code> for at least
	 * <code>quietPeriod</code> with no service or bundle events.
	 *
	 * @param quietPeriod The required time during which no events must occur
	 * @param timeout The maximum time to wait for quiet
	 * @return The events received during the wait, in the order they occurred
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws AwaitCalmTimeoutException if {@code timeout} expires before the
	 *             quiet period is reached
	 */
	List<TimedEvent<EventObject>> waitForQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException;

	/**
	 * Waits for up to <code>timeout</code> for at least
	 * <code>quietPeriod</code> with no bundle events.
	 *
	 * @param quietPeriod The required time during which no events must occur
	 * @param timeout The maximum time to wait for quiet
	 * @return The events received during the wait, in the order they occurred
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws AwaitCalmTimeoutException if {@code timeout} expires before the
	 *             quiet period is reached
	 */
	List<TimedEvent<BundleEvent>> waitForBundleQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException;

	/**
	 * Waits for up to <code>timeout</code> for at least
	 * <code>quietPeriod</code> with no service events.
	 *
	 * @param quietPeriod The required time during which no events must occur
	 * @param timeout The maximum time to wait for quiet
	 * @return The events received during the wait, in the order they occurred
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws AwaitCalmTimeoutException if {@code timeout} expires before the
	 *             quiet period is reached
	 */
	List<TimedEvent<ServiceEvent>> waitForServiceQuiet(Duration quietPeriod, Duration timeout)
		throws InterruptedException, AwaitCalmTimeoutException;

	/**
	 * Waits for up to <code>timeout</code> for at least
	 * <code>quietPeriod</code> with no service events that match the supplied
	 * filter.
	 *
	 * @param quietPeriod The required time during which no events must occur
	 * @param timeout The maximum time to wait for quiet
	 * @param filter The filter to apply when listening for service events
	 * @return The events received during the wait, in the order they occurred
	 * @throws InterruptedException if the waiting thread is interrupted
	 * @throws AwaitCalmTimeoutException if {@code timeout} expires before the
	 *             quiet period is reached
	 * @throws InvalidSyntaxException if the <code>filter</code> is not valid
	 */
	List<TimedEvent<ServiceEvent>> waitForServiceQuiet(Duration quietPeriod, Duration timeout, String filter)
		throws InterruptedException, AwaitCalmTimeoutException, InvalidSyntaxException;
}
