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
 * Entry point for soft assertions of different data types.
 */
public class ConfigurationSoftAssertions extends org.assertj.core.api.SoftAssertions {

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.cm.configuration.ConfigurationAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */

	public ConfigurationAssert assertThat(org.osgi.service.cm.Configuration actual) {
		return proxy(ConfigurationAssert.class, org.osgi.service.cm.Configuration.class, actual);
	}

	/**
	 * Creates a new "soft" instance of
	 * <code>{@link org.osgi.test.assertj.cm.configurationevent.ConfigurationEventAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created "soft" assertion object.
	 */

	public ConfigurationEventAssert assertThat(org.osgi.service.cm.ConfigurationEvent actual) {
		return proxy(ConfigurationEventAssert.class, org.osgi.service.cm.ConfigurationEvent.class, actual);
	}
}
