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
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.test.common.event.TimedEvent;

public class TimedEventTest {

	private static EventObject eventWithHash(int hash) {
		return new EventObject("source") {
			private static final long serialVersionUID = 1L;

			@Override
			public int hashCode() {
				return hash;
			}
		};
	}

	@Test
	public void compareTo_ordersByTimeFirst() {
		TimedEvent<EventObject> earlier = new TimedEvent<>(ofMillis(10), eventWithHash(5));
		TimedEvent<EventObject> later = new TimedEvent<>(ofMillis(20), eventWithHash(1));

		assertThat(earlier.compareTo(later)).isNegative();
		assertThat(later.compareTo(earlier)).isPositive();
	}

	@Test
	public void compareTo_ordersByEventHashCodeForEqualTimes() {
		Duration time = ofMillis(10);
		TimedEvent<EventObject> a = new TimedEvent<>(time, eventWithHash(3));
		TimedEvent<EventObject> b = new TimedEvent<>(time, eventWithHash(1));
		TimedEvent<EventObject> c = new TimedEvent<>(time, eventWithHash(2));

		List<TimedEvent<EventObject>> list = new ArrayList<>();
		list.add(a);
		list.add(b);
		list.add(c);
		Collections.sort(list);

		assertThat(list).containsExactly(b, c, a);
	}

	@Test
	public void compareTo_isAntisymmetricForExtremeHashCodes() {
		Duration time = ofMillis(10);
		TimedEvent<EventObject> max = new TimedEvent<>(time, eventWithHash(Integer.MAX_VALUE));
		TimedEvent<EventObject> min = new TimedEvent<>(time, eventWithHash(Integer.MIN_VALUE));
		TimedEvent<EventObject> minusOne = new TimedEvent<>(time, eventWithHash(-1));

		// Subtracting hash codes would overflow here and flip the sign
		assertThat(max.compareTo(min)).isPositive();
		assertThat(min.compareTo(max)).isNegative();
		assertThat(max.compareTo(minusOne)).isPositive();
		assertThat(minusOne.compareTo(max)).isNegative();
		assertThat(Integer.signum(max.compareTo(min))).isEqualTo(-Integer.signum(min.compareTo(max)));
	}
}
