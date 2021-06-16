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

package org.osgi.test.assertj.bundlereference;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.BundleReference;

public class BundleReferenceAssert extends AbstractBundleReferenceAssert<BundleReferenceAssert, BundleReference> {

	public static final InstanceOfAssertFactory<BundleReference, BundleReferenceAssert> BUNDLE_REFERENCE = new InstanceOfAssertFactory<>(
		BundleReference.class, BundleReferenceAssert::assertThat);

	public BundleReferenceAssert(BundleReference actual) {
		super(actual, BundleReferenceAssert.class);
	}

	public static BundleReferenceAssert assertThat(BundleReference actual) {
		return new BundleReferenceAssert(actual);
	}
}
