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

package org.osgi.test.assertj.test.bundleevent;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.test.assertj.bundleevent.BundleEventPredicates;

public class BundleEventPredicatesTest {

	Bundle bundle = Mockito.mock(Bundle.class);

	@Test
	void test_type() throws Exception {

		SoftAssertions softly = new SoftAssertions();

		//

		//
		softly.assertThat(BundleEventPredicates.type(BundleEvent.INSTALLED)
			.test(new BundleEvent(BundleEvent.INSTALLED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.type(BundleEvent.INSTALLED)
			.test(new BundleEvent(BundleEvent.UNINSTALLED, bundle)))
			.isFalse();

		softly.assertThat(BundleEventPredicates.bundleEvent()
			.test(new BundleEvent(BundleEvent.UPDATED, bundle)))
			.isTrue();

		softly.assertThat(BundleEventPredicates.bundleEvent()
			.test(""))
			.isFalse();

		softly.assertAll();
	}

	@Test
	void test_bundleEvent() throws Exception {
		assertThat(BundleEventPredicates.bundleEvent()
			.test(new BundleEvent(BundleEvent.UPDATED, bundle))).isTrue();

		assertThat(BundleEventPredicates.bundleEvent()
			.test("")).isFalse();

	}

}
