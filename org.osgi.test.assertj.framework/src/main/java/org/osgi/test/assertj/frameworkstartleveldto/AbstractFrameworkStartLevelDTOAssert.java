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

import org.assertj.core.api.AbstractAssert;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;

public abstract class AbstractFrameworkStartLevelDTOAssert<SELF extends AbstractFrameworkStartLevelDTOAssert<SELF, ACTUAL>, ACTUAL extends FrameworkStartLevelDTO>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractFrameworkStartLevelDTOAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF hasStartLevel(int expected) {
		isNotNull();
		if (actual.startLevel != expected) {
			throw failureWithActualExpected(actual.startLevel, expected,
				"%nExpecting%n  <%s>%nto have start level:%n  <%d>%n but was:%n  <%d>", actual, expected,
				actual.startLevel);
		}
		return myself;
	}

	public SELF hasInitialBundleStartLevel(int expected) {
		isNotNull();
		if (actual.initialBundleStartLevel != expected) {
			throw failureWithActualExpected(actual.initialBundleStartLevel, expected,
				"%nExpecting%n  <%s>%nto have initial bundle start level:%n  <%d>%n but was:%n  <%d>", actual,
				expected, actual.initialBundleStartLevel);
		}
		return myself;
	}
}
