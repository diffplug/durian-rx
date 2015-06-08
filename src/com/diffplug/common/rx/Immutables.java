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

import java.util.ArrayList;
import java.util.Comparator;
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
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;

import com.diffplug.common.base.Box;

/**
 * Methods for manipulating Guava's immutable collections.
 * <p>
 * For each Guava {@code ImmutableCollection}, (where {@code Collection} is {@code List, Set, SortedSet, Map, SortedMap}) there are two methods:
 * <ul>
 * <li>{@code ImmutableCollection mutateCollection(ImmutableCollection source, Consumer<MutableCollection> mutator)}</li>
 * <li>{@code <T> T mutateCollectionAndReturn(Box<ImmutableCollection>, Function<MutableCollection, T> mutator)}</li>
 * </ul>
 * Which work like this:
 * <ul>
 * <li>Copy the {@code ImmutableCollection} into a new {@code MutableCollection}.</li>
 * <li>Pass this {@code MutableCollection} collection to the {@code mutator}.</li>
 * <li>Copy the (now-mutated) {@code MutableCollection} into a new {@code ImmutableCollection}.</li>
 * <li>The return value is:
 * <ul>
 *     <li>{@code mutateCollection}: the mutated {@code ImmutableCollection} is returned.</li>
 *     <li>{@code mutateCollectionAndReturn}: the mutated {@code ImmutableCollection} is set into the supplied {@code box}, and the return value of the {@code mutator} is returned.</li>
 * </ul>
 * </ul>
 * <p>
 * This class also contains the simple {@link #optionalToSet} and {@link #setToOptional(ImmutableSet)} methods.
 */
public class Immutables {
	private Immutables() {}

	////////////
	// Mutate //
	////////////
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

	///////////////////////
	// Mutate and return //
	///////////////////////
	/** Mutates the given list and returns a value. */
	public static <T, R> R mutateListAndReturn(Box<ImmutableList<T>> box, Function<List<T>, R> mutator) {
		List<T> mutable = new ArrayList<>(box.get());
		R returnValue = mutator.apply(mutable);
		box.set(ImmutableList.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given set and returns a value. */
	public static <T, R> R mutateSetAndReturn(Box<ImmutableSet<T>> box, Function<Set<T>, R> mutator) {
		Set<T> mutable = new LinkedHashSet<>(box.get());
		R returnValue = mutator.apply(mutable);
		box.set(ImmutableSet.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given sorted set and returns a value. */
	public static <T, R> R mutateSortedSetAndReturn(Box<ImmutableSortedSet<T>> box, Function<NavigableSet<T>, R> mutator) {
		NavigableSet<T> mutable = new TreeSet<>(box.get());
		R returnValue = mutator.apply(mutable);
		box.set(ImmutableSortedSet.copyOfSorted(mutable));
		return returnValue;
	}

	/** Mutates the given map and returns a value. */
	public static <K, V, R> R mutateMapAndReturn(Box<ImmutableMap<K, V>> box, Function<Map<K, V>, R> mutator) {
		Map<K, V> mutable = new LinkedHashMap<>(box.get());
		R returnValue = mutator.apply(mutable);
		box.set(ImmutableMap.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given sorted map and returns a value. */
	public static <K, V, R> R mutateSortedMapAndReturn(Box<ImmutableSortedMap<K, V>> box, Function<NavigableMap<K, V>, R> mutator) {
		NavigableMap<K, V> mutable = new TreeMap<>(box.get());
		R returnValue = mutator.apply(mutable);
		box.set(ImmutableSortedMap.copyOfSorted(mutable));
		return returnValue;
	}

	//////////////////////
	// Optional <-> Set //
	//////////////////////
	/** Converts an Optional to a Set. */
	public static <T> ImmutableSet<T> optionalToSet(Optional<T> selection) {
		return RxConversions.optionalToSet(selection);
	}

	/** Converts a Set to an Optional, throwing an error if there are multiple elements. */
	public static <T> Optional<T> setToOptional(ImmutableSet<T> set) {
		return RxConversions.setToOptional(set, RxConversions.OnMultiple.error());
	}

	///////////////////////
	// Java 8 Collectors //
	///////////////////////
	// Inspired by http://blog.comsysto.com/2014/11/12/java-8-collectors-for-guava-collections/
	/** A Collector which returns an ImmutableList. */
	public static <T> Collector<T, ?, ImmutableList<T>> toList() {
		// called for each combiner element (once if single-threaded, multiple if parallel)
		Supplier<ImmutableList.Builder<T>> supplier = ImmutableList::builder;
		// called for every element in the stream
		BiConsumer<ImmutableList.Builder<T>, T> accumulator = (b, v) -> b.add(v);
		// combines multiple collectors for parallel streams
		BinaryOperator<ImmutableList.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		// converts the builder into the list
		Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableSet. */
	public static <T> Collector<T, ?, ImmutableSet<T>> toSet() {
		Supplier<ImmutableSet.Builder<T>> supplier = ImmutableSet::builder;
		BiConsumer<ImmutableSet.Builder<T>, T> accumulator = (b, v) -> b.add(v);
		BinaryOperator<ImmutableSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector of Comparables which returns an ImmutableSortedSet. */
	public static <T extends Comparable<?>> Collector<T, ?, ImmutableSortedSet<T>> toSortedSet() {
		return toSortedSetImp(ImmutableSortedSet::naturalOrder);
	}

	/** A Collector which returns an ImmutableSortedSet which is ordered by the given comparator. */
	public static <T> Collector<T, ?, ImmutableSortedSet<T>> toSortedSet(Comparator<T> comparator) {
		return toSortedSetImp(() -> ImmutableSortedSet.orderedBy(comparator));
	}

	private static <T> Collector<T, ?, ImmutableSortedSet<T>> toSortedSetImp(Supplier<ImmutableSortedSet.Builder<T>> supplier) {
		BiConsumer<ImmutableSortedSet.Builder<T>, T> accumulator = (b, v) -> b.add(v);
		BinaryOperator<ImmutableSortedSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
		Function<ImmutableSortedSet.Builder<T>, ImmutableSortedSet<T>> finisher = ImmutableSortedSet.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableMap using the given pair of key and value functions. */
	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		Supplier<ImmutableMap.Builder<K, V>> supplier = ImmutableMap::builder;
		BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator = (b, v) -> b.put(keyMapper.apply(v), valueMapper.apply(v));
		BinaryOperator<ImmutableMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
		Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher = ImmutableMap.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	/** A Collector which returns an ImmutableSortedMap which is populated by the given pair of key and value functions. */
	public static <T, K extends Comparable<?>, V> Collector<T, ?, ImmutableSortedMap<K, V>> toSortedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		Supplier<ImmutableSortedMap.Builder<K, V>> supplier = ImmutableSortedMap::naturalOrder;
		return toSortedMap(supplier, keyMapper, valueMapper);
	}

	/** A Collector which returns an ImmutableSortedMap which is ordered by the given comparator, and populated by the given pair of key and value functions. */
	public static <T, K extends Comparable<?>, V> Collector<T, ?, ImmutableSortedMap<K, V>> toSortedMap(Comparator<K> comparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		Supplier<ImmutableSortedMap.Builder<K, V>> supplier = () -> ImmutableSortedMap.orderedBy(comparator);
		return toSortedMap(supplier, keyMapper, valueMapper);
	}

	private static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toSortedMap(Supplier<ImmutableSortedMap.Builder<K, V>> supplier, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		BiConsumer<ImmutableSortedMap.Builder<K, V>, T> accumulator = (b, v) -> b.put(keyMapper.apply(v), valueMapper.apply(v));
		BinaryOperator<ImmutableSortedMap.Builder<K, V>> combiner = (l, r) -> l.putAll(r.build());
		Function<ImmutableSortedMap.Builder<K, V>, ImmutableSortedMap<K, V>> finisher = ImmutableSortedMap.Builder::build;
		return Collector.of(supplier, accumulator, combiner, finisher);
	}
}
