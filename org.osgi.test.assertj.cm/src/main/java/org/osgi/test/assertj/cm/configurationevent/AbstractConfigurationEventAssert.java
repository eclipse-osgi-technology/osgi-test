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

package org.osgi.test.assertj.cm.configurationevent;

import static org.assertj.core.api.Assertions.not;

import org.assertj.core.api.AbstractObjectAssert;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.test.assertj.servicereference.ServiceReferenceAssert;

/**
 * Abstract base class for {@link ConfigurationEvent} specific assertions
 */
public abstract class AbstractConfigurationEventAssert<S extends AbstractConfigurationEventAssert<S, A>, A extends ConfigurationEvent>
	extends AbstractObjectAssert<S, A> {

	/**
	 * Creates a new <code>{@link AbstractConfigurationEventAssert}</code> to
	 * make assertions on actual ConfigurationEvent.
	 *
	 * @param actual the ConfigurationEvent we want to make assertions on.
	 */
	protected AbstractConfigurationEventAssert(A actual, Class<S> selfType) {
		super(actual, selfType);
	}

	/**
	 * Verifies that the actual ConfigurationEvent's factoryPid is equal to the
	 * given one.
	 *
	 * @param factoryPid the given factoryPid to compare the actual
	 *            ConfigurationEvent's factoryPid to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's factoryPid is
	 *             not equal to the given one.
	 */
	public S hasFactoryPidEqualsTo(String factoryPid) {

		isNotNull().has(ConfigurationEventConditions.factoryPid(factoryPid));
		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ConfigurationEvent's factoryPid is not null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's factoryPid is
	 *             null.
	 */
	public S hasFactoryPid() {

		isNotNull().has(not(ConfigurationEventConditions.factoryPidNull()));
		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ConfigurationEvent's factoryPid is null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's factoryPid is
	 *             not null.
	 */
	public S hasNoFactoryPid() {

		isNotNull().has(ConfigurationEventConditions.factoryPidNull());
		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ConfigurationEvent's pid is not null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's pid is null
	 */
	public S hasPid() {

		isNotNull().has(not(ConfigurationEventConditions.pidNull()));

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ConfigurationEvent's pid is null.
	 *
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's pid is not
	 *             null.
	 */
	public S hasNoPid() {

		isNotNull().has(ConfigurationEventConditions.pidNull());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ConfigurationEvent's pid is equal to the given
	 * one.
	 *
	 * @param pid the given pid to compare the actual ConfigurationEvent's pid
	 *            to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's pid is not
	 *             equal to the given one.
	 */
	public S hasPidEqualsTo(String pid) {

		isNotNull().has(ConfigurationEventConditions.pid(pid));

		// return the current assertion for method chaining
		return myself;
	}

	public ServiceReferenceAssert<?> hasReferenceThat() {
		return ServiceReferenceAssert.assertThat(actual.getReference())
			.as(actual + ".reference");
	}

	/**
	 * Verifies that the actual ConfigurationEvent's type matches to the given
	 * type.
	 *
	 * @param type the given type to compare the actual ConfigurationEvent's
	 *            type to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ConfigurationEvent's type is not
	 *             matching to the given one.
	 */
	public S hasTypeEqualTo(int type) {

		isNotNull().has(ConfigurationEventConditions.type(type));

		// return the current assertion for method chaining
		return myself;
	}

}
