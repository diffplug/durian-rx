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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableListIterator;

import com.diffplug.common.base.GetterSetter;

public class ImmutableUtil {
	//////////////////////
	// Generic mutators //
	//////////////////////
	/** Returns a mutated version of the given set. */
	public static <T> ImmutableSet<T> mutate(ImmutableSet<T> source, Consumer<Set<T>> mutator) {
		Set<T> mutable = Sets.newHashSet(source);
		mutator.accept(mutable);
		return ImmutableSet.copyOf(mutable);
	}

	/** Returns a mutated version of the given list. */
	public static <T> ImmutableList<T> mutate(ImmutableList<T> source, Consumer<List<T>> mutator) {
		List<T> mutable = Lists.newArrayList(source);
		mutator.accept(mutable);
		return ImmutableList.copyOf(mutable);
	}

	/** Returns a mutated version of the given map. */
	public static <K, V> ImmutableMap<K, V> mutate(ImmutableMap<K, V> source, Consumer<Map<K, V>> mutator) {
		Map<K, V> mutable = Maps.newHashMap(source);
		mutator.accept(mutable);
		return ImmutableMap.copyOf(mutable);
	}

	/** Mutates the given set and returns a value. */
	public static <T, R> R mutateAndReturnSet(GetterSetter<ImmutableSet<T>> value, Function<Set<T>, R> mutator) {
		Set<T> mutable = Sets.newHashSet(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableSet.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given list and returns a value. */
	public static <T, R> R mutateAndReturnList(GetterSetter<ImmutableList<T>> value, Function<List<T>, R> mutator) {
		List<T> mutable = Lists.newArrayList(value.get());
		R returnValue = mutator.apply(mutable);
		value.set(ImmutableList.copyOf(mutable));
		return returnValue;
	}

	/** Mutates the given map and returns a value. */
	public static <K, V, R> R mutateAndReturnMap(GetterSetter<ImmutableMap<K, V>> value, Function<Map<K, V>, R> mutator) {
		Map<K, V> mutable = Maps.newHashMap(value.get());
		R returnValue =  mutator.apply(mutable);
		value.set(ImmutableMap.copyOf(mutable));
		return returnValue;
	}

	/////////
	// Set //
	/////////
	/** Returns an ImmutableSet which has added the given value. */
	public static <T> ImmutableSet<T> add(ImmutableSet<T> input, T newValue) {
		return mutate(input, set -> set.add(newValue));
	}

	/** Returns an ImmutableSet which has added the given values. */
	public static <T> ImmutableSet<T> addAll(ImmutableSet<T> input, Collection<? extends T> collection) {
		return mutate(input, set -> set.addAll(collection));
	}

	/** Returns an ImmutableSet which has removed the given value. */
	public static <T> ImmutableSet<T> remove(ImmutableSet<T> input, Object object) {
		return mutate(input, set -> set.remove(object));
	}

	/** Returns an ImmutableSet which has removed the given value. */
	public static <T> ImmutableSet<T> removeAll(ImmutableSet<T> input, Collection<?> toRemove) {
		return mutate(input, set -> set.removeAll(toRemove));
	}

	/** Returns an ImmutableSet which has removed the given value. */
	public static <T> ImmutableSet<T> retainAll(ImmutableSet<T> input, Collection<?> toRemove) {
		return mutate(input, set -> set.retainAll(toRemove));
	}

	//////////
	// List //
	//////////














	
	
	/** Returns a mutated version of the given list. */
	public static <T> ImmutableList<T> filter(ImmutableList<T> source, Predicate<T> filter) {
		List<T> mutable = Lists.newArrayListWithCapacity(source.size());
		filterInternal(source, mutable, filter);
		return ImmutableList.copyOf(mutable);
	}

	/** Returns a mutated version of the given set. */
	public static <T> ImmutableSet<T> filter(ImmutableSet<T> source, Predicate<T> filter) {
		Set<T> mutable = Sets.newHashSetWithExpectedSize(source.size());
		filterInternal(source, mutable, filter);
		return ImmutableSet.copyOf(mutable);
	}

	/** Adds each element in source which passes the filter to mutable. */
	private static <T> void filterInternal(ImmutableCollection<T> source, Collection<T> mutable, Predicate<T> filter) {
		for (T e : source) {
			if (filter.test(e)) {
				mutable.add(e);
			}
		}
	}

	/** Returns an ImmutableList which has had the given elements removed. */
	public static <T> ImmutableList<T> remove(ImmutableList<T> source, Collection<T> toRemove) {
		List<T> result = Lists.newArrayListWithCapacity(source.size() - toRemove.size());
		for (T element : source) {
			if (!toRemove.contains(element)) {
				result.add(element);
			}
		}
		return ImmutableList.copyOf(result);
	}

	public static <T> ImmutableSet<T> remove(ImmutableSet<T> source, Collection<T> toRemove) {
		Set<T> set = Sets.newHashSet(source);
		set.removeAll(toRemove);
		return ImmutableSet.copyOf(set);
	}

	/** Returns an ImmutableList which has had all instances of the given object removed. */
	public static <T> ImmutableList<T> remove(ImmutableList<T> source, T toRemove) {
		return remove(source, Collections.singleton(toRemove));
	}

	/** Returns an ImmutableList which has had all instances of the given object removed. */
	public static <T> ImmutableList<T> remove(ImmutableList<T> source, int idx, T toRemove) {
		List<T> result = Lists.newArrayListWithCapacity(source.size() - 1);
		UnmodifiableListIterator<T> iterator = source.listIterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (iterator.previousIndex() == idx) {
				// if it's the one we're supposed to remove, make sure that it actually was
				Preconditions.checkArgument(next == toRemove, "Was going to remove %s, but was %s.", toRemove, next);
			} else {
				result.add(next);
			}
		}
		return ImmutableList.copyOf(result);
	}

	/** Returns an ImmutableList which has had the given index replaced with the given value. */
	public static <T> ImmutableList<T> replace(ImmutableList<T> source, int idx, T newValue) {
		List<T> result = Lists.newArrayList(source);
		result.set(idx, newValue);
		return ImmutableList.copyOf(result);
	}

	/** Returns an ImmutableList which has added the given value to the end. */
	public static <T> ImmutableList<T> add(ImmutableList<T> input, int idx, T newValue) {
		List<T> list = Lists.newArrayListWithCapacity(input.size() + 1);
		if (idx < input.size()) {
			for (int i = 0; i < input.size(); ++i) {
				// add the new value at the appropriate time
				if (i == idx) {
					list.add(newValue);
				}
				// and add the rest when its their turn
				list.add(input.get(i));
			}
		} else {
			list.addAll(input);
			list.add(newValue);
		}
		return ImmutableList.copyOf(list);
	}

	/** Adds the given element to the end, ensuring that there are no duplicate entries. */
	public static <T> ImmutableList<T> noDuplicatesAddToBeginning(ImmutableList<T> input, T value) {
		return noDuplicatesAdd(input, 0, value);
	}

	/** Adds the given element to the beginning, ensuring that there are no duplicate entries. */
	public static <T> ImmutableList<T> noDuplicatesAddToEnd(ImmutableList<T> input, T value) {
		return noDuplicatesAdd(input, input.size(), value);
	}

	/** Adds the given element at the given index, ensuring that there are no duplicate entries. */
	public static <T> ImmutableList<T> noDuplicatesAdd(ImmutableList<T> input, int idx, T value) {
		Preconditions.checkArgument(0 <= idx && idx <= input.size());

		// find the new value in the input
		int oldIdx = input.indexOf(value);

		// create a list with the appropriate size
		List<T> list = Lists.newArrayListWithCapacity(input.size() + (oldIdx >= 0 ? 0 : 1));
		for (int i = 0; i < input.size(); ++i) {
			// add the new value at the appropriate time
			if (i == idx) {
				list.add(value);
			}
			// then add the others (so long as we're not skipping them)
			if (i != oldIdx) {
				list.add(input.get(i));
			}
		}
		// just in case we haven't added it yet, add the last one
		if (idx == input.size()) {
			list.add(value);
		}
		return ImmutableList.copyOf(list);
	}
}
