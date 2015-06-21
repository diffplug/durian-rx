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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * <code>{@link RxBox}&lt;{@link ImmutableBiMap}&lt;T&gt;&gt;</code>
 * with convenience methods for creating and mutating the map.
 */
public class RxBiMap<K, V> extends RxBox.Default<ImmutableBiMap<K, V>> {
	/** Creates an RxMap with an initially empty value. */
	public static <K, V> RxBiMap<K, V> ofEmpty() {
		return new RxBiMap<>(ImmutableBiMap.of());
	}

	/** Creates an RxMap with the given initial value. */
	public static <K, V> RxBiMap<K, V> of(Map<K, V> initial) {
		return new RxBiMap<>(ImmutableBiMap.copyOf(initial));
	}

	/** Initally holds the given collection. */
	protected RxBiMap(ImmutableBiMap<K, V> initial) {
		super(initial);
	}

	/** Sets the value of this map. */
	public void set(Map<K, V> value) {
		super.set(ImmutableBiMap.copyOf(value));
	}

	/** Mutates this map. */
	public void mutate(Consumer<BiMap<K, V>> mutator) {
		super.set(Immutables.mutateBiMap(get(), mutator));
	}
}
