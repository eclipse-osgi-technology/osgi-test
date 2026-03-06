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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.dto.DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

class JdiDtoReader {

	private static final Logger LOG = LoggerFactory.getLogger(JdiDtoReader.class);

	/**
	 * Generically reads any OSGi DTO from a remote ObjectReference using Java
	 * reflection to discover fields and their types.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DTO> T readDto(Class<T> dtoClass, ObjectReference ref, ThreadReference thread) {
		try {
			T dto = dtoClass.getConstructor().newInstance();
			for (java.lang.reflect.Field f : dtoClass.getFields()) {
				if (Modifier.isStatic(f.getModifiers())) {
					continue;
				}
				String fieldName = f.getName();
				Class<?> type = f.getType();

				// Skip fields not present in the remote object (older runtime)
				if (ref.referenceType().fieldByName(fieldName) == null) {
					continue;
				}

				try {
					if (type == int.class) {
						f.setInt(dto, readInt(ref, fieldName));
					} else if (type == long.class) {
						f.setLong(dto, readLong(ref, fieldName));
					} else if (type == boolean.class) {
						f.setBoolean(dto, readBoolean(ref, fieldName));
					} else if (type == String.class) {
						f.set(dto, readString(ref, fieldName));
					} else if (type == Integer.class) {
						f.set(dto, readBoxedInt(ref, fieldName));
					} else if (type == String[].class) {
						f.set(dto, readStringArray(ref, fieldName));
					} else if (type == long[].class) {
						f.set(dto, readLongArray(ref, fieldName));
					} else if (type == Map.class) {
						f.set(dto, readStringObjectMap(ref, fieldName, thread));
					} else if (DTO.class.isAssignableFrom(type)) {
						f.set(dto, readNestedDto(type.asSubclass(DTO.class), ref, fieldName, thread));
					} else if (type == List.class) {
						f.set(dto, readDtoList(f, ref, fieldName, thread));
					} else if (type.isArray() && DTO.class.isAssignableFrom(type.getComponentType())) {
						f.set(dto, readDtoArray(type.getComponentType().asSubclass(DTO.class), ref, fieldName, thread));
					}
				} catch (Exception e) {
					LOG.debug("Failed to read field '{}' on {}: {}", fieldName, dtoClass.getSimpleName(),
							e.getMessage());
				}
			}
			return dto;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to instantiate " + dtoClass.getName(), e);
		}
	}

	public static Integer readBoxedInt(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			return null;
		}
		Value value = obj.getValue(field);
		if (value == null) {
			return null;
		}
		if (value instanceof IntegerValue iv) {
			return iv.value();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T extends DTO> T readNestedDto(Class<T> dtoClass, ObjectReference parent, String fieldName,
			ThreadReference thread) {
		Field field = parent.referenceType().fieldByName(fieldName);
		if (field == null) {
			return null;
		}
		Value value = parent.getValue(field);
		if (value == null || !(value instanceof ObjectReference nestedRef)) {
			return null;
		}
		return readDto(dtoClass, nestedRef, thread);
	}

	@SuppressWarnings("unchecked")
	private static <T extends DTO> List<T> readDtoList(java.lang.reflect.Field reflectField, ObjectReference parent,
			String fieldName, ThreadReference thread) {
		java.lang.reflect.Type genericType = reflectField.getGenericType();
		if (!(genericType instanceof ParameterizedType pt)) {
			return Collections.emptyList();
		}
		java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
		if (typeArgs.length == 0 || !(typeArgs[0] instanceof Class<?> elementClass)) {
			return Collections.emptyList();
		}
		if (!DTO.class.isAssignableFrom(elementClass)) {
			return Collections.emptyList();
		}
		Class<T> dtoElementClass = (Class<T>) elementClass.asSubclass(DTO.class);

		List<ObjectReference> refs = readList(parent, fieldName, thread);
		List<T> result = new ArrayList<>();
		for (ObjectReference ref : refs) {
			result.add(readDto(dtoElementClass, ref, thread));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T extends DTO> T[] readDtoArray(Class<T> elementClass, ObjectReference parent, String fieldName,
			ThreadReference thread) {
		Field field = parent.referenceType().fieldByName(fieldName);
		if (field == null) {
			return (T[]) Array.newInstance(elementClass, 0);
		}
		Value value = parent.getValue(field);
		if (value == null || !(value instanceof ArrayReference arr)) {
			return (T[]) Array.newInstance(elementClass, 0);
		}
		List<Value> values = arr.getValues();
		T[] result = (T[]) Array.newInstance(elementClass, values.size());
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) instanceof ObjectReference elemRef) {
				result[i] = readDto(elementClass, elemRef, thread);
			}
		}
		return result;
	}

	public static int readInt(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value instanceof IntegerValue iv) {
			return iv.value();
		}
		throw new IllegalStateException("Field '" + fieldName + "' is not an int: " + value);
	}

	public static long readLong(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value instanceof LongValue lv) {
			return lv.value();
		}
		throw new IllegalStateException("Field '" + fieldName + "' is not a long: " + value);
	}

	public static boolean readBoolean(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value instanceof BooleanValue bv) {
			return bv.value();
		}
		throw new IllegalStateException("Field '" + fieldName + "' is not a boolean: " + value);
	}

	public static String readString(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value == null) {
			return null;
		}
		if (value instanceof StringReference sr) {
			return sr.value();
		}
		throw new IllegalStateException("Field '" + fieldName + "' is not a String: " + value);
	}

	public static long[] readLongArray(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value == null) {
			return new long[0];
		}
		if (value instanceof ArrayReference arr) {
			List<Value> values = arr.getValues();
			long[] result = new long[values.size()];
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) instanceof LongValue lv) {
					result[i] = lv.value();
				}
			}
			return result;
		}
		throw new IllegalStateException("Field '" + fieldName + "' is not a long[]: " + value);
	}

	public static List<ObjectReference> readList(ObjectReference obj, String fieldName, ThreadReference thread) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value == null) {
			return Collections.emptyList();
		}
		if (!(value instanceof ObjectReference listRef)) {
			throw new IllegalStateException("Field '" + fieldName + "' is not an object: " + value);
		}

		// Call toArray() on the list
		Method toArray = findMethod(listRef.referenceType(), JdiOsgiConstants.Collection.METHOD_TO_ARRAY, 0);
		try {
			Value arrayValue = listRef.invokeMethod(thread, toArray, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);
			if (arrayValue instanceof ArrayReference arr) {
				List<ObjectReference> result = new ArrayList<>();
				for (Value element : arr.getValues()) {
					if (element instanceof ObjectReference or) {
						result.add(or);
					}
				}
				return result;
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke toArray() on list field '" + fieldName + "'", e);
		}
		return Collections.emptyList();
	}

	public static Map<String, Object> readStringObjectMap(ObjectReference obj, String fieldName,
			ThreadReference thread) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value == null) {
			return Collections.emptyMap();
		}
		if (!(value instanceof ObjectReference mapRef)) {
			throw new IllegalStateException("Field '" + fieldName + "' is not an object: " + value);
		}

		Map<String, Object> result = new HashMap<>();
		try {
			// Call entrySet()
			Method entrySet = findMethod(mapRef.referenceType(), JdiOsgiConstants.Map.METHOD_ENTRY_SET, 0);
			Value setValue = mapRef.invokeMethod(thread, entrySet, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);

			if (!(setValue instanceof ObjectReference setRef)) {
				return result;
			}

			// Call toArray() on the set
			Method toArray = findMethod(setRef.referenceType(), JdiOsgiConstants.Collection.METHOD_TO_ARRAY, 0);
			Value arrayValue = setRef.invokeMethod(thread, toArray, Collections.emptyList(),
					ObjectReference.INVOKE_SINGLE_THREADED);

			if (arrayValue instanceof ArrayReference arr) {
				for (Value entryVal : arr.getValues()) {
					if (entryVal instanceof ObjectReference entryRef) {
						Method getKey = findMethod(entryRef.referenceType(), JdiOsgiConstants.MapEntry.METHOD_GET_KEY,
								0);
						Method getValue = findMethod(entryRef.referenceType(),
								JdiOsgiConstants.MapEntry.METHOD_GET_VALUE, 0);

						Value keyVal = entryRef.invokeMethod(thread, getKey, Collections.emptyList(),
								ObjectReference.INVOKE_SINGLE_THREADED);
						Value valVal = entryRef.invokeMethod(thread, getValue, Collections.emptyList(),
								ObjectReference.INVOKE_SINGLE_THREADED);

						String key = (keyVal instanceof StringReference sr) ? sr.value() : String.valueOf(keyVal);
						result.put(key, convertValue(valVal));
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read Map field '" + fieldName + "'", e);
		}

		return result;
	}

	public static Object convertValue(Value value) {
		if (value == null) {
			return null;
		}
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
		if (value instanceof DoubleValue dv) {
			return dv.value();
		}
		if (value instanceof FloatValue fv) {
			return fv.value();
		}
		if (value instanceof ShortValue sv) {
			return sv.value();
		}
		if (value instanceof ByteValue byv) {
			return byv.value();
		}
		if (value instanceof CharValue cv) {
			return cv.value();
		}
		if (value instanceof ArrayReference arr) {
			List<Value> elements = arr.getValues();
			// Check if all elements are strings
			boolean allStrings = elements.stream().allMatch(e -> e == null || e instanceof StringReference);
			if (allStrings) {
				String[] result = new String[elements.size()];
				for (int i = 0; i < elements.size(); i++) {
					Value e = elements.get(i);
					result[i] = e instanceof StringReference sr ? sr.value() : null;
				}
				return result;
			}
			// Convert each element recursively
			Object[] result = new Object[elements.size()];
			for (int i = 0; i < elements.size(); i++) {
				result[i] = convertValue(elements.get(i));
			}
			return result;
		}
		if (value instanceof ObjectReference or) {
			// Unwrap boxed primitives by reading their internal 'value' field
			String typeName = or.referenceType().name();
			Field valueField = or.referenceType().fieldByName("value");
			if (valueField != null) {
				Value inner = or.getValue(valueField);
				if (inner != null) {
					switch (typeName) {
					case "java.lang.Long":
						return ((LongValue) inner).value();
					case "java.lang.Integer":
						return ((IntegerValue) inner).value();
					case "java.lang.Boolean":
						return ((BooleanValue) inner).value();
					case "java.lang.Double":
						return ((DoubleValue) inner).value();
					case "java.lang.Float":
						return ((FloatValue) inner).value();
					case "java.lang.Short":
						return ((ShortValue) inner).value();
					case "java.lang.Byte":
						return ((ByteValue) inner).value();
					case "java.lang.Character":
						return ((CharValue) inner).value();
					}
				}
			}
			return or.referenceType().name() + "@" + or.uniqueID();
		}
		return value.toString();
	}

	public static Method findMethod(ReferenceType type, String name, int paramCount) {
		for (Method m : type.allMethods()) {
			if (m.name().equals(name) && m.argumentTypeNames().size() == paramCount) {
				return m;
			}
		}
		throw new IllegalArgumentException(
				"No method '" + name + "' with " + paramCount + " parameters on " + type.name());
	}

	/**
	 * Finds a method by name and exact argument type names. Used to disambiguate
	 * overloads like {@code getBundle(long)} vs {@code getBundle(String)}.
	 */
	public static Method findMethod(ReferenceType type, String name, List<String> argTypes) {
		for (Method m : type.allMethods()) {
			if (m.name().equals(name) && m.argumentTypeNames().equals(argTypes)) {
				return m;
			}
		}
		throw new IllegalArgumentException("No method '" + name + "' with args " + argTypes + " on " + type.name());
	}

	public static String[] readStringArray(ObjectReference obj, String fieldName) {
		Field field = obj.referenceType().fieldByName(fieldName);
		if (field == null) {
			throw new IllegalArgumentException("No field '" + fieldName + "' on " + obj.referenceType().name());
		}
		Value value = obj.getValue(field);
		if (value == null) {
			return new String[0];
		}
		if (value instanceof ArrayReference arr) {
			List<Value> values = arr.getValues();
			String[] result = new String[values.size()];
			for (int i = 0; i < values.size(); i++) {
				Value v = values.get(i);
				if (v instanceof StringReference sr) {
					result[i] = sr.value();
				} else if (v != null) {
					result[i] = v.toString();
				}
			}
			return result;
		}
		throw new IllegalStateException("Field '" + fieldName + "' is not a String[]: " + value);
	}
}
