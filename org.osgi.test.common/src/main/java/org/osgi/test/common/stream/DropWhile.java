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

package org.osgi.test.common.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class DropWhile<T> extends AbstractWhile<T> {
	public static <O> Stream<O> dropWhile(Stream<O> stream, Predicate<? super O> predicate) {
		return StreamSupport.stream(new DropWhile<>(stream.spliterator(), predicate), stream.isParallel())
			.onClose(stream::close);
	}

	private boolean drop = true;

	private DropWhile(Spliterator<T> spliterator, Predicate<? super T> predicate) {
		super(spliterator, predicate);
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		if (drop) {
			drop = false;
			while (spliterator.tryAdvance(this)) {
				if (!predicate.test(item)) {
					action.accept(item);
					break;
				}
			}
		}
		spliterator.forEachRemaining(action);
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		if (drop) {
			drop = false;
			while (spliterator.tryAdvance(this)) {
				if (!predicate.test(item)) {
					action.accept(item);
					return true;
				}
			}
			return false;
		}
		return spliterator.tryAdvance(action);
	}
}
