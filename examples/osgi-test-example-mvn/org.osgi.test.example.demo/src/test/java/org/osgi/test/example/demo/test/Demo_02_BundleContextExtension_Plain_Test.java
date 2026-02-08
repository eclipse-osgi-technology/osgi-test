package org.osgi.test.example.demo.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

// @InjectBundleContext implicitly enables BundleContextExtension
public class Demo_02_BundleContextExtension_Plain_Test {

	@InjectBundleContext
	BundleContext bc;

	@Test
	void bundleContextExists() throws Exception {
		assertThat(bc).isNotNull();
	}

}
