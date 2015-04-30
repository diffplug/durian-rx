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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.diffplug.common.base.Unhandled;

/** An RxList which guarantees to never have any duplicates. */
public class RxOrderedSet<T> extends RxValue<ImmutableList<T>> {
	/** Creates an RxList with an initially empty value. */
	public static <T> RxOrderedSet<T> ofEmpty() {
		return of(ImmutableList.of());
	}

	/** Creates an RxList with an initially empty value. */
	public static <T> RxOrderedSet<T> ofEmpty(OnDuplicate duplicatePolicy) {
		return of(ImmutableList.of(), duplicatePolicy);
	}

	/** Creates an RxList with the given initial value. */
	public static <T> RxOrderedSet<T> of(ImmutableList<T> initial) {
		return of(initial, OnDuplicate.ERROR);
	}

	/** Creates an RxList with the given initial value. */
	public static <T> RxOrderedSet<T> of(ImmutableList<T> initial, OnDuplicate duplicatePolicy) {
		return new RxOrderedSet<T>(initial, duplicatePolicy);
	}

	/** Initally holds the given collection. */
	protected RxOrderedSet(ImmutableList<T> initial, OnDuplicate duplicatePolicy) {
		super(filter(initial, Maps.newHashMap(), duplicatePolicy));
		this.policy = duplicatePolicy;
	}

	private final OnDuplicate policy;

	/** Policies for disallowing duplicates. */
	public enum OnDuplicate {
		/** Throws an error when a duplicate is encountered. */
		ERROR, /** Resolve duplicates by taking the first duplicate in the list. */
		TAKE_FIRST, /** Resolve duplicates by taking the last duplicate in the list. */
		TAKE_LAST
	}

	/** Returns the duplicate policy for this RxList. */
	public OnDuplicate getDuplicatePolicy() {
		return policy;
	}

	/** Sets the selection. */
	@Override
	public void set(ImmutableList<T> newSelection) {
		Preconditions.checkNotNull(newSelection);
		if (!selection.equals(newSelection)) {
			// the selection changed, so we will check it for duplicates
			newSelection = filter(newSelection, indexToTake, policy);
			// if it's still different than we expect...
			if (!selection.equals(newSelection)) {
				this.selection = newSelection;
				observable.onNext(selection);
			}
		}
	}

	/** Reusable map of indices that we're going to keep (for the case that we have duplicates). */
	private Map<T, Integer> indexToTake = Maps.newHashMap();

	private static <T> ImmutableList<T> filter(ImmutableList<T> newList, Map<T, Integer> indexToTake, OnDuplicate policy) {
		// put all of the new values into the newList
		boolean hasDuplicate = false;
		indexToTake.clear();
		for (int i = 0; i < newList.size(); ++i) {
			T item = newList.get(i);
			Integer previous = indexToTake.put(item, i);

			if (previous != null) {
				hasDuplicate = true;
				switch (policy) {
				case ERROR:
					throw new IllegalArgumentException("Item " + item + " is a duplicate!");
				case TAKE_FIRST:
					// we'll keep the one that used to be there
					indexToTake.put(item, previous);
					break;
				case TAKE_LAST:
					// we'll keep the element that we just set in the map
					break;
				default:
					throw Unhandled.enumException(policy);
				}
			}
		}

		if (!hasDuplicate) {
			// if there wasn't a duplicate, then there's no change necessary
			return newList;
		} else {
			List<T> noDuplicates = Lists.newArrayListWithCapacity(indexToTake.size());
			for (int i = 0; i < newList.size(); ++i) {
				T value = newList.get(i);
				if (indexToTake.get(value) == i) {
					// if we're supposed to take this value, then take it!
					noDuplicates.add(value);
				}
			}
			// returns the filtered list
			return ImmutableList.copyOf(noDuplicates);
		}
	}

	/** Adds the given element at the given index. */
	public void add(int idx, T element) {
		set(ImmutableUtil.noDuplicatesAdd(selection, idx, element));
	}

	/** Adds the given element at the end. */
	public void add(T element) {
		add(selection.size(), element);
	}

	/** Removes the given element. */
	public void remove(T toRemove) {
		set(ImmutableUtil.remove(selection, toRemove));
	}
}
