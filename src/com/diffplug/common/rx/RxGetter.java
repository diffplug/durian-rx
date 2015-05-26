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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import rx.Observable;

import com.diffplug.common.base.Box;

/** 
 * Represents a value which can be accessed through a traditional
 * get() method or by listening to its IObservable.
 * 
 * RxGetter's IObservable has the semantics of a BehaviorSubject,
 * meaning that as soon as a client subscribes to the Observable,
 * the RxGetter will send the current value.
 * 
 * Any time the value changes, RxGetter's observable will notify
 * of the change.  If the value did not change (e.g. a field is
 * set to its current value, which produces no change) then the
 * Observable should not fire.
 */
public interface RxGetter<T> extends IObservable<T>, Supplier<T> {
	/** 
	 * Maps an RxGetter to a new RxGetter by applying the mapper function
	 * to all of its values.
	 * 
	 * If the Observable of the source RxGetter changes, but the
	 * Function<T, R> mapper collapses these values to produce 
	 * no change, then the mapped Observable shall produce no change.
	 * 
	 * Correct Observable mapping:
	 * ("A", "B", "C") -> map(String::length) = (1)
	 * 
	 * Incorrect Observable mapping:
	 * ("A", "B", "C") -> map(String::length) = (1, 1, 1)
	 */
	default <R> RxGetter<R> map(Function<T, R> mapper) {
		final RxGetter<T> src = this;
		final Observable<R> observable = src.asObservable().map(mapper::apply).distinctUntilChanged();
		return new RxGetter<R>() {
			@Override
			public Observable<R> asObservable() {
				return observable;
			}

			@Override
			public R get() {
				return mapper.apply(src.get());
			}
		};
	}

	/** Creates an RxGetter from the given observable and initial value. */
	public static <T> RxGetter<T> from(Observable<T> observableUnfilters, T initialValue) {
		Observable<T> observable = observableUnfilters.distinctUntilChanged();

		Box.NonNull<T> box = Box.NonNull.of(initialValue);
		Rx.subscribe(observable, box::set);
		return new RxGetter<T>() {
			@Override
			public Observable<T> asObservable() {
				return observable;
			}

			@Override
			public T get() {
				return box.get();
			}
		};
	}

	/** Creates an RxGetter from the given observable and initial value. */
	public static <T1, T2, R> RxGetter<R> combineLatest(RxGetter<? extends T1> t, RxGetter<? extends T2> u, BiFunction<? super T1, ? super T2, ? extends R> combine) {
		Observable<R> result = Observable.combineLatest(t.asObservable(), u.asObservable(), combine::apply);
		return from(result, combine.apply(t.get(), u.get()));
	}
}
