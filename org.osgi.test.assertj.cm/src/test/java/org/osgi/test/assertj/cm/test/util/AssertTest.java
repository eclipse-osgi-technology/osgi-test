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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.SoftAssertionsProvider;
import org.opentest4j.AssertionFailedError;
import org.osgi.test.common.exceptions.Exceptions;

public interface AssertTest<SELF extends Assert<SELF, ACTUAL>, ACTUAL> {

	/**
	 * Returns the actual object
	 *
	 * @return The actual object that our assertion-under-test is asserting on.
	 */
	ACTUAL actual();

	/**
	 * Returns the Assertion Under Test (AUT).
	 *
	 * @return The assertion under test
	 */
	SELF aut();

	/**
	 * @return The soft assertions object used for the current tests.
	 */
	SoftAssertions softly();

	@SuppressWarnings("unchecked")
	default <SAP extends SoftAssertionsProvider> void softAssertionsProvider(Class<SAP> sap) throws Exception {
		softAssertionsProvider(sap, (Class<ACTUAL>) actual().getClass());
	}
	// Tests that the SoftAssertionsProvider is "properly implemented".
	// Definition of "properly implemented":
	// - Contains an "assertThat" method with the appropriate parameter and
	// return type.
	// - Checks that this assertThat() method invokes proxy() with the
	// appropriate arguments.
	// - Checks that this assertThat() method returns the result that proxy()
	// returns.
	default <SAP extends SoftAssertionsProvider> void softAssertionsProvider(Class<SAP> sap, Class<ACTUAL> actualClass)
		throws Exception {

		AtomicReference<Method> m = new AtomicReference<>();
		Assertions.assertThatCode(() -> m.set(sap.getMethod("assertThat", actualClass)))
			.doesNotThrowAnyException();

		Assertions.assertThat(m.get()
			.getReturnType())
			.as("returnType")
			.isEqualTo(aut().getClass());

		SAP sapInstance = spy(sap);

		when(sapInstance.proxy(any(), any(), any())).thenReturn(aut());

		Object retval = m.get()
			.invoke(sapInstance, actual());

		// Check that the returned assertion from assertThat() is the one
		// returned by proxy().
		softly().assertThat(retval)
			.as("returnedAssertion")
			.isSameAs(aut());

		doVerify(sapInstance, actualClass);
	}

	// Delegated to another method to reduce the scope of the @SuppressWarnings
	// to the minimum required.
	@SuppressWarnings("unchecked")
	default void doVerify(SoftAssertionsProvider sapInstance, Class<ACTUAL> actualClass) {
		verify(sapInstance).proxy((Class<SELF>) aut().getClass(), actualClass, actual());
	}

	default <T> AbstractThrowableAssert<?, ?> assertEqualityAssertion(String field, Function<T, SELF> assertion,
		T actual, T failing) {
		return assertEqualityAssertion(null, field, assertion, actual, failing);
	}

	/**
	 * Routine containing boilerplate code for checking correct assertion
	 * behavior. This function calls {@link #assertPassing assertPassing()} and
	 * {@link #assertFailing assertFailing()}. It also asserts that the failure
	 * message matches the general pattern "expected <%s>, but was <%s>". It
	 * returns the {@link AbstractThrowableAssert} instance so that you can
	 * perform further assertions on the exception.
	 *
	 * @param <T> the type of the arguments accepted by the assertion method
	 *            under test.
	 * @param softly the {@code SoftAssertions} object passed in to the original
	 *            test message.
	 * @param assertion reference to the assertion method of the {@link #aut()}
	 * @param actual the value that the method under test will actually return.
	 * @param failing an argument to pass to the assertion method that should
	 *            fail.
	 * @return The {@code AbstractThrowableAssert} instance used for chaining
	 *         further assertions.
	 */
	default <T> AbstractThrowableAssert<?, ?> assertEqualityAssertion(String msg, String field,
		Function<T, SELF> assertion, T actual, T failing) {
		assertPassing(msg, assertion, actual);
		AbstractThrowableAssert<?, ?> retval = assertFailing(msg, assertion, failing)
			.hasMessageMatching("(?si).*expecting.*" + field + ".*" + failing + ".*but.*was.*" + actual + ".*")
			.isInstanceOf(AssertionFailedError.class);
		try {
			Field f = AbstractAssert.class.getDeclaredField("actual");
			f.setAccessible(true);
			Object a = f.get(retval);
			if (!(a instanceof AssertionFailedError)) {
				return retval;
			}
			AssertionFailedError afe = (AssertionFailedError) a;
			softly().assertThat(afe.getActual()
				.getStringRepresentation())
				.as("actual")
				.isEqualTo(String.valueOf(actual));
			softly().assertThat(afe.getExpected()
				.getStringRepresentation())
				.as("expected")
				.isEqualTo(String.valueOf(failing));
		} catch (Exception e) {
			throw Exceptions.duck(e);
		}
		return retval;
	}

	default <T> void assertPassing(Function<T, SELF> assertion, T passing) {
		assertPassing(null, assertion, passing);
	}

	default <T> void assertPassing(String msg, Function<T, SELF> assertion, T passing) {
		AtomicReference<SELF> retval = new AtomicReference<>();
		softly().assertThatCode(() -> retval.set(assertion.apply(passing)))
			.as(msg == null ? "passing" : msg + ":passing")
			.doesNotThrowAnyException();
		if (softly().wasSuccess()) {
			softly().assertThat(retval.get())
				.as(msg == null ? "chaining" : msg + ":chaining")
				.isSameAs(aut());
		}
	}

	default <T> AbstractThrowableAssert<?, ?> assertFailing(Function<T, SELF> assertion, T failing) {
		return assertFailing(null, assertion, failing);
	}

	default <T> AbstractThrowableAssert<?, ?> assertFailing(String msg, Function<T, SELF> assertion, T failing) {
		return softly().assertThatThrownBy(() -> assertion.apply(failing))
			.as(msg == null ? "failing" : msg + ":failing")
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining(actual().toString());
	}

	default <CHILD extends AbstractAssert<?, ?>> void assertChildAssertion(String msg,
		Supplier<CHILD> childSupplier, Supplier<?> field) {
		CHILD child = childSupplier.get();
		softly().assertThat(child)
			.extracting("actual")
			.as(msg == null ? "child" : msg + ":child")
			.isEqualTo(field.get());
	}
}
