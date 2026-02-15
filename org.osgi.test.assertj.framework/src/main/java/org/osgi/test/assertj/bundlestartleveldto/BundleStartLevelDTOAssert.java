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

package org.osgi.test.assertj.bundlestartleveldto;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.startlevel.dto.BundleStartLevelDTO;

public class BundleStartLevelDTOAssert
	extends AbstractBundleStartLevelDTOAssert<BundleStartLevelDTOAssert, BundleStartLevelDTO> {

	public static final InstanceOfAssertFactory<BundleStartLevelDTO, BundleStartLevelDTOAssert> BUNDLE_START_LEVEL_DTO = new InstanceOfAssertFactory<>(
		BundleStartLevelDTO.class, BundleStartLevelDTOAssert::assertThat);

	public BundleStartLevelDTOAssert(BundleStartLevelDTO actual) {
		super(actual, BundleStartLevelDTOAssert.class);
	}

	public static BundleStartLevelDTOAssert assertThat(BundleStartLevelDTO actual) {
		return new BundleStartLevelDTOAssert(actual);
	}
}
