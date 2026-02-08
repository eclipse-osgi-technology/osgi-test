package org.osgi.test.example.demo.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

public class Demo_03_InjectBundleContext_Test {

	@InjectBundleContext
	BundleContext bc1;

	@Test
	void bundleContextExists() throws Exception {
		assertThat(bc1).isNotNull();
	}

	@Test
	void bundleContextExists(@InjectBundleContext BundleContext bc2) throws Exception {
		assertThat(bc2).isNotNull();
		bc2.addBundleListener(event -> System.out.println(event));
	}
}
