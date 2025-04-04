package org.osgi.test.assertj.framework;

import org.osgi.framework.dto.FrameworkDTO;

/**
 * {@link FrameworkDTO} specific assertions Although this class is not final to
 * allow Soft assertions proxy, if you wish to extend it, extend
 * {@link AbstractFrameworkDTOAssert} instead.
 */

public class FrameworkDTOAssert extends AbstractFrameworkDTOAssert<FrameworkDTOAssert, FrameworkDTO> {

	/**
	 * Creates a new <code>{@link FrameworkDTOAssert}</code> to make assertions
	 * on actual FrameworkDTO.
	 *
	 * @param actual the FrameworkDTO we want to make assertions on.
	 */
	public FrameworkDTOAssert(FrameworkDTO actual) {
		super(actual, FrameworkDTOAssert.class);
	}

	/**
	 * An entry point for FrameworkDTOAssert to follow AssertJ standard
	 * <code>assertThat()</code> statements.<br>
	 * With a static import, one can write directly:
	 * <code>assertThat(myFrameworkDTO)</code> and get specific assertion with
	 * code completion.
	 *
	 * @param actual the FrameworkDTO we want to make assertions on.
	 * @return a new <code>{@link FrameworkDTOAssert}</code>
	 */
	public static FrameworkDTOAssert assertThat(FrameworkDTO actual) {
		return new FrameworkDTOAssert(actual);
	}
}
