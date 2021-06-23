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

package org.osgi.test.common.test.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.dictionary.Dictionaries;

public class DictionariesTestCase {

	Dictionary<String, String>	dict;
	Dictionary<String, String>	asDictionary;
	Dictionary<String, Object>	asDictionarySr;

	Map<String, String>			map;
	Map<String, String>			asMap;
	Hashtable<String, String>	hashtable;

	ServiceReference<?>			sr;
	Map<String, String>			asMapEmpty;
	Dictionary<String, String>	asDictionaryEmpty;

	List<String>				srKeys;
	List<String>				srValues;

	@BeforeEach
	public void setUp() throws Exception {
		dict = new TestDictionary<>();
		dict.put("key1", "value1");
		dict.put("key2", "value2");
		dict.put("key3", "value3");
		dict.put("key4", "value4");
		dict.put("key5", "value5");

		map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");
		map.put("key4", "value4");
		map.put("key5", "value5");

		sr = mock(ServiceReference.class);

		srKeys = Arrays.asList("key1", "key2", "key3", "key4", "key5");
		srValues = Arrays.asList("value1", "value2", "value3", "value4", "value5");

		when(sr.getPropertyKeys()).thenReturn(map.keySet()
			.stream()
			.toArray(String[]::new));

		for (Entry<String, String> entry : map.entrySet()) {
			when(sr.getProperty(entry.getKey())).thenReturn(entry.getValue());
		}

		hashtable = new Hashtable<>(map);

		asMap = Dictionaries.asMap(dict);
		asDictionary = Dictionaries.asDictionary(map);
		asDictionarySr = Dictionaries.asDictionary(sr);

		asMapEmpty = Dictionaries.asMap(new TestDictionary<>());
		asDictionaryEmpty = Dictionaries.asDictionary(new HashMap<>());
	}

	@Test
	public void not_same_as() {
		assertThat(asDictionary).isNotSameAs(map);
		assertThat(asMap).isNotSameAs(dict);
		assertThat(asDictionarySr).isNotSameAs(sr);
	}

	@Test
	public void same_as() {
		assertThat(Dictionaries.asDictionary(hashtable)).isSameAs(hashtable);
		assertThat(Dictionaries.asMap(hashtable)).isSameAs(hashtable);
	}

	@Test
	public void keys() {
		assertThat(asMap.keySet()
			.isEmpty()).isEqualTo(dict.isEmpty());
		assertThat(asMap.keySet()
			.size()).isEqualTo(dict.size());
		assertThat(asMap.keySet()).containsExactlyInAnyOrderElementsOf(Collections.list(dict.keys()));

		assertThat(Collections.list(asDictionary.keys())).hasSize(map.size())
			.containsExactlyInAnyOrderElementsOf(map.keySet());

		assertThat(Collections.list(asDictionarySr.keys())).hasSize(map.size())
			.containsExactlyInAnyOrderElementsOf(map.keySet());
	}

	@Test
	public void values() {
		assertThat(asMap.values()
			.isEmpty()).isEqualTo(dict.isEmpty());
		assertThat(asMap.values()
			.size()).isEqualTo(dict.size());
		assertThat(asMap.values()).containsExactlyInAnyOrderElementsOf(Collections.list(dict.elements()));

		assertThat(Collections.list(asDictionary.elements())).hasSize(map.size())
			.containsExactlyInAnyOrderElementsOf(map.values());

		assertThat(Collections.list(asDictionarySr.elements())).hasSize(map.size())
			.containsExactlyInAnyOrderElementsOf(map.values());
	}

	@Test
	public void empty() {
		assertThat(asMap.isEmpty()).isEqualTo(dict.isEmpty());
		assertThat(asMapEmpty.isEmpty()).isTrue();

		assertThat(asDictionary.isEmpty()).isEqualTo(map.isEmpty());
		assertThat(asDictionarySr.isEmpty()).isEqualTo(map.isEmpty());
		assertThat(asDictionaryEmpty.isEmpty()).isTrue();

	}

	@Test
	public void size() {
		assertThat(asMap.size()).isEqualTo(dict.size());
		assertThat(asMapEmpty.size()).isZero();

		assertThat(asDictionary.size()).isEqualTo(map.size());
		assertThat(asDictionarySr.size()).isEqualTo(map.size());
		assertThat(asDictionaryEmpty.size()).isZero();

	}

	@Test
	public void get() {
		assertThat(asMap.get("foo")).isNull();
		assertThat(asMapEmpty.get("foo")).isNull();
		for (String key : Collections.list(dict.keys())) {
			assertThat(asMap.get(key)).isSameAs(dict.get(key));
		}

		assertThat(asDictionary.get("foo")).isNull();
		assertThat(asDictionarySr.get("foo")).isNull();
		assertThat(asDictionaryEmpty.get("foo")).isNull();
		for (String key : map.keySet()) {
			assertThat(asDictionary.get(key)).isSameAs(map.get(key));
			assertThat(asDictionarySr.get(key)).isSameAs(map.get(key));
		}
	}

	@Test
	public void put() {
		int size = dict.size();
		String value = dict.get("key1");
		assertThat(asMap.put("key1", "value11")).isSameAs(value);
		assertThat(asMap.get("key1")).isSameAs(dict.get("key1"))
			.isEqualTo("value11");
		assertThat(asMap.size()).isEqualTo(dict.size())
			.isEqualTo(size);
		assertThat(asMap.put("key6", "value6")).isNull();
		assertThat(asMap.get("key6")).isSameAs(dict.get("key6"))
			.isEqualTo("value6");
		assertThat(asMap.size()).isEqualTo(dict.size())
			.isEqualTo(size + 1);

		size = map.size();
		value = map.get("key1");
		assertThat(asDictionary.put("key1", "value11")).isSameAs(value);
		assertThat(asDictionary.get("key1")).isSameAs(map.get("key1"))
			.isEqualTo("value11");
		assertThat(asDictionary.size()).isEqualTo(map.size())
			.isEqualTo(size);
		assertThat(asDictionary.put("key6", "value6")).isNull();
		assertThat(asDictionary.get("key6")).isSameAs(map.get("key6"))
			.isEqualTo("value6");
		assertThat(asDictionary.size()).isEqualTo(map.size())
			.isEqualTo(size + 1);

		assertThatThrownBy(() -> {
			asDictionarySr.put("key6", "value6");
		}).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void remove() {
		assertThat(asMap.remove("foo")).isNull();
		assertThat(asMapEmpty.remove("foo")).isNull();
		for (String key : Collections.list(dict.keys())) {
			String value = dict.get(key);
			assertThat(asMap.remove(key)).isEqualTo(value);
			assertThat(asMap.containsKey(key)).isFalse();
			assertThat(asMap.size()).isEqualTo(dict.size());
			assertThat(dict.get(key)).isNull();
		}
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();

		assertThat(asDictionary.remove("foo")).isNull();
		assertThat(asDictionaryEmpty.remove("foo")).isNull();
		for (String key : new ArrayList<>(map.keySet())) {
			String value = map.get(key);
			assertThat(asDictionary.remove(key)).isEqualTo(value);
			assertThat(asDictionary.get(key)).isNull();
			assertThat(asDictionary.size()).isEqualTo(map.size());
			assertThat(map.containsKey(key)).isFalse();
		}
		assertThat(asDictionary.isEmpty()).isTrue();
		assertThat(asDictionary.size()).isZero();

		assertThatThrownBy(() -> {
			asDictionarySr.remove("key1");
		}).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void null_checks() {
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.asMap(null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.asDictionary((Map<?, ?>) null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.asDictionary((ServiceReference<?>) null);
		});
		Map<String, String> nullKeyMap = new HashMap<>();
		nullKeyMap.put(null, "value");
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.asDictionary(nullKeyMap);
		});
		Map<String, String> nullValueMap = new HashMap<>();
		nullValueMap.put("key", null);
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.asDictionary(nullValueMap);
		});

		assertThat(asMap.containsKey(null)).isFalse();
		assertThat(asMap.get(null)).isNull();
		assertThat(asMap.remove(null)).isNull();
		assertThat(asDictionary.get(null)).isNull();
		assertThat(asDictionary.remove(null)).isNull();
		assertThat(asDictionarySr.get(null)).isNull();

		assertThatNullPointerException().isThrownBy(() -> {
			asMap.put(null, "value1");
		});
		assertThatNullPointerException().isThrownBy(() -> {
			asMap.put("key1", null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			asDictionary.put(null, "value1");
		});
		assertThatNullPointerException().isThrownBy(() -> {
			asDictionary.put("key1", null);
		});

		Entry<String, String> nullKeyEntry = new SimpleEntry<>(null, "value");
		assertThat(asMap.entrySet()
			.contains(nullKeyEntry)).isFalse();
		assertThat(asMap.entrySet()
			.remove(nullKeyEntry)).isFalse();
		Entry<String, String> nullValueEntry = new SimpleEntry<>("key", null);
		assertThat(asMap.entrySet()
			.contains(nullValueEntry)).isFalse();
		assertThat(asMap.entrySet()
			.remove(nullValueEntry)).isFalse();
		Entry<String, String> entry = asMap.entrySet()
			.iterator()
			.next();
		assertThatNullPointerException().isThrownBy(() -> {
			entry.setValue(null);
		});
	}

	@Test
	public void no_null_key_or_value_map() {
		Map<String, String> noNullKeyValueMap = new ConcurrentHashMap<>();
		assertThatNullPointerException().isThrownBy(() -> {
			noNullKeyValueMap.containsKey(null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			noNullKeyValueMap.containsValue(null);
		});
		assertThatCode(() -> {
			Dictionaries.asDictionary(noNullKeyValueMap);
		}).doesNotThrowAnyException();
	}

	@Test
	public void containsKey() {
		Object o = Integer.valueOf(1);
		assertThat(asMap.containsKey(o)).isFalse();
		assertThat(asMap.containsKey("foo")).isFalse();
		for (String key : Collections.list(dict.keys())) {
			assertThat(asMap.containsKey(key)).isTrue();
		}
	}

	@Test
	public void containsValue() {
		Object o = Integer.valueOf(1);
		assertThat(asMap.containsValue(o)).isFalse();
		assertThat(asMap.containsValue("foo")).isFalse();
		for (String value : Collections.list(dict.elements())) {
			assertThat(asMap.containsValue(value)).isTrue();
		}
	}

	@Test
	public void clear() {
		asMap.clear();
		assertThat(asMap.size()).isZero();
		assertThat(dict.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void entrySet() {
		assertThat(asMap.entrySet()
			.isEmpty()).isEqualTo(dict.isEmpty());
		assertThat(asMap.entrySet()
			.size()).isEqualTo(dict.size());
		assertThat(asMap.entrySet()).allMatch(entry -> dict.get(entry.getKey()) == entry.getValue());
		assertThat(asMap.entrySet()).extracting(Entry::getKey)
			.containsExactlyInAnyOrderElementsOf(Collections.list(dict.keys()));
		assertThat(asMap.entrySet()).extracting(Entry::getValue)
			.containsExactlyInAnyOrderElementsOf(Collections.list(dict.elements()));
	}

	@Test
	public void entrySet_contains() {
		Set<Entry<String, String>> entrySet = asMap.entrySet();
		Object o = "foo";
		assertThat(entrySet.contains(o)).isFalse();
		assertThat(entrySet.contains(new SimpleEntry<>("key", "value"))).isFalse();
		for (String key : Collections.list(dict.keys())) {
			String value = dict.get(key);
			Entry<String, String> entry = new SimpleEntry<>(key, value);
			assertThat(entrySet.contains(entry)).isTrue();
		}
	}

	@Test
	public void entrySet_remove() {
		Set<Entry<String, String>> entrySet = asMap.entrySet();
		Object o = "foo";
		assertThat(entrySet.remove(o)).isFalse();
		assertThat(entrySet.remove(new SimpleEntry<>("key", "value"))).isFalse();
		for (String key : Collections.list(dict.keys())) {
			String value = dict.get(key);
			Entry<String, String> entry = new SimpleEntry<>(key, value);
			assertThat(entrySet.remove(entry)).isTrue();
			assertThat(entrySet.contains(entry)).isFalse();
			assertThat(dict.get(key)).isNull();
		}
		assertThat(entrySet.size()).isZero();
		assertThat(entrySet.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void entrySet_clear() {
		Set<Entry<String, String>> entrySet = asMap.entrySet();
		entrySet.clear();
		assertThat(entrySet.size()).isZero();
		assertThat(entrySet.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void entrySet_iterator() {
		Set<Entry<String, String>> entrySet = asMap.entrySet();
		Iterator<Entry<String, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			assertThatIllegalStateException().isThrownBy(() -> {
				iterator.remove();
			});
			Entry<String, String> next = iterator.next();
			assertThat(dict.get(next.getKey())).isSameAs(next.getValue());
			iterator.remove();
			assertThat(dict.get(next.getKey())).isNull();
			assertThatIllegalStateException().isThrownBy(() -> {
				iterator.remove();
			});
		}
		assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
			iterator.next();
		});
		assertThatIllegalStateException().isThrownBy(() -> {
			iterator.remove();
		});
		assertThat(entrySet.size()).isZero();
		assertThat(entrySet.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void entrySet_setValue() {
		Set<Entry<String, String>> entrySet = asMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			assertThat(value).isSameAs(dict.get(key));
			String newValue = value + key;
			entry.setValue(newValue);
			assertThat(entry.getValue()).isSameAs(newValue)
				.isSameAs(dict.get(key));
		}
	}

	@Test
	public void keySet_contains() {
		Set<String> keySet = asMap.keySet();
		Object o = Integer.valueOf(1);
		assertThat(keySet.contains(o)).isFalse();
		assertThat(keySet.contains("foo")).isFalse();
		for (String key : Collections.list(dict.keys())) {
			assertThat(keySet.contains(key)).isTrue();
		}
	}

	@Test
	public void keySet_remove() {
		Set<String> keySet = asMap.keySet();
		Object o = Integer.valueOf(1);
		assertThat(keySet.remove(o)).isFalse();
		assertThat(keySet.remove("foo")).isFalse();
		for (String key : Collections.list(dict.keys())) {
			assertThat(keySet.remove(key)).isTrue();
			assertThat(keySet.contains(key)).isFalse();
			assertThat(dict.get(key)).isNull();
		}
		assertThat(keySet.size()).isZero();
		assertThat(keySet.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void keySet_clear() {
		Set<String> keySet = asMap.keySet();
		keySet.clear();
		assertThat(keySet.size()).isZero();
		assertThat(keySet.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void keySet_iterator() {
		Set<String> keySet = asMap.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			assertThatIllegalStateException().isThrownBy(() -> {
				iterator.remove();
			});
			String next = iterator.next();
			assertThat(dict.get(next)).isNotNull();
			iterator.remove();
			assertThat(dict.get(next)).isNull();
			assertThatIllegalStateException().isThrownBy(() -> {
				iterator.remove();
			});
		}
		assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> {
			iterator.next();
		});
		assertThatIllegalStateException().isThrownBy(() -> {
			iterator.remove();
		});
		assertThat(keySet.size()).isZero();
		assertThat(keySet.isEmpty()).isTrue();
		assertThat(asMap.size()).isZero();
		assertThat(asMap.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
		assertThat(dict.isEmpty()).isTrue();
	}

	@Test
	public void dictionaryOf_exceptions() {
		Supplier<Dictionary<String, String>> supplier = () -> Dictionaries.dictionaryOf("key1", "value1");
		assertThat(supplier.get()).isNotNull();
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
			supplier.get()
				.remove("key1");
		});
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
			supplier.get()
				.put("key2", "value2");
		});
		assertThatIllegalArgumentException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", "key1", "value2");
		});

		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf(null, "value1");
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", null, "value2");
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", "key2", null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", "key2", "value2", null, "value3");
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", "key2", "value2", "key3", null);
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", "key2", "value2", "key3", "value3", null, "value4");
		});
		assertThatNullPointerException().isThrownBy(() -> {
			Dictionaries.dictionaryOf("key1", "value1", "key2", "value2", "key3", "value3", "key4", null);
		});
	}

	@Test
	public void dictionaryOf_empty() {
		Dictionary<String, String> dict = Dictionaries.dictionaryOf();
		assertThat(dict).isNotNull()
			.isInstanceOf(Map.class);
		assertThat(dict.isEmpty()).isTrue();
		assertThat(dict.size()).isZero();
	}

	@Test
	public void dictionaryOf_1() {
		Dictionary<String, String> dict = Dictionaries.dictionaryOf("key1", "value1");
		assertThat(dict).isNotNull()
			.isInstanceOf(Map.class);
		assertThat(dict.isEmpty()).isFalse();
		assertThat(dict.size()).isEqualTo(1);
		assertThat(dict.get("key1")).isEqualTo("value1");
		assertThat(Collections.list(dict.keys())).hasSize(1)
			.containsExactly("key1");
		assertThat(Collections.list(dict.elements())).hasSize(1)
			.containsExactly("value1");
	}

	@Test
	public void dictionaryOf_2() {
		Dictionary<String, String> dict = Dictionaries.dictionaryOf("key1", "value1", "key2", "value2");
		assertThat(dict).isNotNull()
			.isInstanceOf(Map.class);
		assertThat(dict.isEmpty()).isFalse();
		assertThat(dict.size()).isEqualTo(2);
		assertThat(dict.get("key1")).isEqualTo("value1");
		assertThat(dict.get("key2")).isEqualTo("value2");
		assertThat(Collections.list(dict.keys())).hasSize(2)
			.containsExactly("key1", "key2");
		assertThat(Collections.list(dict.elements())).hasSize(2)
			.containsExactly("value1", "value2");
	}

	@Test
	public void dictionaryOf_3() {
		Dictionary<String, String> dict = Dictionaries.dictionaryOf("key1", "value1", "key2", "value2", "key3",
			"value3");
		assertThat(dict).isNotNull()
			.isInstanceOf(Map.class);
		assertThat(dict.isEmpty()).isFalse();
		assertThat(dict.size()).isEqualTo(3);
		assertThat(dict.get("key1")).isEqualTo("value1");
		assertThat(dict.get("key2")).isEqualTo("value2");
		assertThat(dict.get("key3")).isEqualTo("value3");
		assertThat(Collections.list(dict.keys())).hasSize(3)
			.containsExactly("key1", "key2", "key3");
		assertThat(Collections.list(dict.elements())).hasSize(3)
			.containsExactly("value1", "value2", "value3");
	}

	@Test
	public void dictionaryOf_4() {
		Dictionary<String, String> dict = Dictionaries.dictionaryOf("key1", "value1", "key2", "value2", "key3",
			"value3", "key4", "value4");
		assertThat(dict).isNotNull()
			.isInstanceOf(Map.class);
		assertThat(dict.isEmpty()).isFalse();
		assertThat(dict.size()).isEqualTo(4);
		assertThat(dict.get("key1")).isEqualTo("value1");
		assertThat(dict.get("key2")).isEqualTo("value2");
		assertThat(dict.get("key3")).isEqualTo("value3");
		assertThat(dict.get("key4")).isEqualTo("value4");
		assertThat(Collections.list(dict.keys())).hasSize(4)
			.containsExactly("key1", "key2", "key3", "key4");
		assertThat(Collections.list(dict.elements())).hasSize(4)
			.containsExactly("value1", "value2", "value3", "value4");
	}

	public static class TestDictionary<K, V> extends Dictionary<K, V> {
		private final Map<K, V> map;

		public TestDictionary() {
			this.map = new HashMap<>();
		}

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public Enumeration<K> keys() {
			return Collections.enumeration(map.keySet());
		}

		@Override
		public Enumeration<V> elements() {
			return Collections.enumeration(map.values());
		}

		@Override
		public V get(Object key) {
			if (key == null) {
				return null;
			}
			return map.get(key);
		}

		@Override
		public V put(K key, V value) {
			if ((key == null) || (value == null)) {
				throw new NullPointerException();
			}
			return map.put(key, value);
		}

		@Override
		public V remove(Object key) {
			if (key == null) {
				return null;
			}
			return map.remove(key);
		}

		@Override
		public String toString() {
			return map.toString();
		}
	}
}
