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

class RxLockBoxImp<T> extends LockBoxImp<T> implements RxLockBox<T> {
	final BehaviorSubject<T> subject;

	protected RxLockBoxImp(T value) {
		super(value);
		subject = BehaviorSubject.create(value);
	}

	protected RxLockBoxImp(T value, Object lock) {
		super(value, lock);
		subject = BehaviorSubject.create(value);
	}

	@Override
	public void set(T newValue) {
		synchronized (lock()) {
			if (!newValue.equals(value)) {
				value = newValue;
				subject.onNext(newValue);
			}
		}
	}

	@Override
	public Observable<T> asObservable() {
		return subject;
	}

	@Override
	public String toString() {
		return "RxLockBox.of[" + get() + "]";
	}

	static class Mapped<T, R> extends MappedImp<T, R, RxLockBox<T>> implements RxLockBox<R> {
		final Observable<R> observable;

		public Mapped(RxLockBox<T> delegate, Converter<T, R> converter) {
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
