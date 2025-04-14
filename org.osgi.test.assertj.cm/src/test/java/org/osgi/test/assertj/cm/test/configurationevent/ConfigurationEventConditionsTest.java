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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.test.assertj.cm.configurationevent.ConfigurationEventConditions;
import org.osgi.test.assertj.cm.test.util.ConditionAssert;

class ConfigurationEventConditionsTest implements ConditionAssert {

	class A {}

	@SuppressWarnings("rawtypes")
	ConfigurationEvent configurationEvent;

	@BeforeEach
	void beforEach() {
		configurationEvent = mock(ConfigurationEvent.class, "configurationEvent");
	}

	@SuppressWarnings("unchecked")
	@org.junit.jupiter.api.Test
	public void test_factoryPid_String() throws Exception {

		when(configurationEvent.getFactoryPid()).thenReturn("theFactoryPid");
		passingHas(ConfigurationEventConditions.factoryPid("theFactoryPid"), configurationEvent);

		when(configurationEvent.getFactoryPid()).thenReturn("otherFactoryPid");
		failingHas(ConfigurationEventConditions.factoryPid("theFactoryPid"), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "factoryPid equals .* was <otherFactoryPid>"));

		when(configurationEvent.getFactoryPid()).thenReturn(null);
		failingHas(ConfigurationEventConditions.factoryPid("theFactoryPid"), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "factoryPid equals .* was <null>"));

	}


	@SuppressWarnings("unchecked")
	@org.junit.jupiter.api.Test
	public void test_factoryPidNull() throws Exception {

		when(configurationEvent.getFactoryPid()).thenReturn(null);
		passingHas(ConfigurationEventConditions.factoryPidNull(), configurationEvent);

		when(configurationEvent.getFactoryPid()).thenReturn("theFactoryPid");
		failingHas(ConfigurationEventConditions.factoryPidNull(), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "factoryPid is <null> .*was <theFactoryPid>"));

	}

	@org.junit.jupiter.api.Test
	public void test_pidNull() throws Exception {

		when(configurationEvent.getPid()).thenReturn(null);
		passingHas(ConfigurationEventConditions.pidNull(), configurationEvent);

		when(configurationEvent.getPid()).thenReturn("thePid");
		failingHas(ConfigurationEventConditions.pidNull(), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "pid is <null> .*was <thePid>"));

	}


	@org.junit.jupiter.api.Test
	public void test_pid_String() throws Exception {

		when(configurationEvent.getFactoryPid()).thenReturn("thePid");
		passingHas(ConfigurationEventConditions.factoryPid("thePid"), configurationEvent);

		when(configurationEvent.getFactoryPid()).thenReturn("otherPid");
		failingHas(ConfigurationEventConditions.factoryPid("thePid"), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "pid equals .* was <otherPid>"));

		when(configurationEvent.getFactoryPid()).thenReturn(null);
		failingHas(ConfigurationEventConditions.factoryPid("thePid"), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "pid equals .* was <null>"));

	}

	@org.junit.jupiter.api.Test
	public void test_matches() throws Exception {
		Configuration configuration = mock(Configuration.class);

		when(configuration.getFactoryPid()).thenReturn("theFactoryPid");
		when(configuration.getPid()).thenReturn(null);
		when(configurationEvent.getFactoryPid()).thenReturn("theFactoryPid");
		when(configurationEvent.getPid()).thenReturn(null);
		passingHas(ConfigurationEventConditions.matches(configuration), configurationEvent);

		when(configuration.getFactoryPid()).thenReturn(null);
		when(configuration.getPid()).thenReturn("thePid");
		when(configurationEvent.getFactoryPid()).thenReturn(null);
		when(configurationEvent.getPid()).thenReturn("thePid");
		passingHas(ConfigurationEventConditions.matches(configuration), configurationEvent);

		when(configuration.getFactoryPid()).thenReturn("theFactoryPid");
		when(configuration.getPid()).thenReturn(null);
		when(configurationEvent.getFactoryPid()).thenReturn("otherFactoryPid");
		when(configurationEvent.getPid()).thenReturn(null);
		failingHas(ConfigurationEventConditions.matches(configuration), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting
				+ "any of.*not :<pid is <null>>.*factoryPid equals <theFactoryPid> was <otherFactoryPid>.*"));

		when(configuration.getFactoryPid()).thenReturn(null);
		when(configuration.getPid()).thenReturn("thePid");
		when(configurationEvent.getFactoryPid()).thenReturn(null);
		when(configurationEvent.getPid()).thenReturn("otherPid");
		failingHas(ConfigurationEventConditions.matches(configuration), configurationEvent).hasMessageMatching(
			(regex_startWith_Expecting
				+ "any of.*not :<pid is <null> was <otherPid>>,.*was <otherPid>.*not :<factoryPid is <null>>.*"));

	}

	@org.junit.jupiter.api.Test
	public void test_typeAndPid() throws Exception {

		when(configurationEvent.getType()).thenReturn(ConfigurationEvent.CM_UPDATED);
		when(configurationEvent.getPid()).thenReturn("thePid");
		passingHas(ConfigurationEventConditions.typeAndPid(ConfigurationEvent.CM_UPDATED, "thePid"),
			configurationEvent);

		failingHas(ConfigurationEventConditions.typeAndPid(ConfigurationEvent.CM_UPDATED, "otherPid"),
			configurationEvent).hasMessageMatching(
				(regex_startWith_Expecting + "all of.*type is <CM_UPDATED>.*pid equals <otherPid>.*was.*<thePid>.*"));

		failingHas(ConfigurationEventConditions.typeAndPid(ConfigurationEvent.CM_DELETED, "thePid"), configurationEvent)
			.hasMessageMatching(
				(regex_startWith_Expecting + "all of.*type is <CM_DELETED>.*was <CM_UPDATED>.*pid equals <thePid>.*"));

	}

	@org.junit.jupiter.api.Test
	public void test_typeAndFactoryPid() throws Exception {

		when(configurationEvent.getType()).thenReturn(ConfigurationEvent.CM_UPDATED);
		when(configurationEvent.getFactoryPid()).thenReturn("theFactoryPid");
		passingHas(ConfigurationEventConditions.typeAndFactoryPid(ConfigurationEvent.CM_UPDATED, "theFactoryPid"),
			configurationEvent);

		failingHas(ConfigurationEventConditions.typeAndFactoryPid(ConfigurationEvent.CM_UPDATED, "otherFactoryPid"),
			configurationEvent)
				.hasMessageMatching((regex_startWith_Expecting
					+ "all of.*type is <CM_UPDATED>.*factoryPid equals <otherFactoryPid>.*was.*<theFactoryPid>.*"));

		failingHas(ConfigurationEventConditions.typeAndFactoryPid(ConfigurationEvent.CM_DELETED, "theFactoryPid"),
			configurationEvent)
				.hasMessageMatching((regex_startWith_Expecting
					+ "all of.*type is <CM_DELETED>.*was <CM_UPDATED>.*factoryPid equals <theFactoryPid>.*"));

	}

	@org.junit.jupiter.api.Test
	public void test_type() throws Exception {

		when(configurationEvent.getType()).thenReturn(ConfigurationEvent.CM_UPDATED);
		passingHas(ConfigurationEventConditions.type(ConfigurationEvent.CM_UPDATED), configurationEvent);

		when(configurationEvent.getType()).thenReturn(ConfigurationEvent.CM_DELETED);
		failingHas(ConfigurationEventConditions.type(ConfigurationEvent.CM_UPDATED), configurationEvent)
			.hasMessageMatching((regex_startWith_Expecting + "type is <CM_UPDATED>.*was.*<CM_DELETED>"));

	}
}
