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

package org.osgi.test.assertj.test.permission;

import java.security.Permission;
import java.util.Objects;

/**
 * A permission class of the test bundle: the assertion bundle cannot load it
 * by itself, which is the case the serialization round trip must handle.
 */
public class TestPermission extends Permission {
	private static final long serialVersionUID = 1L;

	private final String actions;

	public TestPermission(String name, String actions) {
		super(name);
		this.actions = actions;
	}

	@Override
	public boolean implies(Permission permission) {
		if (!(permission instanceof TestPermission)) {
			return false;
		}
		TestPermission other = (TestPermission) permission;
		return getName().equals(other.getName()) && actions.contains(other.actions);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TestPermission)) {
			return false;
		}
		TestPermission other = (TestPermission) obj;
		return getName().equals(other.getName()) && actions.equals(other.actions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), actions);
	}

	@Override
	public String getActions() {
		return actions;
	}
}
