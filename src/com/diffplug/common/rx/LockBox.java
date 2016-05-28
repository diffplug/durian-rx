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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Converter;

/**
 * LockBox is a box where every call to {@link #modify(Function)}
 * happens within a synchronized block.
 * 
 * Using the {@link #transactOn(LockBox...)} method, you can obtain a lock
 * on multiple boxes in a way which is guaranteed to be free
 * of deadlock, so long as no one is getting a lock except through
 * {@link #modify(Function)} and {@link #transactOn(LockBox...)}.
 */
public interface LockBox<T> extends Box<T> {
	/**
	 * The lock which is used by this LockBox's {@link #modify(Function) method}.
	 * 
	 * For a LockBox which holds state, the LockBox itself is used.  For a mapped
	 * LockBox, the underlying LockBox which actually holds the state is used.
	 */
	Object lock();

	@Override
	default T modify(Function<? super T, ? extends T> mutator) {
		synchronized (lock()) {
			set(mutator.apply(get()));
			return get();
		}
	}

	/** Creates a `LockBox` containing the given value. */
	public static <T> LockBox<T> of(T value) {
		return new Default<>(value);
	}

	static class Default<T> implements LockBox<T> {
		protected T value;

		protected Default(T value) {
			set(value);
		}

		@Override
		public Object lock() {
			return this;
		}

		@Override
		public T get() {
			return value;
		}

		@Override
		public void set(T value) {
			this.value = Objects.requireNonNull(value);
		}

		@Override
		public String toString() {
			return "LockBox.of[" + get() + "]";
		}
	}

	/**
	 * Maps this LockBox to a new value which will have the
	 * same lock as the original lock, since there's still
	 * only one piece of state.
	 */
	@Override
	default <R> LockBox<R> map(Converter<T, R> converter) {
		return new LockMapped<>(this, converter);
	}

	static class LockMapped<T, R> extends MappedImp<T, R, LockBox<T>> implements LockBox<R> {
		public LockMapped(LockBox<T> delegate, Converter<T, R> converter) {
			super(delegate, converter);
		}

		/**
		 * Ensures that we use the root delegate which
		 * is actually holding the state as our lock.
		 */
		@Override
		public Object lock() {
			return delegate.lock();
		}
	}

	/**
	 * Creates an OrderedLock which allows running transactions on the given list of LockBoxes.
	 * 
	 * This OrderedLock can be reused, and it is efficient to do so.
	 */
	public static OrderedLock transactOn(@SuppressWarnings("rawtypes") LockBox... locks) {
		return OrderedLock.on(Arrays.asList(locks).stream().map(LockBox::lock).collect(Collectors.toList()));
	}
}