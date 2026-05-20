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

/**
 * Thrown when the framework does not become quiet within the specified timeout.
 */
public class AwaitCalmTimeoutException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	private final Duration		quietPeriod;
	private final Duration		timeout;
	private final int			eventCount;

	/**
	 * @param quietPeriod the required silence duration that was not reached
	 * @param timeout the maximum wait time that expired
	 * @param eventCount the number of events received before timeout
	 */
	public AwaitCalmTimeoutException(Duration quietPeriod, Duration timeout, int eventCount) {
		super("Framework did not quiesce within " + timeout + " (required quiet period: " + quietPeriod + ", events: "
			+ eventCount + ")");
		this.quietPeriod = quietPeriod;
		this.timeout = timeout;
		this.eventCount = eventCount;
	}

	/** Returns the required silence duration that was not reached. */
	public Duration getQuietPeriod() {
		return quietPeriod;
	}

	/** Returns the maximum wait time that expired. */
	public Duration getTimeout() {
		return timeout;
	}

	/** Returns the number of events received before timeout. */
	public int getEventCount() {
		return eventCount;
	}
}
