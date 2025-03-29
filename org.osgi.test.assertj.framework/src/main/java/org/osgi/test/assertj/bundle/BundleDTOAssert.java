package org.osgi.test.assertj.bundle;

import org.osgi.framework.dto.BundleDTO;

/**
 * {@link BundleDTO} specific assertions Although this class is not final to
 * allow Soft assertions proxy, if you wish to extend it, extend
 * {@link AbstractBundleDTOAssert} instead.
 */

public class BundleDTOAssert extends AbstractBundleDTOAssert<BundleDTOAssert, BundleDTO> {

	/**
	 * Creates a new <code>{@link BundleDTOAssert}</code> to make assertions on
	 * actual BundleDTO.
	 *
	 * @param actual the BundleDTO we want to make assertions on.
	 */
	public BundleDTOAssert(BundleDTO actual) {
		super(actual, BundleDTOAssert.class);
	}

	/**
	 * An entry point for BundleDTOAssert to follow AssertJ standard
	 * <code>assertThat()</code> statements.<br>
	 * With a static import, one can write directly:
	 * <code>assertThat(myBundleDTO)</code> and get specific assertion with code
	 * completion.
	 *
	 * @param actual the BundleDTO we want to make assertions on.
	 * @return a new <code>{@link BundleDTOAssert}</code>
	 */
	public static BundleDTOAssert assertThat(BundleDTO actual) {
		return new BundleDTOAssert(actual);
	}
}
