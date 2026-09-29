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
import java.util.PropertyPermission;

import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.assertj.permission.PermissionSoftAssertions;

@ExtendWith(SoftAssertionsExtension.class)
public class PermissionAssertTest {

	@InjectSoftAssertions
	PermissionSoftAssertions	softly;

	final Permission			readWrite	= new PropertyPermission("java.home", "read,write");
	final Permission			writeRead	= new PropertyPermission("java.home", "write, read");
	final Permission			read		= new PropertyPermission("java.home", "read");
	final Permission			write		= new PropertyPermission("java.home", "write");
	final Permission			other		= new RuntimePermission("exitVM");

	@Test
	void implies() {
		assertThat(readWrite).implies(readWrite, writeRead, read, write)
			.doesNotImply(other);
		assertThat(read).implies(read)
			.doesNotImply(readWrite, write, other);

		assertThatCode(() -> assertThat(read).implies(read, write)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("to imply")
			.hasMessageContaining(write.toString());
		assertThatCode(() -> assertThat(readWrite).doesNotImply(other, read)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("not to imply")
			.hasMessageContaining(read.toString());
	}

	@Test
	void equivalence() {
		assertThat(readWrite).isEquivalentTo(writeRead)
			.isNotEquivalentTo(read)
			.isNotEquivalentTo(other);

		assertThatCode(() -> assertThat(read).isEquivalentTo(write)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("in both directions");
		assertThatCode(() -> assertThat(readWrite).isNotEquivalentTo(writeRead)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("not to be equal");
	}

	@Test
	void nameAndActions() {
		assertThat(writeRead).hasName("java.home")
			.hasActions("read,write");

		assertThatCode(() -> assertThat(read).hasActions("write")).isInstanceOf(AssertionError.class)
			.hasMessageContaining("to have actions");
	}

	@Test
	void serialization() {
		assertThat(readWrite).isSerializable();
		assertThat(new TestPermission("a/b", "publish,subscribe")).isSerializable();
		assertThat(new NotSerializablePermission()).isNotSerializable();

		assertThatCode(() -> assertThat(new NotSerializablePermission()).isSerializable())
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("to be serializable");
		assertThatCode(() -> assertThat(readWrite).isNotSerializable()).isInstanceOf(AssertionError.class)
			.hasMessageContaining("not to be serializable");
	}

	@Test
	void customPermission() {
		TestPermission pubSub = new TestPermission("a/b", "publish,subscribe");
		TestPermission pub = new TestPermission("a/b", "publish");
		assertThat(pubSub).implies(pubSub, pub)
			.doesNotImply(new TestPermission("a/c", "publish"), read)
			.isEquivalentTo(new TestPermission("a/b", "publish,subscribe"));
	}

	@Test
	void softAssertions() {
		softly.assertThat(readWrite)
			.implies(read)
			.isSerializable();
		softly.assertThat(read.newPermissionCollection())
			.isNotReadOnly();
	}

	@Test
	void nullActual() {
		assertThatCode(() -> assertThat((Permission) null).implies(read)).isInstanceOf(AssertionError.class)
			.hasMessageContaining("Expecting actual not to be null");
	}

	static class NotSerializablePermission extends Permission {
		private static final long	serialVersionUID	= 1L;

		@SuppressWarnings("unused")
		private final Object		notSerializable		= new Object();

		NotSerializablePermission() {
			super("x");
		}

		@Override
		public boolean implies(Permission permission) {
			return false;
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String getActions() {
			return "";
		}
	}
}
