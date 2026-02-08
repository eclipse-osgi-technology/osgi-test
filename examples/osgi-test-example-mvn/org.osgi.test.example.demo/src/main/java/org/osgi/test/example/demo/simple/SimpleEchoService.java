package org.osgi.test.example.demo.simple;

import org.osgi.service.component.annotations.Component;
import org.osgi.test.example.demo.api.EchoService;

@Component(property = "type=simple")
public class SimpleEchoService implements EchoService {

	@Override
	public String echo(String text) {
		return text;
	}

}
