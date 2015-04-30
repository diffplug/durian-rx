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

/**
 * An extension of RxValue<Optional<T>>, with convenience
 * methods for converting it to an RxSet<T>.
 */
public class RxOptional<T> extends RxValue<Optional<T>> {
	/** Returns an empty RxOptional. */
	public static <T> RxOptional<T> ofEmpty() {
		return new RxOptional<T>(Optional.empty());
	}

	/** Returns an RxOptional of the given value. */
	public static <T> RxOptional<T> of(Optional<T> value) {
		return new RxOptional<T>(value);
	}

	/** Initially holds the given value. */
	protected RxOptional(Optional<T> initial) {
		super(initial);
	}

	/** Returns a mirror of this RxOptional as an RxSet. */
	public RxSet<T> asSet(Function<ImmutableSet<T>, T> onMultiple) {
		RxSet<T> asSet = new RxSet<T>(setFromOptional(get())) {
			@Override
			public void set(ImmutableSet<T> selection) {
				if (selection.size() == 0) {
					RxOptional.this.set(Optional.empty());
				} else if (selection.size() == 1) {
					RxOptional.this.set(Optional.of(Iterables.getOnlyElement(selection)));
				} else if (selection.size() > 1) {
					RxOptional.this.set(Optional.of(onMultiple.apply(selection)));
				} else {
					throw Unhandled.integerException(selection.size());
				}
			}
		};
		asObservable().subscribe(val -> {
			asSet.set(setFromOptional(val));
		});
		return asSet;
	}

	private static <T> ImmutableSet<T> setFromOptional(Optional<T> selection) {
		if (selection.isPresent()) {
			return ImmutableSet.of(selection.get());
		} else {
			return ImmutableSet.of();
		}
	}
}
