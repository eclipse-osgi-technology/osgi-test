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

package org.osgi.test.assertj.servicereferencedto;

import java.util.Arrays;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.MapAssert;
import org.osgi.framework.dto.ServiceReferenceDTO;

public abstract class AbstractServiceReferenceDTOAssert<SELF extends AbstractServiceReferenceDTOAssert<SELF, ACTUAL>, ACTUAL extends ServiceReferenceDTO>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractServiceReferenceDTOAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF hasServiceId(long expected) {
		isNotNull();
		if (actual.id != expected) {
			throw failureWithActualExpected(actual.id, expected,
				"%nExpecting%n  <%s>%nto have service ID:%n  <%d>%n but was:%n  <%d>", actual, expected,
				actual.id);
		}
		return myself;
	}

	public SELF hasBundle(long expected) {
		isNotNull();
		if (actual.bundle != expected) {
			throw failureWithActualExpected(actual.bundle, expected,
				"%nExpecting%n  <%s>%nto have bundle:%n  <%d>%n but was:%n  <%d>", actual, expected,
				actual.bundle);
		}
		return myself;
	}

	public MapAssert<String, Object> hasServicePropertiesThat() {
		isNotNull();
		return Assertions.assertThat(actual.properties)
			.as(actual + ".properties");
	}

	public SELF hasUsingBundles(long... expected) {
		isNotNull();
		if (actual.usingBundles == null && expected != null) {
			throw failure("%nExpecting%n  <%s>%nto have using bundles:%n  <%s>%n but usingBundles was null", actual,
				Arrays.toString(expected));
		}
		if (actual.usingBundles != null && expected == null) {
			throw failure(
				"%nExpecting%n  <%s>%nto have null using bundles%n but was:%n  <%s>", actual,
				Arrays.toString(actual.usingBundles));
		}
		if (actual.usingBundles != null && expected != null) {
			long[] sortedActual = actual.usingBundles.clone();
			long[] sortedExpected = expected.clone();
			Arrays.sort(sortedActual);
			Arrays.sort(sortedExpected);
			if (!Arrays.equals(sortedActual, sortedExpected)) {
				throw failureWithActualExpected(Arrays.toString(actual.usingBundles), Arrays.toString(expected),
					"%nExpecting%n  <%s>%nto have using bundles:%n  <%s>%n but was:%n  <%s>", actual,
					Arrays.toString(expected), Arrays.toString(actual.usingBundles));
			}
		}
		return myself;
	}

	public SELF isBeingUsedBy(long... bundleIds) {
		isNotNull();
		if (actual.usingBundles == null) {
			throw failure("%nExpecting%n  <%s>%nto be used by bundles:%n  <%s>%n but usingBundles was null", actual,
				Arrays.toString(bundleIds));
		}
		for (long id : bundleIds) {
			boolean found = false;
			for (long ub : actual.usingBundles) {
				if (ub == id) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw failure(
					"%nExpecting%n  <%s>%nto be used by bundle:%n  <%d>%n but usingBundles was:%n  <%s>", actual,
					id, Arrays.toString(actual.usingBundles));
			}
		}
		return myself;
	}
}
