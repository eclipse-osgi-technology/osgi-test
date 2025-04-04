package org.osgi.test.assertj.servicereference;

import org.osgi.framework.dto.ServiceReferenceDTO;

/**
 * {@link ServiceReferenceDTO} specific assertions Although this class is not
 * final to allow Soft assertions proxy, if you wish to extend it, extend
 * {@link AbstractServiceReferenceDTOAssert} instead.
 */

public class ServiceReferenceDTOAssert
	extends AbstractServiceReferenceDTOAssert<ServiceReferenceDTOAssert, ServiceReferenceDTO> {

	/**
	 * Creates a new <code>{@link ServiceReferenceDTOAssert}</code> to make
	 * assertions on actual ServiceReferenceDTO.
	 *
	 * @param actual the ServiceReferenceDTO we want to make assertions on.
	 */
	public ServiceReferenceDTOAssert(ServiceReferenceDTO actual) {
		super(actual, ServiceReferenceDTOAssert.class);
	}

	/**
	 * An entry point for ServiceReferenceDTOAssert to follow AssertJ standard
	 * <code>assertThat()</code> statements.<br>
	 * With a static import, one can write directly:
	 * <code>assertThat(myServiceReferenceDTO)</code> and get specific assertion
	 * with code completion.
	 *
	 * @param actual the ServiceReferenceDTO we want to make assertions on.
	 * @return a new <code>{@link ServiceReferenceDTOAssert}</code>
	 */
	public static ServiceReferenceDTOAssert assertThat(ServiceReferenceDTO actual) {
		return new ServiceReferenceDTOAssert(actual);
	}
}
