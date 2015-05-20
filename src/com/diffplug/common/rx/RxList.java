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
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

/**
 * An extension of RxValue<ImmutableList<T>>, with convenience
 * methods for modifying the list.
 */
public class RxList<T> extends RxValue.Default<ImmutableList<T>> {
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

	/** Sets the list to the given list. */
	public void set(List<T> newSelection) {
		super.set(ImmutableList.copyOf(newSelection));
	}

	/** Mutates this set. */
	public void mutate(Consumer<List<T>> mutator) {
		set(Immutables.mutateList(get(), mutator));
	}

	/** Mutates this set. */
	public <R> R mutateAndReturn(Function<List<T>, R> mutator) {
		return Immutables.mutateListAndReturn(this, mutator);
	}
}
