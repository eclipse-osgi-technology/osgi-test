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

package org.osgi.test.assertj.bundlecontext;

import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.osgi.test.assertj.bundle.BundleAssert.BUNDLE;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ListAssert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.assertj.bundlereference.AbstractBundleReferenceAssert;

public abstract class AbstractBundleContextAssert<SELF extends AbstractBundleContextAssert<SELF, ACTUAL>, ACTUAL extends BundleContext>
	extends AbstractBundleReferenceAssert<SELF, ACTUAL> {

	protected AbstractBundleContextAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public SELF hasProperty(String key) {
		isNotNull();
		if (actual.getProperty(key) == null) {
			throw failure("%nExpecting%n <%s>%nto have property:%n <%s>%n but it did not", actual, key);
		}
		return myself;
	}

	public AbstractStringAssert<?> hasPropertyWithKeyThat(String key) {
		return isNotNull().extracting(bundleContext -> bundleContext.getProperty(key),
			STRING)
			.as(actual + ".property(" + key + ")");
	}

	public SELF doesNotHaveProperty(String key) {
		isNotNull();
		String value = actual.getProperty(key);
		if (value != null) {
			throw failure("%nExpecting%n  <%s>%nto not have property:%n  <%s>%n but it did:%n  <%s>", actual, key,
				value);
		}
		return myself;
	}

	public SELF hasBundleWithId(long id) {
		isNotNull();
		if (actual.getBundle(id) == null) {
			throw failure("%nExpecting%n <%s>%nto have bundle with id:%n <%d>%n but it did not", actual, id);
		}
		return myself;
	}

	public BundleAssert hasBundleWithIdThat(long id) {
		return isNotNull().extracting(bundleContext -> bundleContext.getBundle(id), BUNDLE)
			.as(actual + ".bundle(" + id + ")");
	}

	public SELF doesNotHaveBundleWithId(long id) {
		isNotNull();
		Bundle value = actual.getBundle(id);
		if (value != null) {
			throw failure("%nExpecting%n  <%s>%nto not have bundle with id:%n  <%d>%n but it did:%n  <%s>", actual,
				id, value);
		}
		return myself;
	}

	public SELF hasBundleWithLocation(String location) {
		isNotNull();
		if (actual.getBundle(location) == null) {
			throw failure("%nExpecting%n <%s>%nto have bundle with location:%n <%s>%n but it did not", actual,
				location);
		}
		return myself;
	}

	public BundleAssert hasBundleWithLocationThat(String location) {
		return isNotNull().extracting(bundleContext -> bundleContext.getBundle(location), BUNDLE)
			.as(actual + ".bundle(" + location + ")");
	}

	public SELF doesNotHaveBundleWithLocation(String location) {
		isNotNull();
		Bundle value = actual.getBundle(location);
		if (value != null) {
			throw failure("%nExpecting%n  <%s>%nto not have bundle with location:%n  <%s>%n but it did:%n  <%s>",
				actual, location, value);
		}
		return myself;
	}

	@SuppressWarnings("rawtypes")
	static InstanceOfAssertFactory<List, ListAssert<Bundle>> BUNDLE_LIST = InstanceOfAssertFactories.list(Bundle.class);

	public ListAssert<Bundle> hasBundlesThat() {
		return isNotNull().extracting(bundleContext -> Arrays.asList(bundleContext.getBundles()), BUNDLE_LIST)
			.as(actual + ".bundles");
	}

}
