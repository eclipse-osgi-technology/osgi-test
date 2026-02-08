package org.osgi.test.example.demo.complex;

import org.osgi.service.component.annotations.Component;
import org.osgi.test.example.demo.api.EchoService;

@Component(property = "type=complex")
public class ComplexEchoService implements EchoService {

	@Override
	public String echo(String text) {
		return text +" "+ text;
	}

}
