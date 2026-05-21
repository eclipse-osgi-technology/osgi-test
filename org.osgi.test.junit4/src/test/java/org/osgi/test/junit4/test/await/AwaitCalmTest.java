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

package org.osgi.test.junit4.test.await;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectAwaitCalm;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.await.AwaitCalm;
import org.osgi.test.common.await.AwaitCalmTimeoutException;
import org.osgi.test.junit4.await.AwaitCalmRule;
import org.osgi.test.junit4.context.BundleContextRule;

public class AwaitCalmTest {
	@Rule
	public AwaitCalmRule		acr				= new AwaitCalmRule();
	@InjectAwaitCalm
	AwaitCalm awaitCalmField;

	private static List<Thread>	noisyThreads	= new ArrayList<>();

	private static void makeNoise(int iterations, int iterationDelay, IntConsumer action) {
		Thread t = new Thread(() -> {
			for (int i = 0; i < iterations; i++) {
				try {
					Thread.sleep(iterationDelay);
				} catch (InterruptedException e) {
					return;
				}
				action.accept(i);
			}
		});
		noisyThreads.add(t);
		t.start();
	}

	@After
	public void stopNoise() {
		noisyThreads.forEach(t -> {
			t.interrupt();
			try {
				t.join(100);
			} catch (InterruptedException e) {}
			if (t.isAlive()) {
				System.out.println("Noisy Thread failed to shutdown");
			}
		});
		noisyThreads.clear();
	}

	@Test
	public void testFieldWithNothingHappening() throws Exception {
		assertThatNoException().isThrownBy(() -> awaitCalmField.waitForQuiet(ofMillis(500), ofSeconds(1)));
	}

	public static class FrameworkEvents {
		@Rule
		public AwaitCalmRule		acr	= new AwaitCalmRule();
		@Rule
		public BundleContextRule	bcr	= new BundleContextRule();
		@InjectAwaitCalm
		AwaitCalm		ac;
		@InjectBundleContext
		BundleContext	ctx;

		private Bundle	bundle;

		@Before
		public void createBundle() throws IOException, BundleException {

			Manifest manifest = new Manifest();
			manifest.getMainAttributes()
				.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
			manifest.getMainAttributes()
				.putValue(Constants.BUNDLE_SYMBOLICNAME, "testBundle");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JarOutputStream jos = new JarOutputStream(baos, manifest)) {}

			bundle = ctx.installBundle("test", new ByteArrayInputStream(baos.toByteArray()));
			bundle.start();
		}

		private IntConsumer bundleNoise() {
			return x -> {
				try {
					bundle.stop();
					bundle.start();
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("Failed to cycle the bundle", e);
				}
			};
		}

		@Test
		// Wait for a noisy bundle to finish doing things (Overall)
		public void testNoisyBundleOverall() throws Exception {
			makeNoise(10, 50, bundleNoise());

			assertThat(ac.waitForQuiet(ofMillis(500), ofSeconds(2))).isGreaterThanOrEqualTo(10);
		}

		@Test
		// Wait for a noisy bundle to finish doing things (Bundle)
		public void testNoisyBundleBundle() throws Exception {
			makeNoise(10, 50, bundleNoise());

			assertThat(ac.waitForBundleQuiet(ofMillis(500), ofSeconds(2))).isGreaterThanOrEqualTo(10);
		}

		@Test
		// Time out while a noisy bundle does its work
		public void testNoisyBundleTimeout() throws Exception {
			makeNoise(15, 50, bundleNoise());

			assertThrows(AwaitCalmTimeoutException.class, () -> ac.waitForQuiet(ofMillis(500), ofSeconds(1)));
		}

		@Test
		// Service Events aren't blocked by a noisy bundle
		public void testNoisyBundleNoServiceTimeout() throws Exception {
			makeNoise(15, 50, bundleNoise());

			assertThat(ac.waitForServiceQuiet(ofMillis(500), ofSeconds(1))).isLessThan(3);
		}
	}

	public static class ServiceEvents {
		@Rule
		public AwaitCalmRule		acr	= new AwaitCalmRule();
		@Rule
		public BundleContextRule	bcr	= new BundleContextRule();
		@InjectAwaitCalm
		AwaitCalm		ac;
		@InjectBundleContext
		BundleContext	ctx;

		private IntConsumer serviceNoise(ServiceRegistration<?> reg) {
			return count -> reg.setProperties(new Hashtable<String, Object>() {
				{
					put("count", count);
				}
			});
		}

		@Test
		// Wait for a noisy service to finish doing things (Overall)
		public void testNoisyServiceOverall() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(10, 50, serviceNoise(reg));

			assertThat(ac.waitForQuiet(ofMillis(500), ofSeconds(2))).isGreaterThanOrEqualTo(10);
		}

		@Test
		// Wait for a noisy service to finish doing things (Service)
		public void testNoisyServiceService() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(10, 50, serviceNoise(reg));

			assertThat(ac.waitForServiceQuiet(ofMillis(500), ofSeconds(2)))
				.isGreaterThanOrEqualTo(10);
		}

		@Test
		// Time out while a noisy service does its work
		public void testNoisyServiceTimeout() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(15, 50, serviceNoise(reg));

			assertThrows(AwaitCalmTimeoutException.class,
				() -> ac.waitForQuiet(ofMillis(500), ofSeconds(1)));
		}

		@Test
		// Framework Events aren't blocked by a noisy service
		public void testNoisyServiceNoFrameworkTimeout() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(15, 50, serviceNoise(reg));

			assertThat(ac.waitForBundleQuiet(ofMillis(500), ofSeconds(1))).isLessThan(3);
		}

		@Test
		// Service events should honour the filter
		public void testFilteredNoisyService() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(15, 50, serviceNoise(reg));

			assertThat(ac.waitForServiceQuiet(ofMillis(500), ofSeconds(1), "(count<=5)"))
				.isEqualTo(7);
		}
	}

}
