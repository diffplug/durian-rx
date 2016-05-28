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

import rx.Observable;
import rx.subjects.BehaviorSubject;

import com.diffplug.common.base.Converter;

public interface RxLockBox<T> extends LockBox<T>, RxBox<T> {
	public static <T> RxLockBox<T> of(T value) {
		return new Default<>(value);
	}

	static class Default<T> extends LockBox.Default<T> implements RxLockBox<T> {
		BehaviorSubject<T> subject = BehaviorSubject.create();

		protected Default(T value) {
			super(value);
			subject = BehaviorSubject.create();
		}

		@Override
		public Observable<T> asObservable() {
			return subject;
		}
	}

	/** RxLockBox must map to another kind of LockBox. */
	@Override
	default <R> RxLockBox<R> map(Converter<T, R> converter) {
		return new RxLockMapped<T, R>(this, converter);
	}

	static class RxLockMapped<T, R> extends MappedImp<T, R, RxLockBox<T>> implements RxLockBox<R> {
		final Observable<R> observable;

		public RxLockMapped(RxLockBox<T> delegate, Converter<T, R> converter) {
			super(delegate, converter);
			Observable<R> mapped = delegate.asObservable().map(converter::convertNonNull);
			observable = mapped.distinctUntilChanged();
		}

		@Override
		public Object lock() {
			return delegate.lock();
		}

		@Override
		public Observable<R> asObservable() {
			return observable;
		}
	}
}
