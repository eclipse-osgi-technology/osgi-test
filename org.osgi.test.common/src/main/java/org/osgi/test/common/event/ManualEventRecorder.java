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

import java.util.Objects;

/**
 * An {@link EventRecorder} without an event source of its own: the test feeds
 * it. Use it for callbacks the framework does not deliver as bundle, service
 * or framework events, such as {@code ConfigurationListener},
 * {@code LogListener} or a component's own activation callback, and then wait
 * on it like on any other recorder.
 *
 * <pre>
 * ManualEventRecorder&lt;ConfigurationEvent&gt; cmEvents = EventRecorders.manual();
 * context.registerService(ConfigurationListener.class, cmEvents::record, null);
 * configuration.update(properties);
 * cmEvents.waitForCountThenQuiet(1, Duration.ofMillis(200), Duration.ofSeconds(5));
 * </pre>
 *
 * Events recorded after {@link #close()} are ignored.
 *
 * @param <E> the event type, which need not be an {@link java.util.EventObject}
 */
public final class ManualEventRecorder<E> extends AbstractEventRecorder<E> {

	ManualEventRecorder() {}

	/**
	 * Record an event. Safe to call from any thread.
	 *
	 * @param event the event, not {@code null}
	 */
	@Override
	public void record(E event) {
		Objects.requireNonNull(event, "event");
		super.record(event);
	}

	@Override
	protected void unregister() {
		// nothing was registered: the test owns the event source
	}
}
