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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.diffplug.common.base.GetterSetter;

/** Utilties for manipulating Guava's immutable collections. */
public class Immutables {
	/** Returns a mutated version of the given set. */
	public static <T> ImmutableSet<T> mutateSet(ImmutableSet<T> source, Consumer<Set<T>> mutator) {
		Set<T> mutable = Sets.newHashSet(source);
		mutator.accept(mutable);
		return ImmutableSet.copyOf(mutable);
	}

	/** Returns a mutated version of the given list. */
	public static <T> ImmutableList<T> mutateList(ImmutableList<T> source, Consumer<List<T>> mutator) {
		List<T> mutable = Lists.newArrayList(source);
		mutator.accept(mutable);
		return ImmutableList.copyOf(mutable);
	}

	/** Returns a mutated version of the given map. */
	public static <K, V> ImmutableMap<K, V> mutateMap(ImmutableMap<K, V> source, Consumer<Map<K, V>> mutator) {
		Map<K, V> mutable = Maps.newHashMap(source);
		mutator.accept(mutable);
		return ImmutableMap.copyOf(mutable);
	}

	/** Mutates the given set and returns a value. */
	public static <T, R> R mutateSetAndReturn(GetterSetter<ImmutableSet<T>> value, Function<Set<T>, R> mutator) {
		Set<T> mutable = Sets.newHashSet(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableSet.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given list and returns a value. */
	public static <T, R> R mutateListAndReturn(GetterSetter<ImmutableList<T>> value, Function<List<T>, R> mutator) {
		List<T> mutable = Lists.newArrayList(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableList.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given map and returns a value. */
	public static <K, V, R> R mutateMapAndReturn(GetterSetter<ImmutableMap<K, V>> value, Function<Map<K, V>, R> mutator) {
		Map<K, V> mutable = Maps.newHashMap(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableMap.copyOf(mutable));
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
