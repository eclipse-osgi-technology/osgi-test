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
import static org.osgi.test.junit5.featurelauncher.FeatureTestHelper.findServices;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.jakartars.runtime.JakartarsServiceRuntime;
import org.osgi.service.jakartars.runtime.dto.RuntimeDTO;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;

/**
 * Tests for multi-whiteboard Jakarta RS configuration. The feature configures 2
 * Jakarta RS whiteboards: rest1 on http1 (port 8181), rest2 on http2 (port
 * 8182).
 */
abstract class JakartaRsFeatureTestBase {

	private static final String JAKARTARS_RUNTIME = "org.osgi.service.jakartars.runtime.JakartarsServiceRuntime";

	@Test
	void jakartaRsBundleIsActive(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO b = findBundle(fwk, "org.eclipse.osgitech.rest");
		BundleDTOAssert.assertThat(b).isInState(Bundle.ACTIVE);
	}

	@Test
	void twoJakartaRsWhiteboardsRegistered(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		List<ServiceReferenceDTO> runtimes = findServices(fwk, JAKARTARS_RUNTIME);
		assertThat(runtimes).hasSize(2);

		List<String> names = runtimes.stream()
				.map(s -> String.valueOf(s.properties.get("jersey.jakartars.whiteboard.name"))).toList();
		assertThat(names).containsExactlyInAnyOrder("REST Whiteboard 1", "REST Whiteboard 2");
	}

	@Test
	void jakartaRsContextPaths(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		List<ServiceReferenceDTO> runtimes = findServices(fwk, JAKARTARS_RUNTIME);

		List<String> paths = runtimes.stream().map(s -> String.valueOf(s.properties.get("jersey.context.path")))
				.toList();
		assertThat(paths).containsExactlyInAnyOrder("rest1", "rest2");
	}

	@Test
	void jakartaRsTargetsCorrectHttpWhiteboards(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		List<ServiceReferenceDTO> runtimes = findServices(fwk, JAKARTARS_RUNTIME);

		List<String> targets = runtimes.stream()
				.map(s -> String.valueOf(s.properties.get("osgi.http.whiteboard.target"))).toList();
		assertThat(targets).containsExactlyInAnyOrder("(id=http1)", "(id=http2)");
	}

	@Test
	void readRuntimeDtoForRest1(FeatureLaunchCapture capture) {
		JakartarsServiceRuntime jsr = capture
				.getServiceAware(JakartarsServiceRuntime.class, "(jersey.jakartars.whiteboard.name=REST Whiteboard 1)")
				.getService();
		RuntimeDTO dto = jsr.getRuntimeDTO();
		assertThat(dto).isNotNull();
	}

}
