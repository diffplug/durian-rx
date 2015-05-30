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

/** Static methods for converting between <code>{@link RxBox}&lt;{@link ImmutableSet}&lt;T&gt;&gt;</code> and <code>{@link RxBox}&lt;{@link ImmutableSet}&lt;T&gt;&gt;</code>. */
public class RxConversions {
	/** Converts an {@code RxBox<Optional<T>>} to an {@code RxBox<ImmutableSet<T>>}. */
	public static <T> RxBox<ImmutableSet<T>> asSet(RxBox<Optional<T>> input, Function<ImmutableSet<T>, T> onMultiple) {
		return RxBox.from(input.map(RxConversions::optionalToSet), set -> {
			input.set(setToOptional(set, onMultiple));
		});
	}

	/** Converts an {@code RxBox<ImmutableSet<T>>} to an {@code RxBox<Optional<T>>}. */
	public static <T> RxBox<Optional<T>> asOptional(RxBox<ImmutableSet<T>> input, Function<ImmutableSet<T>, T> onMultiple) {
		return RxBox.from(input.map(set -> setToOptional(set, onMultiple)), newValue -> {
			input.set(optionalToSet(newValue));
		});
	}

	static <T> ImmutableSet<T> optionalToSet(Optional<T> selection) {
		if (selection.isPresent()) {
			return ImmutableSet.of(selection.get());
		} else {
			return ImmutableSet.of();
		}
	}

	static <T> Optional<T> setToOptional(ImmutableSet<T> set, Function<ImmutableSet<T>, T> mode) {
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

	/** Functions which implement the {@code onMultiple} function required to convert a Set to an Optional. */
	public static class OnMultiple {
		private OnMultiple() {}

		/** Throws an exception when ImmutableSet is multiple. */
		public static <T> Function<ImmutableSet<T>, T> error() {
			return val -> {
				throw new IllegalArgumentException();
			};
		}

		/** Just takes the first value when ImmutableSet has multiple values. */
		public static <T> Function<ImmutableSet<T>, T> takeFirst() {
			return val -> val.asList().get(0);
		}
	}
}
