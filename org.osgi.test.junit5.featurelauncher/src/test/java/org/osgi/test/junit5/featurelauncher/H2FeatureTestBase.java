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

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;
import org.osgi.test.assertj.frameworkdto.FrameworkDTOAssert;

abstract class H2FeatureTestBase {

	@Test
	void frameworkIsActive(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		FrameworkDTOAssert.assertThat(fwk).hasBundlesThat().isNotEmpty();
	}

	@Test
	void systemBundleIsActive(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO sys = findBundle(fwk, 0);
		BundleDTOAssert.assertThat(sys).isInState(Bundle.ACTIVE);
	}

	@Test
	void h2BundlePresent(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO h2 = findBundle(fwk, "com.h2database");
		BundleDTOAssert.assertThat(h2).isInStateMaskedBy(Bundle.ACTIVE | Bundle.RESOLVED);
	}

	@Test
	void allBundlesActiveOrResolved(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		// Accept ACTIVE, RESOLVED, and STARTING (lazy activation bundles)
		int acceptedStates = Bundle.ACTIVE | Bundle.RESOLVED | Bundle.STARTING;
		FrameworkDTOAssert.assertThat(fwk).hasBundlesThat()
				.allSatisfy(b -> BundleDTOAssert.assertThat(b).isInStateMaskedBy(acceptedStates));
	}

	@Test
	void startLevelIsPositive(FeatureLaunchCapture capture) {
		FrameworkStartLevelDTO sl = capture.frameworkStartLevelDTO();
		assertThat(sl.startLevel).isGreaterThan(0);
	}

	@Test
	void h2BundleIsActive(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO h2 = findBundle(fwk, "com.h2database");
		BundleDTOAssert.assertThat(h2).isInState(Bundle.ACTIVE);
	}

	@Test
	void frameworkHasRegisteredServices(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		FrameworkDTOAssert.assertThat(fwk).hasServicesThat().isNotEmpty();
	}

	@Test
	void frameworkHasVersionProperty(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		FrameworkDTOAssert.assertThat(fwk).hasPropertiesThat().containsKey("org.osgi.framework.version");
	}

}
