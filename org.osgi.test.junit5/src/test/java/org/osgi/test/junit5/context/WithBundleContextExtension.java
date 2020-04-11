/*
 * Copyright (c) OSGi Alliance (2019). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.test.junit5.context;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.framework.BundleContext;

public class WithBundleContextExtension implements AutoCloseable {
	private final ExtensionContext extensionContext;
	final BundleContextExtension	bundleContextExtension;

	public WithBundleContextExtension(ExtensionContext extensionContext) throws Exception {
		this.extensionContext = extensionContext;
		this.bundleContextExtension = new BundleContextExtension();
		this.bundleContextExtension.beforeEach(this.extensionContext);
	}

	@Override
	public void close() throws Exception {
		bundleContextExtension.afterEach(extensionContext);
	}

	public BundleContext getBundleContext() {
		return BundleContextExtension.getBundleContext(extensionContext);
	}

	public BundleContextExtension getExtension() {
		return bundleContextExtension;
	}

}