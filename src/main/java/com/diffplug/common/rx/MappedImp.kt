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

internal open class MappedImp<T : Any, R : Any, BoxType : Box<T>>(
		@JvmField protected val delegate: BoxType,
		@JvmField protected val converter: Converter<T, R>
) : Box<R> {
	override fun get(): R = converter.convertNonNull(delegate.get())

	override fun set(value: R) = delegate.set(converter.revertNonNull(value))

	/** Shortcut for doing a set() on the result of a get(). */
	override fun modify(mutator: Function<in R, out R>): R {
		val result = Box.Nullable.ofNull<R>()
		delegate.modify { input: T ->
			val unmappedResult = mutator.apply(converter.convertNonNull(input))
			result.set(unmappedResult)
			converter.revertNonNull(unmappedResult)
		}
		return result.get()
	}

	override fun toString(): String =
			"[" + delegate + " mapped to " + get() + " by " + converter + "]"
}
