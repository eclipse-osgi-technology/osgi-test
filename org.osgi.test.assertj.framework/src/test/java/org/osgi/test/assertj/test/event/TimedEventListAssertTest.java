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
package org.osgi.test.assertj.test.event;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STARTING;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;
import static org.osgi.test.assertj.bundleevent.BundleEventConditions.bundle;
import static org.osgi.test.assertj.bundleevent.BundleEventConditions.type;
import static org.osgi.test.assertj.event.TimedEventListAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.common.event.TimedEvent;

public class TimedEventListAssertTest {

	Bundle							bundle;
	Bundle							other;
	List<TimedEvent<BundleEvent>>	events;

	@BeforeEach
	void setUp() {
		bundle = mock(Bundle.class);
		other = mock(Bundle.class);
		events = new ArrayList<>();
		int[] types = {
			STOPPING, STOPPED, STARTING, STARTED
		};
		for (int i = 0; i < types.length; i++) {
			events.add(new TimedEvent<>(ofMillis(10 * i), new BundleEvent(types[i], bundle, bundle)));
		}
	}

	@Test
	public void hasEventTypesExactly() {
		assertThat(events).hasEventTypesExactly(STOPPING, STOPPED, STARTING, STARTED);
		assertThatThrownBy(() -> assertThat(events).hasEventTypesExactly(STOPPED, STOPPING, STARTING, STARTED))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("exactly")
			.hasMessageContaining("STOPPED")
			.hasMessageContaining("STOPPING");
		assertThatThrownBy(() -> assertThat(events).hasEventTypesExactly(STOPPING, STOPPED))
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void hasEventTypesInAnyOrder() {
		assertThat(events).hasEventTypesInAnyOrder(STARTED, STARTING, STOPPED, STOPPING);
		assertThatThrownBy(() -> assertThat(events).hasEventTypesInAnyOrder(STARTED, STARTING, STOPPED))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("unexpected")
			.hasMessageContaining("STOPPING");
		assertThatThrownBy(
			() -> assertThat(events).hasEventTypesInAnyOrder(STARTED, STARTING, STOPPED, STOPPING, BundleEvent.INSTALLED))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("missing")
			.hasMessageContaining("INSTALLED");
	}

	@Test
	public void containsEventTypesInOrder() {
		assertThat(events).containsEventTypesInOrder(STOPPED, STARTED);
		assertThat(events).containsEventTypesInOrder();
		assertThatThrownBy(() -> assertThat(events).containsEventTypesInOrder(STARTED, STOPPED))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("first type not found")
			.hasMessageContaining("STOPPED");
	}

	@Test
	public void hasEventsExactlyWithConditions() {
		assertThat(events).hasEventsExactly(allOf(type(STOPPING), bundle(bundle)), allOf(type(STOPPED), bundle(bundle)),
			allOf(type(STARTING), bundle(bundle)), allOf(type(STARTED), bundle(bundle)));
		assertThatThrownBy(() -> assertThat(events).hasEventsExactly(type(STOPPED), type(STOPPING), type(STARTING),
			type(STARTED))).isInstanceOf(AssertionError.class)
				.hasMessageContaining("index 0");
		assertThatThrownBy(() -> assertThat(events).hasEventsExactly(type(STOPPING)))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("Expecting 1 event(s)");
		assertThatThrownBy(() -> assertThat(events).hasEventsExactly(allOf(type(STOPPING), bundle(other)),
			type(STOPPED), type(STARTING), type(STARTED))).isInstanceOf(AssertionError.class);
	}

	@Test
	public void hasEventsInAnyOrderWithConditions() {
		assertThat(events).hasEventsInAnyOrder(type(STARTED), type(STARTING), type(STOPPED), type(STOPPING));
		assertThatThrownBy(() -> assertThat(events).hasEventsInAnyOrder(type(STARTED), type(STARTED), type(STOPPED),
			type(STOPPING))).isInstanceOf(AssertionError.class)
				.hasMessageContaining("none of the unmatched events fits");
		assertThatThrownBy(() -> assertThat(events).hasEventsInAnyOrder(type(STARTED)))
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void sizeAndEmptiness() {
		assertThat(events).hasSize(4)
			.isNotEmpty();
		assertThat(new ArrayList<TimedEvent<BundleEvent>>()).isEmpty()
			.hasSize(0);
		assertThatThrownBy(() -> assertThat(events).isEmpty()).isInstanceOf(AssertionError.class)
			.hasMessageContaining("recorded 4");
		assertThatThrownBy(() -> assertThat(events).hasSize(3)).isInstanceOf(AssertionError.class);
		assertThatThrownBy(() -> assertThat(new ArrayList<TimedEvent<BundleEvent>>()).isNotEmpty())
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void eventsAndEvent() {
		assertThat(events).events()
			.hasSize(4)
			.first()
			.isSameAs(events.get(0)
				.event());
		assertThat(events).event(2)
			.isSameAs(events.get(2)
				.event());
		assertThatThrownBy(() -> assertThat(events).event(4)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("index 4");
	}

	@Test
	public void conditions() {
		assertThat(events).allEventsAre(bundle(bundle))
			.anyEventIs(type(STARTED));
		assertThatThrownBy(() -> assertThat(events).allEventsAre(bundle(other))).isInstanceOf(AssertionError.class);
		assertThatThrownBy(() -> assertThat(events).anyEventIs(type(BundleEvent.INSTALLED)))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("INSTALLED");
	}

	@Test
	public void nullList() {
		assertThatThrownBy(() -> assertThat((List<TimedEvent<BundleEvent>>) null).hasSize(0))
			.isInstanceOf(AssertionError.class);
	}

	@Test
	public void subsequenceOverUnrelatedEvents() {
		List<TimedEvent<BundleEvent>> list = Arrays.asList(
			new TimedEvent<>(ofMillis(0), new BundleEvent(BundleEvent.INSTALLED, other, other)),
			new TimedEvent<>(ofMillis(1), new BundleEvent(STARTED, bundle, bundle)));
		assertThat(list).containsEventTypesInOrder(STARTED)
			.anyEventIs(bundle(other));
	}
}
