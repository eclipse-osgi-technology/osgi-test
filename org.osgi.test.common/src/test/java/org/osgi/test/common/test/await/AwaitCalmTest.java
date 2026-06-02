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

package org.osgi.test.common.test.await;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.await.AwaitCalm;
import org.osgi.test.common.await.AwaitCalm.TimedEvent;
import org.osgi.test.common.await.AwaitCalmTimeoutException;
import org.osgi.test.common.await.FrameworkWatcher;

public class AwaitCalmTest {

	AwaitCalm				ac;
	BundleContext			ctx;

	private List<Thread> noisyThreads = new ArrayList<>();

	@BeforeEach
	void setupCalmAndContext() {
		ctx = FrameworkUtil.getBundle(getClass())
			.getBundleContext();
		ac = new FrameworkWatcher(ctx);
	}

	private void makeNoise(int iterations, int iterationDelay, IntConsumer action) {
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

	private IntConsumer bundleNoise(Bundle bundle) {
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

	private IntConsumer serviceNoise(ServiceRegistration<?> reg) {
		return count -> reg.setProperties(new Hashtable<String, Object>() {
			{
				put("count", count);
			}
		});
	}

	@AfterEach
	void stopNoise() {
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

	/**
	 * Note that in all tests, each bundle noise iteration triggers four events
	 */
	@Nested
	class FrameworkEvents {
		private Bundle	bundle;

		@BeforeEach
		void createBundle() throws IOException, BundleException {

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

		@Test
		@DisplayName("Wait for a noisy bundle to finish doing things (Overall)")
		public void testNoisyBundleOverall() throws Exception {
			makeNoise(10, 50, bundleNoise(bundle));

			List<TimedEvent<EventObject>> events = ac.waitForQuiet(ofMillis(500), ofSeconds(2));
			assertThat(events.size()).isEqualTo(40);
			assertThat(events).isSorted();
			assertThat(events).allMatch(e -> e.event() instanceof BundleEvent);
		}

		@Test
		@DisplayName("Wait for a noisy bundle to finish doing things (Bundle)")
		public void testNoisyBundleBundle() throws Exception {
			makeNoise(10, 50, bundleNoise(bundle));

			List<TimedEvent<BundleEvent>> events = ac.waitForBundleQuiet(ofMillis(500), ofSeconds(2));
			assertThat(events.size()).isEqualTo(40);
			assertThat(events).isSorted();
		}

		@Test
		@DisplayName("Time out while a noisy bundle does its work")
		public void testNoisyBundleTimeout() throws Exception {
			makeNoise(6, 200, bundleNoise(bundle));

			AwaitCalmTimeoutException exception = assertThrows(AwaitCalmTimeoutException.class,
				() -> ac.waitForQuiet(ofMillis(480), ofSeconds(1)));
			List<TimedEvent<EventObject>> events = exception.getEvents();
			// Technically any event from the third iteration should trigger the
			// timer, but on slow CI machines it may be between 9 and 12
			assertThat(events.size()).isBetween(9, 12);
			assertThat(events).isSorted();
			assertThat(events).allMatch(e -> e.event() instanceof BundleEvent);
		}

		@Test
		@DisplayName("Service Events aren't blocked by a noisy bundle")
		public void testNoisyBundleNoServiceTimeout() throws Exception {
			makeNoise(15, 50, bundleNoise(bundle));

			List<TimedEvent<ServiceEvent>> events = ac.waitForServiceQuiet(ofMillis(500), ofSeconds(1));
			assertThat(events).isEmpty();
		}
	}

	@Nested
	class ServiceEvents {

		@Test
		@DisplayName("Wait for a noisy service to finish doing things (Overall)")
		public void testNoisyServiceOverall() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(10, 50, serviceNoise(reg));

			List<TimedEvent<EventObject>> events = ac.waitForQuiet(ofMillis(500), ofSeconds(2));
			assertThat(events.size()).isEqualTo(10);
			assertThat(events).isSorted();
			assertThat(events).allMatch(e -> e.event() instanceof ServiceEvent);
		}

		@Test
		@DisplayName("Wait for a noisy service to finish doing things (Service)")
		public void testNoisyServiceService() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(10, 50, serviceNoise(reg));

			List<TimedEvent<ServiceEvent>> events = ac.waitForServiceQuiet(ofMillis(500), ofSeconds(2));
			assertThat(events.size()).isEqualTo(10);
			assertThat(events).isSorted();
		}

		@Test
		@DisplayName("Time out while a noisy service does its work")
		public void testNoisyServiceTimeout() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(10, 150, serviceNoise(reg));

			AwaitCalmTimeoutException exception = assertThrows(AwaitCalmTimeoutException.class,
				() -> ac.waitForQuiet(ofMillis(490), ofSeconds(1)));
			List<TimedEvent<EventObject>> events = exception.getEvents();
			assertThat(events.size()).isEqualTo(4);
			assertThat(events).isSorted();
			assertThat(events).allMatch(e -> e.event() instanceof ServiceEvent);
		}

		@Test
		@DisplayName("Framework Events aren't blocked by a noisy service")
		public void testNoisyServiceNoFrameworkTimeout() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(15, 50, serviceNoise(reg));

			List<TimedEvent<BundleEvent>> events = ac.waitForBundleQuiet(ofMillis(500), ofSeconds(1));
			assertThat(events).isEmpty();
		}

		@Test
		@DisplayName("Service events should honour the filter")
		public void testFilteredNoisyService() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(15, 50, serviceNoise(reg));

			List<TimedEvent<ServiceEvent>> events = ac.waitForServiceQuiet(ofMillis(500), ofSeconds(1), "(count<=5)");
			assertThat(events.size()).isEqualTo(7);
			assertThat(events).isSorted();
		}
	}

	@Nested
	class MixedEvents {
		private Bundle bundle;

		@BeforeEach
		void createBundle() throws IOException, BundleException {

			Manifest manifest = new Manifest();
			manifest.getMainAttributes()
				.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
			manifest.getMainAttributes()
				.putValue(Constants.BUNDLE_SYMBOLICNAME, "testBundleMixed");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (JarOutputStream jos = new JarOutputStream(baos, manifest)) {}

			bundle = ctx.installBundle("test", new ByteArrayInputStream(baos.toByteArray()));
			bundle.start();
		}

		@Test
		@DisplayName("Wait for a noisy bundle and service to finish doing things (Overall)")
		public void testNoisyServiceAndBundleOverall() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(10, 50, bundleNoise(bundle));
			makeNoise(10, 50, serviceNoise(reg));

			List<TimedEvent<EventObject>> events = ac.waitForQuiet(ofMillis(500), ofSeconds(2));
			assertThat(events.size()).isEqualTo(50);
			assertThat(events).isSorted();
			assertThat(events).areExactly(40,
				new Condition<>(te -> te.event() instanceof BundleEvent, "a Timed BundleEvent"));
			assertThat(events).areExactly(10,
				new Condition<>(te -> te.event() instanceof ServiceEvent, "a Timed ServiceEvent"));
		}

		@Test
		@DisplayName("Time out while a noisy bundle and service do their work")
		public void testNoisyServiceAndBundleTimeout() throws Exception {
			ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, ac, new Hashtable<>());
			makeNoise(8, 50, bundleNoise(bundle));
			makeNoise(2, 600, serviceNoise(reg));

			AwaitCalmTimeoutException exception = assertThrows(AwaitCalmTimeoutException.class,
				() -> ac.waitForQuiet(ofMillis(480), ofSeconds(1)));
			List<TimedEvent<EventObject>> events = exception.getEvents();
			assertThat(events.size()).isEqualTo(33);
			assertThat(events).isSorted();
		}
	}
}
