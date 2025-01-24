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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal open class RxBoxImp<T : Any>(initial: T) : RxBox<T> {
	private val subject = MutableStateFlow(initial)

	override fun set(newValue: T) {
		if (subject.value != newValue) {
			subject.value = newValue
		}
	}

	override fun get(): T = subject.value

	override fun asFlow(): Flow<T> = subject

	internal class Mapped<T : Any, R : Any>(delegate: RxBox<T>, converter: Converter<T, R>) :
			MappedImp<T, R, RxBox<T>>(delegate, converter), RxBox<R> {
		val flow: Flow<R> = delegate.asFlow().map(converter::convertNonNull).distinctUntilChanged()

		override fun asFlow(): Flow<R> = flow
	}
}
