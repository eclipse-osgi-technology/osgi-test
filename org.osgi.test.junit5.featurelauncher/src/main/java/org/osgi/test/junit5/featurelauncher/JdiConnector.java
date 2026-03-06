/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.junit5.featurelauncher;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

class JdiConnector {

	private static final Logger LOG = LoggerFactory.getLogger(JdiConnector.class);

	private static final String CONNECTOR_ARG_TIMEOUT = "timeout";
	private static final String CONNECTOR_ARG_HOSTNAME = "hostname";
	private static final String CONNECTOR_ARG_PORT = "port";
	private static final String TRANSPORT_DT_SOCKET = "dt_socket";

	private VirtualMachine vm;

	public VirtualMachine attach(int port, int timeoutMs, int maxRetries) throws IOException {
		AttachingConnector socketConnector = findSocketConnector();

		Map<String, Connector.Argument> args = socketConnector.defaultArguments();
		args.get(CONNECTOR_ARG_TIMEOUT).setValue(String.valueOf(timeoutMs));
		args.get(CONNECTOR_ARG_HOSTNAME).setValue("localhost");
		args.get(CONNECTOR_ARG_PORT).setValue(String.valueOf(port));

		IOException lastException = null;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				vm = socketConnector.attach(args);
				LOG.info("Connected to VM: {} (Version {}) on port {}", vm.name(), vm.version(), port);
				return vm;
			} catch (IOException e) {
				lastException = e;
				LOG.debug("Attach attempt {}/{} failed: {}", attempt, maxRetries, e.getMessage());
				if (attempt < maxRetries) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new IOException("Interrupted while waiting to retry attach", ie);
					}
				}
			} catch (IllegalConnectorArgumentsException e) {
				throw new IOException("Invalid connector arguments", e);
			}
		}

		throw new IOException("Failed to attach to VM on port " + port + " after " + maxRetries + " attempts",
				lastException);
	}

	public void resume() {
		if (vm != null) {
			vm.resume();
		}
	}

	public void dispose() {
		if (vm != null) {
			try {
				vm.dispose();
				LOG.info("JDI connection disposed");
			} catch (Exception e) {
				LOG.warn("Error disposing JDI connection: {}", e.getMessage());
			}
			vm = null;
		}
	}

	public VirtualMachine getVm() {
		return vm;
	}

	private static AttachingConnector findSocketConnector() {
		for (AttachingConnector connector : Bootstrap.virtualMachineManager().attachingConnectors()) {
			if (connector.transport().name().equals(TRANSPORT_DT_SOCKET)) {
				return connector;
			}
		}
		throw new IllegalStateException("No " + TRANSPORT_DT_SOCKET + " AttachingConnector found in JDI");
	}
}
