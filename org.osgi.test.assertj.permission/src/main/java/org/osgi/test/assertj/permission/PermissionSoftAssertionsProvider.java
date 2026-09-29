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

import org.assertj.core.api.SoftAssertionsProvider;

/**
 * Soft assertions for {@link Permission}s and {@link PermissionCollection}s.
 */
public interface PermissionSoftAssertionsProvider extends SoftAssertionsProvider {
	/**
	 * Create a soft assertion for a {@link Permission}.
	 *
	 * @param actual The {@link Permission}.
	 * @return The created soft assertion.
	 */
	default PermissionAssert assertThat(Permission actual) {
		return proxy(PermissionAssert.class, Permission.class, actual);
	}

	/**
	 * Create a soft assertion for a {@link PermissionCollection}.
	 *
	 * @param actual The {@link PermissionCollection}.
	 * @return The created soft assertion.
	 */
	default PermissionCollectionAssert assertThat(PermissionCollection actual) {
		return proxy(PermissionCollectionAssert.class, PermissionCollection.class, actual);
	}
}
