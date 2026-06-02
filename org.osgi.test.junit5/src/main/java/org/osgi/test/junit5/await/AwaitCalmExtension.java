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

package org.osgi.test.junit5.await;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.osgi.test.common.annotation.InjectAwaitCalm;
import org.osgi.test.common.await.AwaitCalm;
import org.osgi.test.common.await.FrameworkWatcher;
import org.osgi.test.common.inject.TargetType;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.inject.InjectingExtension;

/**
 * A JUnit 5 Extension to depend on OSGi services.
 * <p>
 * Example: <br>
 *
 * <pre>
 * &#64;ExtendWith(ServiceExtension.class)
 * class MyTests {
 *
 * 	&#64;InjectService
 * 	Foo foo;
 *
 * 	&#64;Test
 * 	public void test() {
 * 		// use foo
 * 	}
 * }
 * </pre>
 */
public class AwaitCalmExtension extends InjectingExtension<InjectAwaitCalm> {

	public AwaitCalmExtension() {
		super(InjectAwaitCalm.class);
	}

	@Override
	protected boolean supportsType(TargetType targetType, ExtensionContext extensionContext) {
		return targetType.getType()
			.isAssignableFrom(AwaitCalm.class);
	}

	@Override
	protected Object resolveValue(TargetType targetType, InjectAwaitCalm injection, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		if (!supportsType(targetType, extensionContext)) {
			throw new ParameterResolutionException(String.format(
				"Element %s has an unsupported type bound %s for annotation @%s. The injection site must be injectable with an AwaitCalm instance.",
				targetType.getName(), targetType.getType(), annotation().getSimpleName()));
		}
		return new FrameworkWatcher(BundleContextExtension.getBundleContext(extensionContext));
	}
}
