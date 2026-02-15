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

package org.osgi.test.assertj.test.frameworkevent;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.test.assertj.frameworkevent.FrameworkEventPredicates;

public class FrameworkEventPredicatesTest {

	Bundle bundle = Mockito.mock(Bundle.class);

	@Test
	void testPredicateFramework() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//
		FrameworkEvent event = new FrameworkEvent(FrameworkEvent.STARTLEVEL_CHANGED, bundle, null);

		//
		softly.assertThat(FrameworkEventPredicates.type(FrameworkEvent.STARTLEVEL_CHANGED)
			.test(event))
			.isTrue();

		softly.assertThat(FrameworkEventPredicates.type(FrameworkEvent.STARTED)
			.test(event))
			.isFalse();

		softly.assertAll();
	}

	@Test
	void test_frameworkEvent() throws Exception {
		assertThat(FrameworkEventPredicates.frameworkEvent()
			.test(new FrameworkEvent(FrameworkEvent.WAIT_TIMEDOUT, bundle, null))).isTrue();

		assertThat(FrameworkEventPredicates.frameworkEvent()
			.test("")).isFalse();

	}

}
