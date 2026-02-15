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

package org.osgi.test.assertj.test.frameworkstartleveldto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import org.osgi.test.assertj.frameworkstartleveldto.FrameworkStartLevelDTOAssert;
import org.osgi.test.assertj.frameworkstartleveldto.FrameworkStartLevelDTOSoftAssertionsProvider;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;

public class FrameworkStartLevelDTOAssertTest extends
	AbstractAssertAndSAPTest<FrameworkStartLevelDTOAssert, FrameworkStartLevelDTO, FrameworkStartLevelDTOSoftAssertionsProvider> {

	public FrameworkStartLevelDTOAssertTest() {
		super(FrameworkStartLevelDTOAssert::assertThat, FrameworkStartLevelDTOSoftAssertionsProvider.class,
			FrameworkStartLevelDTO.class, FrameworkStartLevelDTO::new);
	}

	@BeforeEach
	public void setUp() {
		FrameworkStartLevelDTO dto = new FrameworkStartLevelDTO();
		dto.startLevel = 6;
		dto.initialBundleStartLevel = 1;
		setActual(dto);
	}

	@Test
	public void hasStartLevel() {
		assertEqualityAssertion("start level", aut::hasStartLevel, 6, 10);
	}

	@Test
	public void hasInitialBundleStartLevel() {
		assertEqualityAssertion("initial bundle start level", aut::hasInitialBundleStartLevel, 1, 5);
	}
}
