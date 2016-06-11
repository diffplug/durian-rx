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

import java.util.Objects;

import rx.Observable;
import rx.subjects.BehaviorSubject;

class RxBoxImp<T> implements RxBox<T> {
	private T value;
	private final BehaviorSubject<T> subject;

	/** Creates a Holder which holds the given value. */
	RxBoxImp(T initial) {
		this(initial, BehaviorSubject.create(initial));
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
}
