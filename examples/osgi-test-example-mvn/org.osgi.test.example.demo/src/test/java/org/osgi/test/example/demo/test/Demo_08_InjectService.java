package org.osgi.test.example.demo.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.InjectService.AnyService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.example.demo.api.EchoService;

public class Demo_08_InjectService {

	@InjectBundleContext
	BundleContext bc;
	@InjectService
	EchoService echoService;

	@Test
	void serviceInMethod() throws Exception {
		assertThat(echoService).isNotNull();
	}

	@Test
	void serviceInMethod(@InjectService EchoService es) throws Exception {
		assertThat(es).isNotNull();
	}

	@Test
	void serviceInMethod(@InjectService List<EchoService> ess) throws Exception {
		assertThat(ess).isNotNull().hasSize(2);
	}

	@Test
	void serviceInMethod(@InjectService ServiceAware<EchoService> sa) throws Exception {

		EchoService esWait = sa.waitForService(1000);

		EchoService esGet = sa.getService();

		List<EchoService> essGet = sa.getServices();

		Class<EchoService> es = sa.getServiceType();

		int size = sa.size();

		ServiceReference<EchoService> sr = sa.getServiceReference();

		List<ServiceReference<EchoService>> srs = sa.getServiceReferences();

		EchoService esOfSr = sa.getService(sr);

		SortedMap<ServiceReference<EchoService>, EchoService> tracked = sa.getTracked();
		assertThat(tracked).containsEntry(sr, esOfSr);

		int trackingCount = sa.getTrackingCount();

	}

	@Test
	void serviceInMethodUsage(@InjectService ServiceAware<EchoService> sa) throws Exception {

		assertThat(sa.getTrackingCount()).isEqualTo(2);
		assertThat(sa.size()).isEqualTo(2);

		EchoService newEs = new EchoService() {
			@Override
			public String echo(String text) {
				return "nope";
			};
		};

		ServiceRegistration<EchoService> sReg = bc.registerService(EchoService.class, newEs, Dictionaries.dictionaryOf("a", "b"));

		assertThat(sa.getTrackingCount()).isEqualTo(3);
		assertThat(sa.size()).isEqualTo(3);

		sReg.setProperties(Dictionaries.dictionaryOf("foo", "bar"));
		assertThat(sa.getTrackingCount()).isEqualTo(4);

		Optional<EchoService> optionalService = sa.getServiceReferences()//
				.parallelStream()//
				.filter(sr -> "bar".equals(sr.getProperty("foo")))//
				.findAny()//
				.map(sa::getService);

		assertThat(optionalService).contains(newEs);
	}

	@Test
	void serviceAnyService(@InjectService(service = AnyService.class, filter = "(type=simple)", cardinality = 2) ServiceAware<?> sa) throws Exception {
		assertThat(sa.size()).isEqualTo(2);
	}

	@Test
	void serviceAnyService(@InjectService(filter = "(type=simple)") EchoService simpleEchoService, //
			@InjectService(filter = "(type=complex)") EchoService complexEchoService, //
			@InjectService(filter = "(type=dummy)", cardinality = 0) ServiceAware<EchoService> saLater) throws Exception {

		assertThat(simpleEchoService.echo("a")).isEqualTo("a");
		assertThat(complexEchoService.echo("b")).isEqualTo("b b");

		bc.registerService(EchoService.class, text -> "x", Dictionaries.dictionaryOf("type", "dummy"));

		EchoService dummyEchoService = saLater.waitForService(1000);
		assertThat(dummyEchoService.echo("c")).isEqualTo("x");

	}

}
