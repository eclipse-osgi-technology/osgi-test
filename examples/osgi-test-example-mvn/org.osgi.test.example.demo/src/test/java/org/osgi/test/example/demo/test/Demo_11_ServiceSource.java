package org.osgi.test.example.demo.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Dictionary;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.osgi.framework.ServiceReference;
import org.osgi.test.assertj.dictionary.DictionaryAssert;
import org.osgi.test.assertj.servicereference.ServiceReferenceAssert;
import org.osgi.test.example.demo.api.EchoService;
import org.osgi.test.junit5.service.ServiceSource;

public class Demo_11_ServiceSource {

	@ParameterizedTest
	@ServiceSource(serviceType = EchoService.class, filter = "(type=*)")
	void echoService(EchoService echoService) throws Exception {
		assertThat(echoService.echo("a")).isNotNull();
	}

	@ParameterizedTest
	@ServiceSource(serviceType = EchoService.class)
	void serviceReference(ServiceReference<EchoService> sr) throws Exception {
		ServiceReferenceAssert.assertThat(sr)//
				.hasServicePropertiesThat()//
				.containsKeys("type");

	}

	@ParameterizedTest
	@ServiceSource(serviceType = EchoService.class)
	void serviceDictionary(Dictionary<String, Object> dict) throws Exception {
		DictionaryAssert.assertThat(dict).containsKeys("type");
	}

	@ParameterizedTest
	@ServiceSource(serviceType = EchoService.class)
	void serviceMap(Map<String, Object> map) throws Exception {
		assertThat(map).containsKey("type");
	}
}
