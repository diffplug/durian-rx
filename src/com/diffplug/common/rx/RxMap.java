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

import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;

/**
 * <code>{@link RxBox}&lt;{@link ImmutableMap}&lt;T&gt;&gt;</code>
 * with convenience methods for creating and mutating the map.
 */
public class RxMap<K, V> extends RxBox.Default<ImmutableMap<K, V>> {
	/** Creates an RxMap with an initially empty value. */
	public static <K, V> RxMap<K, V> ofEmpty() {
		return of(ImmutableMap.of());
	}

	/** Creates an RxMap with the given initial value. */
	public static <K, V> RxMap<K, V> of(Map<K, V> initial) {
		return new RxMap<K, V>(ImmutableMap.copyOf(initial));
	}

	/** Initally holds the given collection. */
	protected RxMap(ImmutableMap<K, V> initial) {
		super(initial);
	}

	/** Sets the value of this map. */
	public void set(Map<K, V> value) {
		super.set(ImmutableMap.copyOf(value));
	}

	/** Mutates this map. */
	public ImmutableMap<K, V> mutate(Consumer<Map<K, V>> mutator) {
		ImmutableMap<K, V> mutated = Immutables.mutateMap(get(), mutator);
		super.set(mutated);
		return mutated;
	}
}
