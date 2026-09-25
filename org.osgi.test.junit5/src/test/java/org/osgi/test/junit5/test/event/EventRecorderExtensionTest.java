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
package org.osgi.test.junit5.test.event;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.framework.BundleEvent.STARTED;
import static org.osgi.framework.BundleEvent.STARTING;
import static org.osgi.framework.BundleEvent.STOPPED;
import static org.osgi.framework.BundleEvent.STOPPING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectEventRecorder;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.event.TimedEvent;
import org.osgi.test.common.event.EventRecorder;

@TestMethodOrder(OrderAnnotation.class)
public class EventRecorderExtensionTest {

	@InjectEventRecorder(synchronous = true)
	EventRecorder<BundleEvent>			bundleField;

	@InjectBundleContext
	BundleContext						ctx;

	static EventRecorder<BundleEvent>	fromPreviousTest;

	private static Predicate<BundleEvent> type(int type) {
		return e -> e.getType() == type;
	}

	private static List<Integer> bundleTypes(List<TimedEvent<BundleEvent>> events) {
		List<Integer> result = new ArrayList<>();
		for (TimedEvent<BundleEvent> e : events) {
			result.add(e.event()
				.getType());
		}
		return result;
	}

	@Test
	@Order(1)
	public void fieldRecorderIsArmedBeforeTheTest(@InjectInstalledBundle("tb1.jar")
	Bundle tb1) throws Exception {
		assertThat(bundleField.isClosed()).isFalse();
		fromPreviousTest = bundleField;
		tb1.start();
		tb1.stop();
		List<TimedEvent<BundleEvent>> events = bundleField
			.waitForSequence(Arrays.asList(type(STARTING), type(STARTED), type(STOPPING), type(STOPPED)), ofSeconds(1));
		assertThat(bundleTypes(events)).containsSubsequence(STARTING, STARTED, STOPPING, STOPPED);
		// The recorder was armed before the parameter installed the bundle
		assertThat(bundleTypes(events)).contains(BundleEvent.INSTALLED);
	}

	@Test
	@Order(2)
	public void fieldRecorderOfPreviousTestIsClosed() {
		assertThat(fromPreviousTest).isNotNull();
		assertThat(fromPreviousTest.isClosed()).isTrue();
		assertThat(bundleField).isNotSameAs(fromPreviousTest);
	}

	@Test
	public void parameterRecorderIsAsynchronousByDefault(@InjectEventRecorder
	EventRecorder<BundleEvent> asyncEvents, @InjectInstalledBundle("tb1.jar")
	Bundle tb1) throws Exception {
		tb1.start();
		List<TimedEvent<BundleEvent>> events = asyncEvents.waitFor(type(STARTED), ofSeconds(5));
		assertThat(bundleTypes(events)).contains(STARTED)
			.doesNotContain(STARTING);
	}

	@Test
	public void serviceRecorderWithFilter(@InjectEventRecorder(filter = "(foo=bar)")
	EventRecorder<ServiceEvent> serviceEvents) throws Exception {
		ServiceRegistration<Object> reg = ctx.registerService(Object.class, new Object(), null);
		assertThat(serviceEvents.size()).isZero();
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("foo", "bar");
		reg.setProperties(props);
		assertThat(serviceEvents.events()).hasSize(1);
		assertThat(serviceEvents.events()
			.get(0)
			.event()
			.getType()).isEqualTo(ServiceEvent.MODIFIED);
	}

	@Test
	public void frameworkRecorderWithTypeMask(@InjectEventRecorder(typeMask = FrameworkEvent.STARTLEVEL_CHANGED)
	EventRecorder<FrameworkEvent> frameworkEvents) throws Exception {
		FrameworkStartLevel fsl = ctx.getBundle(0)
			.adapt(FrameworkStartLevel.class);
		fsl.setStartLevel(fsl.getStartLevel());
		List<TimedEvent<FrameworkEvent>> events = frameworkEvents.waitForCount(1, ofSeconds(5));
		assertThat(events.get(0)
			.event()
			.getType()).isEqualTo(FrameworkEvent.STARTLEVEL_CHANGED);
	}

	@Test
	public void eventObjectRecorderSeesEverything(@InjectEventRecorder
	EventRecorder<EventObject> allEvents) throws Exception {
		ServiceRegistration<Object> reg = ctx.registerService(Object.class, new Object(), null);
		reg.unregister();
		assertThat(allEvents.events()).hasSize(2)
			.allSatisfy(e -> assertThat(e.event()).isInstanceOf(ServiceEvent.class));
	}
}
