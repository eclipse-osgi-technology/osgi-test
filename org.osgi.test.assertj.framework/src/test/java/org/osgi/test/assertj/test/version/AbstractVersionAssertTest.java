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

package org.osgi.test.assertj.test.version;

import static java.lang.String.format;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

import org.assertj.core.api.AssertFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.test.assertj.test.testutil.AbstractAssertTest;
import org.osgi.test.assertj.version.AbstractVersionAssert;

public abstract class AbstractVersionAssertTest<SELF extends AbstractVersionAssert<SELF, Version>>
	extends AbstractAssertTest<SELF, Version> {
	protected AbstractVersionAssertTest(AssertFactory<Version, SELF> assertThat) {
		super(assertThat);
	}

	@BeforeEach
	void beforeEach() {
		setActual(Version.parseVersion("1.2.3.qualifier"));
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 33, 44
	})
	public void hasMajor(int major) {
		setActual(Version.valueOf(major + ".2.3.qualifier"));

		assertEqualityAssertion("major version", aut::hasMajor, major, 100);
	}

	@Test
	public void hasMajorThat() {
		assertChildAssertion("version", aut::hasMajorThat, actual::getMajor);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 33, 44
	})
	public void hasMinor(int minor) {
		setActual(Version.valueOf(format("1.%d.3.qualifier", minor)));

		assertEqualityAssertion("minor version", aut::hasMinor, minor, 100);
	}

	@Test
	public void hasMinorThat() {
		assertChildAssertion("version", aut::hasMinorThat, actual::getMinor);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 33, 44
	})
	public void hasMicro(int micro) {
		setActual(Version.valueOf(format("1.2.%d.qualifier", micro)));

		assertEqualityAssertion("micro version", aut::hasMicro, micro, 100);
	}

	@Test
	public void hasMicroThat() {

		assertChildAssertion("version", aut::hasMicroThat, actual::getMicro);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"blah", "qualifier", "SNAPSHOT"
	})
	public void hasQualifier(String qualifier) {
		setActual(Version.valueOf(format("1.2.3.%s", qualifier)));

		assertEqualityAssertion("qualifier", aut::hasQualifier, qualifier, "something else");
	}

	@Test
	public void hasQualifierThat() {
		assertChildAssertion("qualifier", aut::hasQualifierThat, actual::getQualifier);
	}

	@Test
	public void isEmpty() {
		assertFailing("non-empty", ignored -> aut.isEmpty(), null)
			.hasMessageContaining("0.0.0") // expected
			.hasMessageContaining("1.2.3.qualifier"); // but was

		setActual(Version.emptyVersion);

		assertPassing("empty", ignored -> aut.isEmpty(), null);
	}

	@ValueSource(strings = {
		// @formatter:off
		"[0,2)",
		"[1.2.3.qualifier,1.2.4)",
		"(1.2.3.qualifieq,1.2.3.qualifiera)",
		"[1.2.3.qualifier,1.2.3.qualifier]",
		"(0,1.2.3.qualifier]",
		// @formatter:on
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface InclusiveRangeSource {}

	@ValueSource(strings = {
		// @formatter:off
		"(0,1]",
		"[2,100)",
		"[0,1.2.3.qualifier)",
		"(1.2.3.qualifier,1.2.4)",
		"[0, 1.2.3.qualifieq]",
		"[1.2.3.qualifiera,100]",
		"[0,1.2.3.qualifier)"
		// @formatter:on
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface ExclusiveRangeSource {}

	@ParameterizedTest
	@InclusiveRangeSource
	public void isInRange_withPassingValues(String range) {
		assertPassing("String arg", aut::isInRange, range);
		assertPassing("VersionRange arg", aut::isInRange, VersionRange.valueOf(range));
	}

	@ParameterizedTest
	@ExclusiveRangeSource
	public void isInRange_withFailingValues(String range) {
		VersionRange versionRange = VersionRange.valueOf(range);
		String quotedRange = Pattern.quote(versionRange.toString());
		String quotedActual = Pattern.quote(actual.toString());
		assertFailing("String arg", aut::isInRange, range)
			.hasMessageMatching("(?si).*" + quotedActual + ".*in range.*<" + quotedRange + ">.*was not.*");
		assertFailing("VersionRange arg", aut::isInRange, versionRange)
			.hasMessageMatching("(?si).*" + quotedActual + ".*in range.*<" + quotedRange + ">.*was not.*");
	}

	@ParameterizedTest
	@ExclusiveRangeSource
	public void isNotInRange_withPassingValues(String range) {
		assertPassing("String arg", aut::isNotInRange, range);
		assertPassing("VersionRange arg", aut::isNotInRange, VersionRange.valueOf(range));
	}

	@ParameterizedTest
	@InclusiveRangeSource
	public void isNotInRange_withFailingValues(String range) {
		VersionRange versionRange = VersionRange.valueOf(range);
		String quotedRange = Pattern.quote(versionRange.toString());
		String quotedActual = Pattern.quote(actual.toString());
		assertFailing("String arg", aut::isNotInRange, range)
			.hasMessageMatching("(?si).*" + quotedActual + ".*not.*in range.*<" + quotedRange + ">.*was.*");
		assertFailing("VersionRange arg", aut::isNotInRange, versionRange)
			.hasMessageMatching("(?si).*" + quotedActual + ".*not.*in range.*<" + quotedRange + ">.*was.*");
	}
}
