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

package org.osgi.test.assertj.bundlestartleveldto;

import org.assertj.core.api.AbstractAssert;
import org.osgi.framework.startlevel.dto.BundleStartLevelDTO;

public abstract class AbstractBundleStartLevelDTOAssert<SELF extends AbstractBundleStartLevelDTOAssert<SELF, ACTUAL>, ACTUAL extends BundleStartLevelDTO>
	extends AbstractAssert<SELF, ACTUAL> {

	protected AbstractBundleStartLevelDTOAssert(ACTUAL actual, Class<SELF> selfType) {
		super(actual, selfType);
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

	public SELF hasStartLevel(int expected) {
		isNotNull();
		if (actual.startLevel != expected) {
			throw failureWithActualExpected(actual.startLevel, expected,
				"%nExpecting%n  <%s>%nto have start level:%n  <%d>%n but was:%n  <%d>", actual, expected,
				actual.startLevel);
		}
		return myself;
	}

	public SELF isActivationPolicyUsed() {
		isNotNull();
		if (!actual.activationPolicyUsed) {
			throw failure("%nExpecting%n  <%s>%nto have activation policy used%n but it was not", actual);
		}
		return myself;
	}

	public SELF isNotActivationPolicyUsed() {
		isNotNull();
		if (actual.activationPolicyUsed) {
			throw failure("%nExpecting%n  <%s>%nto not have activation policy used%n but it was", actual);
		}
		return myself;
	}

	public SELF isPersistentlyStarted() {
		isNotNull();
		if (!actual.persistentlyStarted) {
			throw failure("%nExpecting%n  <%s>%nto be persistently started%n but it was not", actual);
		}
		return myself;
	}

	public SELF isNotPersistentlyStarted() {
		isNotNull();
		if (actual.persistentlyStarted) {
			throw failure("%nExpecting%n  <%s>%nto not be persistently started%n but it was", actual);
		}
		return myself;
	}
}
