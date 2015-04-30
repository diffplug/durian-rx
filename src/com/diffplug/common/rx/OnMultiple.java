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

import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

/** Functions for dealing with the impedance mismatch between set and optionals. */
public class OnMultiple {
	/** Throws an exception when ImmutableSet is multiple. */
	public static <T> Function<ImmutableSet<T>, T> error() {
		return val -> {
			throw new IllegalArgumentException();
		};
	}

	/** Throws an exception when ImmutableSet is multiple. */
	public static <T> Function<ImmutableSet<T>, T> takeFirst() {
		return val -> val.asList().get(0);
	}
}