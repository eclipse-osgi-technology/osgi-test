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

package org.osgi.test.assertj.permission;

import java.security.Permission;

import org.assertj.core.api.AbstractObjectAssert;

/**
 * Assertions for {@link Permission}s.
 *
 * @param <SELF> The self type.
 * @param <ACTUAL> The permission type.
 */
public abstract class AbstractPermissionAssert<SELF extends AbstractPermissionAssert<SELF, ACTUAL>, ACTUAL extends Permission>
	extends AbstractObjectAssert<SELF, ACTUAL> {

	protected AbstractPermissionAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	/**
	 * Verifies that the permission implies each of the given permissions.
	 *
	 * @param permissions The permissions that must be implied.
	 * @return This assertion object.
	 */
	public SELF implies(Permission... permissions) {
		isNotNull();
		for (Permission permission : permissions) {
			if (!actual.implies(permission)) {
				throw failure("%nExpecting%n  <%s>%nto imply%n  <%s>%nbut it does not.", actual, permission);
			}
		}
		return myself;
	}

	/**
	 * Verifies that the permission implies none of the given permissions.
	 *
	 * @param permissions The permissions that must not be implied.
	 * @return This assertion object.
	 */
	public SELF doesNotImply(Permission... permissions) {
		isNotNull();
		for (Permission permission : permissions) {
			if (actual.implies(permission)) {
				throw failure("%nExpecting%n  <%s>%nnot to imply%n  <%s>%nbut it does.", actual, permission);
			}
		}
		return myself;
	}

	/**
	 * Verifies that the permission and the given one are equal in both
	 * directions and have the same hash code.
	 *
	 * @param other The equivalent permission.
	 * @return This assertion object.
	 */
	public SELF isEquivalentTo(Permission other) {
		isNotNull();
		if (!actual.equals(other) || other == null || !other.equals(actual)) {
			throw failure("%nExpecting%n  <%s>%nto be equal to%n  <%s>%nin both directions.", actual, other);
		}
		if (actual.hashCode() != other.hashCode()) {
			throw failure("%nExpecting%n  <%s>%nto have the same hash code as%n  <%s>%nbut was %s and %s.", actual,
				other, actual.hashCode(), other.hashCode());
		}
		return myself;
	}

	/**
	 * Verifies that the permission and the given one are not equal, in either
	 * direction.
	 *
	 * @param other The different permission.
	 * @return This assertion object.
	 */
	public SELF isNotEquivalentTo(Permission other) {
		isNotNull();
		if (actual.equals(other) || (other != null && other.equals(actual))) {
			throw failure("%nExpecting%n  <%s>%nnot to be equal to%n  <%s>%n", actual, other);
		}
		return myself;
	}

	/**
	 * Verifies that the permission has the given name.
	 *
	 * @param name The expected name.
	 * @return This assertion object.
	 */
	public SELF hasName(String name) {
		isNotNull();
		if (!java.util.Objects.equals(actual.getName(), name)) {
			throw failure("%nExpecting%n  <%s>%nto have name%n  <%s>%nbut was%n  <%s>", actual, name,
				actual.getName());
		}
		return myself;
	}

	/**
	 * Verifies that the permission has the given canonical actions, as
	 * returned by {@link Permission#getActions()}.
	 *
	 * @param actions The expected actions.
	 * @return This assertion object.
	 */
	public SELF hasActions(String actions) {
		isNotNull();
		if (!java.util.Objects.equals(actual.getActions(), actions)) {
			throw failure("%nExpecting%n  <%s>%nto have actions%n  <%s>%nbut was%n  <%s>", actual, actions,
				actual.getActions());
		}
		return myself;
	}

	/**
	 * Verifies that the permission survives a serialization round trip: the
	 * copy is a different object, equivalent to the original.
	 *
	 * @return This assertion object.
	 */
	public SELF isSerializable() {
		isNotNull();
		Object copy;
		try {
			copy = Serialization.roundTrip(actual);
		} catch (Exception e) {
			throw failure("%nExpecting%n  <%s>%nto be serializable but got%n  <%s>", actual, e);
		}
		if (copy == actual) {
			throw failure("%nExpecting the deserialized copy of%n  <%s>%nto be a new object.", actual);
		}
		if (!(copy instanceof Permission)) {
			throw failure("%nExpecting the deserialized copy of%n  <%s>%nto be a Permission but was%n  <%s>", actual,
				copy);
		}
		Permission permission = (Permission) copy;
		if (!actual.equals(permission) || !permission.equals(actual) || actual.hashCode() != permission.hashCode()) {
			throw failure("%nExpecting the deserialized copy%n  <%s>%nto be equivalent to%n  <%s>", permission,
				actual);
		}
		return myself;
	}

	/**
	 * Verifies that serializing the permission fails.
	 *
	 * @return This assertion object.
	 */
	public SELF isNotSerializable() {
		isNotNull();
		try {
			Serialization.roundTrip(actual);
		} catch (Exception e) {
			return myself;
		}
		throw failure("%nExpecting%n  <%s>%nnot to be serializable.", actual);
	}
}
