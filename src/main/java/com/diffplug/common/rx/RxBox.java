/*
 * Copyright 2018 DiffPlug
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

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Converter;
import io.reactivex.Observable;

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
	 * Provides a mechanism for enforcing an invariant on an existing
	 * `RxBox`.
	 *
	 * The returned `RxBox` and its observable will **always** satisfy the
	 * given invariant.  If the underlying `RxBox` changes in a way which
	 * does not satisfy the invariant, it will be set so that it does match
	 * the invariant.
	 *
	 * During this process, the underlying `RxBox` will momentarily fail to
	 * meet the invariant, and its `Observable` will emit values which fail
	 * the invariant.  The returned `RxBox`, however, will always meet the
	 * invariant, so downstream consumers can rely on the invariant holding
	 * true at all times.
	 *
	 * The returned `RxBox` can be mapped, and has the same atomicity guarantees
	 * as the underlying `RxBox` (e.g. an enforced {@link RxLockBox} can still be
	 * modified atomically).
	 *
	 * Conflicting calls to `enforce` can cause an infinite loop, see {@link Breaker}
	 * for a possible solution.
	 *
	 * ```java
	 * // this will not end well...
	 * RxBox.of(1).enforce(Math::abs).enforce(i -> -Math.abs(i));
	 * ```
	 */
	default RxBox<T> enforce(Function<? super T, ? extends T> enforcer) {
		// this must be a plain-old observable, because it needs to fire
		// every time an invariant is violated, not only when a violation
		// of the invariant causes a change in the output
		Observable<T> mapped = asObservable().map(enforcer::apply);
		Rx.subscribe(mapped, this::set);
		// now we can return the RxBox
		return map(Converter.from(enforcer, enforcer));
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
