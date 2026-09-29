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

import org.assertj.core.api.InstanceOfAssertFactory;

/**
 * Assertions for {@link Permission}s.
 */
public class PermissionAssert extends AbstractPermissionAssert<PermissionAssert, Permission> {

	/**
	 * Create an assertion for a {@link Permission}.
	 *
	 * @param actual The {@link Permission}.
	 */
	public PermissionAssert(Permission actual) {
		super(actual, PermissionAssert.class);
	}

	/**
	 * Create an assertion for a {@link Permission}.
	 *
	 * @param actual The {@link Permission}.
	 * @return The created assertion.
	 */
	public static PermissionAssert assertThat(Permission actual) {
		return new PermissionAssert(actual);
	}

	/**
	 * {@link InstanceOfAssertFactory} for a {@link PermissionAssert}.
	 */
	public static final InstanceOfAssertFactory<Permission, PermissionAssert> PERMISSION = new InstanceOfAssertFactory<>(
		Permission.class, PermissionAssert::assertThat);
}
