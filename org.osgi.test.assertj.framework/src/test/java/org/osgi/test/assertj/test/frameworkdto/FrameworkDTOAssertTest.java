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

package org.osgi.test.assertj.test.frameworkdto;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.test.assertj.frameworkdto.FrameworkDTOAssert;
import org.osgi.test.assertj.frameworkdto.FrameworkDTOSoftAssertionsProvider;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;

public class FrameworkDTOAssertTest
	extends AbstractAssertAndSAPTest<FrameworkDTOAssert, FrameworkDTO, FrameworkDTOSoftAssertionsProvider> {

	public FrameworkDTOAssertTest() {
		super(FrameworkDTOAssert::assertThat, FrameworkDTOSoftAssertionsProvider.class, FrameworkDTO.class,
			FrameworkDTO::new);
	}

	@BeforeEach
	public void setUp() {
		FrameworkDTO dto = new FrameworkDTO();
		dto.bundles = new ArrayList<>();
		BundleDTO bundleDTO = new BundleDTO();
		bundleDTO.id = 0L;
		bundleDTO.symbolicName = "system.bundle";
		bundleDTO.version = "1.0.0";
		dto.bundles.add(bundleDTO);
		dto.properties = new HashMap<>();
		dto.properties.put("org.osgi.framework.version", "1.10.0");
		dto.services = new ArrayList<>();
		ServiceReferenceDTO srefDTO = new ServiceReferenceDTO();
		srefDTO.id = 1L;
		srefDTO.bundle = 0L;
		srefDTO.properties = new HashMap<>();
		dto.services.add(srefDTO);
		setActual(dto);
	}

	@Test
	public void hasBundlesThat() {
		assertChildAssertion("bundles", aut::hasBundlesThat, () -> actual.bundles);
	}

	@Test
	public void hasServicesThat() {
		assertChildAssertion("services", aut::hasServicesThat, () -> actual.services);
	}

	@Test
	public void hasPropertiesThat() {
		assertChildAssertion("properties", aut::hasPropertiesThat, () -> actual.properties);
	}
}
