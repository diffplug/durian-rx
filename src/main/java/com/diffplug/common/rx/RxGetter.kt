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
import com.diffplug.common.rx.Rx.subscribe
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Represents a value which can be accessed through a traditional `get()` method or by listening to
 * its [io.reactivex.Observable].
 *
 * `RxGetter`'s `Observable` has the semantics of a [io.reactivex.subjects.BehaviorSubject], meaning
 * that as soon as a listener subscribes to the `Observable`, it will emit the current value.
 *
 * Any time the value changes, `RxGetter`'s `Observable` will notify of the change. If the value did
 * not change (e.g. a field is set to its current value, which produces no change) then the
 * `Observable` will not fire.
 */
interface RxGetter<T : Any> : IFlowable<T>, Supplier<T> {
	/**
	 * Maps an `RxGetter` to a new `RxGetter` by applying the `mapper` function to all of its values.
	 *
	 * If the `Observable` of the source `RxGetter` changes, but the `Function<T></T>, R> mapper`
	 * collapses these values to produce no change, then the mapped `Observable` shall not emit a new
	 * value.
	 * * Incorrect: `("A", "B", "C") -> map(String::length) = (1, 1, 1)`
	 * * Correct: `("A", "B", "C") -> map(String::length) = (1)`
	 */
	fun <R : Any> map(mapper: Function<in T, out R>): RxGetter<R> {
		val src = this
		val mapped = src.asFlow().map { t: T -> mapper.apply(t) }
		val observable = mapped.distinctUntilChanged()
		return object : RxGetter<R> {
			override fun asFlow(): Flow<R> {
				return observable
			}

			override fun get(): R {
				return mapper.apply(src.get())
			}
		}
	}

	companion object {
		/**
		 * Creates an `RxGetter` from the given `Observable` and `initialValue`, appropriate for
		 * observables which emit values on a single thread.
		 *
		 * The value returned by [RxGetter.get] will be the last value emitted by the observable, as
		 * recorded by a non-volatile field.
		 */
		@JvmStatic
		fun <T : Any> from(observable: Flow<T>, initialValue: T): RxGetter<T> {
			val box = Box.of(initialValue)
			subscribe(observable) { value: T -> box.set(value) }
			return object : RxGetter<T> {
				override fun asFlow(): Flow<T> {
					return observable
				}

				override fun get(): T {
					return box.get()
				}
			}
		}

		/**
		 * Creates an `RxGetter` which combines two `RxGetter`s using the `BiFunction combine`.
		 *
		 * As with [.map], the observable only emits a new value if its value has changed.
		 */
		@JvmStatic
		fun <T1 : Any, T2 : Any, R : Any> combineLatest(
				t: RxGetter<out T1>,
				u: RxGetter<out T2>,
				combine: BiFunction<in T1, in T2, out R>
		): RxGetter<R> {
			val result: Flow<R> = t.asFlow().combine(u.asFlow()) { a, b -> combine.apply(a, b) }
			return from(result.distinctUntilChanged(), combine.apply(t.get(), u.get()))
		}
	}
}
