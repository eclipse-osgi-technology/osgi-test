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
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.assertj.core.api.AbstractObjectAssert;

/**
 * Assertions for {@link PermissionCollection}s.
 *
 * @param <SELF> The self type.
 * @param <ACTUAL> The permission collection type.
 */
public abstract class AbstractPermissionCollectionAssert<SELF extends AbstractPermissionCollectionAssert<SELF, ACTUAL>, ACTUAL extends PermissionCollection>
	extends AbstractObjectAssert<SELF, ACTUAL> {

	protected AbstractPermissionCollectionAssert(ACTUAL actual, Class<?> selfType) {
		super(actual, selfType);
	}

	/**
	 * Verifies that each of the given permissions can be added to the
	 * collection. The permissions are added.
	 *
	 * @param permissions The permissions to add.
	 * @return This assertion object.
	 */
	public SELF accepts(Permission... permissions) {
		isNotNull();
		for (Permission permission : permissions) {
			try {
				actual.add(permission);
			} catch (RuntimeException e) {
				throw failure("%nExpecting%n  <%s>%nto accept%n  <%s>%nbut add threw%n  <%s>", actual, permission, e);
			}
		}
		return myself;
	}

	/**
	 * Verifies that adding each of the given permissions to the collection
	 * fails with an {@link IllegalArgumentException} or a
	 * {@link SecurityException}.
	 *
	 * @param permissions The permissions that must be rejected.
	 * @return This assertion object.
	 */
	public SELF rejects(Permission... permissions) {
		isNotNull();
		for (Permission permission : permissions) {
			try {
				actual.add(permission);
			} catch (IllegalArgumentException | SecurityException e) {
				continue;
			}
			throw failure("%nExpecting%n  <%s>%nto reject%n  <%s>%nbut it was added.", actual, permission);
		}
		return myself;
	}

	/**
	 * Verifies that the collection implies each of the given permissions.
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
	 * Verifies that the collection implies none of the given permissions.
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
	 * Verifies that {@link PermissionCollection#elements()} is empty and that
	 * the enumeration honours its contract: {@code nextElement()} throws
	 * {@link NoSuchElementException}.
	 *
	 * @return This assertion object.
	 */
	public SELF hasNoElements() {
		isNotNull();
		List<Permission> found = enumerate();
		if (!found.isEmpty()) {
			throw failure("%nExpecting the elements of%n  <%s>%nto be empty but were%n  <%s>", actual, found);
		}
		return myself;
	}

	/**
	 * Verifies that {@link PermissionCollection#elements()} is not empty and
	 * that the enumeration honours its contract: after the last element
	 * {@code nextElement()} throws {@link NoSuchElementException}.
	 *
	 * @return This assertion object.
	 */
	public SELF hasElements() {
		isNotNull();
		if (enumerate().isEmpty()) {
			throw failure("%nExpecting the elements of%n  <%s>%nnot to be empty.", actual);
		}
		return myself;
	}

	/**
	 * Verifies that {@link PermissionCollection#elements()} contains exactly
	 * the given permissions, in any order, and that the enumeration honours
	 * its contract.
	 *
	 * @param permissions The expected elements.
	 * @return This assertion object.
	 */
	public SELF hasExactlyElements(Permission... permissions) {
		isNotNull();
		List<Permission> found = enumerate();
		List<Permission> missing = new ArrayList<>(Arrays.asList(permissions));
		List<Permission> unexpected = new ArrayList<>();
		for (Permission permission : found) {
			if (!missing.remove(permission)) {
				unexpected.add(permission);
			}
		}
		if (!missing.isEmpty() || !unexpected.isEmpty()) {
			throw failure(
				"%nExpecting the elements of%n  <%s>%nto be exactly%n  <%s>%nbut were%n  <%s>%nmissing:%n  <%s>%nunexpected:%n  <%s>",
				actual, Arrays.asList(permissions), found, missing, unexpected);
		}
		return myself;
	}

	/**
	 * Enumerates {@link PermissionCollection#elements()} and checks the
	 * enumeration contract on the way.
	 */
	private List<Permission> enumerate() {
		Enumeration<Permission> elements = actual.elements();
		List<Permission> found = new ArrayList<>();
		try {
			while (elements.hasMoreElements()) {
				found.add(elements.nextElement());
			}
		} catch (NoSuchElementException e) {
			throw failure("%nExpecting the elements of%n  <%s>%nto enumerate without%n  <%s>", actual, e);
		}
		try {
			elements.nextElement();
		} catch (NoSuchElementException e) {
			return found;
		}
		throw failure("%nExpecting the exhausted elements of%n  <%s>%nto throw NoSuchElementException.", actual);
	}

	/**
	 * Verifies that the collection is read-only.
	 *
	 * @return This assertion object.
	 */
	public SELF isReadOnly() {
		isNotNull();
		if (!actual.isReadOnly()) {
			throw failure("%nExpecting%n  <%s>%nto be read-only.", actual);
		}
		return myself;
	}

	/**
	 * Verifies that the collection is not read-only.
	 *
	 * @return This assertion object.
	 */
	public SELF isNotReadOnly() {
		isNotNull();
		if (actual.isReadOnly()) {
			throw failure("%nExpecting%n  <%s>%nnot to be read-only.", actual);
		}
		return myself;
	}

	/**
	 * Verifies that the collection survives a serialization round trip: the
	 * copy is a different object with the same elements.
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
		if (!(copy instanceof PermissionCollection)) {
			throw failure("%nExpecting the deserialized copy of%n  <%s>%nto be a PermissionCollection but was%n  <%s>",
				actual, copy);
		}
		Set<Permission> expected = elements(actual);
		Set<Permission> found = elements((PermissionCollection) copy);
		if (!expected.equals(found)) {
			throw failure("%nExpecting the deserialized copy of%n  <%s>%nto have the elements%n  <%s>%nbut had%n  <%s>",
				actual, expected, found);
		}
		return myself;
	}

	private static Set<Permission> elements(PermissionCollection collection) {
		return new LinkedHashSet<>(Collections.list(collection.elements()));
	}
}
