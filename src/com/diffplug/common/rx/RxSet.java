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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

/** 
 * An extension of RxBox<ImmutableSet<T>>, with
 * convenience methods for modifying and observing the set,
 * as well as for converting it into an RxBox<Optional<T>>.
 */
public class RxSet<T> extends RxBox.Default<ImmutableSet<T>> {
	/** Creates an RxSet with an initially empty value. */
	public static <T> RxSet<T> ofEmpty() {
		return of(ImmutableSet.of());
	}

	/** Creates an RxSet with the given initial value. */
	public static <T> RxSet<T> of(Set<T> initial) {
		return new RxSet<T>(ImmutableSet.copyOf(initial));
	}

	/** Initally holds the given collection. */
	protected RxSet(ImmutableSet<T> initial) {
		super(initial);
	}

	/** Removes the given value from the set. */
	public void set(Set<T> value) {
		super.set(ImmutableSet.copyOf(value));
	}

	/** Returns a mirror of this Set as an RxOptional. */
	public RxBox<Optional<T>> asOptional(Function<ImmutableSet<T>, T> onMultiple) {
		return RxConversions.asOptional(this, onMultiple);
	}

	/** Mutates this set. */
	public void mutate(Consumer<Set<T>> mutator) {
		set(Immutables.mutateSet(get(), mutator));
	}

	/** Mutates this set. */
	public <R> R mutateAndReturn(Function<Set<T>, R> mutator) {
		return Immutables.mutateSetAndReturn(this, mutator);
	}
}
