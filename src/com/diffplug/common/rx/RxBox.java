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

import java.util.function.Consumer;
import java.util.function.Function;

import rx.Observable;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Consumers;
import com.diffplug.common.base.Converter;

/**
 * {@link RxGetter} and {@link Box} combined in one: a value you can set, get, and subscribe to.
 */
public interface RxBox<T> extends RxGetter<T>, Box<T> {
	/** Returns a read-only version of this `RxBox`. */
	default RxGetter<T> readOnly() {
		return this;
	}

	/** Maps one `RxBox` to another `RxBox`. */
	@Override
	default <R> RxBox<R> map(Converter<T, R> converter) {
		return new RxBoxImp.Mapped<>(this, converter);
	}

	/**
	 * Provides a mechanism for enforcing an invariant.
	 *
	 * For example, let's say that there is an `RxBox<List<T>>` which represents
	 * a user's multiselection in some widget. You would like the user to select
	 * a pair of values - you want to enforce that the selection doesn't grow
	 * beyond 2 elements.
	 *
	 * You could write code like this:
	 *
	 * ```java
	 * RxBox<List<T>> selection = widget.rxSelection();
	 * RxGetter<List<T>> onlyPairs = selection.map(list -> list.stream().limit(2).collect(Collectors.toList()));
	 * Rx.subscribe(onlyPairs, selection);
	 * ```
	 *
	 * But there's a subtle bug in this code.  Because {@link RxGetter} and {@link RxBox} only
	 * fire their observables when their value changes, the selection invariant won't be enforced
	 * unless `onlyPairs` actually changes.  It's easy to fix:
	 *
	 * ```java
	 * RxBox<List<T>> selection = widget.rxSelection();
	 * Observable<List<T>> onlyPairs = selection.asObservable().map(list -> list.stream().limit(2).collect(Collectors.toList()));
	 * Rx.subscribe(onlyPairs, selection);
	 * ```
	 *
	 * But there's still a problem - `selection` will momentarily contain values which violate the invariant. That
	 * means you can't write listener code that relies on the invariant being true.  Which is why the `enforce()` method exists:
	 *
	 * ```java
	 * RxBox<List<T>> onlyPairs = widget.rxSelection().enforce(list -> list.stream().limit(2).collect(Collectors.toList()));
	 * ```
	 *
	 * This creates a new `RxBox` which will always satisfy the invariant, and it will enforce this invariant
	 * on the original `RxBox`. 
	 */
	default RxBox<T> enforce(Function<? super T, ? extends T> enforcer) {
		// this must be a plain-old observable, because it needs to fire
		// every time an invariant is violated, not only when a violation
		// of the invariant causes a change in the output
		Observable<T> mapped = asObservable().map(enforcer::apply);
		Rx.subscribe(mapped, this::set);
		// now we can return the RxBox
		return from(map(enforcer), Consumers.compose(enforcer, this::set));
	}

	/** Creates an `RxBox` with the given initial value. */
	public static <T> RxBox<T> of(T initial) {
		return new RxBoxImp<T>(initial);
	}

	/** Creates an `RxBox` which implements the "getter" part with `RxGetter`, and the setter part with `Consumer`. */
	public static <T> RxBox<T> from(RxGetter<T> getter, Consumer<T> setter) {
		return new RxBox<T>() {
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
}
