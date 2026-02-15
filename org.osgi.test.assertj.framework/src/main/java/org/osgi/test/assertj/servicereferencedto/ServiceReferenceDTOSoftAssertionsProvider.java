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

package org.osgi.test.assertj.servicereferencedto;

import org.assertj.core.api.SoftAssertionsProvider;
import org.osgi.framework.dto.ServiceReferenceDTO;

public interface ServiceReferenceDTOSoftAssertionsProvider extends SoftAssertionsProvider {
	/**
	 * Create assertion for {@link org.osgi.framework.dto.ServiceReferenceDTO}.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	default ServiceReferenceDTOAssert assertThat(ServiceReferenceDTO actual) {
		return proxy(ServiceReferenceDTOAssert.class, ServiceReferenceDTO.class, actual);
	}
}
