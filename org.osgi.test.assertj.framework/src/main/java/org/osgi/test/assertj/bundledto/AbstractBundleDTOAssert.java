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

package org.osgi.test.assertj.bundledto;

import static org.assertj.core.api.InstanceOfAssertFactories.LONG;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.test.assertj.date.Dates.LONG_AS_DATE;
import static org.osgi.test.common.bitmaps.BundleState.BITMAP;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractDateAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.osgi.framework.dto.BundleDTO;

public abstract class AbstractBundleDTOAssert<SELF extends AbstractBundleDTOAssert<SELF, ACTUAL>, ACTUAL extends BundleDTO>
	extends AbstractAssert<SELF, ACTUAL> {
	protected AbstractBundleDTOAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF hasBundleId(long expected) {
		isNotNull();
		if (actual.id != expected) {
			throw failureWithActualExpected(actual.id, expected,
				"%nExpecting%n  <%s>%nto have bundle ID:%n  <%d>%n but was:%n  <%d>", actual, expected,
				actual.id);
		}
		return myself;
	}

	public SELF hasSymbolicName(String expected) {
		isNotNull();
		if (!Objects.equals(actual.symbolicName, expected)) {
			throw failureWithActualExpected(actual.symbolicName, expected,
				"%nExpecting%n  <%s>%nto have symbolic name:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.symbolicName);
		}
		return myself;
	}

	public AbstractStringAssert<?> hasSymbolicNameThat() {
		return isNotNull().extracting(d -> d.symbolicName, STRING)
			.as(actual + ".symbolicName");
	}

	public SELF hasVersion(String expected) {
		isNotNull();
		if (!Objects.equals(actual.version, expected)) {
			throw failureWithActualExpected(actual.version, expected,
				"%nExpecting%n  <%s>%nto have version:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.version);
		}
		return myself;
	}

	public AbstractStringAssert<?> hasVersionThat() {
		return isNotNull().extracting(d -> d.version, STRING)
			.as(actual + ".version");
	}

	public SELF hasLastModified(long expected) {
		isNotNull();
		if (actual.lastModified != expected) {
			throw failure("%nExpecting%n <%s>%nto have last modified time:%n  <%d>%n but it was:%n  <%d>", actual,
				expected, actual.lastModified);
		}
		return myself;
	}

	public AbstractLongAssert<?> hasLastModifiedLongThat() {
		return isNotNull().extracting(d -> d.lastModified, LONG)
			.as(actual + ".lastModified");
	}

	public AbstractDateAssert<?> hasLastModifiedDateThat() {
		return isNotNull().extracting(d -> d.lastModified, LONG_AS_DATE)
			.as(actual + ".lastModified");
	}

	public SELF isInState(int expected) {
		isNotNull();
		if ((expected & (expected - 1)) != 0) {
			throw new IllegalArgumentException(
				"Multiple bits set in expected (" + expected + ") - do you mean to use isInStateMaskedBy()?");
		}
		final String expectedString = BITMAP.toString(expected);
		if ((actual.state & expected) == 0) {
			throw failure("%nExpecting%n  <%s>%nto be in state:%n  <%d:%s>%n but was in state:%n  <%s>", actual,
				expected, expectedString, BITMAP.maskToString(actual.state));
		}
		return myself;
	}

	public SELF isNotInState(int expected) {
		isNotNull();
		final String expectedState = BITMAP.toString(expected);
		if ((actual.state & expected) != 0) {
			throw failure("%nExpecting%n  <%s>%nnot to be in state:%n  <%d:%s>%n but it was", actual, expected,
				expectedState);
		}
		return myself;
	}

	public SELF isInStateMaskedBy(int mask) {
		isNotNull();
		if (mask <= 0 || mask >= ACTIVE << 1) {
			throw new IllegalArgumentException("Mask testing for an illegal state: " + mask);
		}
		if ((actual.state & mask) == 0) {
			final String states = BITMAP.maskToString(mask);
			throw failure("%nExpecting%n  <%s>%nto be in one of states:%n  [%s]%n but was in state:%n  <%s>", actual,
				states, BITMAP.maskToString(actual.state));
		}
		return myself;
	}

	public SELF isNotInStateMask(int mask) {
		isNotNull();
		if (mask <= 0 || mask >= ACTIVE * 2) {
			throw new IllegalArgumentException("Mask testing for an illegal state: " + mask);
		}
		if ((actual.state & mask) != 0) {
			final String states = BITMAP.maskToString(mask);
			throw failure("%nExpecting%n  <%s>%nto not be in one of states:%n  [%s]%n but was in state:%n  <%s>",
				actual, states, BITMAP.maskToString(actual.state));
		}
		return myself;
	}
}
