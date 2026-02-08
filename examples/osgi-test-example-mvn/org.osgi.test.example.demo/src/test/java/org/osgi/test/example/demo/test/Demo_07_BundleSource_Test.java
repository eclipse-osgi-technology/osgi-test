package org.osgi.test.example.demo.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.test.assertj.bundle.BundleAssert;
import org.osgi.test.junit5.bundle.BundleSource;

public class Demo_07_BundleSource_Test {

	@ParameterizedTest
	@BundleSource
	void bundle(Bundle bundle) throws Exception {

		BundleAssert.assertThat(bundle)//
				.hasEntry("META-INF/MANIFEST.MF") // or a LICENSE File
				.isInStateMaskedBy(Bundle.ACTIVE | Bundle.RESOLVED).hasVersionThat()//
				.isBetween(Version.parseVersion("1"), Version.parseVersion("99"));
		// Check BundleHeaders
	}

	@ParameterizedTest
	@BundleSource(headerFilter = ("(Fragment-Host=*)"))
	void bundleByHeader(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)//
				.hasSymbolicName("org.osgi.test.example.demo-tests");

		// Check BundleHeaders
	}

	@ParameterizedTest
	@BundleSource(symbolicNamePattern = { "(.*)-tests" })
	void bundleBySymbolicNamePattern(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle)//
				.hasSymbolicName("org.osgi.test.example.demo-tests");
	}

	@ParameterizedTest
	@BundleSource(stateMask = Bundle.ACTIVE | Bundle.RESOLVED)
	void bundleByState(Bundle bundle) throws Exception {
		BundleAssert.assertThat(bundle).hasEntry("META-INF/MANIFEST.MF");
	}
}
