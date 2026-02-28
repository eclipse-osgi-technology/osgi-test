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

package org.osgi.test.assertj.cm.configurationevent;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.not;

import java.util.Objects;

import org.assertj.core.api.Condition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationEvent;

/**
 * A Utility-Class thats Provides public static methods to create
 * {@link Condition}s for an <code>ConfigurationEvent</code>}
 */
public final class ConfigurationEventConditions {

	private ConfigurationEventConditions() {}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if a given {@link Configuration} <b>matches</b> the <b>pid OR
	 * factoryPid</b> of the Event.
	 *
	 * @param configuration the configuration
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> matches(Configuration configuration) {
		return anyOf(allOf(not(pidNull()), pid(configuration.getPid())),
			allOf(not(factoryPidNull()), factoryPid(configuration.getFactoryPid())));

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if a given type- <b>matches</b> the type of the Event.
	 *
	 * &#64;param eventType the type that would be checked against the type of the
	 *            <code>ConfigurationEvent</code>}
	 *
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> type(final int eventType) {

		return VerboseCondition.verboseCondition((ConfigurationEvent e) -> e.getType() == eventType,
			"type is <" + ConfigurationEventType.toString(eventType) + ">", //
			e -> " was <" + ConfigurationEventType.toString(e.getType()) + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if the pid of the Event is null.
	 *
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> pidNull() {

		return VerboseCondition.verboseCondition((ConfigurationEvent e) -> e.getPid() == null, "pid is <null>", //
			e -> " was <" + e.getPid() + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if the factoryPid of the Event is null.
	 *
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> factoryPidNull() {

		return VerboseCondition.verboseCondition((ConfigurationEvent e) -> e.getFactoryPid() == null,
			"factoryPid is <null>", //
			e -> " was <" + e.getFactoryPid() + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if a given pid <b>matches</b> the pid of the Event.
	 *
	 * @param pid the pid
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> pid(final String pid) {

		return VerboseCondition.verboseCondition((ConfigurationEvent e) -> Objects.equals(e.getPid(), pid),
			"pid equals <" + pid + ">", //
			e -> " was <" + e.getPid() + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if a given factoryPid <b>matches</b> the factoryPid of the
	 * Event.
	 *
	 * @param factoryPid the factory pid
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> factoryPid(final String factoryPid) {

		return VerboseCondition.verboseCondition(
			(ConfigurationEvent e) -> Objects.equals(e.getFactoryPid(), factoryPid),
			"factoryPid equals <" + factoryPid + ">", //
			e -> " was <" + e.getFactoryPid() + ">");

	}
	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if a given pid <b>matches</b> the pid and a given type
	 * <b>matches</b> the type of the Event.
	 *
	 * &#64;param eventType the type that would be checked against the type of the
	 *            <code>ConfigurationEvent</code>}
	 *
	 * @param pid the pid
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> typeAndPid(final int eventType, String pid) {
		return allOf(type(eventType), pid(pid));
	}

	/**
	 * Creates a {@link Condition} to be met by an <code>ConfigurationEvent.
	 * Checking if a given factoryPid <b>matches</b> the factoryPid and a given
	 * type <b>matches</b> the type of the Event.
	 *
	 * &#64;param eventType the type that would be checked against the type of the
	 *            <code>ConfigurationEvent</code>}
	 *
	 * @param factoryPid the factory pid
	 * @return the condition
	 */
	public static Condition<ConfigurationEvent> typeAndFactoryPid(final int eventType, String factoryPid) {
		return allOf(type(eventType), factoryPid(factoryPid));

	}

}
