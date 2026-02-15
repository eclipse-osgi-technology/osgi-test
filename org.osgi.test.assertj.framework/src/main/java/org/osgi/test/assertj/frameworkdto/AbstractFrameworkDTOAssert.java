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

package org.osgi.test.assertj.frameworkdto;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;

public abstract class AbstractFrameworkDTOAssert<SELF extends AbstractFrameworkDTOAssert<SELF, ACTUAL>, ACTUAL extends FrameworkDTO>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractFrameworkDTOAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public ListAssert<BundleDTO> hasBundlesThat() {
		isNotNull();
		return Assertions.assertThat(actual.bundles)
			.as(actual + ".bundles");
	}

	public ListAssert<ServiceReferenceDTO> hasServicesThat() {
		isNotNull();
		return Assertions.assertThat(actual.services)
			.as(actual + ".services");
	}

	public MapAssert<String, Object> hasPropertiesThat() {
		isNotNull();
		return Assertions.assertThat(actual.properties)
			.as(actual + ".properties");
	}
}
