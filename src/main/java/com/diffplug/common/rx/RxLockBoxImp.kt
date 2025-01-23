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
import java.util.function.Function
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class RxLockBoxImp<T> : LockBoxImp<T>, RxLockBox<T> {
	val subject: MutableStateFlow<T>

	constructor(value: T) : super(value) {
		subject = MutableStateFlow(value)
	}

	constructor(value: T, lock: Any) : super(value, lock) {
		subject = MutableStateFlow(value)
	}

	override fun set(newValue: T) {
		synchronized(lock()) {
			if (newValue != value) {
				value = newValue
				subject.value = newValue
			}
		}
	}

	override fun asFlow(): Flow<T> {
		return subject
	}

	override fun toString(): String {
		return "RxLockBox.of[" + get() + "]"
	}

	internal class Mapped<T, R>(delegate: RxLockBox<T>, converter: Converter<T, R>) :
			MappedImp<T, R, RxLockBox<T>>(delegate, converter), RxLockBox<R> {
		val observable: Flow<R>

		init {
			val mapped = delegate.asFlow().map { a: T -> converter.convertNonNull(a) }
			observable = mapped.distinctUntilChanged()
		}

		override fun lock(): Any {
			return delegate.lock()
		}

		override fun asFlow(): Flow<R> {
			return observable
		}

		override fun modify(mutator: Function<in R, out R>): R {
			val result = Box.Nullable.ofNull<R>()
			delegate.modify { input: T ->
				val unmappedResult = mutator.apply(converter.convertNonNull(input))
				result.set(unmappedResult)
				converter.revertNonNull(unmappedResult)
			}
			return result.get()
		}
	}
}
