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

package org.osgi.test.assertj.cm.test.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.test.assertj.cm.configuration.ConfigurationAssert;
import org.osgi.test.assertj.cm.test.util.AbstractAssertTest;

class ConfigurationEventAssertTest extends AbstractAssertTest<ConfigurationAssert, Configuration> {

	ConfigurationEventAssertTest() {
		super(ConfigurationAssert::assertThat);
	}

	Configuration event;

	@BeforeEach
	void setUp() {
		event = mock(Configuration.class);

		setActual(event);
	}

	@Test
	public void test_hasPid_value() {

		when(event.getPid()).thenReturn(null);
		assertPassing(aut::hasPidEqualsTo, (String) null);

		when(event.getPid()).thenReturn("thePid");
		assertPassing(aut::hasPidEqualsTo, "thePid");
		assertFailing(aut::hasPidEqualsTo, "otherPid");

	}

	@Test
	public void test_hasFactoryPid_value() {

		when(event.getFactoryPid()).thenReturn(null);
		assertPassing(aut::hasFactoryPidEqualsTo, (String) null);

		when(event.getFactoryPid()).thenReturn("theFactoryPid");
		assertPassing(aut::hasFactoryPidEqualsTo, "theFactoryPid");
		assertFailing(aut::hasFactoryPidEqualsTo, "otherFactoryPid");

	}

	@Test
	public void test_hasNoFactoryPid() {

		when(event.getFactoryPid()).thenReturn(null);
		assertPassing(ignores -> aut.hasNoFactoryPid(), null);

		when(event.getFactoryPid()).thenReturn("theFactoryPid");
		assertFailing(ignores -> aut.hasNoFactoryPid(), null);

	}

	@Test
	public void test_hasNoPid() {

		when(event.getPid()).thenReturn(null);
		assertPassing(ignores -> aut.hasNoPid(), null);

		when(event.getPid()).thenReturn("thePid");
		assertFailing(ignores -> aut.hasNoPid(), null);

	}

	@Test
	public void test_hasPid() {

		when(event.getPid()).thenReturn("thePid");
		assertPassing(ignores -> aut.hasPid(), null);

		when(event.getPid()).thenReturn(null);
		assertFailing(ignores -> aut.hasPid(), null);

	}

	@Test
	public void test_hasFactoryPid() {

		when(event.getFactoryPid()).thenReturn("theFactoryPid");
		assertPassing(ignores -> aut.hasFactoryPid(), null);

		when(event.getFactoryPid()).thenReturn(null);
		assertFailing(ignores -> aut.hasFactoryPid(), null);

	}

	@Test
	public void test_hasBundleLocation() {

		when(event.getBundleLocation()).thenReturn(null);
		assertPassing(aut::hasBundleLocation, (String) null);

		when(event.getPid()).thenReturn("loc");
		assertPassing(aut::hasPidEqualsTo, "loc");
		assertFailing(aut::hasPidEqualsTo, "otherLoc");

	}

	@Test
	public void test_hasChangeCount() {

		when(event.getChangeCount()).thenReturn(1l);
		assertPassing(aut::hasChangeCount, 1l);
		assertFailing(aut::hasChangeCount, 2l);

	}

	@Test
	public void test_hasChangeCountGreater() {

		when(event.getChangeCount()).thenReturn(5l);
		assertPassing(aut::hasChangeCountGreater, 4l);
		assertFailing(aut::hasChangeCountGreater, 5l);

	}

}
