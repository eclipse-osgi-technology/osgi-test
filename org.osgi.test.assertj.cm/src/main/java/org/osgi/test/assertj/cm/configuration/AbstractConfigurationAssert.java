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

package org.osgi.test.assertj.cm.configuration;

import java.util.Objects;

import org.assertj.core.api.AbstractObjectAssert;
import org.osgi.service.cm.Configuration;
import org.osgi.test.assertj.dictionary.DictionaryAssert;

/**
 * Abstract base class for {@link Configuration} specific assertions
 */
public abstract class AbstractConfigurationAssert<S extends AbstractConfigurationAssert<S, A>, A extends Configuration>
	extends AbstractObjectAssert<S, A> {

	/**
	 * Creates a new <code>{@link AbstractConfigurationAssert}</code> to make
	 * assertions on actual Configuration.
	 *
	 * @param actual the Configuration we want to make assertions on.
	 */
	protected AbstractConfigurationAssert(A actual, Class<S> selfType) {
		super(actual, selfType);
	}


	/**
	 * Verifies that the actual Configuration's bundleLocation is equal to the
	 * given one.
	 *
	 * @param bundleLocation the given bundleLocation to compare the actual
	 *            Configuration's bundleLocation to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's bundleLocation is
	 *             not equal to the given one.
	 */
	public S hasBundleLocation(String bundleLocation) {
		// check that actual Configuration we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting bundleLocation of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// null safe check
		String actualBundleLocation = actual.getBundleLocation();
		if (!Objects.equals(actualBundleLocation, bundleLocation)) {
			failWithMessage(assertjErrorMessage, actual, bundleLocation, actualBundleLocation);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's changeCount is equal to the
	 * given one.
	 *
	 * @param changeCount the given changeCount to compare the actual
	 *            Configuration's changeCount to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's changeCount is not
	 *             equal to the given one.
	 */
	public S hasChangeCount(long changeCount) {
		// check that actual Configuration we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting changeCount of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// check
		long actualChangeCount = actual.getChangeCount();
		if (actualChangeCount != changeCount) {
			failWithMessage(assertjErrorMessage, actual, changeCount, actualChangeCount);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's changeCount is greater then the
	 * given one.
	 *
	 * @param changeCount the given changeCount to compare the actual
	 *            Configuration's changeCount to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's changeCount is not
	 *             greaterThen the given one.
	 */
	public S hasChangeCountGreater(long changeCount) {
		// check that actual Configuration we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting changeCount of:\n  <%s>\nto be greater then:\n  <%s>\nbut was:\n  <%s>";

		// check
		long actualChangeCount = actual.getChangeCount();
		if (actualChangeCount <= changeCount) {
			failWithMessage(assertjErrorMessage, actual, changeCount, actualChangeCount);
		}

		// return the current assertion for method chaining
		return myself;
	}


	/**
	 * Delegates to a DictionaryAssert using the properties of the
	 * Configuration.
	 *
	 * @throws AssertionError - if the actual Configuration's properties is
	 *             null.
	 * @return the dictionary assert
	 */
	public DictionaryAssert<String, Object> hasProperiesThat() {
		isNotNull();
		return DictionaryAssert.assertThat(actual.getProperties())
			.as(actual + ".properties");
	}

	/**
	 * Verifies that the actual Configuration's factoryPid is equal to the given
	 * one.
	 *
	 * @param factoryPid the given factoryPid to compare the actual
	 *            Configuration's factoryPid to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's factoryPid is not
	 *             equal to the given one.
	 */
	public S hasFactoryPidEqualsTo(String factoryPid) {

		isNotNull().has(ConfigurationConditions.factoryPid(factoryPid));
		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's factoryPid is not null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's factoryPid is
	 *             null.
	 */
	public S hasFactoryPid() {

		isNotNull().has(ConfigurationConditions.factoryPidNotNull());
		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's factoryPid is null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's factoryPid is not
	 *             null.
	 */
	public S hasNoFactoryPid() {

		isNotNull().has(ConfigurationConditions.factoryPidNull());
		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's pid is not null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's pid is null
	 */
	public S hasPid() {

		isNotNull().has(ConfigurationConditions.pidNotNull());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's pid is null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's pid is not null.
	 */
	public S hasNoPid() {

		isNotNull().has(ConfigurationConditions.pidNull());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual Configuration's pid is equal to the given one.
	 *
	 * @param pid the given pid to compare the actual Configuration's pid to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual Configuration's pid is not equal
	 *             to the given one.
	 */
	public S hasPidEqualsTo(String pid) {

		isNotNull().has(ConfigurationConditions.pid(pid));

		// return the current assertion for method chaining
		return myself;
	}

}
