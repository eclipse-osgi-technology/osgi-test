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

package org.osgi.test.junit5.featurelauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PropertyResolutionTest {

	@Test
	void resolveSingleProperty() {
		System.setProperty("test.prop", "hello");
		try {
			String result = FeatureLaunchExtension.resolveProperty("${test.prop}");
			assertThat(result).isEqualTo("hello");
		} finally {
			System.clearProperty("test.prop");
		}
	}

	@Test
	void resolveMultipleProperties() {
		System.setProperty("test.a", "foo");
		System.setProperty("test.b", "bar");
		try {
			String result = FeatureLaunchExtension.resolveProperty("${test.a}/${test.b}");
			assertThat(result).isEqualTo("foo/bar");
		} finally {
			System.clearProperty("test.a");
			System.clearProperty("test.b");
		}
	}

	@Test
	void resolveLiteralString() {
		String result = FeatureLaunchExtension.resolveProperty("/some/literal/path");
		assertThat(result).isEqualTo("/some/literal/path");
	}

	@Test
	void resolveEmptyString() {
		String result = FeatureLaunchExtension.resolveProperty("");
		assertThat(result).isEmpty();
	}

	@Test
	void resolveNull() {
		String result = FeatureLaunchExtension.resolveProperty(null);
		assertThat(result).isNull();
	}

	@Test
	void resolveMissingPropertyThrows() {
		assertThatThrownBy(() -> FeatureLaunchExtension.resolveProperty("${missing.prop}"))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("missing.prop");
	}

	@Test
	void resolvePropertiesArray() {
		System.setProperty("test.x", "val1");
		System.setProperty("test.y", "val2");
		try {
			String[] result = FeatureLaunchExtension
					.resolveProperties(new String[] { "${test.x}", "${test.y}", "literal" });
			assertThat(result).containsExactly("val1", "val2", "literal");
		} finally {
			System.clearProperty("test.x");
			System.clearProperty("test.y");
		}
	}

	@Test
	void resolveAdjacentPlaceholders() {
		System.setProperty("test.a", "foo");
		System.setProperty("test.b", "bar");
		try {
			String result = FeatureLaunchExtension.resolveProperty("${test.a}${test.b}");
			assertThat(result).isEqualTo("foobar");
		} finally {
			System.clearProperty("test.a");
			System.clearProperty("test.b");
		}
	}

	@Test
	void resolveMixedLiteralAndPlaceholder() {
		System.setProperty("test.a", "hello");
		try {
			String result = FeatureLaunchExtension.resolveProperty("prefix-${test.a}-suffix");
			assertThat(result).isEqualTo("prefix-hello-suffix");
		} finally {
			System.clearProperty("test.a");
		}
	}

	@Test
	void resolvePropertiesEmptyArray() {
		String[] result = FeatureLaunchExtension.resolveProperties(new String[0]);
		assertThat(result).isEmpty();
	}

	@Test
	void resolveDollarWithoutBrace() {
		String result = FeatureLaunchExtension.resolveProperty("$100");
		assertThat(result).isEqualTo("$100");
	}
}
