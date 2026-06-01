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

package org.osgi.test.junit5.test.await;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.function.IntConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectAwaitCalm;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.await.AwaitCalm;
import org.osgi.test.common.await.AwaitCalm.TimedEvent;

public class AwaitCalmTest {

	@InjectAwaitCalm
	AwaitCalm awaitCalmField;

	private List<Thread> noisyThreads = new ArrayList<>();

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

	@AfterEach
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

	private IntConsumer serviceNoise(ServiceRegistration<?> reg) {
		return count -> reg.setProperties(new Hashtable<String, Object>() {
			{
				put("count", count);
			}
		});
	}

	@Test
	public void testFieldWithNothingHappening() throws Exception {
		List<TimedEvent<EventObject>> events = assertDoesNotThrow(
			() -> awaitCalmField.waitForQuiet(ofMillis(500), ofSeconds(1)));
		assertThat(events).isEmpty();
	}

	@Test
	public void testParameterWithNothingHappening(@InjectAwaitCalm
	AwaitCalm awaitCalmParameter) throws Exception {
		List<TimedEvent<EventObject>> events = assertDoesNotThrow(
			() -> awaitCalmParameter.waitForQuiet(ofMillis(500), ofSeconds(1)));
		assertThat(events).isEmpty();
	}

	@Test
	public void testFieldWithEvents(@InjectBundleContext
	BundleContext ctx) throws Exception {
		ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, awaitCalmField, new Hashtable<>());
		makeNoise(5, 50, serviceNoise(reg));
		List<TimedEvent<EventObject>> events = assertDoesNotThrow(
			() -> awaitCalmField.waitForQuiet(ofMillis(500), ofSeconds(1)));
		assertThat(events.size()).isEqualTo(5);
		assertThat(events).isSorted();
	}

	@Test
	public void testParameterWithEvents(@InjectAwaitCalm
	AwaitCalm awaitCalmParameter, @InjectBundleContext
	BundleContext ctx) throws Exception {
		ServiceRegistration<?> reg = ctx.registerService(AwaitCalm.class, awaitCalmParameter, new Hashtable<>());
		makeNoise(5, 50, serviceNoise(reg));
		List<TimedEvent<EventObject>> events = assertDoesNotThrow(
			() -> awaitCalmParameter.waitForQuiet(ofMillis(500), ofSeconds(1)));
		assertThat(events.size()).isEqualTo(5);
		assertThat(events).isSorted();
	}
}
