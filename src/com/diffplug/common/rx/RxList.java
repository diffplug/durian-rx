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

import com.google.common.collect.ImmutableList;

/**
 * An extension of RxValue<ImmutableList<T>>, with convenience
 * methods for modifying the list.
 */
public class RxList<T> extends RxValue<ImmutableList<T>> {
	/** Creates an RxList with an initially empty value. */
	public static <T> RxList<T> ofEmpty() {
		return of(ImmutableList.of());
	}

	/** Creates an RxList with the given initial value. */
	public static <T> RxList<T> of(ImmutableList<T> initial) {
		return new RxList<T>(initial);
	}

	/** Initally holds the given collection. */
	protected RxList(ImmutableList<T> initial) {
		super(initial);
	}

	/** Adds the given element at the given index. */
	public void add(int idx, T element) {
		set(ImmutableUtil.add(selection, idx, element));
	}

	/** Adds the given element at the end. */
	public void add(T element) {
		add(selection.size(), element);
	}

	/** Removes all instances of the given element. */
	public void removeAllInstancesOf(T toRemove) {
		set(ImmutableUtil.remove(selection, toRemove));
	}

	/** Removes the element at the given index. */
	public void remove(int oldIdx, T toRemove) {
		set(ImmutableUtil.remove(selection, oldIdx, toRemove));
	}

	/** Sets the list to the given list. */
	public void set(List<T> newSelection) {
		super.set(ImmutableList.copyOf(newSelection));
	}
}
