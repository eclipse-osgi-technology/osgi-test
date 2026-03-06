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

import java.util.Dictionary;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;
import org.osgi.test.assertj.frameworkdto.FrameworkDTOAssert;
import org.osgi.test.assertj.frameworkstartleveldto.FrameworkStartLevelDTOAssert;
import org.osgi.test.assertj.servicereferencedto.ServiceReferenceDTOAssert;
import org.osgi.test.common.service.ServiceAware;

abstract class ConfigAdminFeatureTestBase {

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
	void configAdminBundlePresent(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO b = findBundle(fwk, "org.apache.felix.configadmin");
		BundleDTOAssert.assertThat(b).isInStateMaskedBy(Bundle.ACTIVE | Bundle.RESOLVED);
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
	void frameworkHasServices(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		FrameworkDTOAssert.assertThat(fwk).hasServicesThat().isNotEmpty();
	}

	@Test
	void frameworkHasProperties(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		FrameworkDTOAssert.assertThat(fwk).hasPropertiesThat().containsKey("org.osgi.framework.version");
	}

	@Test
	void configAdminServiceRegistered(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		ServiceReferenceDTO svc = findService(fwk, "org.osgi.service.cm.ConfigurationAdmin");
		ServiceReferenceDTOAssert.assertThat(svc).hasServicePropertiesThat().containsKey("service.id");
	}

	@Test
	void configAdminBundleHasVersion(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		BundleDTO b = findBundle(fwk, "org.apache.felix.configadmin");
		BundleDTOAssert.assertThat(b).hasVersion("1.9.26");
	}

	@Test
	void startLevelIsOne(FeatureLaunchCapture capture) {
		FrameworkStartLevelDTO sl = capture.frameworkStartLevelDTO();
		FrameworkStartLevelDTOAssert.assertThat(sl).hasStartLevel(1).hasInitialBundleStartLevel(1);
	}

	@Test
	void configAdminServiceFromCorrectBundle(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		ServiceReferenceDTO svc = findService(fwk, "org.osgi.service.cm.ConfigurationAdmin");
		Object oc = svc.properties.get("objectClass");
		assertThat(oc).isInstanceOf(String[].class);
		assertThat((String[]) oc).contains("org.osgi.service.cm.ConfigurationAdmin");
	}

	@Test
	void managedServiceFactoryRegistered(FeatureLaunchCapture capture) {
		FrameworkDTO fwk = capture.frameworkDTO();
		ServiceReferenceDTO svc = findService(fwk, "org.osgi.service.cm.ManagedServiceFactory");
		ServiceReferenceDTOAssert.assertThat(svc).hasServicePropertiesThat().containsEntry("service.pid",
				"org.apache.felix.http");
	}

	@Test
	void configAdminReadTypedProperties(FeatureLaunchCapture capture) throws Exception {
		ConfigurationAdmin cm = capture.getServiceAware(ConfigurationAdmin.class).getService();
		Configuration config = cm.getConfiguration("org.eclipse.osgi.technology.featurelauncher.test.typecheck", "?");
		Dictionary<String, Object> props = config.getProperties();
		assertThat(props).isNotNull();

		assertThat(props.get("string.value")).isInstanceOf(String.class).isEqualTo("hello");
		assertThat(props.get("int.value")).isInstanceOf(Integer.class).isEqualTo(42);
		assertThat(props.get("long.value")).isInstanceOf(Long.class).isEqualTo(123456789L);
		assertThat(props.get("boolean.value")).isInstanceOf(Boolean.class).isEqualTo(true);
		assertThat(props.get("double.value")).isInstanceOf(Double.class).isEqualTo(3.14);
	}

	@Test
	void snapshotShortcuts(FeatureLaunchCapture capture) {
		FrameworkStartLevelDTO sl = capture.frameworkStartLevelDTO();
		FrameworkDTO fwk = capture.frameworkDTO();
		assertThat(sl.startLevel).isGreaterThan(0);
		FrameworkDTOAssert.assertThat(fwk).hasBundlesThat().isNotEmpty();
	}

	// --- resolveBundleDTO tests ---

	@Test
	void resolveBundleDTOById(FeatureLaunchCapture capture) {
		BundleDTO dto = capture.resolveBundleDTO(0);
		assertThat(dto).isNotNull();
		assertThat(dto.id).isEqualTo(0);
	}

	@Test
	void resolveBundleDTOBySymbolicName(FeatureLaunchCapture capture) {
		BundleDTO dto = capture.resolveBundleDTO("org.apache.felix.configadmin");
		assertThat(dto).isNotNull();
		assertThat(dto.symbolicName).isEqualTo("org.apache.felix.configadmin");
		assertThat(dto.version).isEqualTo("1.9.26");
	}

	// --- ServiceAware parameter injection tests ---

	@Test
	void serviceAwareInjection(ServiceAware<ConfigurationAdmin> cmAware) {
		assertThat(cmAware).isNotNull();
		assertThat(cmAware.getServiceType()).isEqualTo(ConfigurationAdmin.class);
		assertThat(cmAware.isEmpty()).isFalse();
		assertThat(cmAware.size()).isGreaterThanOrEqualTo(1);
	}

	@Test
	void serviceAwareGetService(ServiceAware<ConfigurationAdmin> cmAware) throws Exception {
		ConfigurationAdmin cm = cmAware.getService();
		assertThat(cm).isNotNull();
		Configuration config = cm.getConfiguration("org.eclipse.osgi.technology.featurelauncher.test.typecheck", "?");
		assertThat(config).isNotNull();
		assertThat(config.getProperties()).isNotNull();
	}

	@Test
	void serviceAwareGetServiceReference(ServiceAware<ConfigurationAdmin> cmAware) {
		ServiceReference<ConfigurationAdmin> ref = cmAware.getServiceReference();
		assertThat(ref).isNotNull();
		String[] keys = ref.getPropertyKeys();
		assertThat(keys).contains("objectClass", "service.id");
	}

	@Test
	void serviceAwareGetServices(ServiceAware<ConfigurationAdmin> cmAware) {
		List<ConfigurationAdmin> services = cmAware.getServices();
		assertThat(services).isNotEmpty();
	}

}
