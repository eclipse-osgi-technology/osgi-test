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
package org.osgi.test.assertj.frameworkevent;

import org.assertj.core.api.Condition;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.common.bitmaps.Bitmap;
import org.osgi.test.common.bitmaps.FrameworkEventType;

/**
 * {@link Condition}s on {@link FrameworkEvent}s, for use with AssertJ's
 * {@code is}/{@code has}/{@code are} methods and as predicates when waiting
 * for events.
 */
public final class FrameworkEventConditions {

	private FrameworkEventConditions() {}

	/**
	 * @param type a single {@link FrameworkEvent} type
	 * @return a condition matching events of exactly that type
	 */
	public static Condition<FrameworkEvent> type(int type) {
		if (!Bitmap.hasSingleBit(type) || type == 0) {
			throw new IllegalArgumentException(
				"Multiple bits set in type (" + type + ") - do you mean to use typeMaskedBy()?");
		}
		return new Condition<>(e -> e.getType() == type, "of type %s", FrameworkEventType.BITMAP.maskToString(type));
	}

	/**
	 * @param mask a mask of {@link FrameworkEvent} types
	 * @return a condition matching events of any of the types in the mask
	 */
	public static Condition<FrameworkEvent> typeMaskedBy(int mask) {
		return new Condition<>(e -> Bitmap.typeMatchesMask(e.getType(), mask), "of one of types [%s]",
			FrameworkEventType.BITMAP.maskToString(mask));
	}

	/**
	 * @param expected the bundle the event must be about
	 * @return a condition matching events whose bundle is {@code expected}
	 */
	public static Condition<FrameworkEvent> bundle(Bundle expected) {
		return new Condition<>(e -> e.getBundle() == expected, "for bundle %s", expected);
	}

	/**
	 * @param expected the type the event's throwable must be an instance of
	 * @return a condition matching events that carry such a throwable
	 */
	public static Condition<FrameworkEvent> throwableOfType(Class<? extends Throwable> expected) {
		return new Condition<>(e -> expected.isInstance(e.getThrowable()), "with a throwable of type %s",
			expected.getName());
	}
}
