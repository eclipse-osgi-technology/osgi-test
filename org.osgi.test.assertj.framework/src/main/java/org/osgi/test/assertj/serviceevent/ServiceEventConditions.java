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
package org.osgi.test.assertj.serviceevent;

import org.assertj.core.api.Condition;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.bitmaps.Bitmap;
import org.osgi.test.common.bitmaps.ServiceEventType;

/**
 * {@link Condition}s on {@link ServiceEvent}s, for use with AssertJ's
 * {@code is}/{@code has}/{@code are} methods and as predicates when waiting
 * for events.
 */
public final class ServiceEventConditions {

	private ServiceEventConditions() {}

	/**
	 * @param type a single {@link ServiceEvent} type
	 * @return a condition matching events of exactly that type
	 */
	public static Condition<ServiceEvent> type(int type) {
		if (!Bitmap.hasSingleBit(type) || type == 0) {
			throw new IllegalArgumentException(
				"Multiple bits set in type (" + type + ") - do you mean to use typeMaskedBy()?");
		}
		return new Condition<>(e -> e.getType() == type, "of type %s", ServiceEventType.BITMAP.maskToString(type));
	}

	/**
	 * @param mask a mask of {@link ServiceEvent} types
	 * @return a condition matching events of any of the types in the mask
	 */
	public static Condition<ServiceEvent> typeMaskedBy(int mask) {
		return new Condition<>(e -> Bitmap.typeMatchesMask(e.getType(), mask), "of one of types [%s]",
			ServiceEventType.BITMAP.maskToString(mask));
	}

	/**
	 * @param expected the service reference the event must be about
	 * @return a condition matching events whose service reference equals
	 *         {@code expected}
	 */
	public static Condition<ServiceEvent> serviceReference(ServiceReference<?> expected) {
		return new Condition<>(e -> e.getServiceReference()
			.equals(expected), "for service %s", expected);
	}
}
