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

package org.osgi.test.assertj.bundle;

import java.util.Objects;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.condition.VerboseCondition;
import org.osgi.framework.Bundle;

/**
 * A Utility-Class thats Provides public static methods to create
 * {@link Condition}s for an {@link Bundle}
 *
 * @since 1.1
 */
public final class BundleConditions {
	/**
	 * Creates a {@link Condition} to be met by an {@link Bundle}. Checking if a
	 * {@link Bundle} is <b>equal</b> to an other Bundle.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * List<Bundle> bundles = null;
	 *
	 * public static void isEqualsTo(Bundle bundle) {
	 *
	 * 	assertThat(bundles)// created an {@link ListAssert}
	 * 		.have(isEqualsTo(bundle))
	 * 		.filteredOn(isEqualsTo(bundle))
	 * 		.first()// map to {@link ObjectAssert}
	 * 		.is(isEqualsTo(bundle));// used on {@link ObjectAssert}
	 * }
	 * </pre>
	 *
	 * @param bundle - the expected bundle that would be checked against other
	 *            {@link Bundle}
	 * @return the Condition<br>
	 */
	public static Condition<Bundle> isEqualsTo(Bundle bundle) {
		Condition<Bundle> c = VerboseCondition.verboseCondition((b) -> Objects.equals(b, bundle), "bundle equals",
			(b) -> " was <" + b.toString() + ">");
		return c;
	}
}
