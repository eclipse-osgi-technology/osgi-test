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

package org.osgi.test.junit5.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.test.junit5.test.types.Foo;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class InteractWithServiceAwareBeforeTest {

	@InjectBundleContext
	BundleContext			bundleContext;

	@BeforeEach
	public void before(TestInfo testInfo) {
		String methodName = testInfo.getTestMethod()
			.get()
			.getName();
		bundleContext.registerService(Foo.class, new Foo() {},
			Dictionaries.dictionaryOf("testMethod", methodName, "entry", "1"));
		bundleContext.registerService(Foo.class, new Foo() {},
			Dictionaries.dictionaryOf("testMethod", methodName, "entry", "2"));
		bundleContext.registerService(Foo.class, new Foo() {},
			Dictionaries.dictionaryOf("testMethod", methodName, "entry", "3"));
	}

	@InjectService(cardinality = 0)
	ServiceAware<Foo> fServiceAware;

	@Test
	public void testServiceAware3Services() throws Exception {
		assertThat(fServiceAware.getServices()).size()
			.isEqualTo(3);
	}

}