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

package org.osgi.test.assertj.cm.configuration;

import java.util.Objects;

import org.assertj.core.api.Condition;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.service.cm.Configuration;

/**
 * A Utility-Class thats Provides public static methods to create
 * {@link Condition}s for an <code>Configuration</code>}
 */
public class ConfigurationConditions {

	private ConfigurationConditions() {}

	/**
	 * Creates a {@link Condition} to be met by an <code>Configuration. Checking
	 * if the pid of the Configuration is not null.
	 *
	 * @return the condition
	 */
	public static Condition<Configuration> pidNotNull() {

		return new Condition<Configuration>(e -> Objects.nonNull(e.getPid()), "pid is not <null>");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>Configuration. Checking
	 * if the pid of the Configuration is null.
	 *
	 * @return the condition
	 */
	public static Condition<Configuration> pidNull() {

		return VerboseCondition.verboseCondition((Configuration e) -> e.getPid() == null, "pid is <null>", //
			e -> " was <" + e.getPid() + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>Configuration. Checking
	 * if the factoryPid of the Configuration is not null.
	 *
	 * @return the condition
	 */
	public static Condition<Configuration> factoryPidNotNull() {

		return new Condition<Configuration>(e -> Objects.nonNull(e.getFactoryPid()), "factoryPid is not <null>");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>Configuration. Checking
	 * if the factoryPid of the Configuration is null.
	 *
	 * @return the condition
	 */
	public static Condition<Configuration> factoryPidNull() {

		return VerboseCondition.verboseCondition((Configuration e) -> e.getFactoryPid() == null, "factoryPid is <null>", //
			e -> " was <" + e.getFactoryPid() + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>Configuration. Checking
	 * if a given pid <b>matches</b> the pid of the Configuration.
	 *
	 * @param pid the pid
	 * @return the condition
	 */
	public static Condition<Configuration> pid(final String pid) {

		return VerboseCondition.verboseCondition((Configuration e) -> Objects.equals(e.getPid(), pid),
			"pid equals <" + pid + ">", //
			e -> " was <" + e.getPid() + ">");

	}

	/**
	 * Creates a {@link Condition} to be met by an <code>Configuration. Checking
	 * if a given factoryPid <b>matches</b> the factoryPid of the Configuration.
	 *
	 * @param factoryPid the factory pid
	 * @return the condition
	 */
	public static Condition<Configuration> factoryPid(final String factoryPid) {

		return VerboseCondition.verboseCondition((Configuration e) -> Objects.equals(e.getFactoryPid(), factoryPid),
			"factoryPid equals <" + factoryPid + ">", //
			e -> " was <" + e.getFactoryPid() + ">");

	}

}
