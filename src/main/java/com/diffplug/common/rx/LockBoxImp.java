/*
 * Copyright 2020 DiffPlug
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


import com.diffplug.common.base.Converter;
import java.util.Objects;

class LockBoxImp<T> implements LockBox<T> {
	protected T value;
	protected final Object lock;

	protected LockBoxImp(T value) {
		this.value = value;
		this.lock = this;
	}

	protected LockBoxImp(T value, Object lock) {
		this.value = value;
		this.lock = lock;
	}

	@Override
	public Object lock() {
		return lock;
	}

	@Override
	public T get() {
		synchronized (lock()) {
			return value;
		}
	}

	@Override
	public void set(T value) {
		synchronized (lock()) {
			this.value = Objects.requireNonNull(value);
		}
	}

	@Override
	public String toString() {
		return "LockBox.of[" + get() + "]";
	}

	static class Mapped<T, R> extends MappedImp<T, R, LockBox<T>> implements LockBox<R> {
		public Mapped(LockBox<T> delegate, Converter<T, R> converter) {
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
}
