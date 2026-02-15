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

package org.osgi.test.assertj.test.bundledto;

import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Bundle.INSTALLED;
import static org.osgi.framework.Bundle.RESOLVED;
import static org.osgi.framework.Bundle.STARTING;
import static org.osgi.framework.Bundle.STOPPING;
import static org.osgi.framework.Bundle.UNINSTALLED;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.test.assertj.bundledto.BundleDTOAssert;
import org.osgi.test.assertj.bundledto.BundleDTOSoftAssertionsProvider;
import org.osgi.test.assertj.test.testutil.AbstractAssertAndSAPTest;

public class BundleDTOAssertTest
	extends AbstractAssertAndSAPTest<BundleDTOAssert, BundleDTO, BundleDTOSoftAssertionsProvider> {

	public BundleDTOAssertTest() {
		super(BundleDTOAssert::assertThat, BundleDTOSoftAssertionsProvider.class, BundleDTO.class, BundleDTO::new);
	}

	@BeforeEach
	public void setUp() {
		BundleDTO dto = new BundleDTO();
		dto.id = 42L;
		dto.symbolicName = "my.bundle";
		dto.version = "1.2.3";
		dto.state = ACTIVE;
		dto.lastModified = 1000L;
		setActual(dto);
	}

	@Test
	public void hasBundleId() {
		assertEqualityAssertion("bundle ID", aut::hasBundleId, 42L, 99L);
	}

	@Test
	public void hasSymbolicName() {
		assertEqualityAssertion("symbolic name", aut::hasSymbolicName, "my.bundle", "other.name");
	}

	@Test
	public void hasSymbolicName_withNull() {
		actual.symbolicName = null;
		setActual(actual);

		assertEqualityAssertion("symbolic name", aut::hasSymbolicName, null, "some.other.name");
	}

	@Test
	public void hasSymbolicNameThat() {
		assertChildAssertion("symbolic name", aut::hasSymbolicNameThat, () -> actual.symbolicName);
	}

	@Test
	public void hasVersion() {
		assertEqualityAssertion("version", aut::hasVersion, "1.2.3", "2.3.4");
	}

	@Test
	public void hasVersion_withNull() {
		actual.version = null;
		setActual(actual);

		assertEqualityAssertion("version", aut::hasVersion, null, "1.2.3");
	}

	@Test
	public void hasVersionThat() {
		assertChildAssertion("version", aut::hasVersionThat, () -> actual.version);
	}

	@Test
	public void hasLastModified() {
		assertPassing(aut::hasLastModified, 1000L);
		assertFailing(aut::hasLastModified, 2000L)
			.hasMessageMatching("(?si).*have last modified.*2000.*but it was.*1000.*");
	}

	@Test
	public void hasLastModifiedLongThat() {
		assertChildAssertion("last modified", aut::hasLastModifiedLongThat, () -> actual.lastModified);
	}

	@Test
	public void hasLastModifiedDateThat() {
		assertChildAssertion("last modified", aut::hasLastModifiedDateThat, () -> new Date(actual.lastModified));
	}

	@ParameterizedTest
	@StatesSource
	public void isInState(int passingState) {
		final int actualState = passingState | (passingState << 2);
		actual.state = actualState;
		setActual(actual);

		int failingState = (passingState > 16) ? UNINSTALLED : passingState << 1;
		assertPassing(aut::isInState, passingState);
		assertFailing(aut::isInState, failingState).hasMessageMatching("(?si).*expect.*in state.*" + failingState + ":"
			+ stateToString(failingState) + ".*but was in state.*" + actualState + ":"
			+ Pattern.quote(stateMaskToString(actualState)) + ".*");
	}

	@ParameterizedTest
	@StatesSource
	public void isInState_withMultipleStates_throwsIAE(int expected) {
		actual.state = ACTIVE;
		setActual(actual);

		final int mask = expected | (expected << 1);
		softly().assertThatThrownBy(() -> aut.isInState(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageMatching("(?si).*" + mask + ".*isInStateMaskedBy.*");
	}

	public final static int UNKNOWN = 128;

	@ValueSource(ints = {
		ACTIVE, INSTALLED, RESOLVED, STARTING, STOPPING, UNINSTALLED, UNKNOWN
	})
	@Retention(RetentionPolicy.RUNTIME)
	static @interface StatesSource {}

	@ParameterizedTest
	@StatesSource
	public void isNotInState(int state) {
		actual.state = state;
		setActual(actual);

		int passingState = state == ACTIVE ? RESOLVED : ACTIVE;
		assertPassing(aut::isNotInState, passingState);
		assertFailing(aut::isNotInState, state)
			.hasMessageMatching("(?s).*not.* in state.*" + state + ".*" + stateToString(state) + ".*but it was.*");
	}

	@Test
	public void isInStateMaskedBy() {
		actual.state = ACTIVE;
		setActual(actual);

		assertPassing(aut::isInStateMaskedBy, ACTIVE | INSTALLED);
		assertPassing(aut::isInStateMaskedBy, ACTIVE);
		assertPassing(aut::isInStateMaskedBy, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isInStateMaskedBy, INSTALLED)
			.hasMessageMatching(
				"(?si).*in one of states.*\\[2:INSTALLED\\].*but was in state.*32:ACTIVE.*");
		assertFailing(aut::isInStateMaskedBy, INSTALLED | STOPPING | RESOLVED).hasMessageMatching(
			"(?si).*in one of states.*\\Q[22:INSTALLED | RESOLVED | STOPPING]\\E.*but was in state.*32:ACTIVE.*");
		assertFailing(aut::isInStateMaskedBy, STOPPING | RESOLVED | STARTING).hasMessageMatching(
			"(?si).*in one of states.*\\Q[28:RESOLVED | STARTING | STOPPING]\\E.*but was in state.*32:ACTIVE.*");

		actual.state = RESOLVED;
		setActual(actual);

		assertPassing(aut::isInStateMaskedBy, RESOLVED | INSTALLED);
		assertPassing(aut::isInStateMaskedBy, RESOLVED);
		assertPassing(aut::isInStateMaskedBy, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isInStateMaskedBy, INSTALLED)
			.hasMessageMatching("(?si).*in one of states.*\\[2:INSTALLED\\].*but was in state.*4:RESOLVED.*");
		assertFailing(aut::isInStateMaskedBy, INSTALLED | STOPPING | ACTIVE).hasMessageMatching(
			"(?si).*in one of states.*\\Q[50:INSTALLED | STOPPING | ACTIVE]\\E.*but was in state.*4:RESOLVED.*");
		assertFailing(aut::isInStateMaskedBy, STOPPING | ACTIVE | STARTING).hasMessageMatching(
			"(?si).*in one of states.*\\Q[56:STARTING | STOPPING | ACTIVE]\\E.*but was in state.*4:RESOLVED.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 64, 512
	})
	public void isInStateMask_throwsIAE_forInvalidMask(int mask) {
		actual.state = ACTIVE;
		setActual(actual);

		softly().assertThatThrownBy(() -> aut.isInStateMaskedBy(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	@Test
	public void isNotInStateMask() {
		actual.state = STARTING;
		setActual(actual);

		assertPassing(aut::isNotInStateMask, ACTIVE | INSTALLED);
		assertPassing(aut::isNotInStateMask, ACTIVE);
		assertPassing(aut::isNotInStateMask, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isNotInStateMask, STARTING)
			.hasMessageMatching("(?si).*not.*in one of states.*\\Q[8:STARTING]\\E.*but was in state.*8:STARTING.*");
		assertFailing(aut::isNotInStateMask, INSTALLED | STARTING | STOPPING).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[26:INSTALLED | STARTING | STOPPING]\\E.*but was in state.*8:STARTING.*");
		assertFailing(aut::isNotInStateMask, STOPPING | RESOLVED | STARTING).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[28:RESOLVED | STARTING | STOPPING]\\E.*but was in state.*8:STARTING.*");

		actual.state = UNINSTALLED;
		setActual(actual);

		assertPassing(aut::isNotInStateMask, RESOLVED | INSTALLED);
		assertPassing(aut::isNotInStateMask, RESOLVED);
		assertPassing(aut::isNotInStateMask, ACTIVE | STOPPING | RESOLVED);
		assertFailing(aut::isNotInStateMask, UNINSTALLED)
			.hasMessageMatching(
				"(?si).*not.*in one of states.*\\Q[1:UNINSTALLED]\\E.*but was in state.*1:UNINSTALLED.*");
		assertFailing(aut::isNotInStateMask, UNINSTALLED | STOPPING | ACTIVE).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[49:UNINSTALLED | STOPPING | ACTIVE]\\E.*but was in state.*1:UNINSTALLED.*");
		assertFailing(aut::isNotInStateMask, STOPPING | UNINSTALLED | STARTING).hasMessageMatching(
			"(?si).*not.*in one of states.*\\Q[25:UNINSTALLED | STARTING | STOPPING]\\E.*but was in state.*1:UNINSTALLED.*");
	}

	@ParameterizedTest
	@ValueSource(ints = {
		-23, 0, 64, 512
	})
	public void isNotInStateMask_throwsIAE_forInvalidMask(int mask) {
		actual.state = ACTIVE;
		setActual(actual);

		softly().assertThatThrownBy(() -> aut.isNotInStateMask(mask))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(Integer.toString(mask));
	}

	private static final int[] STATES = {
		UNINSTALLED, INSTALLED, RESOLVED, STARTING, STOPPING, ACTIVE
	};

	private static final int	KNOWN_MASK		= UNINSTALLED | INSTALLED | RESOLVED | STARTING | STOPPING | ACTIVE;
	private static final int	UNKNOWN_MASK	= ~KNOWN_MASK;

	private static String stateMaskToString(int state) {
		Stream<String> bits = IntStream.of(STATES)
			.filter(x -> (x & state) != 0)
			.mapToObj(BundleDTOAssertTest::stateToString);

		if ((state & UNKNOWN_MASK) != 0) {
			bits = Stream.concat(bits, Stream.of("UNKNOWN"));
		}
		return bits.collect(Collectors.joining(" | "));
	}

	private static String stateToString(int state) {
		switch (state) {
			case INSTALLED :
				return "INSTALLED";
			case RESOLVED :
				return "RESOLVED";
			case STARTING :
				return "STARTING";
			case ACTIVE :
				return "ACTIVE";
			case STOPPING :
				return "STOPPING";
			case UNINSTALLED :
				return "UNINSTALLED";
			default :
				return "UNKNOWN";
		}
	}
}
