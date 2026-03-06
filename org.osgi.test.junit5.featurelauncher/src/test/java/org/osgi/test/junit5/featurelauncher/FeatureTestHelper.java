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

package org.osgi.test.junit5.featurelauncher;

import java.util.Arrays;
import java.util.List;

import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;

final class FeatureTestHelper {

	private FeatureTestHelper() {
	}

	static BundleDTO findBundle(FrameworkDTO fwk, long id) {
		return fwk.bundles.stream().filter(b -> b.id == id).findFirst().orElseThrow();
	}

	static BundleDTO findBundle(FrameworkDTO fwk, String symbolicName) {
		return fwk.bundles.stream().filter(b -> symbolicName.equals(b.symbolicName)).findFirst().orElseThrow();
	}

	static ServiceReferenceDTO findService(FrameworkDTO fwk, String objectClass) {
		return findServices(fwk, objectClass).stream().findFirst().orElseThrow();
	}

	static List<ServiceReferenceDTO> findServices(FrameworkDTO fwk, String objectClass) {
		return fwk.services.stream().filter(s -> {
			Object oc = s.properties.get("objectClass");
			return oc instanceof String[] arr && Arrays.asList(arr).contains(objectClass);
		}).toList();
	}
}
