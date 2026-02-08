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

package org.osgi.test.assertj.feature;

/**
 * Entry point for assertions of different data types. Each method in this class
 * is a static factory for the type-specific assertion objects.
 */
public class Assertions {

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public static org.osgi.test.assertj.feature.FeatureAssert assertThat(org.osgi.service.feature.Feature actual) {
		return new org.osgi.test.assertj.feature.FeatureAssert(actual);
	}

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureArtifactAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public static org.osgi.test.assertj.feature.FeatureArtifactAssert assertThat(
		org.osgi.service.feature.FeatureArtifact actual) {
		return new org.osgi.test.assertj.feature.FeatureArtifactAssert(actual);
	}

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureBundleAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public static org.osgi.test.assertj.feature.FeatureBundleAssert assertThat(
		org.osgi.service.feature.FeatureBundle actual) {
		return new org.osgi.test.assertj.feature.FeatureBundleAssert(actual);
	}

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureConfigurationAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public static org.osgi.test.assertj.feature.FeatureConfigurationAssert assertThat(
		org.osgi.service.feature.FeatureConfiguration actual) {
		return new org.osgi.test.assertj.feature.FeatureConfigurationAssert(actual);
	}

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.feature.FeatureExtensionAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public static org.osgi.test.assertj.feature.FeatureExtensionAssert assertThat(
		org.osgi.service.feature.FeatureExtension actual) {
		return new org.osgi.test.assertj.feature.FeatureExtensionAssert(actual);
	}

	/**
	 * Creates a new instance of
	 * <code>{@link org.osgi.test.assertj.feature.IDAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	@org.assertj.core.util.CheckReturnValue
	public static org.osgi.test.assertj.feature.IDAssert assertThat(org.osgi.service.feature.ID actual) {
		return new org.osgi.test.assertj.feature.IDAssert(actual);
	}

	/**
	 * Creates a new <code>{@link Assertions}</code>.
	 */
	protected Assertions() {
		// empty
	}
}
