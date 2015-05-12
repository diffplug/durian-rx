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
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableListIterator;

public class ImmutableUtil {
	/** Returns an ImmutableList which has had the given elements removed. */
	public static <T> ImmutableList<T> filter(ImmutableList<T> source, Predicate<T> predicate) {
		return ImmutableList.copyOf(Collections2.filter(source, predicate::test));
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

	/** Returns an ImmutableList which has added the given value to the end. */
	public static <T> ImmutableSet<T> add(ImmutableSet<T> input, T newValue) {
		Set<T> mutable = Sets.newHashSet(input);
		mutable.add(newValue);
		return ImmutableSet.copyOf(mutable);
	}

	/** Returns an ImmutableList which has added the given value to the end. */
	public static <T> ImmutableSet<T> remove(ImmutableSet<T> input, T newValue) {
		Set<T> mutable = Sets.newHashSet(input);
		mutable.remove(newValue);
		return ImmutableSet.copyOf(mutable);
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
