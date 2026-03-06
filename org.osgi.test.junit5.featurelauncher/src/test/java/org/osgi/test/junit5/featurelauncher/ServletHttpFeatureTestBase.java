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

import java.util.Dictionary;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.servlet.runtime.HttpServiceRuntime;
import org.osgi.service.servlet.runtime.dto.RuntimeDTO;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;

/**
 * Tests for multi-whiteboard HTTP configuration. The feature configures 3
 * factory HTTP instances (http1:8181, http2:8182, http3:8183) with the default
 * HTTP instance disabled.
 */
abstract class ServletHttpFeatureTestBase {

	private static final String HTTP_RUNTIME = "org.osgi.service.servlet.runtime.HttpServiceRuntime";

	@Test
	void httpBundleIsActive(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO b = findBundle(fwk, "org.apache.felix.http.jetty12");
		BundleDTOAssert.assertThat(b).isInState(Bundle.ACTIVE);
	}

	@Test
	void threeHttpWhiteboardsRegistered(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		List<ServiceReferenceDTO> runtimes = findServices(fwk, HTTP_RUNTIME);
		assertThat(runtimes).hasSize(3);

		List<Object> ids = runtimes.stream().map(s -> s.properties.get("id")).toList();
		assertThat(ids).containsExactlyInAnyOrder("http1", "http2", "http3");
	}

	@Test
	void httpWhiteboardPorts(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		List<ServiceReferenceDTO> runtimes = findServices(fwk, HTTP_RUNTIME);

		for (ServiceReferenceDTO svc : runtimes) {
			String id = String.valueOf(svc.properties.get("id"));
			String port = String.valueOf(svc.properties.get("org.osgi.service.http.port"));
			switch (id) {
			case "http1" -> assertThat(port).isEqualTo("8181");
			case "http2" -> assertThat(port).isEqualTo("8182");
			case "http3" -> assertThat(port).isEqualTo("8183");
			default -> throw new AssertionError("Unexpected HTTP runtime id: " + id);
			}
		}
	}

	@Test
	void readRuntimeDtoForHttp1(FeatureLaunchCapture capture) {
		HttpServiceRuntime hsr = capture.getServiceAware(HttpServiceRuntime.class, "(id=http1)").getService();
		RuntimeDTO dto = hsr.getRuntimeDTO();
		assertThat(dto).isNotNull();
	}

	@Test
	void httpServicePropertyTypesFromServiceDto(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		List<ServiceReferenceDTO> runtimes = findServices(fwk, HTTP_RUNTIME);

		ServiceReferenceDTO http1 = runtimes.stream().filter(s -> "http1".equals(s.properties.get("id"))).findFirst()
				.orElseThrow();

		// Verify service properties exposed by HTTP runtime via ServiceReferenceDTO
		assertThat(http1.properties).containsKey("org.osgi.service.http.port");
		assertThat(http1.properties).containsKey("id");
		assertThat(http1.properties).containsKey("service.id");
		assertThat(http1.properties.get("id")).isEqualTo("http1");
	}

	@Test
	void httpConfigTypedPropertiesFromConfigAdmin(FeatureLaunchCapture capture) throws Exception {
		// Felix HTTP does not propagate unknown config properties to its service
		// registration. Read typed properties from ConfigAdmin directly to verify
		// the FeatureLauncher stored them with correct types.
		ConfigurationAdmin cm = capture.getServiceAware(ConfigurationAdmin.class).getService();
		Configuration config = cm.getConfiguration("org.apache.felix.http~http1", "?");
		Dictionary<String, Object> props = config.getProperties();
		assertThat(props).isNotNull();

		assertThat(props.get("test.string")).isInstanceOf(String.class).isEqualTo("hello");
		assertThat(props.get("test.int")).isInstanceOf(Integer.class).isEqualTo(42);
		assertThat(props.get("test.long")).isInstanceOf(Long.class).isEqualTo(123456789L);
		assertThat(props.get("test.boolean")).isInstanceOf(Boolean.class).isEqualTo(true);
		assertThat(props.get("test.double")).isInstanceOf(Double.class).isEqualTo(3.14);
	}

}
