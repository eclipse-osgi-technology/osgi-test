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
package org.osgi.test.junit5.cm.test.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.config.InjectConfiguration;

public class ExtendingClassTest extends AbstractConfiguringClass {

	@BeforeAll
	static void chlidBeforeAll(@InjectConfiguration(CONFIG)
	Configuration cm, @InjectConfiguration(CONFIG + "~" + CONFIG)
	Configuration fcm) throws IOException {

		assertNotNull(cm);
		assertEquals(CONFIG, cm.getPid());
		assertEquals("ca", cm.getProperties()
			.get("location"));

		assertNotNull(fcm);
		assertEquals(CONFIG, fcm.getFactoryPid());
		assertEquals("ca", fcm.getProperties()
			.get("location"));
	}

	@Test
	void test() {
		// test assertions are all in ExtendingClassTest#childBeforeAll and
		// AbstractConfiguringClass#beforeAll
	}
}
