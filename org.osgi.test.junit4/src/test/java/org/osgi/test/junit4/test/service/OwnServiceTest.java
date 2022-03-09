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

package org.osgi.test.junit4.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit4.context.BundleContextRule;
import org.osgi.test.junit4.service.ServiceRule;
import org.osgi.test.junit4.test.types.Foo;

public class OwnServiceTest {

	@Rule
	public BundleContextRule	bcr	= new BundleContextRule();
	@Rule
	public ServiceRule			sur	= new ServiceRule();

	@InjectBundleContext
	BundleContext				bundleContext;
	@InjectService(cardinality = 0, service = Foo.class)
	ServiceAware<Foo>	fooServiceAware;

	@Test
	public void testWithLogServiceUse() throws Exception {
		bundleContext.registerService(Foo.class, new Foo() {}, null);
		assertThat(fooServiceAware.isEmpty()).isFalse();
	}

}
