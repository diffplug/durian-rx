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
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import java.util.Objects;

class RxBoxImp<T> implements RxBox<T> {
	private T value;
	private final BehaviorSubject<T> subject;

	/** Creates a Holder which holds the given value. */
	RxBoxImp(T initial) {
		this(initial, BehaviorSubject.createDefault(initial));
	}

	/** The constructor for implementing these selection models. */
	private RxBoxImp(T initial, BehaviorSubject<T> subject) {
		this.value = Objects.requireNonNull(initial);
		this.subject = Objects.requireNonNull(subject);
	}

	/** Sets the value. */
	@Override
	public void set(T newValue) {
		if (!newValue.equals(value)) {
			value = newValue;
			subject.onNext(newValue);
		}
	}

	/** Returns the value. */
	@Override
	public T get() {
		return value;
	}

	@Override
	public Observable<T> asObservable() {
		return subject;
	}

	static class Mapped<T, R> extends MappedImp<T, R, RxBox<T>> implements RxBox<R> {
		final Observable<R> observable;

		public Mapped(RxBox<T> delegate, Converter<T, R> converter) {
			super(delegate, converter);
			Observable<R> mapped = delegate.asObservable().map(converter::convertNonNull);
			observable = mapped.distinctUntilChanged();
		}

		@Override
		public Observable<R> asObservable() {
			return observable;
		}
	}
}
