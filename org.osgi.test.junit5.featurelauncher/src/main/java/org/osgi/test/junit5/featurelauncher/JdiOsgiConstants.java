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

/**
 * String constants for OSGi class names and method names used during JDI
 * reflection. Centralises all remote-VM identifiers in one place so typos are
 * caught at compile time.
 * <p>
 * DTO field names are no longer needed here — the generic
 * {@link JdiDtoReader#readDto(Class, com.sun.jdi.ObjectReference, com.sun.jdi.ThreadReference)}
 * method discovers field names via Java reflection.
 */
final class JdiOsgiConstants {

	private JdiOsgiConstants() {
	}

	static final class Framework {
		static final String CLASS_NAME = "org.osgi.framework.launch.Framework";
		static final int ACTIVE = 32;

		private Framework() {
		}
	}

	static final class Bundle {
		static final String METHOD_ADAPT = "adapt";
		static final String METHOD_GET_STATE = "getState";
		static final String METHOD_LOAD_CLASS = "loadClass";
		static final String METHOD_GET_BUNDLE_CONTEXT = "getBundleContext";
		static final String METHOD_GET_SYMBOLIC_NAME = "getSymbolicName";
		static final String METHOD_GET_VERSION = "getVersion";

		private Bundle() {
		}
	}

	static final class BundleContext {
		static final String METHOD_GET_BUNDLE = "getBundle";
		static final String METHOD_GET_BUNDLES = "getBundles";
		static final String METHOD_GET_SERVICE_REFERENCES = "getServiceReferences";
		static final String METHOD_GET_SERVICE = "getService";
		static final String METHOD_UNGET_SERVICE = "ungetService";

		private BundleContext() {
		}
	}

	static final class FrameworkDTO {
		static final String CLASS_NAME = "org.osgi.framework.dto.FrameworkDTO";

		private FrameworkDTO() {
		}
	}

	static final class FrameworkStartLevelDTO {
		static final String CLASS_NAME = "org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO";

		private FrameworkStartLevelDTO() {
		}
	}

	static final class Collection {
		static final String METHOD_TO_ARRAY = "toArray";

		private Collection() {
		}
	}

	static final class Map {
		static final String METHOD_ENTRY_SET = "entrySet";

		private Map() {
		}
	}

	static final class MapEntry {
		static final String METHOD_GET_KEY = "getKey";
		static final String METHOD_GET_VALUE = "getValue";

		private MapEntry() {
		}
	}
}
