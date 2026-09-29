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

package org.osgi.test.assertj.permission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 * Serialization round trip that resolves classes through the class loader of
 * the serialized object first. In OSGi the default resolution would use the
 * class loader of this bundle, which does not see permission classes of other
 * bundles.
 */
final class Serialization {
	private Serialization() {}

	static Object roundTrip(Object object) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(object);
		}
		ClassLoader loader = object.getClass()
			.getClassLoader();
		try (ObjectInputStream in = new LoaderObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()),
			loader)) {
			return in.readObject();
		}
	}

	private static final class LoaderObjectInputStream extends ObjectInputStream {
		private final ClassLoader loader;

		LoaderObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
			super(in);
			this.loader = loader;
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			if (loader != null) {
				try {
					return Class.forName(desc.getName(), false, loader);
				} catch (ClassNotFoundException e) {
					// fall back to the default resolution
				}
			}
			return super.resolveClass(desc);
		}
	}
}
