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

package org.osgi.test.assertj.cm;

import org.osgi.test.assertj.cm.configuration.ConfigurationAssert;
import org.osgi.test.assertj.cm.configurationevent.ConfigurationEventAssert;

/**
 * Entry point for BDD assertions of different data types.
 */
public class ConfigurationBddAssertions {

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.cm.configuration.ConfigurationAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */

	public static ConfigurationAssert then(org.osgi.service.cm.Configuration actual) {
		return new ConfigurationAssert(actual);
	}

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.cm.configurationevent.ConfigurationEventAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */

	public static ConfigurationEventAssert then(org.osgi.service.cm.ConfigurationEvent actual) {
		return new ConfigurationEventAssert(actual);
	}

	/**
	 * Creates a new <code>{@link ConfigurationBddAssertions}</code>.
	 */
	protected ConfigurationBddAssertions() {
		// empty
	}
}
