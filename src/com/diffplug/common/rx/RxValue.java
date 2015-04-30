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

import rx.Observable;
import rx.subjects.BehaviorSubject;

import com.google.common.base.Preconditions;

import com.diffplug.common.base.GetterSetter;

/**
 * A simple implementation of RxGetter<T> which allows
 * the value to be set, and provides a mechanism for supplying
 * a read-only RxGetter<T> if desired.
 */
public class RxValue<T> implements RxGetter<T>, GetterSetter<T> {
	/** The constructor for implementing these selection models. */
	public static <T> RxValue<T> of(T initial) {
		return new RxValue<T>(initial);
	}

	protected volatile T selection;
	protected final BehaviorSubject<T> observable;

	/** Creates a Holder which holds the given value. */
	protected RxValue(T initial) {
		this(initial, BehaviorSubject.create(initial));
	}

	/** The constructor for implementing these selection models. */
	private RxValue(T initial, BehaviorSubject<T> subject) {
		selection = initial;
		this.observable = subject;
	}

	/** Sets the selection. */
	@Override
	public void set(T newSelection) {
		Preconditions.checkNotNull(newSelection);
		if (!selection.equals(newSelection)) {
			this.selection = newSelection;
			observable.onNext(selection);
		}
	}

	/** Returns the selection. */
	@Override
	public T get() {
		return selection;
	}

	@Override
	public Observable<T> asObservable() {
		return observable;
	}

	/** A read-only version of this RxValue. */
	public RxGetter<T> readOnly() {
		return this;
	}
}
