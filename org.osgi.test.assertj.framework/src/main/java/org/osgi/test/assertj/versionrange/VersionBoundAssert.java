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

package org.osgi.test.assertj.versionrange;

import org.assertj.core.api.InstanceOfAssertFactory;
import org.osgi.framework.Version;

public class VersionBoundAssert extends AbstractVersionBoundAssert<VersionBoundAssert, Version> {
	public static final InstanceOfAssertFactory<Version, VersionBoundAssert>	OPEN_VERSION	= new InstanceOfAssertFactory<>(
		Version.class, actual -> VersionBoundAssert.assertThat(actual, true));

	public static final InstanceOfAssertFactory<Version, VersionBoundAssert>	CLOSED_VERSION	= new InstanceOfAssertFactory<>(
		Version.class, actual -> VersionBoundAssert.assertThat(actual, false));

	public static InstanceOfAssertFactory<Version, VersionBoundAssert> versionBoundAssertFactory(boolean open) {
		return open ? OPEN_VERSION : CLOSED_VERSION;
	}

	public VersionBoundAssert(Version actual, boolean open) {
		super(actual, open, VersionBoundAssert.class);
	}

	public static VersionBoundAssert assertThat(Version actual, boolean open) {
		return new VersionBoundAssert(actual, open);
	}
}
