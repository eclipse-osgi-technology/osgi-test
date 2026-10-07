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
package org.osgi.test.common.test.event;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.test.common.event.EventRecorders;
import org.osgi.test.common.event.EventTimeoutException;
import org.osgi.test.common.event.ManualEventRecorder;
import org.osgi.test.common.event.TimedEvent;

public class ManualEventRecorderTest {

	/** An event type of the test's own, not an EventObject. */
	static final class Updated {
		final String pid;

		Updated(String pid) {
			this.pid = pid;
		}

		@Override
		public String toString() {
			return "Updated[" + pid + "]";
		}
	}

	@Test
	void recordsWhatTheTestFeeds() throws Exception {
		try (ManualEventRecorder<Updated> recorder = EventRecorders.manual()) {
			recorder.record(new Updated("a"));
			recorder.record(new Updated("b"));
			List<TimedEvent<Updated>> events = recorder.waitForCount(2, ofSeconds(1));
			assertThat(events).extracting(e -> e.event().pid)
				.containsExactly("a", "b");
			assertThat(recorder.size()).isZero();
		}
	}

	@Test
	void waitsForAnEventFromAnotherThread() throws Exception {
		try (ManualEventRecorder<Updated> recorder = EventRecorders.manual()) {
			Thread feeder = new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				recorder.record(new Updated("late"));
			});
			feeder.start();
			List<TimedEvent<Updated>> events = recorder.waitFor(e -> "late".equals(e.pid), ofSeconds(5));
			assertThat(events).hasSize(1);
			assertThat(events.get(0)
				.time()).isGreaterThanOrEqualTo(ofMillis(50));
			feeder.join();
		}
	}

	@Test
	void waitsForQuietAfterTheEvents() throws Exception {
		try (ManualEventRecorder<Updated> recorder = EventRecorders.manual()) {
			recorder.record(new Updated("a"));
			List<TimedEvent<Updated>> events = recorder.waitForCountThenQuiet(1, ofMillis(100), ofSeconds(5));
			assertThat(events).hasSize(1);
		}
	}

	@Test
	void timesOutWithoutEvents() {
		try (ManualEventRecorder<Updated> recorder = EventRecorders.manual()) {
			assertThatThrownBy(() -> recorder.waitForCount(1, ofMillis(100))).isInstanceOf(EventTimeoutException.class);
		}
	}

	@Test
	void ignoresEventsAfterClose() throws Exception {
		ManualEventRecorder<Updated> recorder = EventRecorders.manual();
		recorder.close();
		assertThat(recorder.isClosed()).isTrue();
		recorder.record(new Updated("ignored"));
		assertThat(recorder.events()).isEmpty();
	}

	@Test
	void rejectsNull() {
		try (ManualEventRecorder<Updated> recorder = EventRecorders.manual()) {
			assertThatThrownBy(() -> recorder.record(null)).isInstanceOf(NullPointerException.class);
		}
	}
}
