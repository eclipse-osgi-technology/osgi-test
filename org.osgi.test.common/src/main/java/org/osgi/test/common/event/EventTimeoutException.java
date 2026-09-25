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
import java.util.Collections;
import java.util.List;

import org.osgi.test.common.event.TimedEvent;

/**
 * Thrown when an {@link EventRecorder} wait does not complete in time. The
 * exception carries the events recorded up to that point, so that a failing
 * test can report what actually happened.
 */
public class EventTimeoutException extends RuntimeException {

	private static final long				serialVersionUID	= 1L;

	private final String					expectation;
	private final Duration					timeout;
	private final List<TimedEvent<?>>	events;

	public EventTimeoutException(String expectation, Duration timeout, List<? extends TimedEvent<?>> events) {
		super(String.format("Timed out after %s waiting for %s. Recorded %d event(s): %s", timeout, expectation,
			events.size(), events));
		this.expectation = expectation;
		this.timeout = timeout;
		this.events = Collections.unmodifiableList(events);
	}

	/**
	 * @return a description of what the wait was for
	 */
	public String getExpectation() {
		return expectation;
	}

	/**
	 * @return the timeout that expired
	 */
	public Duration getTimeout() {
		return timeout;
	}

	/**
	 * @return the events recorded when the timeout expired, in the order they
	 *         were recorded
	 */
	public List<TimedEvent<?>> getEvents() {
		return events;
	}
}
