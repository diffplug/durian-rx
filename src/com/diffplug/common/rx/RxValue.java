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

import java.util.function.Consumer;

import rx.Observable;
import rx.subjects.BehaviorSubject;

import com.google.common.base.Preconditions;

import com.diffplug.common.base.GetterSetter;

/**
 * RxGetter<T> and GetterSetter<T> combined in one.
 */
public interface RxValue<T> extends RxGetter<T>, GetterSetter<T> {
	/** Returns a read-only version of this RxValue. */
	default RxGetter<T> readOnly() {
		return this;
	}

	/** Creates an RxValue with the given initial value. */
	public static <T> RxValue<T> of(T initial) {
		return new Default<T>(initial);
	}

	/** Creates an RxValue which implements the "getter" part with RxGetter, and the setter part with the given Consumer. */
	public static <T> RxValue<T> of(RxGetter<T> getter, Consumer<T> setter) {
		return new RxValue<T>() {
			@Override
			public Observable<T> asObservable() {
				return getter.asObservable();
			}

			@Override
			public T get() {
				return getter.get();
			}

			@Override
			public void set(T value) {
				setter.accept(value);
			}
		};
	}

	/** The default implementation of an RxValue. */
	public static class Default<T> implements RxValue<T> {
		private volatile T value;
		private final BehaviorSubject<T> subject;

		/** Creates a Holder which holds the given value. */
		protected Default(T initial) {
			this(initial, BehaviorSubject.create(initial));
		}

		/** The constructor for implementing these selection models. */
		private Default(T initial, BehaviorSubject<T> subject) {
			this.value = initial;
			this.subject = subject;
		}

		/** Sets the value. */
		@Override
		public void set(T newValue) {
			Preconditions.checkNotNull(newValue);
			if (!value.equals(newValue)) {
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
}
