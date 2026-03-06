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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;

@FeatureLaunch(featureFile = "features/full-feature.json", framework = "org.apache.felix:org.apache.felix.framework:7.0.5", classpath = {
		"${user.home}/.m2/repository/org/slf4j/slf4j-simple/2.0.11/slf4j-simple-2.0.11.jar" }, startupTimeoutSeconds = 30, serviceSettleMs = 2000)
class JakartaRsFelixTest extends JakartaRsFeatureTestBase {

	@Test
	void diagnostics(FeatureLaunchCapture capture) throws IOException {
		FrameworkDTO fwk = capture.frameworkDTO();

		Path outFile = Path.of("target", "diagnostics-jakartars.txt");
		Files.createDirectories(outFile.getParent());

		try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(outFile))) {
			out.println("=== BUNDLES ===");
			for (BundleDTO b : fwk.bundles) {
				String state = switch (b.state) {
				case 1 -> "UNINSTALLED";
				case 2 -> "INSTALLED";
				case 4 -> "RESOLVED";
				case 8 -> "STARTING";
				case 16 -> "STOPPING";
				case 32 -> "ACTIVE";
				default -> "UNKNOWN(" + b.state + ")";
				};
				out.println("  " + b.id + " " + b.symbolicName + " " + b.version + " [" + state + "]");
			}
			out.println("\n=== ALL SERVICES ===");
			for (ServiceReferenceDTO svc : fwk.services) {
				Object oc = svc.properties.get("objectClass");
				String ocStr = oc instanceof String[] arr ? String.join(", ", arr) : String.valueOf(oc);
				out.println("  #" + svc.id + " bundle=" + svc.bundle + " " + ocStr);
				for (Map.Entry<String, Object> e : svc.properties.entrySet()) {
					if ("objectClass".equals(e.getKey()))
						continue;
					Object v = e.getValue();
					String vs = v instanceof String[] a ? Arrays.toString(a) : String.valueOf(v);
					out.println("    " + e.getKey() + " = " + vs);
				}
			}
		}
		System.out.println("Diagnostics written to: " + outFile.toAbsolutePath());
	}
}
