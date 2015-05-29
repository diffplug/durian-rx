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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;

import com.diffplug.common.base.Box;

/** Utilties for manipulating Guava's immutable collections. */
public class Immutables {
	/** Returns a mutated version of the given list. */
	public static <T> ImmutableList<T> mutateList(ImmutableList<T> source, Consumer<List<T>> mutator) {
		List<T> mutable = new ArrayList<>(source);
		mutator.accept(mutable);
		return ImmutableList.copyOf(mutable);
	}

	/** Returns a mutated version of the given set. */
	public static <T> ImmutableSet<T> mutateSet(ImmutableSet<T> source, Consumer<Set<T>> mutator) {
		Set<T> mutable = new LinkedHashSet<>(source);
		mutator.accept(mutable);
		return ImmutableSet.copyOf(mutable);
	}

	/** Returns a mutated version of the given sorted set. */
	public static <T> ImmutableSortedSet<T> mutateSortedSet(ImmutableSortedSet<T> source, Consumer<NavigableSet<T>> mutator) {
		NavigableSet<T> mutable = new TreeSet<>(source);
		mutator.accept(mutable);
		return ImmutableSortedSet.copyOfSorted(mutable);
	}

	/** Returns a mutated version of the given map. */
	public static <K, V> ImmutableMap<K, V> mutateMap(ImmutableMap<K, V> source, Consumer<Map<K, V>> mutator) {
		Map<K, V> mutable = new LinkedHashMap<>(source);
		mutator.accept(mutable);
		return ImmutableMap.copyOf(mutable);
	}

	/** Returns a mutated version of the given sorted map. */
	public static <K, V> ImmutableSortedMap<K, V> mutateSortedMap(ImmutableSortedMap<K, V> source, Consumer<NavigableMap<K, V>> mutator) {
		NavigableMap<K, V> mutable = new TreeMap<>(source);
		mutator.accept(mutable);
		return ImmutableSortedMap.copyOfSorted(mutable);
	}

	/** Mutates the given list and returns a value. */
	public static <T, R> R mutateListAndReturn(Box<ImmutableList<T>> value, Function<List<T>, R> mutator) {
		List<T> mutable = new ArrayList<>(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableList.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given set and returns a value. */
	public static <T, R> R mutateSetAndReturn(Box<ImmutableSet<T>> value, Function<Set<T>, R> mutator) {
		Set<T> mutable = new LinkedHashSet<>(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableSet.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given sorted set and returns a value. */
	public static <T, R> R mutateSortedSetAndReturn(Box<ImmutableSortedSet<T>> value, Function<NavigableSet<T>, R> mutator) {
		NavigableSet<T> mutable = new TreeSet<>(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableSortedSet.copyOfSorted(mutable));
		return returnValue;
	}

	/** Mutates the given map and returns a value. */
	public static <K, V, R> R mutateMapAndReturn(Box<ImmutableMap<K, V>> value, Function<Map<K, V>, R> mutator) {
		Map<K, V> mutable = new LinkedHashMap<>(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableMap.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given sorted map and returns a value. */
	public static <K, V, R> R mutateSortedMapAndReturn(Box<ImmutableSortedMap<K, V>> value, Function<NavigableMap<K, V>, R> mutator) {
		NavigableMap<K, V> mutable = new TreeMap<>(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableSortedMap.copyOfSorted(mutable));
		return returnValue;
	}

	/** Converts an Optional to a Set. */
	public static <T> ImmutableSet<T> optionalToSet(Optional<T> selection) {
		return RxConversions.optionalToSet(selection);
	}

	/** Converts a Set to an Optional. */
	public static <T> Optional<T> setToOptional(ImmutableSet<T> set) {
		return RxConversions.setToOptional(set, RxConversions.OnMultiple.error());
	}
}
