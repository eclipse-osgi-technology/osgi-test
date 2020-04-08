/*
 * Copyright (c) OSGi Alliance (2019-2020). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osgi.test.assertj.version;

import static java.lang.String.format;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Pattern;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.osgi.test.assertj.testutil.AbstractAssertTest;

@ExtendWith(SoftAssertionsExtension.class)
public class VersionAssertTest extends AbstractAssertTest<VersionAssert, Version> {
	public VersionAssertTest() {
		super(VersionAssert::assertThat);
	}

	@BeforeEach
	void beforeEach() {
		setActual(Version.parseVersion("1.2.3.qualifier"));
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 33, 44
	})
	public void hasMajor(int major, SoftAssertions softly) {
		this.softly = softly;
		setActual(Version.valueOf(major + ".2.3.qualifier"));

		assertEqualityAssertion("major version", aut::hasMajor, major, 100);
	}

	@Test
	public void hasMajorThat(SoftAssertions softly) {
		this.softly = softly;
		assertChildAssertion("version", aut::hasMajorThat, actual::getMajor);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 33, 44
	})
	public void hasMinor(int minor, SoftAssertions softly) {
		this.softly = softly;
		setActual(Version.valueOf(format("1.%d.3.qualifier", minor)));

		assertEqualityAssertion("minor version", aut::hasMinor, minor, 100);
	}

	@Test
	public void hasMinorThat(SoftAssertions softly) {
		this.softly = softly;
		assertChildAssertion("version", aut::hasMinorThat, actual::getMinor);
	}

	@ParameterizedTest
	@ValueSource(ints = {
		1, 2, 33, 44
	})
	public void hasMicro(int micro, SoftAssertions softly) {
		this.softly = softly;
		setActual(Version.valueOf(format("1.2.%d.qualifier", micro)));

		assertEqualityAssertion("micro version", aut::hasMicro, micro, 100);
	}

	@Test
	public void hasMicroThat(SoftAssertions softly) {
		this.softly = softly;

		assertChildAssertion("version", aut::hasMicroThat, actual::getMicro);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"blah", "qualifier", "SNAPSHOT"
	})
	public void hasQualifier(String qualifier, SoftAssertions softly) {
		this.softly = softly;
		setActual(Version.valueOf(format("1.2.3.%s", qualifier)));

		assertEqualityAssertion("qualifier", aut::hasQualifier, qualifier, "something else");
	}

	@Test
	public void hasQualifierThat(SoftAssertions softly) {
		this.softly = softly;
		assertChildAssertion("qualifier", aut::hasQualifierThat, actual::getQualifier);
	}

	@Test
	public void isEmpty(SoftAssertions softly) {
		this.softly = softly;
		assertFailing("non-empty", ignored -> aut.isEmpty(), null).hasMessageMatching("(?si).*equal.*0.0.0.*was not.*");

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
	public void isInRange_withPassingValues(String range, SoftAssertions softly) {
		this.softly = softly;
		assertPassing("String arg", aut::isInRange, range);
		assertPassing("VersionRange arg", aut::isInRange, VersionRange.valueOf(range));
	}

	@ParameterizedTest
	@ExclusiveRangeSource
	public void isInRange_withFailingValues(String range, SoftAssertions softly) {
		this.softly = softly;
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
	public void isNotInRange_withPassingValues(String range, SoftAssertions softly) {
		this.softly = softly;
		assertPassing("String arg", aut::isNotInRange, range);
		assertPassing("VersionRange arg", aut::isNotInRange, VersionRange.valueOf(range));
	}

	@ParameterizedTest
	@InclusiveRangeSource
	public void isNotInRange_withFailingValues(String range, SoftAssertions softly) {
		this.softly = softly;
		VersionRange versionRange = VersionRange.valueOf(range);
		String quotedRange = Pattern.quote(versionRange.toString());
		String quotedActual = Pattern.quote(actual.toString());
		assertFailing("String arg", aut::isNotInRange, range)
			.hasMessageMatching("(?si).*" + quotedActual + ".*not.*in range.*<" + quotedRange + ">.*was.*");
		assertFailing("VersionRange arg", aut::isNotInRange, versionRange)
			.hasMessageMatching("(?si).*" + quotedActual + ".*not.*in range.*<" + quotedRange + ">.*was.*");
	}
}
