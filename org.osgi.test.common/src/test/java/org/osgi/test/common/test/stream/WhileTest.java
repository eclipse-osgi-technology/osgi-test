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

package org.osgi.test.common.test.stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.stream.DropWhile;
import org.osgi.test.common.stream.TakeWhile;

public class WhileTest {

	Set<String> testSet;

	@BeforeEach
	public void setUp() throws Exception {
		testSet = new HashSet<>();
		testSet.add("key1");
		testSet.add("key2");
		testSet.add("key3");
		testSet.add("key4");
		testSet.add("key5");
	}

	@AfterEach
	public void tearDown() throws Exception {}

	@Test
	public void takeWhile() {
		Supplier<Stream<String>> supplier = () -> TakeWhile.takeWhile(testSet.stream()
			.sorted(), k -> !k.equals("key3"));
		assertThat(supplier.get()
			.count()).isEqualTo(2);
		assertThat(supplier.get()).containsExactly("key1", "key2");
		assertThat(supplier.get()
			.collect(toList())).containsExactly("key1", "key2");
	}

	@Test
	public void takeWhileAll() {
		Supplier<Stream<String>> supplier = () -> TakeWhile.takeWhile(testSet.stream(), k -> !k.equals("all"));
		assertThat(supplier.get()
			.count()).isEqualTo(testSet.size());
		assertThat(supplier.get()).containsExactlyInAnyOrder("key1", "key2", "key3", "key4", "key5");
		assertThat(supplier.get()
			.collect(toList())).containsExactlyInAnyOrder("key1", "key2", "key3", "key4", "key5");
	}

	@Test
	public void takeWhileNone() {
		Supplier<Stream<String>> supplier = () -> TakeWhile.takeWhile(testSet.stream(), k -> k.equals("none"));
		assertThat(supplier.get()
			.count()).isEqualTo(0);
		assertThat(supplier.get()).isEmpty();
		assertThat(supplier.get()
			.collect(toList())).isEmpty();
	}

	@Test
	public void takeWhileNull() {
		assertThatNullPointerException().isThrownBy(() -> TakeWhile.takeWhile(testSet.stream(), null));
		assertThatNullPointerException().isThrownBy(() -> TakeWhile.takeWhile(null, k -> true));
	}

	@Test
	public void dropWhile() {
		Supplier<Stream<String>> supplier = () -> DropWhile.dropWhile(testSet.stream()
			.sorted(), k -> !k.equals("key3"));
		assertThat(supplier.get()
			.count()).isEqualTo(3);
		assertThat(supplier.get()).containsExactly("key3", "key4", "key5");
		assertThat(supplier.get()
			.collect(toList())).containsExactly("key3", "key4", "key5");
	}

	@Test
	public void dropWhileNone() {
		Supplier<Stream<String>> supplier = () -> DropWhile.dropWhile(testSet.stream()
			.sorted(), k -> k.equals("none"));
		assertThat(supplier.get()
			.count()).isEqualTo(testSet.size());
		assertThat(supplier.get()).containsExactlyInAnyOrder("key1", "key2", "key3", "key4", "key5");
		assertThat(supplier.get()
			.collect(toList())).containsExactlyInAnyOrder("key1", "key2", "key3", "key4", "key5");
	}

	@Test
	public void dropWhileAll() {
		Supplier<Stream<String>> supplier = () -> DropWhile.dropWhile(testSet.stream(), k -> !k.equals("all"));
		assertThat(supplier.get()
			.count()).isEqualTo(0);
		assertThat(supplier.get()).isEmpty();
		assertThat(supplier.get()
			.collect(toList())).isEmpty();
	}

	@Test
	public void dropWhileNull() {
		assertThatNullPointerException().isThrownBy(() -> DropWhile.dropWhile(testSet.stream(), null));
		assertThatNullPointerException().isThrownBy(() -> DropWhile.dropWhile(null, k -> false));
	}

}
