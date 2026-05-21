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

package org.osgi.test.junit4.await;

import static org.osgi.test.common.inject.FieldInjector.findAnnotatedNonStaticFields;
import static org.osgi.test.common.inject.FieldInjector.setField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectAwaitCalm;
import org.osgi.test.common.await.FrameworkWatcher;
import org.osgi.test.common.context.ContextHelper;

/**
 * A JUnit 4 Rule to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;Rule
 * public ServiceRule sr = new ServiceRule();
 *
 * &#64;InjectService
 * Foo foo;
 *
 * &#64;Test
 * public void aTest() {
 * 	// use foo
 * }
 * </pre>
 */
public class AwaitCalmRule implements MethodRule {

	public AwaitCalmRule init(Object testInstance) {
		BundleContext bundleContext = ContextHelper.getBundleContext(testInstance.getClass());
		List<Field> fields = findAnnotatedNonStaticFields(testInstance.getClass(), InjectAwaitCalm.class);

		fields.forEach(field -> {
			assertValidFieldCandidate(field);
			setField(field, testInstance, new FrameworkWatcher(bundleContext));
		});

		return this;
	}

	@Override
	public Statement apply(Statement statement, FrameworkMethod method, Object testInstance) {
		init(testInstance);
		return statement;
	}

	static void assertValidFieldCandidate(Field field) {
		if (Modifier.isFinal(field.getModifiers()) || Modifier.isPrivate(field.getModifiers())
			|| Modifier.isStatic(field.getModifiers())) {
			throw new RuntimeException(
				InjectAwaitCalm.class.getName() + " field [" + field + "] must not be final, private or static.");
		}
	}

}
