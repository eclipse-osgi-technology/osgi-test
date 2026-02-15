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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.dictionary.Dictionaries;

/**
 * The Class ServiceEventPredicates.
 *
 * @since 1.1
 */
public final class ServiceEventPredicates {

	private ServiceEventPredicates() {}
	/**
	 * Returns a predicate that tests if the object Service event.
	 *
	 * @return the predicate
	 */
	public static Predicate<Object> serviceEvent() {
		return e -> e instanceof ServiceEvent;
	}

	/**
	 * Returns a predicate that tests if the event-type matches the
	 * eventTypeMask.
	 *
	 * @param eventTypeMask the event type mask
	 * @return the predicate
	 */
	// ServiceEvents
	public static Predicate<ServiceEvent> type(final int eventTypeMask) {
		return e -> (e.getType() & eventTypeMask) != 0;
	}


	/**
	 * Returns a predicate that tests if the Service of the event matches the
	 * objectClass.
	 *
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> objectClass(final Class<?> objectClass) {

		return e -> {
			Object classes = e.getServiceReference()
				.getProperty(Constants.OBJECTCLASS);

			if (classes instanceof String[]) {
				return Stream.of((String[]) classes)
					.filter(Objects::nonNull)
					.anyMatch(objectClass.getName()::equals);
			}
			return false;
		};

	}

	/**
	 * Returns a predicate that tests if the object Contains the service
	 * properties.
	 *
	 * @param map the map
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> containsServiceProperties(Map<String, ?> map) {
		return e -> {
			ServiceReference<?> sr = e.getServiceReference();
			List<String> keys = Arrays.asList(sr.getPropertyKeys());
			for (Entry<String, ?> entry : map.entrySet()) {
				if (!keys.contains(entry.getKey())) {
					return false;
				}
				if (!Objects.equals(sr.getProperty(entry.getKey()), entry.getValue())) {
					return false;
				}
			}
			return true;
		};
	}

	/**
	 * Returns a predicate that tests if the object Contains the service
	 * properties.
	 *
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> containsServiceProperties(Dictionary<String, ?> properties) {
		return containsServiceProperties(Dictionaries.asMap(properties));
	}

	/**
	 * Returns a predicate that tests if the object Matches matches the given
	 * parameters.
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass the objectClass
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass) {
		return type(eventTypeMask).and((objectClass(objectClass)));
	}

	/**
	 * Returns a predicate that tests if the object matches the given
	 * parameters.
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass the objectClass
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Map<String, ?> properties) {
		return type(eventTypeMask).and(objectClass(objectClass))
			.and(containsServiceProperties(properties));
	}

	/**
	 * Returns a predicate that tests if the object Matches the given
	 * parameters.
	 *
	 * @param eventTypeMask the event type mask
	 * @param objectClass the objectClass
	 * @param properties the properties
	 * @return the predicate
	 */
	public static Predicate<ServiceEvent> matches(int eventTypeMask, final Class<?> objectClass,
		Dictionary<String, ?> properties) {
		return matches(eventTypeMask, objectClass, Dictionaries.asMap(properties));
	}
}
