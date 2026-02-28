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

package org.osgi.test.assertj.cm.test.configurationevent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.test.assertj.cm.configurationevent.ConfigurationEventAssert;
import org.osgi.test.assertj.cm.test.util.AbstractAssertTest;

class ConfigurationEventAssertTest extends AbstractAssertTest<ConfigurationEventAssert, ConfigurationEvent> {

	ConfigurationEventAssertTest() {
		super(ConfigurationEventAssert::assertThat);
	}

	ConfigurationEvent event;

	@BeforeEach
	void setUp() {
		event = mock(ConfigurationEvent.class);

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
	public void test_hasType() {

		when(event.getType()).thenReturn(ConfigurationEvent.CM_DELETED);
		assertPassing(aut::hasTypeEqualTo, ConfigurationEvent.CM_DELETED);
		assertFailing(aut::hasTypeEqualTo, ConfigurationEvent.CM_UPDATED);
		assertFailing(aut::hasTypeEqualTo, ConfigurationEvent.CM_LOCATION_CHANGED);

		when(event.getType()).thenReturn(ConfigurationEvent.CM_UPDATED);
		assertPassing(aut::hasTypeEqualTo, ConfigurationEvent.CM_UPDATED);
		assertFailing(aut::hasTypeEqualTo, ConfigurationEvent.CM_DELETED);
		assertFailing(aut::hasTypeEqualTo, ConfigurationEvent.CM_LOCATION_CHANGED);

		when(event.getType()).thenReturn(ConfigurationEvent.CM_LOCATION_CHANGED);
		assertPassing(aut::hasTypeEqualTo, ConfigurationEvent.CM_LOCATION_CHANGED);
		assertFailing(aut::hasTypeEqualTo, ConfigurationEvent.CM_UPDATED);
		assertFailing(aut::hasTypeEqualTo, ConfigurationEvent.CM_DELETED);

	}

	@Test
	public void test_hasReference() {
		ServiceReference<ConfigurationAdmin> sr = mock(ServiceReference.class);
		ServiceReference<ConfigurationAdmin> otherSr = mock(ServiceReference.class);

		when(event.getReference()).thenReturn(sr);

		aut.hasReferenceThat()
			.isEqualTo(sr);

		assertThatThrownBy(() -> {
			aut.hasReferenceThat()
				.isEqualTo(otherSr);
		});

	}
}
