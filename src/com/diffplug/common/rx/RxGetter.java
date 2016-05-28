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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import rx.Observable;

import com.diffplug.common.base.Box;

/** 
 * Represents a value which can be accessed through a traditional
 * {@code get()} method or by listening to its {@link rx.Observable}.
 * <p>
 * {@code RxGetter}'s {@code Observable} has the semantics of a
 * {@link rx.BehaviorSubject}, meaning that as soon as a listener
 * subscribes to the {@code Observable}, it will emit the current value.
 * <p>
 * Any time the value changes, {@code RxGetter}'s {@code Observable} will notify
 * of the change.  If the value did not change (e.g. a field is
 * set to its current value, which produces no change) then the
 * {@code Observable} will not fire.
 */
public interface RxGetter<T> extends IObservable<T>, Supplier<T> {
	/** 
	 * Maps an {@code RxGetter} to a new {@code RxGetter} by applying the {@code mapper} function
	 * to all of its values.
	 * <p>
	 * If the {@code Observable} of the source {@code RxGetter} changes, but the
	 * {@code Function<T, R> mapper} collapses these values to produce 
	 * no change, then the mapped {@code Observable} shall not emit a new value.
	 * <ul>
	 * <li>Incorrect: {@code ("A", "B", "C") -> map(String::length) = (1, 1, 1)}</li>
	 * <li>Correct: {@code ("A", "B", "C") -> map(String::length) = (1)}</li>
	 * </ul>
	 */
	default <R> RxGetter<R> map(Function<? super T, ? extends R> mapper) {
		final RxGetter<T> src = this;
		final Observable<R> mapped = src.asObservable().map(mapper::apply);
		final Observable<R> observable = mapped.distinctUntilChanged();
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

	/** Creates an {@code RxGetter} from the given {@code Observable} and {@code initialValue}. */
	public static <T> RxGetter<T> from(Observable<T> observableUnfiltered, T initialValue) {
		Observable<T> observable = observableUnfiltered.distinctUntilChanged();

		Box<T> box = Box.ofVolatile(initialValue);
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

	/**
	 * Creates an {@code RxGetter} which combines two {@code RxGetter}s using the {@code BiFunction combine}.
	 * 
	 * As with {@link #map}, the observable only emits a new value if its value has changed.
	 */
	public static <T1, T2, R> RxGetter<R> combineLatest(RxGetter<? extends T1> t, RxGetter<? extends T2> u, BiFunction<? super T1, ? super T2, ? extends R> combine) {
		Observable<R> result = Observable.combineLatest(t.asObservable(), u.asObservable(), combine::apply);
		return from(result.distinctUntilChanged(), combine.apply(t.get(), u.get()));
	}
}
