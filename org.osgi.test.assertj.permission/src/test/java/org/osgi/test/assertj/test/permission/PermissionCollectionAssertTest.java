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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.osgi.test.assertj.permission.PermissionAssertions.assertThat;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.PropertyPermission;

import org.junit.jupiter.api.Test;

public class PermissionCollectionAssertTest {

	final Permission	read	= new PropertyPermission("java.home", "read");
	final Permission	write	= new PropertyPermission("java.home", "write");
	final Permission	all		= new PropertyPermission("*", "read");
	final Permission	other	= new RuntimePermission("exitVM");

	@Test
	void addAndImply() {
		PermissionCollection collection = read.newPermissionCollection();
		assertThat(collection).hasNoElements()
			.isNotReadOnly()
			.accepts(read, write)
			.rejects(other)
			.hasElements()
			// the collection merges the actions per name
			.hasExactlyElements(new PropertyPermission("java.home", "read,write"))
			.implies(read, write)
			.doesNotImply(all, other)
			.isSerializable();

		assertThatCode(() -> assertThat(collection).implies(all)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("to imply");
		assertThatCode(() -> assertThat(collection).rejects(read)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("to reject");
		assertThatCode(() -> assertThat(collection).accepts(other)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("to accept");
	}

	@Test
	void readOnly() {
		PermissionCollection collection = read.newPermissionCollection();
		collection.add(read);
		collection.setReadOnly();
		assertThat(collection).isReadOnly()
			.rejects(write)
			.implies(read);

		assertThatCode(() -> assertThat(collection).isNotReadOnly()).isInstanceOf(AssertionError.class)
			.hasMessageContaining("not to be read-only");
	}

	@Test
	void elements() {
		PermissionCollection collection = read.newPermissionCollection();
		assertThatCode(() -> assertThat(collection).hasElements()).isInstanceOf(AssertionError.class)
			.hasMessageContaining("not to be empty");
		collection.add(read);
		assertThatCode(() -> assertThat(collection).hasNoElements()).isInstanceOf(AssertionError.class)
			.hasMessageContaining("to be empty");
		assertThatCode(() -> assertThat(collection).hasExactlyElements(read, write)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("missing")
			.hasMessageContaining(write.toString());
	}

	@Test
	void brokenEnumeration() {
		PermissionCollection broken = new PermissionCollection() {
			private static final long serialVersionUID = 1L;

			@Override
			public void add(Permission permission) {}

			@Override
			public boolean implies(Permission permission) {
				return false;
			}

			@Override
			public Enumeration<Permission> elements() {
				// never throws NoSuchElementException at the end
				return new Enumeration<Permission>() {
					@Override
					public boolean hasMoreElements() {
						return false;
					}

					@Override
					public Permission nextElement() {
						return null;
					}
				};
			}
		};
		assertThatCode(() -> assertThat(broken).hasNoElements()).isInstanceOf(AssertionError.class)
			.hasMessageContaining("NoSuchElementException");
	}
}
