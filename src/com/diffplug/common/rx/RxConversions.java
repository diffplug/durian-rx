/**
 * Copyright 2015 DiffPlug
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
 */
package com.diffplug.common.rx;

import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import com.diffplug.common.base.Unhandled;

public class RxConversions {
	/** Converts an RxValue<Optional<T>> to an RxValue<ImmutableSet<T>>. */
	public static <T> RxValue<ImmutableSet<T>> asSet(RxValue<Optional<T>> input, Function<ImmutableSet<T>, T> onMultiple) {
		return RxValue.of(input.map(RxConversions::setFromOptional), set -> {
			input.set(optionalFromSet(set, onMultiple));
		});
	}

	/** Converts an RxValue<ImmutableSet<T>> to an RxValue<Optional<T>>. */
	public static <T> RxValue<Optional<T>> asOptional(RxValue<ImmutableSet<T>> input, Function<ImmutableSet<T>, T> onMultiple) {
		return RxValue.of(input.map(set -> optionalFromSet(set, onMultiple)), newValue -> {
			input.set(setFromOptional(newValue));
		});
	}

	private static <T> ImmutableSet<T> setFromOptional(Optional<T> selection) {
		if (selection.isPresent()) {
			return ImmutableSet.of(selection.get());
		} else {
			return ImmutableSet.of();
		}
	}

	private static <T> Optional<T> optionalFromSet(ImmutableSet<T> set, Function<ImmutableSet<T>, T> mode) {
		if (set.size() == 0) {
			return Optional.empty();
		} else if (set.size() == 1) {
			return Optional.of(Iterables.getOnlyElement(set));
		} else if (set.size() > 1) {
			return Optional.of(mode.apply(set));
		} else {
			throw Unhandled.integerException(set.size());
		}
	}

	/** Functions which implement the onMultiple function required by the conversions. */
	public static class OnMultiple {
		private OnMultiple() {}

		/** Throws an exception when ImmutableSet is multiple. */
		public static <T> Function<ImmutableSet<T>, T> error() {
			return val -> {
				throw new IllegalArgumentException();
			};
		}

		/** Throws an exception when ImmutableSet is multiple. */
		public static <T> Function<ImmutableSet<T>, T> takeFirst() {
			return val -> val.asList().get(0);
		}
	}
}
