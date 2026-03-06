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

import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.test.junit5.featurelauncher.FeatureTestHelper.findBundle;
import static org.osgi.test.junit5.featurelauncher.FeatureTestHelper.findService;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;
import org.osgi.test.assertj.servicereferencedto.ServiceReferenceDTOAssert;

abstract class ScrFeatureTestBase {

	@Test
	void scrBundleIsActive(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO b = findBundle(fwk, "org.apache.felix.scr");
		BundleDTOAssert.assertThat(b).isInState(Bundle.ACTIVE);
	}

	@Test
	void scrServiceRegistered(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		ServiceReferenceDTO svc = findService(fwk, "org.osgi.service.component.runtime.ServiceComponentRuntime");
		ServiceReferenceDTOAssert.assertThat(svc).hasServicePropertiesThat().containsKey("service.id");
	}

	@Test
	void scrGetDescriptionReturnsNullForUnknownComponent(FeatureLaunchCapture capture) {
		ServiceComponentRuntime scr = capture.getServiceAware(ServiceComponentRuntime.class).getService();
		Bundle bundle = capture.resolveBundle("org.apache.felix.scr");
		ComponentDescriptionDTO dto = scr.getComponentDescriptionDTO(bundle, "does.not.exist");
		assertThat(dto).isNull();
	}

}
