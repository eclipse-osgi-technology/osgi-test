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

package org.osgi.test.assertj.cm.test.util;

import org.assertj.core.api.Assert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public abstract class AbstractAssertTest<SELF extends Assert<SELF, ACTUAL>, ACTUAL>
	implements AssertTest<SELF, ACTUAL> {

	protected final AssertFactory<ACTUAL, SELF>	assertThat;
	protected ACTUAL							actual;
	protected SELF								aut;
	@InjectSoftAssertions
	SoftAssertions								softly;

	protected AbstractAssertTest(AssertFactory<ACTUAL, SELF> assertThat) {
		this.assertThat = assertThat;
	}

	@Override
	public ACTUAL actual() {
		return actual;
	}

	@Override
	public SELF aut() {
		return aut;
	}

	@Override
	public SoftAssertions softly() {
		return softly;
	}

	protected void setActual(ACTUAL actual) {
		this.actual = actual;
		aut = assertThat.createAssert(actual);
	}
}
