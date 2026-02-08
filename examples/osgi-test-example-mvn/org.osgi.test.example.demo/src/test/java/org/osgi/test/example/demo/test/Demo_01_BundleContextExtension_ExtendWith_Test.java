package org.osgi.test.example.demo.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;

@ExtendWith(BundleContextExtension.class)
public class Demo_01_BundleContextExtension_ExtendWith_Test {

	@InjectBundleContext
	BundleContext bc;

	@Test
	void bundleContextExists() throws Exception {
		assertThat(bc).isNotNull();
	}

}
