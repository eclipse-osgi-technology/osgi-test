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

import java.security.PermissionCollection;

import org.assertj.core.api.InstanceOfAssertFactory;

/**
 * Assertions for {@link PermissionCollection}s.
 */
public class PermissionCollectionAssert
	extends AbstractPermissionCollectionAssert<PermissionCollectionAssert, PermissionCollection> {

	/**
	 * Create an assertion for a {@link PermissionCollection}.
	 *
	 * @param actual The {@link PermissionCollection}.
	 */
	public PermissionCollectionAssert(PermissionCollection actual) {
		super(actual, PermissionCollectionAssert.class);
	}

	/**
	 * Create an assertion for a {@link PermissionCollection}.
	 *
	 * @param actual The {@link PermissionCollection}.
	 * @return The created assertion.
	 */
	public static PermissionCollectionAssert assertThat(PermissionCollection actual) {
		return new PermissionCollectionAssert(actual);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link PermissionCollectionAssert}.
	 */
	public static final InstanceOfAssertFactory<PermissionCollection, PermissionCollectionAssert> PERMISSION_COLLECTION = new InstanceOfAssertFactory<>(
		PermissionCollection.class, PermissionCollectionAssert::assertThat);
}
