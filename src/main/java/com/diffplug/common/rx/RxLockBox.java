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

import java.util.function.Function;

import com.diffplug.common.base.Converter;
import io.reactivex.Observable;

/** {@link RxBox} and {@link LockBox} in one. */
public interface RxLockBox<T> extends LockBox<T>, RxBox<T> {
	/** Creates an `RxLockBox` containing the given value, which uses itself as the lock. */
	public static <T> RxLockBox<T> of(T value) {
		return new RxLockBoxImp<>(value);
	}

	/** Creates an `RxLockBox` containing the given value, which uses `lock` as the lock. */
	public static <T> RxLockBox<T> of(T value, Object lock) {
		return new RxLockBoxImp<>(value, lock);
	}

	/** RxLockBox must map to another kind of LockBox. */
	@Override
	default <R> RxLockBox<R> map(Converter<T, R> converter) {
		return new RxLockBoxImp.Mapped<T, R>(this, converter);
	}

	@Override
	default RxLockBox<T> enforce(Function<? super T, ? extends T> enforcer) {
		// this must be a plain-old observable, because it needs to fire
		// every time an invariant is violated, not only when a violation
		// of the invariant causes a change in the output
		Observable<T> mapped = asObservable().map(enforcer::apply);
		Rx.subscribe(mapped, this::set);
		// now we can return the RxBox
		return map(Converter.from(enforcer, enforcer));
	}
}
