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

package org.osgi.test.assertj.test.servicereferencedto;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.test.assertj.servicereferencedto.ServiceReferenceDTOAssert;
import org.osgi.test.assertj.servicereferencedto.ServiceReferenceDTOSoftAssertionsProvider;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;

public class ServiceReferenceDTOAssertTest extends
	AbstractAssertAndSAPTest<ServiceReferenceDTOAssert, ServiceReferenceDTO, ServiceReferenceDTOSoftAssertionsProvider> {

	public ServiceReferenceDTOAssertTest() {
		super(ServiceReferenceDTOAssert::assertThat, ServiceReferenceDTOSoftAssertionsProvider.class,
			ServiceReferenceDTO.class, ServiceReferenceDTO::new);
	}

	@BeforeEach
	public void setUp() {
		ServiceReferenceDTO dto = new ServiceReferenceDTO();
		dto.id = 10L;
		dto.bundle = 5L;
		dto.properties = new HashMap<>();
		dto.properties.put("service.id", 10L);
		dto.properties.put("objectClass", new String[] {
			"com.example.MyService"
		});
		dto.usingBundles = new long[] {
			1L, 2L, 3L
		};
		setActual(dto);
	}

	@Test
	public void hasServiceId() {
		assertEqualityAssertion("service ID", aut::hasServiceId, 10L, 99L);
	}

	@Test
	public void hasBundle() {
		assertEqualityAssertion("bundle", aut::hasBundle, 5L, 99L);
	}

	@Test
	public void hasServicePropertiesThat() {
		assertChildAssertion("properties", aut::hasServicePropertiesThat, () -> actual.properties);
	}

	@Test
	public void hasUsingBundles() {
		assertPassing(x -> aut.hasUsingBundles(x), new long[] {
			1L, 2L, 3L
		});
		assertFailing(x -> aut.hasUsingBundles(x), new long[] {
			1L, 2L, 4L
		}).hasMessageMatching("(?si).*using bundles.*4.*but was.*");
	}

	@Test
	public void isBeingUsedBy() {
		assertPassing(x -> aut.isBeingUsedBy(x), new long[] {
			1L, 2L
		});
		assertFailing(x -> aut.isBeingUsedBy(x), new long[] {
			99L
		}).hasMessageMatching("(?si).*used by bundle.*99.*but usingBundles was.*");
	}

	@Test
	public void isBeingUsedBy_withNullUsingBundles() {
		actual.usingBundles = null;
		setActual(actual);

		assertFailing(x -> aut.isBeingUsedBy(x), new long[] {
			1L
		}).hasMessageMatching("(?si).*used by bundle.*1.*usingBundles was null.*");
	}
}
