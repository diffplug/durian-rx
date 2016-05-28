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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Converter;

/**
 * CasBox is a lock-free and race-condition-free mechanism
 * for updating a value.
 *
 * Its API and implementation is taken straight from [Clojure's Atom](http://clojure.org/reference/atoms)
 * concept.  Many thanks to Rich Hickey for his excellent work.
 */
public interface CasBox<T> extends Box<T> {
	/** The compare and set method which this box is capable of using. */
	boolean compareAndSet(T expect, T update);

	/**
	 * Applies the given mutator function to this box, which may require
	 * calling the function more than once, so make sure it's pure!
	 * 
	 * The function is called using the box's current input, and
	 * {@link compareAndSet} is used to ensure that the input does
	 * not change.
	 * 
	 * The implementation is more or less verbatim from Rich Hickey's
	 * [Clojure](https://github.com/clojure/clojure/blob/bfb82f86631bde45a8e3749ea7df509e59a0791c/src/jvm/clojure/lang/Atom.java#L75-L87).
	 */
	@Override
	default T modify(Function<? super T, ? extends T> mutator) {
		while (true) {
			T value = get();
			T newv = mutator.apply(value);
			if (compareAndSet(value, newv)) {
				return newv;
			}
		}
	}

	/** Returns a CasBox around the given value. */
	public static <T> CasBox<T> of(T value) {
		return new Default<>(value);
	}

	static class Default<T> implements CasBox<T> {
		private final AtomicReference<T> ref;

		Default(T value) {
			ref = new AtomicReference<>(value);
		}

		@Override
		public boolean compareAndSet(T expect, T update) {
			return ref.compareAndSet(expect, update);
		}

		@Override
		public T get() {
			return ref.get();
		}

		@Override
		public void set(T value) {
			ref.set(value);
		}

		@Override
		public String toString() {
			return "CasBox.of[" + get() + "]";
		}
	}

	@Override
	default <R> CasBox<R> map(Converter<T, R> converter) {
		return new CasMapped<>(this, converter);
	}

	static class CasMapped<T, R> extends MappedImp<T, R, CasBox<T>> implements CasBox<R> {
		public CasMapped(CasBox<T> delegate, Converter<T, R> converter) {
			super(delegate, converter);
		}

		@Override
		public boolean compareAndSet(R expect, R update) {
			T expectOrig = converter.revertNonNull(expect);
			T updateOrig = converter.revertNonNull(update);
			return delegate.compareAndSet(expectOrig, updateOrig);
		}
	}
}
