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

package org.osgi.test.example.player.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Dictionary;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.example.api.Ball;
import org.osgi.test.example.api.Player;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class PlayerTest {

	static Ball b;

	@InjectBundleContext
	BundleContext bc;

	public PlayerTest() {
		System.out.println();
	}

	@BeforeAll
	static void beforeAll(@InjectBundleContext BundleContext staticBC) {
		b = mock(Ball.class);
		Dictionary<String, Object> props = Dictionaries.dictionaryOf("test", "testball");
		staticBC.registerService(Ball.class, b, props);

	}

	@InjectService
	Player p;

	@Test
	void myTest() {
		assertThat(p).isNotNull();
		assertThat(p.getBall()).isSameAs(b);
		verifyNoInteractions(b);
		p.kickBall();
		verify(b).kick();
	}

	static class DummyBall implements Ball {

		@Override
		public void inflate() {
		}

		@Override
		public void kick() {
		}
	}

	@Test
	void myServiceAwareTest(@InjectService(cardinality = 0) ServiceAware<Ball> ball) {
		assertThat(ball.getServices()).hasSize(1);
		DictionaryAssert.assertThat(ball.getServiceReference().getProperties()).containsEntry("test", "testball");
		bc.registerService(Ball.class, new DummyBall(), null);
		assertThat(ball.getServices()).hasSize(2);
		bc.registerService(Ball.class, new DummyBall(), null);
		assertThat(ball.getServices()).hasSize(3);
	}

	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class TwoBalls {
		Ball b2;

		@BeforeAll
		void beforeAll(@InjectBundleContext BundleContext bc) {
			b2 = mock(Ball.class);
			bc.registerService(Ball.class, b2, null);
		}

		@Test
		void twoBallTest(@InjectService(cardinality = 2) List<Ball> services) {
			assertThat(services).containsExactlyInAnyOrder(b, b2);
		}
	}

	// This test should fail - proves that the second ball registered
	// in the nested test gets cleaned up.
//	@Test
//	void myDoubleTest(@InjectService(cardinality = 2) List<Ball> balls) {
//	}
}
