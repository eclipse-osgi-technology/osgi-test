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
package org.osgi.test.assertj.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.test.common.bitmaps.Bitmap;
import org.osgi.test.common.bitmaps.BundleEventType;
import org.osgi.test.common.bitmaps.FrameworkEventType;
import org.osgi.test.common.bitmaps.ServiceEventType;
import org.osgi.test.common.event.TimedEvent;

/**
 * Assertions on a list of {@link TimedEvent}s, as returned by an
 * {@code EventRecorder}. Event types are the {@code getType()} values of
 * {@link BundleEvent}, {@link ServiceEvent} and {@link FrameworkEvent}; they
 * are shown by name in failure messages.
 *
 * @param <E> the type of event
 */
public class TimedEventListAssert<E extends EventObject>
	extends AbstractAssert<TimedEventListAssert<E>, List<? extends TimedEvent<E>>> {

	public TimedEventListAssert(List<? extends TimedEvent<E>> actual) {
		super(actual, TimedEventListAssert.class);
	}

	public static <E extends EventObject> TimedEventListAssert<E> assertThat(List<? extends TimedEvent<E>> actual) {
		return new TimedEventListAssert<>(actual);
	}

	/**
	 * @return the events without their times
	 */
	public ListAssert<E> events() {
		isNotNull();
		return Assertions.assertThat(eventList());
	}

	/**
	 * @param index the position in the list
	 * @return the event at that position
	 */
	public ObjectAssert<E> event(int index) {
		isNotNull();
		if (index < 0 || index >= actual.size()) {
			throw failure("%nExpecting an event at index %d but the list has %d event(s):%n <%s>", index,
				actual.size(), describe(actual));
		}
		return Assertions.assertThat(actual.get(index)
			.event());
	}

	public TimedEventListAssert<E> isEmpty() {
		isNotNull();
		if (!actual.isEmpty()) {
			throw failure("%nExpecting no events but recorded %d:%n <%s>", actual.size(), describe(actual));
		}
		return myself;
	}

	public TimedEventListAssert<E> isNotEmpty() {
		isNotNull();
		if (actual.isEmpty()) {
			throw failure("%nExpecting at least one event but none was recorded");
		}
		return myself;
	}

	public TimedEventListAssert<E> hasSize(int expected) {
		isNotNull();
		if (actual.size() != expected) {
			throw failure("%nExpecting %d event(s) but recorded %d:%n <%s>", expected, actual.size(),
				describe(actual));
		}
		return myself;
	}

	/**
	 * The events must have exactly the given types, in the given order.
	 *
	 * @param types the expected event types
	 * @return this assertion
	 */
	public TimedEventListAssert<E> hasEventTypesExactly(int... types) {
		isNotNull();
		List<Integer> actualTypes = types();
		List<Integer> expectedTypes = boxed(types);
		if (!actualTypes.equals(expectedTypes)) {
			throw failure("%nExpecting event types exactly:%n <%s>%nbut recorded:%n <%s>", describeTypes(expectedTypes),
				describe(actual));
		}
		return myself;
	}

	/**
	 * The events must have exactly the given types, in any order.
	 *
	 * @param types the expected event types
	 * @return this assertion
	 */
	public TimedEventListAssert<E> hasEventTypesInAnyOrder(int... types) {
		isNotNull();
		List<Integer> remaining = types();
		List<Integer> missing = new ArrayList<>();
		for (int type : types) {
			if (!remaining.remove(Integer.valueOf(type))) {
				missing.add(type);
			}
		}
		if (!missing.isEmpty() || !remaining.isEmpty()) {
			throw failure(
				"%nExpecting event types in any order:%n <%s>%nbut recorded:%n <%s>%nmissing:%n <%s>%nunexpected:%n <%s>",
				describeTypes(boxed(types)), describe(actual), describeTypes(missing), describeTypes(remaining));
		}
		return myself;
	}

	/**
	 * The events must contain the given types in the given order, possibly
	 * with other events in between.
	 *
	 * @param types the expected event types
	 * @return this assertion
	 */
	public TimedEventListAssert<E> containsEventTypesInOrder(int... types) {
		isNotNull();
		List<Integer> actualTypes = types();
		int matched = 0;
		for (int type : actualTypes) {
			if (matched < types.length && type == types[matched]) {
				matched++;
			}
		}
		if (matched < types.length) {
			throw failure("%nExpecting event types in order:%n <%s>%nbut recorded:%n <%s>%nfirst type not found:%n <%s>",
				describeTypes(boxed(types)), describe(actual), describeType(types[matched]));
		}
		return myself;
	}

	/**
	 * The events must match the conditions one to one, in the given order.
	 * Conditions can be combined with {@link Assertions#allOf(Condition...)},
	 * e.g. {@code allOf(type(STARTED), bundle(b))}.
	 *
	 * @param conditions one condition per expected event
	 * @return this assertion
	 */
	@SafeVarargs
	public final TimedEventListAssert<E> hasEventsExactly(Condition<? super E>... conditions) {
		isNotNull();
		if (actual.size() != conditions.length) {
			throw failure("%nExpecting %d event(s) matching:%n <%s>%nbut recorded %d:%n <%s>", conditions.length,
				describeConditions(conditions), actual.size(), describe(actual));
		}
		for (int i = 0; i < conditions.length; i++) {
			E event = actual.get(i)
				.event();
			if (!conditions[i].matches(event)) {
				throw failure("%nExpecting event at index %d to be:%n <%s>%nbut was:%n <%s>%nin:%n <%s>", i,
					conditions[i], actual.get(i), describe(actual));
			}
		}
		return myself;
	}

	/**
	 * The events must match the conditions one to one, in any order.
	 *
	 * @param conditions one condition per expected event
	 * @return this assertion
	 */
	@SafeVarargs
	public final TimedEventListAssert<E> hasEventsInAnyOrder(Condition<? super E>... conditions) {
		isNotNull();
		if (actual.size() != conditions.length) {
			throw failure("%nExpecting %d event(s) matching:%n <%s>%nbut recorded %d:%n <%s>", conditions.length,
				describeConditions(conditions), actual.size(), describe(actual));
		}
		List<TimedEvent<E>> remaining = new ArrayList<>(actual);
		for (Condition<? super E> condition : conditions) {
			boolean found = false;
			for (int i = 0; i < remaining.size(); i++) {
				if (condition.matches(remaining.get(i)
					.event())) {
					remaining.remove(i);
					found = true;
					break;
				}
			}
			if (!found) {
				throw failure("%nExpecting an event:%n <%s>%nbut none of the unmatched events fits:%n <%s>%nin:%n <%s>",
					condition, describe(remaining), describe(actual));
			}
		}
		return myself;
	}

	@SafeVarargs
	private static <E> String describeConditions(Condition<? super E>... conditions) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < conditions.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(conditions[i]);
		}
		return sb.append("]")
			.toString();
	}

	/**
	 * Every event must satisfy the condition.
	 *
	 * @param condition the condition
	 * @return this assertion
	 */
	public TimedEventListAssert<E> allEventsAre(Condition<? super E> condition) {
		events().are(condition);
		return myself;
	}

	/**
	 * At least one event must satisfy the condition.
	 *
	 * @param condition the condition
	 * @return this assertion
	 */
	public TimedEventListAssert<E> anyEventIs(Condition<? super E> condition) {
		events().areAtLeastOne(condition);
		return myself;
	}

	private List<E> eventList() {
		List<E> events = new ArrayList<>(actual.size());
		for (TimedEvent<E> timed : actual) {
			events.add(timed.event());
		}
		return events;
	}

	private List<Integer> types() {
		List<Integer> types = new ArrayList<>(actual.size());
		for (TimedEvent<E> timed : actual) {
			types.add(typeOf(timed.event()));
		}
		return types;
	}

	private static List<Integer> boxed(int... types) {
		List<Integer> result = new ArrayList<>(types.length);
		for (int type : types) {
			result.add(type);
		}
		return result;
	}

	/**
	 * @param event a bundle, service or framework event
	 * @return its {@code getType()} value
	 * @throws IllegalArgumentException for any other kind of event
	 */
	static int typeOf(EventObject event) {
		if (event instanceof BundleEvent) {
			return ((BundleEvent) event).getType();
		}
		if (event instanceof ServiceEvent) {
			return ((ServiceEvent) event).getType();
		}
		if (event instanceof FrameworkEvent) {
			return ((FrameworkEvent) event).getType();
		}
		throw new IllegalArgumentException("Not a bundle, service or framework event: " + event);
	}

	private Bitmap bitmap() {
		for (TimedEvent<E> timed : actual) {
			EventObject event = timed.event();
			if (event instanceof BundleEvent) {
				return BundleEventType.BITMAP;
			}
			if (event instanceof ServiceEvent) {
				return ServiceEventType.BITMAP;
			}
			if (event instanceof FrameworkEvent) {
				return FrameworkEventType.BITMAP;
			}
		}
		return null;
	}

	private String describeType(int type) {
		Bitmap bitmap = bitmap();
		return bitmap == null ? Integer.toString(type) : bitmap.maskToString(type);
	}

	private String describeTypes(List<Integer> types) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < types.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(describeType(types.get(i)));
		}
		return sb.append("]")
			.toString();
	}

	private String describe(List<? extends TimedEvent<E>> events) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < events.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(events.get(i));
		}
		return sb.append("]")
			.toString();
	}
}
