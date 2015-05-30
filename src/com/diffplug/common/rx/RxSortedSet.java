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

import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

/**
 * <code>{@link RxBox}&lt;{@link ImmutableSortedSet}&lt;T&gt;&gt;</code>
 * with convenience methods for creating and mutating the set.
 */
public class RxSortedSet<T> extends RxBox.Default<ImmutableSortedSet<T>> {
	/** Creates an RxSet with an initially empty value. */
	public static <T> RxSortedSet<T> ofEmpty() {
		return of(ImmutableSet.of());
	}

	/** Creates an RxSet with the given initial value. */
	public static <T> RxSortedSet<T> of(SortedSet<T> initial) {
		return new RxSortedSet<T>(ImmutableSortedSet.copyOfSorted(initial));
	}

	/** Creates an RxSet with the given initial value. */
	public static <T> RxSortedSet<T> of(Set<T> initial) {
		return new RxSortedSet<T>(ImmutableSortedSet.copyOf(initial));
	}

	/** Initally holds the given collection. */
	protected RxSortedSet(ImmutableSortedSet<T> initial) {
		super(initial);
	}

	/** Sets the value of this set. */
	public void set(SortedSet<T> value) {
		super.set(ImmutableSortedSet.copyOfSorted(value));
	}

	/** Sets the value of this set. */
	public void set(Set<T> value) {
		super.set(ImmutableSortedSet.copyOf(value));
	}

	/** Mutates this set. */
	public void mutate(Consumer<Set<T>> mutator) {
		set(Immutables.mutateSet(get(), mutator));
	}

	/** Mutates this set. */
	public <R> R mutateAndReturn(Function<NavigableSet<T>, R> mutator) {
		return Immutables.mutateSortedSetAndReturn(this, mutator);
	}
}
