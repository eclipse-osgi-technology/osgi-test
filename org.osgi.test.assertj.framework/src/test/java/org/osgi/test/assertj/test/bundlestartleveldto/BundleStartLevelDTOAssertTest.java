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

package org.osgi.test.assertj.test.bundlestartleveldto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.startlevel.dto.BundleStartLevelDTO;
import org.osgi.test.assertj.bundlestartleveldto.BundleStartLevelDTOAssert;
import org.osgi.test.assertj.bundlestartleveldto.BundleStartLevelDTOSoftAssertionsProvider;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;

public class BundleStartLevelDTOAssertTest extends
	AbstractAssertAndSAPTest<BundleStartLevelDTOAssert, BundleStartLevelDTO, BundleStartLevelDTOSoftAssertionsProvider> {

	public BundleStartLevelDTOAssertTest() {
		super(BundleStartLevelDTOAssert::assertThat, BundleStartLevelDTOSoftAssertionsProvider.class,
			BundleStartLevelDTO.class, BundleStartLevelDTO::new);
	}

	@BeforeEach
	public void setUp() {
		BundleStartLevelDTO dto = new BundleStartLevelDTO();
		dto.bundle = 42L;
		dto.startLevel = 10;
		dto.activationPolicyUsed = true;
		dto.persistentlyStarted = true;
		setActual(dto);
	}

	@Test
	public void hasBundle() {
		assertEqualityAssertion("bundle", aut::hasBundle, 42L, 99L);
	}

	@Test
	public void hasStartLevel() {
		assertEqualityAssertion("start level", aut::hasStartLevel, 10, 20);
	}

	@Test
	public void isActivationPolicyUsed() {
		assertPassing("isActivationPolicyUsed", x -> aut.isActivationPolicyUsed(), null);
		assertFailing("isNotActivationPolicyUsed", x -> aut.isNotActivationPolicyUsed(), null)
			.hasMessageMatching("(?si).*not have activation policy used.*but it was.*");

		actual.activationPolicyUsed = false;
		setActual(actual);

		assertPassing("isNotActivationPolicyUsed", x -> aut.isNotActivationPolicyUsed(), null);
		assertFailing("isActivationPolicyUsed", x -> aut.isActivationPolicyUsed(), null)
			.hasMessageMatching("(?si).*have activation policy used.*but it was not.*");
	}

	@Test
	public void isPersistentlyStarted() {
		assertPassing("isPersistentlyStarted", x -> aut.isPersistentlyStarted(), null);
		assertFailing("isNotPersistentlyStarted", x -> aut.isNotPersistentlyStarted(), null)
			.hasMessageMatching("(?si).*not be persistently started.*but it was.*");

		actual.persistentlyStarted = false;
		setActual(actual);

		assertPassing("isNotPersistentlyStarted", x -> aut.isNotPersistentlyStarted(), null);
		assertFailing("isPersistentlyStarted", x -> aut.isPersistentlyStarted(), null)
			.hasMessageMatching("(?si).*be persistently started.*but it was not.*");
	}
}
