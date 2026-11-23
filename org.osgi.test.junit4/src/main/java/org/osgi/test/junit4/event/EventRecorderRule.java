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
package org.osgi.test.junit4.event;

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.annotation.InjectEventRecorder;
import org.osgi.test.common.context.ContextHelper;
import org.osgi.test.common.event.EventRecorder;
import org.osgi.test.common.event.EventRecorders;

/**
 * A JUnit 4 Rule that injects an armed {@link EventRecorder} into the fields
 * annotated with {@link InjectEventRecorder} and closes it when the test is
 * finished.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;Rule
 * public EventRecorderRule erc = new EventRecorderRule();
 *
 * &#64;InjectEventRecorder(synchronous = true)
 * EventRecorder&lt;BundleEvent&gt; bundleEvents;
 *
 * &#64;Test
 * public void aTest() throws Exception {
 * 	// act, then
 * 	bundleEvents.waitForCount(2, Duration.ofSeconds(1));
 * }
 * </pre>
 */
public class EventRecorderRule implements MethodRule {

	@Override
	public Statement apply(Statement statement, FrameworkMethod method, Object testInstance) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				List<EventRecorder<?>> recorders = new ArrayList<>();
				try {
					inject(testInstance, recorders);
					statement.evaluate();
				} finally {
					// Close every recorder that was created, even when the
					// injection of a later field failed
					recorders.forEach(EventRecorder::close);
				}
			}
		};
	}

	private static void inject(Object testInstance, List<EventRecorder<?>> recorders) {
		BundleContext bundleContext = ContextHelper.getBundleContext(testInstance.getClass());
		for (Field field : findAnnotatedNonStaticFields(testInstance.getClass(), InjectEventRecorder.class)) {
			assertValidFieldCandidate(field);
			InjectEventRecorder injection = field.getAnnotation(InjectEventRecorder.class);
			String filter = injection.filter()
				.isEmpty() ? null : injection.filter();
			EventRecorder<?> recorder;
			try {
				recorder = EventRecorders.create(bundleContext, eventTypeOf(field), injection.typeMask(),
					injection.synchronous(), injection.allServices(), filter);
			} catch (InvalidSyntaxException | IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Field [%s]: cannot create an EventRecorder for @%s: %s",
					field, InjectEventRecorder.class.getSimpleName(), e.getMessage()), e);
			}
			recorders.add(recorder);
			setField(field, testInstance, recorder);
		}
	}

	/**
	 * The event class from the generic type of the field; {@link EventObject}
	 * if the field is raw or uses a wildcard.
	 */
	static Class<? extends EventObject> eventTypeOf(Field field) {
		Type generic = field.getGenericType();
		if (generic instanceof ParameterizedType) {
			Type argument = ((ParameterizedType) generic).getActualTypeArguments()[0];
			if (argument instanceof Class) {
				Class<?> eventType = (Class<?>) argument;
				if (!EventObject.class.isAssignableFrom(eventType)) {
					throw new IllegalArgumentException(String.format(
						"Field [%s]: the event type %s of the EventRecorder is not an EventObject", field,
						eventType.getName()));
				}
				return eventType.asSubclass(EventObject.class);
			}
		}
		return EventObject.class;
	}

	static void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new IllegalArgumentException(
				InjectEventRecorder.class.getName() + " field [" + field + "] must not be final, private or static.");
		}
		if (field.getType() != EventRecorder.class) {
			throw new IllegalArgumentException(
				InjectEventRecorder.class.getName() + " field [" + field + "] must be of type EventRecorder.");
		}
	}
}
