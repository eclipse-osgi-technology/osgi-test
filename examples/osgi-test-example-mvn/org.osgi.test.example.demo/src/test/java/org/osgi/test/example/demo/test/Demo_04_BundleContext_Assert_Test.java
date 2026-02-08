package org.osgi.test.example.demo.test;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.assertj.bundlecontext.BundleContextAssert;
import org.osgi.test.common.annotation.InjectBundleContext;

public class Demo_04_BundleContext_Assert_Test {

	@InjectBundleContext
	BundleContext bc;

	@Test
	void bundleContextAssert() throws Exception {

		BundleContextAssert bcAssert = BundleContextAssert.assertThat(bc)//
				.hasBundleWithId(0)//
				.doesNotHaveBundleWithId(-1);

		bcAssert.hasProperty("org.osgi.service.http.port")//
				.doesNotHaveProperty("foo")//
				.hasPropertyWithKeyThat("org.osgi.service.http.port").isEqualTo("*");

		bcAssert.hasBundlesThat()//
				.hasSizeGreaterThanOrEqualTo(3);

		bcAssert.hasBundleWithIdThat(0).hasSymbolicName("org.eclipse.osgi");
	}

}
