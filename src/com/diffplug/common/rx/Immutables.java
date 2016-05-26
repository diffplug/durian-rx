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
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;

import com.diffplug.common.base.ConverterNonNull;
import com.diffplug.common.base.ConverterNullable;

/**
 * Methods for manipulating Guava's immutable collections.
 * <p>
 * For each Guava {@code ImmutableCollection}, (where {@code Collection} is {@code List, Set, SortedSet, Map, SortedMap, BiMap}) there is a method:
 * <ul>
 * <li>{@code ImmutableCollection mutateCollection(ImmutableCollection source, Consumer<MutableCollection> mutator)}</li>
 * </ul>
 * Which works like this:
 * <ul>
 * <li>Copy the {@code ImmutableCollection} into a new {@code MutableCollection}.</li>
 * <li>Pass this {@code MutableCollection} collection to the {@code mutator}.</li>
 * <li>Copy the (now-mutated) {@code MutableCollection} into a new {@code ImmutableCollection}.</li>
 * <li>Return this {@code ImmutableCollecion}.</li>
 * </ul>
 * <p>
 * There are also {@code Function<ImmutableCollection, ImmutableCollection> mutatorCollection(Consumer<MutableCollection> mutator)}
 * methods for each type, to easily create functions which operate on immutable collections.
 * <p>
 * This class also contains the simple {@link #optionalToSet} and {@link #optionalFrom(ImmutableCollection)} methods.
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

	/** Returns a mutated version of the given sorted map. */
	public static <K, V> ImmutableBiMap<K, V> mutateBiMap(ImmutableBiMap<K, V> source, Consumer<BiMap<K, V>> mutator) {
		BiMap<K, V> mutable = HashBiMap.create(source);
		mutator.accept(mutable);
		return ImmutableBiMap.copyOf(mutable);
	}

	/////////////
	// Mutator //
	/////////////
	/** Returns a function which mutates a list using the given mutator. */
	public static <T> UnaryOperator<ImmutableList<T>> mutatorList(Consumer<List<T>> mutator) {
		return input -> mutateList(input, mutator);
	}

	/** Returns a function which mutates a set using the given mutator. */
	public static <T> UnaryOperator<ImmutableSet<T>> mutatorSet(Consumer<Set<T>> mutator) {
		return input -> mutateSet(input, mutator);
	}

	/** Returns a function which mutates a sorted set using the given mutator. */
	public static <T> UnaryOperator<ImmutableSortedSet<T>> mutatorSortedSet(Consumer<NavigableSet<T>> mutator) {
		return input -> mutateSortedSet(input, mutator);
	}

	/** Returns a function which mutates a map using the given mutator. */
	public static <K, V> UnaryOperator<ImmutableMap<K, V>> mutatorMap(Consumer<Map<K, V>> mutator) {
		return input -> mutateMap(input, mutator);
	}

	/** Returns a function which mutates a sorted map using the given mutator. */
	public static <K, V> UnaryOperator<ImmutableSortedMap<K, V>> mutatorSortedMap(Consumer<NavigableMap<K, V>> mutator) {
		return input -> mutateSortedMap(input, mutator);
	}

	/** Returns a function which mutates a sorted map using the given mutator. */
	public static <K, V> UnaryOperator<ImmutableBiMap<K, V>> mutatorBiMap(Consumer<BiMap<K, V>> mutator) {
		return input -> mutateBiMap(input, mutator);
	}

	////////////////////////
	// Optional <-> Stuff //
	////////////////////////
	/** Converts an {@link Optional} to an {@link ImmutableSet}. */
	public static <T> ImmutableSet<T> optionalToSet(Optional<T> selection) {
		if (selection.isPresent()) {
			return ImmutableSet.of(selection.get());
		} else {
			return ImmutableSet.of();
		}
	}

	/**
	 * Converts an {@link ImmutableCollection} to an {@link Optional}.
	 * @throws IllegalArgumentException if there are multiple elements.
	 */
	public static <T> Optional<T> optionalFrom(ImmutableCollection<T> collection) {
		if (collection.size() == 0) {
			return Optional.empty();
		} else if (collection.size() == 1) {
			return Optional.of(collection.iterator().next());
		} else {
			throw new IllegalArgumentException("Collection contains multiple elements.");
		}
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

	/////////////////////////
	// Mutate element-wise //
	/////////////////////////
	/** Returns a mutated version of the given set.  Function can return null to indicate the element should be removed. */
	public static <T, R> ImmutableSet<R> perElementMutateSet(ImmutableSet<T> source, Function<? super T, ? extends R> mutator) {
		ImmutableSet.Builder<R> builder = ImmutableSet.builder();
		for (T element : source) {
			R result = mutator.apply(element);
			if (result != null) {
				builder.add(result);
			}
		}
		return builder.build();
	}

	////////////////////////////
	// Per-element converters //
	////////////////////////////
	public static <T, R> ConverterNonNull<Optional<T>, Optional<R>> perElementConverterOpt(ConverterNullable<T, R> perElement) {
		return ConverterNonNull.from(
				optT -> optT.map(perElement::convert),
				optR -> optR.map(perElement::revert),
				"perElement=" + perElement);
	}

	public static <T, R> ConverterNonNull<ImmutableSet<T>, ImmutableSet<R>> perElementConverterSet(ConverterNullable<T, R> perElement) {
		return ConverterNonNull.from(
				setOfT -> perElementMutateSet(setOfT, perElement::convert),
				setOfR -> perElementMutateSet(setOfR, perElement::revert),
				perElement.toString());
	}
}
