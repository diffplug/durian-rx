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

import com.diffplug.common.base.Converter
import com.diffplug.common.rx.Rx.subscribe
import java.util.function.Function
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [RxBox] and [LockBox] in one. */
interface RxLockBox<T> : LockBox<T>, RxBox<T> {
	/** RxLockBox must map to another kind of LockBox. */
	override fun <R> map(converter: Converter<T, R>): RxLockBox<R> {
		return RxLockBoxImp.Mapped(this, converter)
	}

	override fun enforce(enforcer: Function<in T, out T>): RxLockBox<T> {
		// this must be a plain-old observable, because it needs to fire
		// every time an invariant is violated, not only when a violation
		// of the invariant causes a change in the output
		val mapped: Flow<T> = asFlow().map { t: T -> enforcer.apply(t) }
		subscribe(mapped) { value: T -> this.set(value) }
		// now we can return the RxBox
		return map(Converter.from(enforcer, enforcer))
	}

	companion object {
		/** Creates an `RxLockBox` containing the given value, which uses itself as the lock. */
		@JvmStatic
		fun <T> of(value: T): RxLockBox<T> {
			return RxLockBoxImp(value)
		}

		/** Creates an `RxLockBox` containing the given value, which uses `lock` as the lock. */
		@JvmStatic
		fun <T> of(value: T, lock: Any): RxLockBox<T> {
			return RxLockBoxImp(value, lock)
		}
	}
}
