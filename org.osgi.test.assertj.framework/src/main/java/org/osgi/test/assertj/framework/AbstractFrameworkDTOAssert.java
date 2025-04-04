package org.osgi.test.assertj.framework;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.internal.Iterables;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;

/**
 * Abstract base class for {@link FrameworkDTO} specific assertions
 */

public abstract class AbstractFrameworkDTOAssert<S extends AbstractFrameworkDTOAssert<S, A>, A extends FrameworkDTO>
	extends AbstractObjectAssert<S, A> {

	/**
	 * Creates a new <code>{@link AbstractFrameworkDTOAssert}</code> to make
	 * assertions on actual FrameworkDTO.
	 *
	 * @param actual the FrameworkDTO we want to make assertions on.
	 */
	protected AbstractFrameworkDTOAssert(A actual, Class<S> selfType) {
		super(actual, selfType);
	}

	/**
	 * Verifies that the actual FrameworkDTO's bundles contains the given
	 * BundleDTO elements.
	 *
	 * @param bundles the given elements that should be contained in actual
	 *            FrameworkDTO's bundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles does not
	 *             contain all given BundleDTO elements.
	 */
	public S hasBundles(BundleDTO... bundles) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given BundleDTO varargs is not null.
		if (bundles == null)
			failWithMessage("Expecting bundles parameter not to be null.");

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContains(info, actual.bundles, bundles);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's bundles contains the given
	 * BundleDTO elements in Collection.
	 *
	 * @param bundles the given elements that should be contained in actual
	 *            FrameworkDTO's bundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles does not
	 *             contain all given BundleDTO elements.
	 */
	public S hasBundles(java.util.Collection<? extends BundleDTO> bundles) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given BundleDTO collection is not null.
		if (bundles == null) {
			failWithMessage("Expecting bundles parameter not to be null.");
			return myself; // to fool Eclipse "Null pointer access" warning on
							// toArray.
		}

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContains(info, actual.bundles, bundles.toArray());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's bundles contains <b>only</b> the
	 * given BundleDTO elements and nothing else in whatever order.
	 *
	 * @param bundles the given elements that should be contained in actual
	 *            FrameworkDTO's bundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles does not
	 *             contain all given BundleDTO elements.
	 */
	public S hasOnlyBundles(BundleDTO... bundles) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given BundleDTO varargs is not null.
		if (bundles == null)
			failWithMessage("Expecting bundles parameter not to be null.");

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContainsOnly(info, actual.bundles, bundles);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's bundles contains <b>only</b> the
	 * given BundleDTO elements in Collection and nothing else in whatever
	 * order.
	 *
	 * @param bundles the given elements that should be contained in actual
	 *            FrameworkDTO's bundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles does not
	 *             contain all given BundleDTO elements.
	 */
	public S hasOnlyBundles(java.util.Collection<? extends BundleDTO> bundles) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given BundleDTO collection is not null.
		if (bundles == null) {
			failWithMessage("Expecting bundles parameter not to be null.");
			return myself; // to fool Eclipse "Null pointer access" warning on
							// toArray.
		}

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContainsOnly(info, actual.bundles, bundles.toArray());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's bundles does not contain the
	 * given BundleDTO elements.
	 *
	 * @param bundles the given elements that should not be in actual
	 *            FrameworkDTO's bundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles contains any
	 *             given BundleDTO elements.
	 */
	public S doesNotHaveBundles(BundleDTO... bundles) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given BundleDTO varargs is not null.
		if (bundles == null)
			failWithMessage("Expecting bundles parameter not to be null.");

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Iterables.instance()
			.assertDoesNotContain(info, actual.bundles, bundles);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's bundles does not contain the
	 * given BundleDTO elements in Collection.
	 *
	 * @param bundles the given elements that should not be in actual
	 *            FrameworkDTO's bundles.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles contains any
	 *             given BundleDTO elements.
	 */
	public S doesNotHaveBundles(java.util.Collection<? extends BundleDTO> bundles) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given BundleDTO collection is not null.
		if (bundles == null) {
			failWithMessage("Expecting bundles parameter not to be null.");
			return myself; // to fool Eclipse "Null pointer access" warning on
							// toArray.
		}

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Iterables.instance()
			.assertDoesNotContain(info, actual.bundles, bundles.toArray());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO has no bundles.
	 *
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's bundles is not empty.
	 */
	public S hasNoBundles() {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// we override the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting :\n  <%s>\nnot to have bundles but had :\n  <%s>";

		// check
		if (actual.bundles.iterator()
			.hasNext()) {
			failWithMessage(assertjErrorMessage, actual, actual.bundles);
		}

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's properties is equal to the given
	 * one.
	 *
	 * @param properties the given properties to compare the actual
	 *            FrameworkDTO's properties to.
	 * @return this assertion object.
	 * @throws AssertionError - if the actual FrameworkDTO's properties is not
	 *             equal to the given one.
	 */
	public S hasProperties(java.util.Map<String, Object> properties) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
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
	 * Verifies that the actual FrameworkDTO's services contains the given
	 * ServiceReferenceDTO elements.
	 *
	 * @param services the given elements that should be contained in actual
	 *            FrameworkDTO's services.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services does not
	 *             contain all given ServiceReferenceDTO elements.
	 */
	public S hasServices(ServiceReferenceDTO... services) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given ServiceReferenceDTO varargs is not null.
		if (services == null)
			failWithMessage("Expecting services parameter not to be null.");

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContains(info, actual.services, services);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's services contains the given
	 * ServiceReferenceDTO elements in Collection.
	 *
	 * @param services the given elements that should be contained in actual
	 *            FrameworkDTO's services.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services does not
	 *             contain all given ServiceReferenceDTO elements.
	 */
	public S hasServices(java.util.Collection<? extends ServiceReferenceDTO> services) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given ServiceReferenceDTO collection is not null.
		if (services == null) {
			failWithMessage("Expecting services parameter not to be null.");
			return myself; // to fool Eclipse "Null pointer access" warning on
							// toArray.
		}

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContains(info, actual.services, services.toArray());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's services contains <b>only</b> the
	 * given ServiceReferenceDTO elements and nothing else in whatever order.
	 *
	 * @param services the given elements that should be contained in actual
	 *            FrameworkDTO's services.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services does not
	 *             contain all given ServiceReferenceDTO elements.
	 */
	public S hasOnlyServices(ServiceReferenceDTO... services) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given ServiceReferenceDTO varargs is not null.
		if (services == null)
			failWithMessage("Expecting services parameter not to be null.");

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContainsOnly(info, actual.services, services);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's services contains <b>only</b> the
	 * given ServiceReferenceDTO elements in Collection and nothing else in
	 * whatever order.
	 *
	 * @param services the given elements that should be contained in actual
	 *            FrameworkDTO's services.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services does not
	 *             contain all given ServiceReferenceDTO elements.
	 */
	public S hasOnlyServices(java.util.Collection<? extends ServiceReferenceDTO> services) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given ServiceReferenceDTO collection is not null.
		if (services == null) {
			failWithMessage("Expecting services parameter not to be null.");
			return myself; // to fool Eclipse "Null pointer access" warning on
							// toArray.
		}

		// check with standard error message, to set another message call:
		// info.overridingErrorMessage("my error message");
		Iterables.instance()
			.assertContainsOnly(info, actual.services, services.toArray());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's services does not contain the
	 * given ServiceReferenceDTO elements.
	 *
	 * @param services the given elements that should not be in actual
	 *            FrameworkDTO's services.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services contains any
	 *             given ServiceReferenceDTO elements.
	 */
	public S doesNotHaveServices(ServiceReferenceDTO... services) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given ServiceReferenceDTO varargs is not null.
		if (services == null)
			failWithMessage("Expecting services parameter not to be null.");

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Iterables.instance()
			.assertDoesNotContain(info, actual.services, services);

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO's services does not contain the
	 * given ServiceReferenceDTO elements in Collection.
	 *
	 * @param services the given elements that should not be in actual
	 *            FrameworkDTO's services.
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services contains any
	 *             given ServiceReferenceDTO elements.
	 */
	public S doesNotHaveServices(java.util.Collection<? extends ServiceReferenceDTO> services) {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// check that given ServiceReferenceDTO collection is not null.
		if (services == null) {
			failWithMessage("Expecting services parameter not to be null.");
			return myself; // to fool Eclipse "Null pointer access" warning on
							// toArray.
		}

		// check with standard error message (use overridingErrorMessage before
		// contains to set your own message).
		Iterables.instance()
			.assertDoesNotContain(info, actual.services, services.toArray());

		// return the current assertion for method chaining
		return myself;
	}

	/**
	 * Verifies that the actual FrameworkDTO has no services.
	 *
	 * @return this assertion object.
	 * @throws AssertionError if the actual FrameworkDTO's services is not
	 *             empty.
	 */
	public S hasNoServices() {
		// check that actual FrameworkDTO we want to make assertions on is not
		// null.
		isNotNull();

		// we override the default error message with a more explicit one
		String assertjErrorMessage = "\nExpecting :\n  <%s>\nnot to have services but had :\n  <%s>";

		// check
		if (actual.services.iterator()
			.hasNext()) {
			failWithMessage(assertjErrorMessage, actual, actual.services);
		}

		// return the current assertion for method chaining
		return myself;
	}

}
