package org.osgi.test.example.demo.simple;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = Object.class, property = {"type=simple", "f=b"}, scope = ServiceScope.PROTOTYPE)
public class SimpleService {

	@Activate
	private void act() throws InterruptedException {

	}

}
