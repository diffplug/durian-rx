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

import com.diffplug.common.base.Converter;

class CasBoxImp<T> implements CasBox<T> {
	private final AtomicReference<T> ref;

	CasBoxImp(T value) {
		ref = new AtomicReference<>(value);
	}

	@Override
	public boolean compareAndSet(T expect, T update) {
		return ref.compareAndSet(expect, update);
	}

	@Override
	public T getAndSet(T newValue) {
		return ref.getAndSet(newValue);
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

	static class Mapped<T, R> extends MappedImp<T, R, CasBox<T>> implements CasBox<R> {
		public Mapped(CasBox<T> delegate, Converter<T, R> converter) {
			super(delegate, converter);
		}

		@Override
		public boolean compareAndSet(R expect, R update) {
			T expectOrig = converter.revertNonNull(expect);
			T updateOrig = converter.revertNonNull(update);
			return delegate.compareAndSet(expectOrig, updateOrig);
		}

		@Override
		public R getAndSet(R newValue) {
			T newValueOrig = converter.revertNonNull(newValue);
			T oldValueOrig = delegate.getAndSet(newValueOrig);
			return converter.convertNonNull(oldValueOrig);
		}
	}
}
