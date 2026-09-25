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
package org.osgi.test.junit5.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EventObject;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.annotation.InjectEventRecorder;
import org.osgi.test.common.event.EventRecorder;
import org.osgi.test.common.event.EventRecorders;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * A JUnit 5 Extension that injects an armed {@link EventRecorder} and closes
 * it when the test is finished.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;ExtendWith(EventRecorderExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectEventRecorder(synchronous = true)
 * 	EventRecorder&lt;BundleEvent&gt; bundleEvents;
 *
 * 	&#64;Test
 * 	public void test(&#64;InjectEventRecorder(filter = "(objectClass=foo.Bar)")
 * 	EventRecorder&lt;ServiceEvent&gt; serviceEvents) throws Exception {
 * 		// act, then
 * 		bundleEvents.waitForCount(2, Duration.ofSeconds(1));
 * 	}
 * }
 * </pre>
 */
public class EventRecorderExtension extends InjectingExtension<InjectEventRecorder> {

	public EventRecorderExtension() {
		super(InjectEventRecorder.class, EventRecorder.class);
	}

	@Override
	protected Object resolveValue(TargetType targetType, InjectEventRecorder injection,
		ExtensionContext extensionContext) throws ParameterResolutionException {
		Class<? extends EventObject> eventType = eventTypeOf(targetType);
		String filter = injection.filter()
			.isEmpty() ? null : injection.filter();
		EventRecorder<?> recorder;
		try {
			recorder = EventRecorders.create(BundleContextExtension.getBundleContext(extensionContext), eventType,
				injection.typeMask(), injection.synchronous(), injection.allServices(), filter);
		} catch (InvalidSyntaxException | IllegalArgumentException e) {
			throw new ParameterResolutionException(
				String.format("Element %s: cannot create an EventRecorder for annotation @%s: %s", targetType.getName(),
					annotation().getSimpleName(), e.getMessage()),
				e);
		}
		// Close the recorder when the context that injected it ends: after the
		// test for parameters and per-method fields, after the class for
		// per-class fields
		getStore(extensionContext).put(recorder, new CloseableRecorder(recorder));
		return recorder;
	}

	/**
	 * The event class from the generic type of the injection target;
	 * {@link EventObject} if the target is raw or uses a wildcard.
	 */
	static Class<? extends EventObject> eventTypeOf(TargetType targetType) {
		Type generic = targetType.getGenericType();
		if (generic instanceof ParameterizedType) {
			Type argument = ((ParameterizedType) generic).getActualTypeArguments()[0];
			if (argument instanceof Class) {
				Class<?> eventType = (Class<?>) argument;
				if (!EventObject.class.isAssignableFrom(eventType)) {
					throw new ParameterResolutionException(String.format(
						"Element %s: the event type %s of the EventRecorder is not an EventObject",
						targetType.getName(), eventType.getName()));
				}
				return eventType.asSubclass(EventObject.class);
			}
		}
		return EventObject.class;
	}

	static Store getStore(ExtensionContext extensionContext) {
		return extensionContext
			.getStore(Namespace.create(EventRecorderExtension.class, extensionContext.getUniqueId()));
	}

	static final class CloseableRecorder implements CloseableResource {
		private final EventRecorder<?> recorder;

		CloseableRecorder(EventRecorder<?> recorder) {
			this.recorder = recorder;
		}

		@Override
		public void close() {
			recorder.close();
		}
	}
}
