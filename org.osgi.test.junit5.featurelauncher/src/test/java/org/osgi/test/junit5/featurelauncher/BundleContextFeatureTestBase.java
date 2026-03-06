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

import java.util.Dictionary;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Comprehensive proxy chain tests exercising BundleContext, Bundle,
 * ServiceReference, and Filter proxies. Tests cover a wide variety of parameter
 * types, return type transformations, and proxy chain depths from 1 to 4
 * levels.
 */
abstract class BundleContextFeatureTestBase {

	@Test
	void getBundleContextReturnsProxy(FeatureLaunchCapture capture) {
		BundleContext bc = capture.getBundleContext();
		assertThat(bc).isNotNull();
	}

	@Test
	void getBundleReturnsSystemBundle(FeatureLaunchCapture capture) {
		// BundleContext.getBundle() -> Bundle (interface auto-proxy, no-arg)
		BundleContext bc = capture.getBundleContext();
		Bundle systemBundle = bc.getBundle();
		assertThat(systemBundle).isNotNull();
		assertThat(systemBundle.getBundleId()).isEqualTo(0L);
	}

	@Test
	void getBundleByIdReturnsBundle(FeatureLaunchCapture capture) {
		// BundleContext.getBundle(long) -> Bundle (long parameter + interface
		// auto-proxy)
		BundleContext bc = capture.getBundleContext();
		Bundle bundle = bc.getBundle(0);
		assertThat(bundle).isNotNull();
		assertThat(bundle.getBundleId()).isEqualTo(0L);
	}

	@Test
	void getBundlesReturnsArray(FeatureLaunchCapture capture) {
		// BundleContext.getBundles() -> Bundle[] (interface array return)
		BundleContext bc = capture.getBundleContext();
		Bundle[] bundles = bc.getBundles();
		assertThat(bundles).isNotNull().isNotEmpty();
		assertThat(bundles.length).isGreaterThanOrEqualTo(3);
	}

	@Test
	void getPropertyReturnsString(FeatureLaunchCapture capture) {
		// BundleContext.getProperty(String) -> String
		BundleContext bc = capture.getBundleContext();
		String version = bc.getProperty("org.osgi.framework.version");
		assertThat(version).isNotNull().isNotEmpty();
	}

	@Test
	void createFilterAndToString(FeatureLaunchCapture capture) throws Exception {
		// BundleContext.createFilter(String) -> Filter (interface auto-proxy)
		// Filter.toString() -> String (delegated to remote VM)
		BundleContext bc = capture.getBundleContext();
		Filter filter = bc.createFilter("(objectClass=*)");
		assertThat(filter).isNotNull();
		assertThat(filter.toString()).contains("objectClass");
	}

	@Test
	void getServiceReferencesReturnsArray(FeatureLaunchCapture capture) throws Exception {
		// BundleContext.getServiceReferences(String, String) -> ServiceReference[]
		// (two String params + interface array return)
		BundleContext bc = capture.getBundleContext();
		ServiceReference<?>[] refs = bc.getServiceReferences("org.osgi.service.cm.ConfigurationAdmin", null);
		assertThat(refs).isNotNull().hasSize(1);
	}

	@Test
	void bundleGetSymbolicName(FeatureLaunchCapture capture) {
		// Bundle.getSymbolicName() -> String
		Bundle bundle = capture.resolveBundle("org.apache.felix.configadmin");
		assertThat(bundle.getSymbolicName()).isEqualTo("org.apache.felix.configadmin");
	}

	@Test
	void bundleGetBundleId(FeatureLaunchCapture capture) {
		// Bundle.getBundleId() -> long (primitive)
		Bundle bundle = capture.resolveBundle("org.apache.felix.configadmin");
		assertThat(bundle.getBundleId()).isGreaterThan(0L);
	}

	@Test
	void bundleGetState(FeatureLaunchCapture capture) {
		// Bundle.getState() -> int (primitive)
		Bundle bundle = capture.resolveBundle("org.apache.felix.configadmin");
		assertThat(bundle.getState()).isEqualTo(Bundle.ACTIVE);
	}

	@Test
	void bundleGetHeaders(FeatureLaunchCapture capture) {
		// Bundle.getHeaders() -> Dictionary<String, String>
		Bundle bundle = capture.resolveBundle("org.apache.felix.configadmin");
		Dictionary<String, String> headers = bundle.getHeaders();
		assertThat(headers).isNotNull();
		assertThat(headers.get("Bundle-SymbolicName")).isEqualTo("org.apache.felix.configadmin");
		assertThat(headers.get("Bundle-Version")).isNotNull();
	}

	@Test
	void bundleGetLocation(FeatureLaunchCapture capture) {
		// Bundle.getLocation() -> String
		Bundle bundle = capture.resolveBundle("org.apache.felix.configadmin");
		assertThat(bundle.getLocation()).isNotNull().isNotEmpty();
	}

	@Test
	void bundleGetBundleContextChainedProxy(FeatureLaunchCapture capture) {
		// Bundle.getBundleContext() -> BundleContext (chained proxy!)
		// Then BundleContext.getBundle() -> Bundle (another proxy)
		Bundle bundle = capture.resolveBundle("org.apache.felix.configadmin");
		BundleContext bc = bundle.getBundleContext();
		assertThat(bc).isNotNull();
		Bundle self = bc.getBundle();
		assertThat(self).isNotNull();
		assertThat(self.getSymbolicName()).isEqualTo("org.apache.felix.configadmin");
	}

	@Test
	void serviceReferenceGetPropertyKeys(FeatureLaunchCapture capture) throws Exception {
		// ServiceReference.getPropertyKeys() -> String[]
		BundleContext bc = capture.getBundleContext();
		ServiceReference<?>[] refs = bc.getServiceReferences("org.osgi.service.cm.ConfigurationAdmin", null);
		assertThat(refs).isNotNull().hasSize(1);

		String[] keys = refs[0].getPropertyKeys();
		assertThat(keys).isNotNull().contains("objectClass", "service.id");
	}

	@Test
	void serviceReferenceGetProperty(FeatureLaunchCapture capture) throws Exception {
		// ServiceReference.getProperty(String) -> Object (untyped)
		BundleContext bc = capture.getBundleContext();
		ServiceReference<?>[] refs = bc.getServiceReferences("org.osgi.service.cm.ConfigurationAdmin", null);
		assertThat(refs).isNotNull().hasSize(1);

		Object serviceId = refs[0].getProperty("service.id");
		assertThat(serviceId).isNotNull();
	}

	@Test
	void serviceReferenceGetBundle(FeatureLaunchCapture capture) throws Exception {
		// ServiceReference.getBundle() -> Bundle (interface auto-proxy)
		BundleContext bc = capture.getBundleContext();
		ServiceReference<?>[] refs = bc.getServiceReferences("org.osgi.service.cm.ConfigurationAdmin", null);
		assertThat(refs).isNotNull().hasSize(1);

		Bundle bundle = refs[0].getBundle();
		assertThat(bundle).isNotNull();
		assertThat(bundle.getSymbolicName()).isEqualTo("org.apache.felix.configadmin");
	}

	@Test
	void configAdminListConfigurations(FeatureLaunchCapture capture) throws Exception {
		// ConfigurationAdmin.listConfigurations(String) -> Configuration[]
		// (String param + array of interface return)
		ConfigurationAdmin cm = capture.getServiceAware(ConfigurationAdmin.class).getService();
		Configuration[] configs = cm.listConfigurations("(service.pid=org.apache.felix.http~http1)");
		assertThat(configs).isNotNull().hasSize(1);
		assertThat(configs[0].getPid()).isEqualTo("org.apache.felix.http~http1");
	}

	@Test
	void configAdminReadHttpFactoryTypedProps(FeatureLaunchCapture capture) throws Exception {
		// Full proxy chain: ConfigurationAdmin -> Configuration -> Dictionary
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

	@Test
	void bundleStopAndStart(FeatureLaunchCapture capture) throws Exception {
		// Bundle.stop() -> void, Bundle.start() -> void, Bundle.getState() -> int
		// Use slf4j-api which no other test depends on being active
		Bundle bundle = capture.resolveBundle("slf4j.api");
		assertThat(bundle.getState()).isIn(Bundle.ACTIVE, Bundle.RESOLVED);

		bundle.stop();
		assertThat(bundle.getState()).isEqualTo(Bundle.RESOLVED);

		bundle.start();
		assertThat(bundle.getState()).isEqualTo(Bundle.ACTIVE);
	}

	@Test
	void deepProxyChain(FeatureLaunchCapture capture) {
		// 4-level proxy chain:
		// 1. capture.getBundleContext() -> BundleContext proxy
		// 2. bc.getBundles() -> Bundle[] (each element is a proxy)
		// 3. bundle.getBundleContext() -> BundleContext proxy (chained)
		// 4. innerBc.getProperty(String) -> String
		BundleContext bc = capture.getBundleContext();
		Bundle[] bundles = bc.getBundles();
		assertThat(bundles).isNotEmpty();

		Bundle configAdmin = null;
		for (Bundle b : bundles) {
			if ("org.apache.felix.configadmin".equals(b.getSymbolicName())) {
				configAdmin = b;
				break;
			}
		}
		assertThat(configAdmin).isNotNull();
		BundleContext innerBc = configAdmin.getBundleContext();
		assertThat(innerBc).isNotNull();

		String version = innerBc.getProperty("org.osgi.framework.version");
		assertThat(version).isNotNull().isNotEmpty();
	}
}
