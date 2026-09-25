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
package org.osgi.test.common.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.event.EventRecorder;
import org.osgi.test.common.event.EventRecorders;
import org.osgi.test.junit5.event.EventRecorderExtension;

/**
 * Inject an {@link EventRecorder} into test classes and methods. The recorder
 * is armed when it is injected, before the test method runs, and closed when
 * the test (or, for {@code PER_CLASS} lifecycle fields, the test class) is
 * finished.
 * <p>
 * The kind of events recorded is taken from the generic type of the injection
 * target: {@code EventRecorder<BundleEvent>}, {@code EventRecorder<ServiceEvent>},
 * {@code EventRecorder<FrameworkEvent>}, or {@code EventRecorder<EventObject>}
 * for all three kinds in one recorder.
 * <p>
 * Example:
 *
 * <pre>
 * class MyTests {
 * 
 * 	&#64;InjectEventRecorder(synchronous = true, typeMask = BundleEvent.STARTED | BundleEvent.STOPPED)
 * 	EventRecorder&lt;BundleEvent&gt; bundleEvents;
 *
 * 	&#64;Test
 * 	public void test() throws Exception {
 * 		// act
 * 		List&lt;TimedEvent&lt;BundleEvent&gt;&gt; events = bundleEvents.waitForCount(2, Duration.ofSeconds(1));
 * 	}
 * }
 * </pre>
 */
@Inherited
@Target({
	FIELD, PARAMETER
})
@Retention(RUNTIME)
@ExtendWith(EventRecorderExtension.class)
@Documented
public @interface InjectEventRecorder {

	/**
	 * The event types to record, as a mask of the {@code getType()} values of
	 * the event class. The default records all types.
	 */
	int typeMask() default EventRecorders.ALL_TYPES;

	/**
	 * For {@code EventRecorder<BundleEvent>}: register a
	 * {@code SynchronousBundleListener} instead of an asynchronous
	 * {@code BundleListener}. Only a synchronous listener receives
	 * {@code STARTING}, {@code STOPPING} and {@code LAZY_ACTIVATION}.
	 */
	boolean synchronous() default false;

	/**
	 * For {@code EventRecorder<ServiceEvent>}: register an
	 * {@code AllServiceListener}, which is exempt from the framework's package
	 * compatibility check.
	 */
	boolean allServices() default false;

	/**
	 * For {@code EventRecorder<ServiceEvent>}: an LDAP filter on the service
	 * properties. Empty records events for all services.
	 */
	String filter() default "";
}
