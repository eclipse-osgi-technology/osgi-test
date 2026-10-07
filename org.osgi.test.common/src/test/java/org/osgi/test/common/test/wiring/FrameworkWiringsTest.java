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
package org.osgi.test.common.test.wiring;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.test.common.wiring.FrameworkWirings;

public class FrameworkWiringsTest {

	BundleContext	context;
	Bundle			bundle;

	@BeforeEach
	void installBundle() throws Exception {
		context = FrameworkUtil.getBundle(getClass())
			.getBundleContext();
		bundle = context.installBundle("frameworkwirings.test", bundleJar("org.osgi.test.common.test.wirings"));
		context.getBundle(Constants.SYSTEM_BUNDLE_ID)
			.adapt(FrameworkWiring.class)
			.resolveBundles(Collections.singleton(bundle));
		assertThat(bundle.getState()).isEqualTo(Bundle.RESOLVED);
	}

	@AfterEach
	void uninstallBundle() throws Exception {
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED) {
			bundle.uninstall();
		}
	}

	@Test
	void refreshesBundlesAndWaitsForThisRefresh() {
		FrameworkEvent event = FrameworkWirings.refreshBundles(context, ofSeconds(30), bundle);

		assertThat(event.getType()).isEqualTo(FrameworkEvent.PACKAGES_REFRESHED);
		// a refreshed bundle is unresolved afterwards, or resolved again by
		// the framework; either way the refresh went through
		assertThat(bundle.getState()).isIn(Bundle.INSTALLED, Bundle.RESOLVED);
	}

	@Test
	void refreshesWithTheDefaultTimeout() {
		FrameworkEvent event = FrameworkWirings.refreshBundles(context, bundle);
		assertThat(event.getType()).isEqualTo(FrameworkEvent.PACKAGES_REFRESHED);
		assertThat(FrameworkWirings.DEFAULT_REFRESH_TIMEOUT).isEqualTo(ofSeconds(60));
	}

	@Test
	void refreshesACollection() {
		FrameworkEvent event = FrameworkWirings.refreshBundles(context, Collections.singletonList(bundle));
		assertThat(event.getType()).isEqualTo(FrameworkEvent.PACKAGES_REFRESHED);
	}

	@Test
	void rejectsNulls() {
		assertThatThrownBy(() -> FrameworkWirings.refreshBundles(null, bundle)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> FrameworkWirings.refreshBundles(context, (Duration) null, bundle))
			.isInstanceOf(NullPointerException.class);
	}

	private static InputStream bundleJar(String symbolicName) throws IOException {
		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		attributes.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
		attributes.putValue(Constants.BUNDLE_SYMBOLICNAME, symbolicName);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (JarOutputStream jar = new JarOutputStream(bytes, manifest)) {
			// an empty bundle is enough
		}
		return new ByteArrayInputStream(bytes.toByteArray());
	}
}
