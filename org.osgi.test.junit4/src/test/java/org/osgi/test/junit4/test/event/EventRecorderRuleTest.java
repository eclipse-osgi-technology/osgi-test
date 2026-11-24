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
package org.osgi.test.junit4.test.event;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectEventRecorder;
import org.osgi.test.common.event.EventRecorder;
import org.osgi.test.common.event.TimedEvent;
import org.osgi.test.junit4.context.BundleContextRule;
import org.osgi.test.junit4.event.EventRecorderRule;

public class EventRecorderRuleTest {

	@Rule
	public BundleContextRule		bcr				= new BundleContextRule();
	@Rule
	public EventRecorderRule		erc				= new EventRecorderRule();

	@InjectBundleContext
	BundleContext					ctx;
	@InjectEventRecorder(filter = "(foo=bar)")
	EventRecorder<ServiceEvent>		serviceEvents;
	@InjectEventRecorder
	EventRecorder<EventObject>		allEvents;

	static EventRecorder<?>			fromThisTest;

	@Test
	public void serviceRecorderWithFilter() throws Exception {
		assertThat(serviceEvents.isClosed()).isFalse();
		ServiceRegistration<Object> reg = ctx.registerService(Object.class, new Object(), null);
		assertThat(serviceEvents.size()).isZero();
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("foo", "bar");
		reg.setProperties(props);
		List<TimedEvent<ServiceEvent>> events = serviceEvents.waitForCount(1, ofSeconds(5));
		assertThat(events.get(0)
			.event()
			.getType()).isEqualTo(ServiceEvent.MODIFIED);
	}

	@Test
	public void eventObjectRecorderSeesEverything() throws Exception {
		ServiceRegistration<Object> reg = ctx.registerService(Object.class, new Object(), null);
		reg.unregister();
		assertThat(allEvents.events()).hasSize(2)
			.allSatisfy(e -> assertThat(e.event()).isInstanceOf(ServiceEvent.class));
	}

	@Test
	public void recordersAreClosedAfterTheTest() throws Exception {
		Result result = new JUnitCore().run(Request.method(Capture.class, "capture"));
		assertThat(result.wasSuccessful()).isTrue();
		assertThat(fromThisTest).isNotNull();
		assertThat(fromThisTest.isClosed()).isTrue();
	}

	public static class Capture {
		@Rule
		public BundleContextRule	bcr	= new BundleContextRule();
		@Rule
		public EventRecorderRule	erc	= new EventRecorderRule();

		@InjectEventRecorder
		EventRecorder<EventObject>	recorder;

		@Test
		public void capture() {
			assertThat(recorder.isClosed()).isFalse();
			fromThisTest = recorder;
		}
	}
}
