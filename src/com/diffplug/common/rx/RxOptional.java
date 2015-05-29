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

import java.util.Optional;

/**
 * An extension of RxBox<Optional<T>>, with convenience
 * methods for creation.
 */
public class RxOptional<T> extends RxBox.Default<Optional<T>> {
	/** Returns an empty RxOptional. */
	public static <T> RxOptional<T> ofEmpty() {
		return new RxOptional<T>(Optional.empty());
	}

	/** Returns an RxOptional of the given value. */
	public static <T> RxOptional<T> of(Optional<T> value) {
		return new RxOptional<T>(value);
	}

	/** Initially holds the given value. */
	protected RxOptional(Optional<T> initial) {
		super(initial);
	}
}
