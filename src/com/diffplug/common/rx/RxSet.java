/*
 * Copyright 2016 DiffPlug
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

import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;

/**
 * <code>{@link RxBox}&lt;{@link ImmutableSet}&lt;T&gt;&gt;</code>
 * with convenience methods for creating and mutating the set.
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

	/** Mutates this set. */
	public ImmutableSet<T> mutate(Consumer<Set<T>> mutator) {
		ImmutableSet<T> mutated = Immutables.mutateSet(get(), mutator);
		set(mutated);
		return mutated;
	}
}
