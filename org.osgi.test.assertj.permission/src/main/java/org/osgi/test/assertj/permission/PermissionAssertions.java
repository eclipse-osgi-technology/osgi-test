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

/**
 * Entry point for the permission assertions: one static import for both
 * {@link Permission} and {@link PermissionCollection}.
 */
public final class PermissionAssertions {
	private PermissionAssertions() {}

	/**
	 * Create an assertion for a {@link Permission}.
	 *
	 * @param actual The {@link Permission}.
	 * @return The created assertion.
	 */
	public static PermissionAssert assertThat(Permission actual) {
		return PermissionAssert.assertThat(actual);
	}

	/**
	 * Create an assertion for a {@link PermissionCollection}.
	 *
	 * @param actual The {@link PermissionCollection}.
	 * @return The created assertion.
	 */
	public static PermissionCollectionAssert assertThat(PermissionCollection actual) {
		return PermissionCollectionAssert.assertThat(actual);
	}
}
