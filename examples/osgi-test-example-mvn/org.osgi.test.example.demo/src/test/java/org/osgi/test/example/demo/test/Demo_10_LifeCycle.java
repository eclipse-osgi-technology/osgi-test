package org.osgi.test.example.demo.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.example.demo.api.EchoService;

public class Demo_10_LifeCycle {

	@InjectService
	static ServiceAware<EchoService> serviceAware;

	@BeforeAll
	static void beforeAll(@InjectBundleContext BundleContext bc) {
		// changes made here will persist until the afterAll phase completes
		assertThat(serviceAware.size()).isEqualTo(2);
		// simple and complex
		bc.registerService(EchoService.class, text -> "beforeAll", null);
		assertThat(serviceAware.size()).isEqualTo(3);
	}

	@BeforeEach
	void beforeEach(@InjectBundleContext BundleContext bc) {
		// changes made here will persist until the afterEach phase completes
		assertThat(serviceAware.size()).isEqualTo(3);
		// simple and complex
		bc.registerService(EchoService.class, text -> "beforeAll", null);
		assertThat(serviceAware.size()).isEqualTo(4);
	}

	@Test
	void test1(@InjectBundleContext BundleContext bc) throws Exception {
		// changes made here will persist until the afterEach phase completes
		assertThat(serviceAware.size()).isEqualTo(4);
		// simple and complex
		bc.registerService(EchoService.class, text -> "test1", null);
		assertThat(serviceAware.size()).isEqualTo(5);
	}

	@Nested
	class NestedTests {

		@Test
		void test1(@InjectBundleContext BundleContext bc) throws Exception {

			assertThat(serviceAware.size()).isEqualTo(4);
			// simple and complex
			bc.registerService(EchoService.class, text -> "test1", null);
			assertThat(serviceAware.size()).isEqualTo(5);
		}
	}

	@Test
	void test2(@InjectBundleContext BundleContext bc) throws Exception {

		assertThat(serviceAware.size()).isEqualTo(4);
		// simple and complex
		bc.registerService(EchoService.class, text -> "test2", null);
		assertThat(serviceAware.size()).isEqualTo(5);
	}

	@AfterEach
	void afterEach(@InjectBundleContext BundleContext bc) {
		// changes made in Test persist until the afterEach phase completes
		assertThat(serviceAware.size()).isEqualTo(5);
	}

	@AfterAll
	static void afterAll(@InjectBundleContext BundleContext bc) {
		assertThat(serviceAware.size()).isEqualTo(3);
	}

}
