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

import static org.assertj.core.api.InstanceOfAssertFactories.LONG;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.test.assertj.version.VersionAssert.VERSION;
import static org.osgi.test.common.bitmaps.BundleState.BITMAP;

import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractDateAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.test.assertj.version.VersionAssert;

public abstract class AbstractBundleAssert<SELF extends AbstractBundleAssert<SELF, ACTUAL>, ACTUAL extends Bundle>
	extends AbstractAssert<SELF, ACTUAL> {
	protected AbstractBundleAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
	}

	public SELF isFragment() {
		isNotNull();
		if ((actual.adapt(BundleRevision.class)
			.getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
			throw failure("%nExpecting%n  <%s>%nto be a fragment, but it was not", actual);
		}
		return myself;
	}

	public SELF isNotFragment() {
		isNotNull();
		if ((actual.adapt(BundleRevision.class)
			.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			String host = actual.getHeaders()
				.get("Fragment-Host");
			throw failure("%nExpecting%n  <%s>%nto not be a fragment, but it was, with host:%n  <%s>", actual, host);
		}
		return myself;
	}

	public SELF hasPermission(Object permission) {
		isNotNull();
		if (!actual.hasPermission(permission)) {
			throw failure("%nExpecting%n  <%s>%nto have permission%n  <%s>%nbut it did not", actual, permission);
		}
		return myself;
	}

	public SELF doesNotHavePermission(Object permission) {
		isNotNull();
		if (actual.hasPermission(permission)) {
			throw failure("%nExpecting%n  <%s>%nnot to have permission%n  <%s>%nbut it did", actual, permission);
		}
		return myself;
	}

	public SELF hasSymbolicName(String expected) {
		isNotNull();
		if (!Objects.equals(actual.getSymbolicName(), expected)) {
			throw failureWithActualExpected(actual.getSymbolicName(), expected,
				"%nExpecting%n  <%s>%nto have symbolic name:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getSymbolicName());
		}
		return myself;
	}

	public AbstractStringAssert<?> hasSymbolicNameThat() {
		return isNotNull().extracting(Bundle::getSymbolicName, STRING)
			.as(actual + ".symbolicName");
	}

	public SELF hasLocation(String expected) {
		isNotNull();
		if (!Objects.equals(actual.getLocation(), expected)) {
			throw failureWithActualExpected(actual.getLocation(), expected,
				"%nExpecting%n  <%s>%nto have location:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getLocation());
		}
		return myself;
	}

	public AbstractStringAssert<?> hasLocationThat() {
		return isNotNull().extracting(Bundle::getLocation, STRING)
			.as(actual + ".location");
	}

	public SELF hasEntry(String entry) {
		isNotNull();
		if (actual.getEntry(entry) == null) {
			throw failure("%nExpecting%n <%s>%nto have entry:%n <%s>%n but it did not", actual, entry);
		}
		return myself;
	}

	public SELF doesNotHaveEntry(String resource) {
		isNotNull();
		URL url = actual.getEntry(resource);
		if (url != null) {
			throw failure("%nExpecting%n  <%s>%nto not have entry:%n  <%s>%n but it did:%n  <%s>", actual, resource,
				url);
		}
		return myself;
	}

	public SELF hasResource(String resource) {
		isNotNull();
		if (actual.getResource(resource) == null) {
			throw failure("%nExpecting%n  <%s>%nto have resource:%n  <%s>%n but it did not", actual, resource);
		}
		return myself;
	}

	public SELF doesNotHaveResource(String resource) {
		isNotNull();
		URL url = actual.getResource(resource);
		if (url != null) {
			throw failure("%nExpecting%n  <%s>%nto not have resource:%n  <%s>%n but it did:%n  <%s>", actual,
				resource, url);
		}
		return myself;
	}

	public SELF hasBundleId(long expected) {
		isNotNull();
		if (actual.getBundleId() != expected) {
			throw failureWithActualExpected(actual.getBundleId(), expected,
				"%nExpecting%n  <%s>%nto have bundle ID:%n  <%d>%n but was:%n  <%d>", actual, expected,
				actual.getBundleId());
		}
		return myself;
	}

	private static Version getVersion(Object version) {
		if (version == null) {
			return null;
		} else if (version instanceof Version) {
			return (Version) version;
		} else if (version instanceof CharSequence) {
			return Version.valueOf(version.toString());
		} else {
			throw new IllegalArgumentException(
				"expected should be an instance of CharSequence or Version; was " + version.getClass()
					.getName());
		}
	}

	public VersionAssert hasVersionThat() {
		return isNotNull().extracting(Bundle::getVersion, VERSION)
			.as(actual + ".version");
	}

	public SELF hasVersion(Object expected) {
		isNotNull();
		Version expectedVersion = getVersion(expected);
		if (!Objects.equals(actual.getVersion(), expectedVersion)) {
			throw failureWithActualExpected(actual.getVersion(), expected,
				"%nExpecting%n  <%s>%nto have version:%n  <%s>%n but was:%n  <%s>", actual, expected,
				actual.getVersion());
		}
		return myself;
	}

	public SELF hasLastModified(Object expected) {
		isNotNull();
		long expectedTime = getTime(expected);
		if (actual.getLastModified() != expectedTime) {
			throw failure("%nExpecting%n <%s>%nto have last modified time:%n  <%d>%n but it was:%n  <%d>", actual,
				expectedTime, actual.getLastModified());
		}
		return myself;
	}

	public AbstractLongAssert<?> hasLastModifiedLongThat() {
		return isNotNull().extracting(Bundle::getLastModified, LONG)
			.as(actual + ".lastModified");
	}

	static final InstanceOfAssertFactory<Long, AbstractDateAssert<?>> LONG_AS_DATE = new InstanceOfAssertFactory<>(
		Long.class, date -> Assertions.assertThat(new Date(date)));

	public AbstractDateAssert<?> hasLastModifiedDateThat() {
		return isNotNull().extracting(Bundle::getLastModified, LONG_AS_DATE)
			.as(actual + ".lastModified");
	}

	private static long getTime(Object expected) {
		if (expected == null) {
			throw new IllegalArgumentException("expected cannot be null");
		} else if (expected instanceof Long) {
			return (Long) expected;
		} else if (expected instanceof Date) {
			return ((Date) expected).getTime();
		} else if (expected instanceof Instant) {
			return ((Instant) expected).toEpochMilli();
		} else {
			throw new IllegalArgumentException("Expected must be a long, Date or Instant");
		}
	}

	public SELF isInState(int expected) {
		isNotNull();
		if ((expected & (expected - 1)) != 0) {
			throw new IllegalArgumentException(
				"Multiple bits set in expected (" + expected + ") - do you mean to use isInStateMaskedBy()?");
		}
		final String expectedString = BITMAP.toString(expected);
		if ((actual.getState() & expected) == 0) {
			throw failure("%nExpecting%n  <%s>%nto be in state:%n  <%d:%s>%n but was in state:%n  <%s>", actual,
				expected, expectedString, BITMAP.maskToString(actual.getState()));
		}
		return myself;
	}

	public SELF isNotInState(int expected) {
		isNotNull();
		final String expectedState = BITMAP.toString(expected);
		if ((actual.getState() & expected) != 0) {
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
		if ((actual.getState() & mask) == 0) {
			final String states = BITMAP.maskToString(mask);
			throw failure("%nExpecting%n  <%s>%nto be in one of states:%n  [%s]%n but was in state:%n  <%s>", actual,
				states, BITMAP.maskToString(actual.getState()));
		}
		return myself;
	}

	public SELF isNotInStateMask(int mask) {
		isNotNull();
		if (mask <= 0 || mask >= ACTIVE * 2) {
			throw new IllegalArgumentException("Mask testing for an illegal state: " + mask);
		}
		if ((actual.getState() & mask) != 0) {
			final String states = BITMAP.maskToString(mask);
			throw failure("%nExpecting%n  <%s>%nto not be in one of states:%n  [%s]%n but was in state:%n  <%s>",
				actual, states, BITMAP.maskToString(actual.getState()));
		}
		return myself;
	}
}
