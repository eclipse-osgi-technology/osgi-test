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

import java.util.function.Predicate;

import org.osgi.framework.FrameworkEvent;

/**
 * The Class FrameworkEventPredicates.
 *
 * @since 1.1
 */
public final class FrameworkEventPredicates {

	private FrameworkEventPredicates() {}
	/**
	 * Returns a predicate that tests if the object is a framework event.
	 *
	 * @return the predicate
	 */
	public static Predicate<Object> frameworkEvent() {
		return e -> e instanceof FrameworkEvent;
	}

	/**
	 * Returns a predicate that tests if the event-type matches the
	 * eventTypeMask.
	 *
	 * @param eventTypeMask the event type mask
	 * @return the predicate
	 */
	public static Predicate<FrameworkEvent> type(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}

}
