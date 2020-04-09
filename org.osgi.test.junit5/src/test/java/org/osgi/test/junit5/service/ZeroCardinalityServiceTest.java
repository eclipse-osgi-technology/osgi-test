/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.junit5.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.types.Foo;

@ExtendWith(ServiceUseExtension.class)
public class ZeroCardinalityServiceTest {

	@ServiceUseParameter(cardinality = 0)
	Foo					foo;
	@ServiceUseParameter(cardinality = 0)
	ServiceAware<Foo>	fServiceAware;

	@Test
	public void testNoService() throws Exception {
		assertThat(fServiceAware.getService()).isNull();
	}

	@Test
	public void testNullField() throws Exception {
		assertThat(foo).isNull();
	}

	@Test
	public void testWithLogServiceParameter(@ServiceUseParameter(cardinality = 0) Foo foo) throws Exception {
		assertThat(foo).isNull();
	}

}
