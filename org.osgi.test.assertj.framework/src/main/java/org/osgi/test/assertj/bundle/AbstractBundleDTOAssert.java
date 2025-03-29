package org.osgi.test.assertj.bundle;

import org.assertj.core.api.AbstractObjectAssert;
import org.osgi.framework.dto.BundleDTO;

/**
 * Abstract base class for {@link BundleDTO} specific assertions
 */

public abstract class AbstractBundleDTOAssert<S extends AbstractBundleDTOAssert<S, A>, A extends BundleDTO>
	extends AbstractObjectAssert<S, A> {

	/**
	 * Creates a new <code>{@link AbstractBundleDTOAssert}</code> to make
	 * assertions on actual BundleDTO.
	 *
	 * @param actual the BundleDTO we want to make assertions on.
	 */
	protected AbstractBundleDTOAssert(A actual, Class<S> selfType) {
		super(actual, selfType);
	}

	/**
	 * Verifies that the actual BundleDTO's id is equal to the given one.
	 *
	 * @param id the given id to compare the actual BundleDTO's id to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual BundleDTO's id is not equal to the
	 *             given one.
	 */
	public S hasId(long id) {
		// check that actual BundleDTO we want to make assertions on is not
		// null.
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
	 * Verifies that the actual BundleDTO's lastModified is equal to the given
	 * one.
	 *
	 * @param lastModified the given lastModified to compare the actual
	 *            BundleDTO's lastModified to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual BundleDTO's lastModified is not
	 *             equal to the given one.
	 */
	public S hasLastModified(long lastModified) {
		// check that actual BundleDTO we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting lastModified of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// check
		long actualLastModified = actual.lastModified;
		if (actualLastModified != lastModified) {
			failWithMessage(assertjErrorMessage, actual, lastModified, actualLastModified);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual BundleDTO's state is equal to the given one.
	 *
	 * @param state the given state to compare the actual BundleDTO's state to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual BundleDTO's state is not equal to
	 *             the given one.
	 */
	public S hasState(int state) {
		// check that actual BundleDTO we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting state of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// check
		int actualState = actual.state;
		if (actualState != state) {
			failWithMessage(assertjErrorMessage, actual, state, actualState);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual BundleDTO's symbolicName is equal to the given
	 * one.
	 *
	 * @param symbolicName the given symbolicName to compare the actual
	 *            BundleDTO's symbolicName to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual BundleDTO's symbolicName is not
	 *             equal to the given one.
	 */
	public S hasSymbolicName(String symbolicName) {
		// check that actual BundleDTO we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting symbolicName of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// null safe check
		String actualSymbolicName = actual.symbolicName;
		if (!java.util.Objects.deepEquals(actualSymbolicName, symbolicName)) {
			failWithMessage(assertjErrorMessage, actual, symbolicName, actualSymbolicName);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual BundleDTO's version is equal to the given one.
	 *
	 * @param version the given version to compare the actual BundleDTO's
	 *            version to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual BundleDTO's version is not equal
	 *             to the given one.
	 */
	public S hasVersion(String version) {
		// check that actual BundleDTO we want to make assertions on is not
		// null.
		isNotNull();

		// overrides the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting version of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

		// null safe check
		String actualVersion = actual.version;
		if (!java.util.Objects.deepEquals(actualVersion, version)) {
			failWithMessage(assertjErrorMessage, actual, version, actualVersion);
		}

		// return the current assertion for method chaining
		return myself;
	}

}
