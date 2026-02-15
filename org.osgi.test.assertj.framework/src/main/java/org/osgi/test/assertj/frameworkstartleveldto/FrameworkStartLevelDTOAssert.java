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

package org.osgi.test.assertj.frameworkstartleveldto;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;

public class FrameworkStartLevelDTOAssert
	extends AbstractFrameworkStartLevelDTOAssert<FrameworkStartLevelDTOAssert, FrameworkStartLevelDTO> {

	public static final InstanceOfAssertFactory<FrameworkStartLevelDTO, FrameworkStartLevelDTOAssert> FRAMEWORK_START_LEVEL_DTO = new InstanceOfAssertFactory<>(
		FrameworkStartLevelDTO.class, FrameworkStartLevelDTOAssert::assertThat);

	public FrameworkStartLevelDTOAssert(FrameworkStartLevelDTO actual) {
		super(actual, FrameworkStartLevelDTOAssert.class);
	}

	public static FrameworkStartLevelDTOAssert assertThat(FrameworkStartLevelDTO actual) {
		return new FrameworkStartLevelDTOAssert(actual);
	}
}
