/*
 * Copyright (C) 2020-2022 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.rx;


import com.diffplug.common.base.Preconditions;
import com.diffplug.common.base.Unhandled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * <code>{@link RxBox}&lt;{@link List}&lt;T&gt;&gt;</code>
 * which promises to exclude duplicates.
 *
 * We dont' actually want it to be this - we actually want it to be a
 * stateful wrapper around calls to `set`, but we'll settle for this
 * for now.
 */
public class RxOrderedSet<T> extends ForwardingBox.Rx<List<T>> {
	/** Creates an RxList with an initially empty value. */

	public static <T> RxOrderedSet<T> ofEmpty() {
		return of(Collections.emptyList());
	}

	/** Creates an RxList with an initially empty value. */
	public static <T> RxOrderedSet<T> ofEmpty(OnDuplicate duplicatePolicy) {
		return of(Collections.emptyList(), duplicatePolicy);
	}

	/** Creates an RxList with the given initial value. */
	public static <T> RxOrderedSet<T> of(List<T> initial) {
		return of(initial, OnDuplicate.ERROR);
	}

	/** Creates an RxList with the given initial value. */
	public static <T> RxOrderedSet<T> of(List<T> initial, OnDuplicate duplicatePolicy) {
		return new RxOrderedSet<T>(initial, duplicatePolicy);
	}

	/** Initally holds the given collection. */
	protected RxOrderedSet(List<T> initial, OnDuplicate duplicatePolicy) {
		super(RxBox.of(filter(initial, duplicatePolicy)));
		this.policy = duplicatePolicy;
	}

	private final OnDuplicate policy;

	// @formatter:off
	/** Policies for disallowing duplicates. */
	public enum OnDuplicate {
		/** Throws an error when a duplicate is encountered. */
		ERROR,
		/** Resolve duplicates by taking the first duplicate in the list. */
		TAKE_FIRST,
		/** Resolve duplicates by taking the last duplicate in the list. */
		TAKE_LAST
	}
	// @formatter:on

	/** Returns the duplicate policy for this RxList. */
	public OnDuplicate getDuplicatePolicy() {
		return policy;
	}

	/** Sets the selection. */
	@Override
	public void set(List<T> newSelection) {
		Preconditions.checkNotNull(newSelection);
		if (!get().equals(newSelection)) {
			// the selection changed, so we will check it for duplicates
			newSelection = filter(newSelection, policy);
			// if it's still different than we expect...
			super.set(newSelection);
		}
	}

	private static <T> List<T> filter(List<T> newList, OnDuplicate policy) {
		Objects.requireNonNull(policy);
		Map<T, Integer> indexToTake = new HashMap<>();

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
			List<T> noDuplicates = new ArrayList<>(indexToTake.size());
			for (int i = 0; i < newList.size(); ++i) {
				T value = newList.get(i);
				if (indexToTake.get(value) == i) {
					// if we're supposed to take this value, then take it!
					noDuplicates.add(value);
				}
			}
			// returns the filtered list
			return noDuplicates;
		}
	}

	/** Mutates this set. */
	public List<T> mutate(Consumer<List<T>> mutator) {
		List<T> mutated = new ArrayList<>(get());
		mutator.accept(mutated);
		set(mutated);
		return mutated;
	}
}
