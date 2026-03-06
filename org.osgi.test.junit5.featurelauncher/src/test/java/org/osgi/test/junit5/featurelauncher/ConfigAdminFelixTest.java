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

import static org.osgi.test.junit5.featurelauncher.FeatureTestHelper.findBundle;

import org.junit.jupiter.api.Test;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;
import org.osgi.test.assertj.frameworkdto.FrameworkDTOAssert;

@FeatureLaunch(featureFile = "features/full-feature.json", framework = "org.apache.felix:org.apache.felix.framework:7.0.5", startupTimeoutSeconds = 30)
class ConfigAdminFelixTest extends ConfigAdminFeatureTestBase {

	@Test
	void systemBundleHasSymbolicName(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO sys = findBundle(fwk, 0);
		BundleDTOAssert.assertThat(sys).hasSymbolicName("org.apache.felix.framework");
	}

	@Test
	void frameworkHasVendorProperty(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		FrameworkDTOAssert.assertThat(fwk).hasPropertiesThat().containsEntry("org.osgi.framework.vendor",
				"Apache Software Foundation");
	}
}
