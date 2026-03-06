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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Thrown when a JDI service proxy cannot transform a parameter or return value
 * between the local and remote VM.
 * <p>
 * Carries detailed debug information about the proxy call context: the remote
 * service type, the method being invoked, and exactly which parameter or return
 * type could not be transformed.
 * <p>
 * Use the static factory methods {@link #forParameter} and
 * {@link #forReturnType} to create instances with full context.
 */
class JdiProxyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String SUPPORTED_TYPES = "Supported types: void, primitives (int, long, boolean, byte, "
			+ "short, char, float, double), wrapper types, String, DTO subclasses, "
			+ "List, Collection, Set, Iterable (any element type), " + "Map, Dictionary, arrays of any supported type, "
			+ "interface types (auto-proxied), and JDI proxy objects.";

	JdiProxyException(String message) {
		super(message);
	}

	JdiProxyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an exception for a parameter that cannot be transformed.
	 *
	 * @param remoteServiceType the remote service type name (from JDI
	 *                          ObjectReference)
	 * @param method            the method being invoked
	 * @param parameterIndex    the 0-based index of the failing parameter
	 * @param actualValue       the actual parameter value
	 * @return a JdiProxyException with full debug context
	 */
	public static JdiProxyException forParameter(String remoteServiceType, Method method, int parameterIndex,
			Object actualValue) {

		Class<?> declaredType = method.getParameterTypes()[parameterIndex];
		Type genericType = method.getGenericParameterTypes()[parameterIndex];

		StringBuilder sb = new StringBuilder();
		sb.append("Cannot proxy parameter for remote service call.\n");
		sb.append("\n");
		sb.append("  Remote service : ").append(remoteServiceType).append('\n');
		sb.append("  Method         : ").append(formatMethodSignature(method)).append('\n');
		sb.append("  Parameter      : index ").append(parameterIndex).append(" (")
				.append(method.getParameters()[parameterIndex].getName()).append(")\n");
		sb.append("  Declared type  : ").append(genericType.getTypeName()).append('\n');
		sb.append("  Actual type    : ").append(actualValue.getClass().getName()).append('\n');
		sb.append("  Actual value   : ").append(abbreviate(String.valueOf(actualValue), 200)).append('\n');
		sb.append("\n");
		sb.append("  ").append(SUPPORTED_TYPES);

		return new JdiProxyException(sb.toString());
	}

	/**
	 * Creates an exception for a return type that cannot be transformed.
	 *
	 * @param remoteServiceType the remote service type name (from JDI
	 *                          ObjectReference)
	 * @param method            the method being invoked
	 * @param jdiValue          the JDI Value returned by the remote method (may be
	 *                          null)
	 * @return a JdiProxyException with full debug context
	 */
	public static JdiProxyException forReturnType(String remoteServiceType, Method method, Object jdiValue) {

		Class<?> declaredReturn = method.getReturnType();
		Type genericReturn = method.getGenericReturnType();

		StringBuilder sb = new StringBuilder();
		sb.append("Cannot proxy return value from remote service call.\n");
		sb.append("\n");
		sb.append("  Remote service : ").append(remoteServiceType).append('\n');
		sb.append("  Method         : ").append(formatMethodSignature(method)).append('\n');
		sb.append("  Declared return: ").append(genericReturn.getTypeName()).append('\n');
		if (jdiValue != null) {
			sb.append("  JDI value type : ").append(jdiValue.getClass().getSimpleName()).append('\n');
			sb.append("  JDI value      : ").append(abbreviate(String.valueOf(jdiValue), 200)).append('\n');
		} else {
			sb.append("  JDI value      : null\n");
		}
		if (declaredReturn.isArray()) {
			sb.append("  Array component: ").append(declaredReturn.getComponentType().getName()).append('\n');
		}
		sb.append("\n");
		sb.append("  ").append(SUPPORTED_TYPES);

		return new JdiProxyException(sb.toString());
	}

	private static String formatMethodSignature(Method method) {
		String params = Arrays.stream(method.getGenericParameterTypes()).map(Type::getTypeName)
				.collect(Collectors.joining(", "));
		return method.getDeclaringClass().getSimpleName() + "." + method.getName() + "(" + params + "): "
				+ method.getGenericReturnType().getTypeName();
	}

	private static String abbreviate(String s, int maxLen) {
		if (s.length() <= maxLen) {
			return s;
		}
		return s.substring(0, maxLen - 3) + "...";
	}
}
