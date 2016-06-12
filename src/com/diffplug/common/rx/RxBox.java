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
	/** Returns a read-only version of this {@code RxBox}. */
	default RxGetter<T> readOnly() {
		return this;
	}

	/** Maps one {@code RxBox} to another {@code RxBox}. */
	@Override
	default <R> RxBox<R> map(Converter<T, R> converter) {
		return new RxBoxImp.Mapped<>(this, converter);
	}

	/**
	 * Provides a mechanism for enforcing an invariant.
	 * <p>
	 * For example, let's say that there is an {@code RxBox<List<T>>} which represents
	 * a user's multiselection in some widget. You would like the user to select
	 * a pair of values - you want to enforce that the selection doesn't grow
	 * beyond 2 elements.
	 * <p>
	 * You could write code like this:
	 * <pre>
	 * {@code
	 * RxBox<List<T>> selection = widget.rxSelection();
	 * RxGetter<List<T>> onlyPairs = selection.map(list -> list.stream().limit(2).collect(Collectors.toList()));
	 * Rx.subscribe(onlyPairs, selection);
	 * }
	 * </pre>
	 * But there's a subtle bug in this code.  Because {@link RxGetter} and {@link RxBox} only
	 * fire their observables when their value changes, the selection invariant won't be enforced
	 * unless {@code onlyPairs} actually changes.  It's easy to fix:
	 * <pre>
	 * {@code
	 * RxBox<List<T>> selection = widget.rxSelection();
	 * Observable<List<T>> onlyPairs = selection.asObservable().map(list -> list.stream().limit(2).collect(Collectors.toList()));
	 * Rx.subscribe(onlyPairs, selection);
	 * }
	 * </pre>
	 * But there's still a problem - {@code selection} will momentarily contain values which violate the invariant. That
	 * means you can't write listener code that relies on the invariant being true.  Which is why the {@code enforce()} method exists:
	 * <pre>
	 * {@code
	 * RxBox<List<T>> onlyPairs = widget.rxSelection().enforce(list -> list.stream().limit(2).collect(Collectors.toList()));
	 * }
	 * </pre>
	 * This creates a new {@code RxBox} which will always satisfy the invariant, and it will enforce this invariant
	 * on the original {@code RxBox}. 
	 */
	default RxBox<T> enforce(Function<? super T, ? extends T> getMapper) {
		// this must be a plain-old observable, because it needs to fire
		// every time an invariant is violated, not only when a violation
		// of the invariant causes a change in the output
		Observable<T> mapped = asObservable().map(getMapper::apply);
		Rx.subscribe(mapped, this::set);
		// now we can return the RxBox
		return from(map(getMapper), Consumers.compose(getMapper, this::set));
	}

	/** Creates an {@code RxBox} with the given initial value. */
	public static <T> RxBox<T> of(T initial) {
		return new RxBoxImp<T>(initial);
	}

	/** Creates an {@code RxBox} which implements the "getter" part with {@code RxGetter}, and the setter part with {@code Consumer}. */
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
