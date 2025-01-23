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
package com.diffplug.common.rx

import com.diffplug.common.base.Box
import com.diffplug.common.base.Converter
import com.diffplug.common.rx.Rx.subscribe
import java.util.function.Consumer
import java.util.function.Function
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [RxGetter] and [Box] combined in one: a value you can set, get, and subscribe to. */
interface RxBox<T> : RxGetter<T>, Box<T> {
	/** Returns a read-only version of this `RxBox`. */
	fun readOnly(): RxGetter<T> {
		return this
	}

	/** Maps one `RxBox` to another `RxBox`. */
	override fun <R> map(converter: Converter<T, R>): RxBox<R> {
		return RxBoxImp.Mapped(this, converter)
	}

	/**
	 * Provides a mechanism for enforcing an invariant on an existing `RxBox`.
	 *
	 * The returned `RxBox` and its observable will **always** satisfy the given invariant. If the
	 * underlying `RxBox` changes in a way which does not satisfy the invariant, it will be set so
	 * that it does match the invariant.
	 *
	 * During this process, the underlying `RxBox` will momentarily fail to meet the invariant, and
	 * its `Observable` will emit values which fail the invariant. The returned `RxBox`, however, will
	 * always meet the invariant, so downstream consumers can rely on the invariant holding true at
	 * all times.
	 *
	 * The returned `RxBox` can be mapped, and has the same atomicity guarantees as the underlying
	 * `RxBox` (e.g. an enforced [RxLockBox] can still be modified atomically).
	 *
	 * Conflicting calls to `enforce` can cause an infinite loop, see [Breaker] for a possible
	 * solution.
	 *
	 * ```java
	 * // this will not end well...
	 * RxBox.of(1).enforce(Math::abs).enforce(i -> -Math.abs(i));
	 * ```
	 */
	fun enforce(enforcer: Function<in T, out T>): RxBox<T> {
		// this must be a plain-old observable, because it needs to fire
		// every time an invariant is violated, not only when a violation
		// of the invariant causes a change in the output
		val mapped = asFlow().map { t: T -> enforcer.apply(t) }
		subscribe(mapped) { value: T -> this.set(value) }
		// now we can return the RxBox
		return map(Converter.from(enforcer, enforcer))
	}

	companion object {
		/** Creates an `RxBox` with the given initial value. */
		@JvmStatic
		fun <T> of(initial: T): RxBox<T> {
			return RxBoxImp(initial)
		}

		/**
		 * Creates an `RxBox` which implements the "getter" part with `RxGetter`, and the setter part
		 * with `Consumer`.
		 */
		@JvmStatic
		fun <T> from(getter: RxGetter<T>, setter: Consumer<T>): RxBox<T> {
			return object : RxBox<T> {
				override fun asFlow(): Flow<T> {
					return getter.asFlow()
				}

				override fun get(): T {
					return getter.get()
				}

				override fun set(value: T) {
					setter.accept(value)
				}
			}
		}
	}
}
