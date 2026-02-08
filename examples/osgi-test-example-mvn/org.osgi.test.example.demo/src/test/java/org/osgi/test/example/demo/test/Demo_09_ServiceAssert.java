package org.osgi.test.example.demo.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.assertj.serviceevent.ServiceEventAssert;
import org.osgi.test.assertj.serviceregistration.ServiceRegistrationAssert;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.dictionary.Dictionaries;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.example.demo.api.EchoService;

public class Demo_09_ServiceAssert {

	@InjectBundleContext
	BundleContext bc;
	@InjectService
	EchoService echoService;

	@Test
	void serviceInMethodUsage(@InjectService(filter = "(x=y)", cardinality = 0) ServiceAware<EchoService> sa) throws Exception {

		List<ServiceEvent> events = new ArrayList<>();
		bc.addServiceListener(new ServiceListener() {
			@Override
			public void serviceChanged(ServiceEvent event) {
				events.add(event);
			}
		}, "(x=y)");

		EchoService newEs = new EchoService() {
			@Override
			public String echo(String text) {
				return "nope";
			};
		};

		ServiceRegistration<EchoService> sReg = bc.registerService(EchoService.class, newEs, Dictionaries.dictionaryOf("x", "y"));

		new ServiceRegistrationAssert<>(sReg)//
				.hasServiceReferenceThat()// ServiceReferenceAssert
				.hasServicePropertiesThat()// DictionaryAssert
				.containsEntry("x", "y");

		sa.waitForService(1000);

		ServiceEvent serviceEvent = events.get(0);

		new ServiceEventAssert(serviceEvent)//
				.hasServiceReference(sReg.getReference())//
				.isOfType(ServiceEvent.REGISTERED);

	}

}
