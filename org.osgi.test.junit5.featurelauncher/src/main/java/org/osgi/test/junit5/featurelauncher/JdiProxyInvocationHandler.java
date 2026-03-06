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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.dto.DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

/**
 * {@link InvocationHandler} that proxies an OSGi service in a remote VM.
 * <p>
 * Each method call on the proxy suspends the remote VM, invokes the
 * corresponding method via JDI, transforms the return value, and resumes the
 * VM. Parameters and return values are transformed between local Java objects
 * and remote JDI values.
 * <p>
 * Supported types for transformation:
 * <ul>
 * <li>All primitive types and their wrapper types</li>
 * <li>{@link String} and {@code String[]}</li>
 * <li>Subclasses of {@link DTO} (using {@link JdiDtoReader})</li>
 * <li>{@link List}, {@link Collection}, {@link Set}, {@link Iterable}</li>
 * <li>{@link Map} and {@link Dictionary}</li>
 * <li>Arrays of any supported element type</li>
 * <li>Interface types (auto-proxied via JDI)</li>
 * <li>Proxy objects created by this handler (pass-through of
 * ObjectReference)</li>
 * </ul>
 */
class JdiProxyInvocationHandler implements InvocationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(JdiProxyInvocationHandler.class);

	private final JdiFrameworkBridge bridge;
	private final VirtualMachine vm;
	private final ObjectReference serviceRef;

	JdiProxyInvocationHandler(JdiFrameworkBridge bridge, ObjectReference serviceRef) {
		this.bridge = bridge;
		this.vm = bridge.vm();
		this.serviceRef = serviceRef;
	}

	ObjectReference getServiceRef() {
		return serviceRef;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Object.class) {
			return handleObjectMethod(method, proxy, args);
		}

		vm.suspend();
		try {
			ThreadReference thread = bridge.acquireEventSuspendedThread();
			List<Value> jdiArgs = convertParameters(method, args);
			Value result;
			if (jdiArgs.isEmpty()) {
				result = bridge.invokeNoArgMethod(serviceRef, method.getName(), thread);
			} else {
				result = bridge.invokeMethodWithArgs(serviceRef, method.getName(), jdiArgs, thread);
			}
			return transformReturnValue(method, result, thread);
		} finally {
			vm.resume();
		}
	}

	private Object handleObjectMethod(Method method, Object proxy, Object[] args) {
		return switch (method.getName()) {
		case "toString" -> {
			vm.suspend();
			try {
				ThreadReference thread = bridge.acquireEventSuspendedThread();
				Value result = bridge.invokeNoArgMethod(serviceRef, "toString", thread);
				yield result instanceof StringReference sr ? sr.value()
						: "JdiServiceProxy[" + serviceRef.referenceType().name() + "]";
			} finally {
				vm.resume();
			}
		}
		case "hashCode" -> System.identityHashCode(proxy);
		case "equals" -> proxy == args[0];
		default -> throw new UnsupportedOperationException(method.getName());
		};
	}

	private List<Value> convertParameters(Method method, Object[] args) {
		if (args == null || args.length == 0) {
			return Collections.emptyList();
		}
		List<Value> jdiArgs = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			jdiArgs.add(convertParameter(method, i, args[i]));
		}
		return jdiArgs;
	}

	private Value convertParameter(Method method, int parameterIndex, Object arg) {
		if (arg == null) {
			return null;
		}
		// Proxy pass-through: extract ObjectReference from our proxies
		if (Proxy.isProxyClass(arg.getClass())) {
			InvocationHandler handler = Proxy.getInvocationHandler(arg);
			if (handler instanceof JdiProxyInvocationHandler jdiHandler) {
				return jdiHandler.getServiceRef();
			}
		}
		// String
		if (arg instanceof String s) {
			return vm.mirrorOf(s);
		}
		// Primitives and wrappers
		if (arg instanceof Long l) {
			return vm.mirrorOf(l);
		}
		if (arg instanceof Integer i) {
			return vm.mirrorOf(i);
		}
		if (arg instanceof Boolean b) {
			return vm.mirrorOf(b);
		}
		if (arg instanceof Short s) {
			return vm.mirrorOf(s);
		}
		if (arg instanceof Byte b) {
			return vm.mirrorOf(b);
		}
		if (arg instanceof Character c) {
			return vm.mirrorOf(c);
		}
		if (arg instanceof Float f) {
			return vm.mirrorOf(f);
		}
		if (arg instanceof Double d) {
			return vm.mirrorOf(d);
		}
		// DTO: look up previously returned reference
		if (arg instanceof DTO dto) {
			return bridge.lookupDtoReference(dto);
		}
		throw JdiProxyException.forParameter(serviceRef.referenceType().name(), method, parameterIndex, arg);
	}

	@SuppressWarnings("unchecked")
	private Object transformReturnValue(Method method, Value result, ThreadReference thread) {
		Class<?> returnType = method.getReturnType();

		// void
		if (returnType == void.class || returnType == Void.class) {
			return null;
		}

		// null result for reference types
		if (result == null) {
			if (returnType.isPrimitive()) {
				throw new IllegalStateException(
						"Remote method " + method.getName() + " returned null for primitive return type " + returnType);
			}
			return null;
		}

		// Primitive types and wrappers
		Object primitive = tryExtractPrimitive(returnType, result);
		if (primitive != null) {
			return primitive;
		}

		// String
		if (returnType == String.class) {
			return extractString(result);
		}

		// DTO subclass
		if (DTO.class.isAssignableFrom(returnType)) {
			if (result instanceof ObjectReference ref) {
				return bridge.readAndRegisterDto((Class<? extends DTO>) returnType, ref, thread);
			}
			return null;
		}

		// Map or Dictionary
		if ((Map.class.isAssignableFrom(returnType) || Dictionary.class.isAssignableFrom(returnType))
				&& result instanceof ObjectReference ref) {
			Type[] kvTypes = extractTypeArguments(method.getGenericReturnType(), 2);
			Class<?> keyType = kvTypes != null ? toClass(kvTypes[0]) : Object.class;
			Class<?> valueType = kvTypes != null ? toClass(kvTypes[1]) : Object.class;
			// Check if the remote object implements java.util.Map
			boolean isMap = implementsInterface(ref, "java.util.Map");
			Map<Object, Object> map = isMap ? readMap(ref, keyType, valueType, thread)
					: readDictionary(ref, keyType, valueType, thread);
			if (Dictionary.class.isAssignableFrom(returnType) && !Map.class.isAssignableFrom(returnType)) {
				return mapToDictionary(map);
			}
			return map;
		}

		// List, Collection, Set, Iterable
		if ((Collection.class.isAssignableFrom(returnType) || Iterable.class.isAssignableFrom(returnType))
				&& result instanceof ObjectReference ref) {
			Type[] elTypes = extractTypeArguments(method.getGenericReturnType(), 1);
			Class<?> elementType = elTypes != null ? toClass(elTypes[0]) : Object.class;
			List<Object> list = readCollection(ref, elementType, thread);
			if (Set.class.isAssignableFrom(returnType)) {
				return new LinkedHashSet<>(list);
			}
			return list;
		}

		// Arrays
		if (returnType.isArray() && result instanceof ArrayReference arr) {
			return readArray(returnType.getComponentType(), arr, thread);
		}

		// Object.class (unknown target) — inspect JDI value type
		if (returnType == Object.class) {
			return transformUntyped(result, thread);
		}

		// Interface return type — auto-proxy the remote ObjectReference
		if (returnType.isInterface() && result instanceof ObjectReference ref) {
			return createAutoProxy(returnType, ref);
		}

		throw JdiProxyException.forReturnType(serviceRef.referenceType().name(), method, result);
	}

	@SuppressWarnings("unchecked")
	private Object transformElement(Value value, Class<?> targetType, ThreadReference thread) {
		if (value == null) {
			return null;
		}

		// Primitive wrappers
		Object primitive = tryExtractPrimitive(targetType, value);
		if (primitive != null) {
			return primitive;
		}

		// String
		if (targetType == String.class) {
			return extractString(value);
		}

		// DTO
		if (DTO.class.isAssignableFrom(targetType) && value instanceof ObjectReference ref) {
			return bridge.readAndRegisterDto((Class<? extends DTO>) targetType, ref, thread);
		}

		// Interface → auto-proxy
		if (targetType.isInterface() && value instanceof ObjectReference ref) {
			return createAutoProxy(targetType, ref);
		}

		// Object.class (unknown target) — inspect JDI value type
		if (targetType == Object.class) {
			return transformUntyped(value, thread);
		}

		// Fallback for concrete classes — try to read as a value
		if (value instanceof ObjectReference ref) {
			return createAutoProxy(targetType, ref);
		}

		throw new JdiProxyException("Cannot transform element of target type '" + targetType.getName()
				+ "' from JDI value '" + value.getClass().getSimpleName() + "'.");
	}

	/**
	 * Transforms a JDI Value when the target type is unknown (Object.class).
	 * Inspects the JDI value type to determine the appropriate local type.
	 */
	private Object transformUntyped(Value value, ThreadReference thread) {
		if (value instanceof StringReference sr) {
			return sr.value();
		}
		if (value instanceof IntegerValue iv) {
			return iv.value();
		}
		if (value instanceof LongValue lv) {
			return lv.value();
		}
		if (value instanceof BooleanValue bv) {
			return bv.value();
		}
		if (value instanceof ByteValue bv) {
			return bv.value();
		}
		if (value instanceof ShortValue sv) {
			return sv.value();
		}
		if (value instanceof CharValue cv) {
			return cv.value();
		}
		if (value instanceof FloatValue fv) {
			return fv.value();
		}
		if (value instanceof DoubleValue dv) {
			return dv.value();
		}
		if (value instanceof ArrayReference arr) {
			// Untyped array — transform each element as Object
			List<Object> elements = new ArrayList<>();
			for (Value v : arr.getValues()) {
				elements.add(v != null ? transformUntyped(v, thread) : null);
			}
			return elements.toArray();
		}
		if (value instanceof ObjectReference ref) {
			// Check if it's a String (boxed by the remote VM)
			String typeName = ref.referenceType().name();
			if ("java.lang.String".equals(typeName)) {
				return ref.toString();
			}
			// For other objects — try to extract as known wrappers
			if (typeName.startsWith("java.lang.")) {
				Value unboxed = tryUnbox(ref, thread);
				if (unboxed != null) {
					return transformUntyped(unboxed, thread);
				}
			}
			return ref;
		}
		return null;
	}

	/**
	 * Tries to unbox a wrapper ObjectReference (e.g., java.lang.Integer) to its
	 * primitive JDI value by calling the xxxValue() method.
	 */
	private Value tryUnbox(ObjectReference ref, ThreadReference thread) {
		String typeName = ref.referenceType().name();
		String valueMethod = switch (typeName) {
		case "java.lang.Integer" -> "intValue";
		case "java.lang.Long" -> "longValue";
		case "java.lang.Boolean" -> "booleanValue";
		case "java.lang.Byte" -> "byteValue";
		case "java.lang.Short" -> "shortValue";
		case "java.lang.Character" -> "charValue";
		case "java.lang.Float" -> "floatValue";
		case "java.lang.Double" -> "doubleValue";
		default -> null;
		};
		if (valueMethod == null) {
			return null;
		}
		try {
			return bridge.invokeNoArgMethod(ref, valueMethod, thread);
		} catch (Exception e) {
			LOG.debug("Failed to unbox {}: {}", typeName, e.getMessage());
			return null;
		}
	}

	/**
	 * Reads a remote Collection/List/Set into a local List by calling
	 * {@code toArray()} on the remote collection and transforming each element.
	 */
	private List<Object> readCollection(ObjectReference collectionRef, Class<?> elementType, ThreadReference thread) {
		try {
			com.sun.jdi.Method toArray = JdiDtoReader.findMethod(collectionRef.referenceType(),
					JdiOsgiConstants.Collection.METHOD_TO_ARRAY, 0);
			Value arrayValue = collectionRef.invokeMethod(thread, toArray, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);
			if (!(arrayValue instanceof ArrayReference arr)) {
				return Collections.emptyList();
			}
			List<Object> result = new ArrayList<>();
			for (Value element : arr.getValues()) {
				result.add(transformElement(element, elementType, thread));
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read collection", e);
		}
	}

	/**
	 * Reads a remote Map into a local LinkedHashMap by calling
	 * {@code entrySet().toArray()} and transforming each key/value pair.
	 */
	private Map<Object, Object> readMap(ObjectReference mapRef, Class<?> keyType, Class<?> valueType,
			ThreadReference thread) {
		try {
			// Call entrySet()
			Value entrySetValue = bridge.invokeNoArgMethod(mapRef, JdiOsgiConstants.Map.METHOD_ENTRY_SET, thread);
			if (!(entrySetValue instanceof ObjectReference entrySetRef)) {
				return Collections.emptyMap();
			}
			// Call toArray() on the entry set
			com.sun.jdi.Method toArray = JdiDtoReader.findMethod(entrySetRef.referenceType(),
					JdiOsgiConstants.Collection.METHOD_TO_ARRAY, 0);
			Value arrayValue = entrySetRef.invokeMethod(thread, toArray, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);
			if (!(arrayValue instanceof ArrayReference arr)) {
				return Collections.emptyMap();
			}
			Map<Object, Object> result = new LinkedHashMap<>();
			for (Value entry : arr.getValues()) {
				if (!(entry instanceof ObjectReference entryRef)) {
					continue;
				}
				Value keyVal = bridge.invokeNoArgMethod(entryRef, JdiOsgiConstants.MapEntry.METHOD_GET_KEY, thread);
				Value valVal = bridge.invokeNoArgMethod(entryRef, JdiOsgiConstants.MapEntry.METHOD_GET_VALUE, thread);
				Object key = transformElement(keyVal, keyType, thread);
				Object value = transformElement(valVal, valueType, thread);
				result.put(key, value);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read map", e);
		}
	}

	/**
	 * Reads a remote Dictionary (non-Map) into a local LinkedHashMap by iterating
	 * {@code keys()} and calling {@code get()} for each key.
	 */
	private Map<Object, Object> readDictionary(ObjectReference dictRef, Class<?> keyType, Class<?> valueType,
			ThreadReference thread) {
		try {
			// Call keys() → Enumeration
			Value keysValue = bridge.invokeNoArgMethod(dictRef, "keys", thread);
			if (!(keysValue instanceof ObjectReference keysRef)) {
				return Collections.emptyMap();
			}
			Map<Object, Object> result = new LinkedHashMap<>();
			// Iterate via hasMoreElements() / nextElement()
			while (true) {
				Value hasMore = bridge.invokeNoArgMethod(keysRef, "hasMoreElements", thread);
				if (!(hasMore instanceof com.sun.jdi.BooleanValue bv) || !bv.value()) {
					break;
				}
				Value keyVal = bridge.invokeNoArgMethod(keysRef, "nextElement", thread);
				Object key = transformElement(keyVal, keyType, thread);
				// Call dict.get(key)
				Value valVal = bridge.invokeMethodWithArgs(dictRef, "get", List.of(keyVal), thread);
				Object value = transformElement(valVal, valueType, thread);
				result.put(key, value);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read dictionary", e);
		}
	}

	/**
	 * Checks whether a remote ObjectReference implements a given interface.
	 */
	private static boolean implementsInterface(ObjectReference ref, String interfaceName) {
		for (com.sun.jdi.InterfaceType iface : ((com.sun.jdi.ClassType) ref.referenceType()).allInterfaces()) {
			if (iface.name().equals(interfaceName)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Dictionary<?, ?> mapToDictionary(Map<Object, Object> map) {
		Hashtable ht = new Hashtable<>();
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				ht.put(entry.getKey(), entry.getValue());
			}
		}
		return ht;
	}

	/**
	 * Reads a remote array into a local typed array, transforming each element.
	 */
	@SuppressWarnings("unchecked")
	private Object readArray(Class<?> componentType, ArrayReference arr, ThreadReference thread) {
		List<Value> values = arr.getValues();
		Object result = Array.newInstance(componentType, values.size());
		for (int i = 0; i < values.size(); i++) {
			Object element = transformElement(values.get(i), componentType, thread);
			Array.set(result, i, element);
		}
		return result;
	}

	/**
	 * Creates a new JDI proxy for any interface type wrapping the given remote
	 * ObjectReference.
	 */
	@SuppressWarnings("unchecked")
	private <T> T createAutoProxy(Class<?> iface, ObjectReference ref) {
		if (!iface.isInterface()) {
			// For concrete classes we cannot create a proxy — return the
			// ObjectReference itself (caller gets a raw JDI value)
			LOG.debug("Cannot auto-proxy concrete class '{}', returning ObjectReference", iface.getName());
			return (T) ref;
		}
		JdiProxyInvocationHandler handler = new JdiProxyInvocationHandler(bridge, ref);
		return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] { iface }, handler);
	}

	/**
	 * Tries to extract a primitive/wrapper value. Returns null if the return type
	 * is not a primitive or wrapper.
	 */
	private static Object tryExtractPrimitive(Class<?> type, Value v) {
		if (type == int.class || type == Integer.class) {
			return extractInt(v);
		}
		if (type == long.class || type == Long.class) {
			return extractLong(v);
		}
		if (type == boolean.class || type == Boolean.class) {
			return extractBoolean(v);
		}
		if (type == byte.class || type == Byte.class) {
			return extractByte(v);
		}
		if (type == short.class || type == Short.class) {
			return extractShort(v);
		}
		if (type == char.class || type == Character.class) {
			return extractChar(v);
		}
		if (type == float.class || type == Float.class) {
			return extractFloat(v);
		}
		if (type == double.class || type == Double.class) {
			return extractDouble(v);
		}
		return null;
	}

	private static String extractString(Value v) {
		if (v instanceof StringReference sr) {
			return sr.value();
		}
		return null;
	}

	private static int extractInt(Value v) {
		if (v instanceof IntegerValue iv) {
			return iv.value();
		}
		throw new IllegalStateException("Expected IntegerValue but got: " + v);
	}

	private static long extractLong(Value v) {
		if (v instanceof LongValue lv) {
			return lv.value();
		}
		throw new IllegalStateException("Expected LongValue but got: " + v);
	}

	private static boolean extractBoolean(Value v) {
		if (v instanceof BooleanValue bv) {
			return bv.value();
		}
		throw new IllegalStateException("Expected BooleanValue but got: " + v);
	}

	private static byte extractByte(Value v) {
		if (v instanceof ByteValue bv) {
			return bv.value();
		}
		throw new IllegalStateException("Expected ByteValue but got: " + v);
	}

	private static short extractShort(Value v) {
		if (v instanceof ShortValue sv) {
			return sv.value();
		}
		throw new IllegalStateException("Expected ShortValue but got: " + v);
	}

	private static char extractChar(Value v) {
		if (v instanceof CharValue cv) {
			return cv.value();
		}
		throw new IllegalStateException("Expected CharValue but got: " + v);
	}

	private static float extractFloat(Value v) {
		if (v instanceof FloatValue fv) {
			return fv.value();
		}
		throw new IllegalStateException("Expected FloatValue but got: " + v);
	}

	private static double extractDouble(Value v) {
		if (v instanceof DoubleValue dv) {
			return dv.value();
		}
		throw new IllegalStateException("Expected DoubleValue but got: " + v);
	}

	/**
	 * Extracts type arguments from a generic type. Returns null if the type is not
	 * parameterized or has fewer arguments than expected.
	 */
	private static Type[] extractTypeArguments(Type genericType, int expectedCount) {
		if (!(genericType instanceof ParameterizedType pt)) {
			return null;
		}
		Type[] typeArgs = pt.getActualTypeArguments();
		if (typeArgs.length < expectedCount) {
			return null;
		}
		return typeArgs;
	}

	/**
	 * Converts a reflection Type to a Class. Falls back to Object.class for
	 * wildcard and type variable types.
	 */
	private static Class<?> toClass(Type type) {
		if (type instanceof Class<?> c) {
			return c;
		}
		return Object.class;
	}
}
