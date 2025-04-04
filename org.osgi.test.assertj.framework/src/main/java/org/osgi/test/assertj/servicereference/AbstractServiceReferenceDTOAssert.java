package org.osgi.test.assertj.servicereference;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.osgi.framework.dto.ServiceReferenceDTO;

/**
 * Abstract base class for {@link ServiceReferenceDTO} specific assertions
 */

public abstract class AbstractServiceReferenceDTOAssert<S extends AbstractServiceReferenceDTOAssert<S, A>, A extends ServiceReferenceDTO>
	extends AbstractObjectAssert<S, A> {

	/**
	 * Creates a new <code>{@link AbstractServiceReferenceDTOAssert}</code> to
	 * make assertions on actual ServiceReferenceDTO.
	 *
	 * @param actual the ServiceReferenceDTO we want to make assertions on.
	 */
	protected AbstractServiceReferenceDTOAssert(A actual, Class<S> selfType) {
		super(actual, selfType);
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO's bundle is equal to the
	 * given one.
	 *
	 * @param bundle the given bundle to compare the actual
	 *            ServiceReferenceDTO's bundle to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ServiceReferenceDTO's bundle is
	 *             not equal to the given one.
	 */
	public S hasBundle(long bundle) {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting bundle of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// check
		long actualBundle = actual.bundle;
		if (actualBundle != bundle) {
			failWithMessage(assertjErrorMessage, actual, bundle, actualBundle);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO's id is equal to the given
	 * one.
	 *
	 * @param id the given id to compare the actual ServiceReferenceDTO's id to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ServiceReferenceDTO's id is not
	 *             equal to the given one.
	 */
	public S hasId(long id) {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting id of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// check
		long actualId = actual.id;
		if (actualId != id) {
			failWithMessage(assertjErrorMessage, actual, id, actualId);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO's properties is equal to the
	 * given one.
	 *
	 * @param properties the given properties to compare the actual
	 *            ServiceReferenceDTO's properties to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual ServiceReferenceDTO's properties
	 *             is not equal to the given one.
	 */
	public S hasProperties(java.util.Map<String, Object> properties) {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting properties of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// null safe check
		java.util.Map<String, Object> actualProperties = actual.properties;
		if (!java.util.Objects.deepEquals(actualProperties, properties)) {
			failWithMessage(assertjErrorMessage, actual, properties, actualProperties);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO's usingBundles contains the
	 * given long elements.
	 *
	 * @param usingBundles the given elements that should be contained in actual
	 *            ServiceReferenceDTO's usingBundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual ServiceReferenceDTO's usingBundles
	 *             does not contain all given long elements.
	 */
	public S hasUsingBundles(long... usingBundles) {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// check that given long varargs is not null.
		if (usingBundles == null)
			failWithMessage("Expecting usingBundles parameter not to be null.");

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Assertions.assertThat(actual.usingBundles)
			.contains(usingBundles);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO's usingBundles contains
	 * <b>only</b> the given long elements and nothing else in whatever order.
	 *
	 * @param usingBundles the given elements that should be contained in actual
	 *            ServiceReferenceDTO's usingBundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual ServiceReferenceDTO's usingBundles
	 *             does not contain all given long elements and nothing else.
	 */
	public S hasOnlyUsingBundles(long... usingBundles) {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// check that given long varargs is not null.
		if (usingBundles == null)
			failWithMessage("Expecting usingBundles parameter not to be null.");

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Assertions.assertThat(actual.usingBundles)
			.containsOnly(usingBundles);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO's usingBundles does not
	 * contain the given long elements.
	 *
	 * @param usingBundles the given elements that should not be in actual
	 *            ServiceReferenceDTO's usingBundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual ServiceReferenceDTO's usingBundles
	 *             contains any given long elements.
	 */
	public S doesNotHaveUsingBundles(long... usingBundles) {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// check that given long varargs is not null.
		if (usingBundles == null)
			failWithMessage("Expecting usingBundles parameter not to be null.");

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Assertions.assertThat(actual.usingBundles)
			.doesNotContain(usingBundles);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual ServiceReferenceDTO has no usingBundles.
	 *
	 * @return this assertion object.
	 * @throws AssertionError if the actual ServiceReferenceDTO's usingBundles
	 *             is not empty.
	 */
	public S hasNoUsingBundles() {
		// check that actual ServiceReferenceDTO we want to make assertions on
		// is not null.
		isNotNull();

		// we override the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting :\n  <%s>\nnot to have usingBundles but had :\n  <%s>";

		// check that it is not empty
		if (actual.usingBundles.length > 0) {
			failWithMessage(assertjErrorMessage, actual, java.util.Arrays.toString(actual.usingBundles));
		}

		// return the current assertion for method chaining
		return myself;
	}

}
