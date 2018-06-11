/*
 * Copyright 2016 DiffPlug
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

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Converter;

class MappedImp<T, R, BoxType extends Box<T>> implements Box<R> {
	protected final BoxType delegate;
	protected final Converter<T, R> converter;

	public MappedImp(BoxType delegate, Converter<T, R> converter) {
		this.delegate = delegate;
		this.converter = converter;
	}

	@Override
	public R get() {
		return converter.convertNonNull(delegate.get());
	}

	@Override
	public void set(R value) {
		delegate.set(converter.revertNonNull(value));
	}

	/** Shortcut for doing a set() on the result of a get(). */
	@Override
	public R modify(Function<? super R, ? extends R> mutator) {
		Box.Nullable<R> result = Box.Nullable.ofNull();
		delegate.modify(input -> {
			R unmappedResult = mutator.apply(converter.convertNonNull(input));
			result.set(unmappedResult);
			return converter.revertNonNull(unmappedResult);
		});
		return result.get();
	}

	@Override
	public String toString() {
		return "[" + delegate + " mapped to " + get() + " by " + converter + "]";
	}
}
