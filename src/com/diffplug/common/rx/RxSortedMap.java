/*
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

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableSortedMap;

/**
 * <code>{@link RxBox}&lt;{@link ImmutableSortedMap}&lt;T&gt;&gt;</code>
 * with convenience methods for creating and mutating the map.
 */
public class RxSortedMap<K, V> extends RxBox.Default<ImmutableSortedMap<K, V>> {
	/** Creates an RxMap with an initially empty value. */
	public static <K, V> RxSortedMap<K, V> ofEmpty() {
		return of(ImmutableSortedMap.of());
	}

	/** Creates an RxMap with the given initial value. */
	public static <K, V> RxSortedMap<K, V> of(SortedMap<K, V> initial) {
		return new RxSortedMap<K, V>(ImmutableSortedMap.copyOfSorted(initial));
	}

	/** Creates an RxMap with the given initial value. */
	public static <K, V> RxSortedMap<K, V> of(Map<K, V> initial) {
		return new RxSortedMap<K, V>(ImmutableSortedMap.copyOf(initial));
	}

	/** Initally holds the given collection. */
	protected RxSortedMap(ImmutableSortedMap<K, V> initial) {
		super(initial);
	}

	/** Sets the value of this map. */
	public void set(SortedMap<K, V> value) {
		super.set(ImmutableSortedMap.copyOfSorted(value));
	}

	/** Sets the value of this map. */
	public void set(Map<K, V> value) {
		super.set(ImmutableSortedMap.copyOf(value));
	}

	/** Mutates this map. */
	public void mutate(Consumer<NavigableMap<K, V>> mutator) {
		super.set(Immutables.mutateSortedMap(get(), mutator));
	}

	/** Mutates this map, and returns a value. */
	public <R> R mutateAndReturn(Function<NavigableMap<K, V>, R> mutator) {
		return Immutables.mutateSortedMapAndReturn(this, mutator);
	}
}
