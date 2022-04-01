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
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

internal open class RxBoxImp<T> private constructor(initial: T, subject: BehaviorSubject<T>) :
		RxBox<T> {
	private var value: T
	private val subject: BehaviorSubject<T>

	/** Creates a Holder which holds the given value. */
	constructor(initial: T) : this(initial, BehaviorSubject.createDefault<T>(initial)) {}

	/** The constructor for implementing these selection models. */
	init {
		value = Objects.requireNonNull(initial)
		this.subject = Objects.requireNonNull(subject)
	}

	/** Sets the value. */
	override fun set(newValue: T) {
		if (newValue != value) {
			value = newValue
			subject.onNext(newValue)
		}
	}

	/** Returns the value. */
	override fun get(): T {
		return value
	}

	override fun asObservable(): Observable<T> {
		return subject
	}

	internal class Mapped<T, R>(delegate: RxBox<T>, converter: Converter<T, R>) :
			MappedImp<T, R, RxBox<T>>(delegate, converter), RxBox<R> {
		val observable: Observable<R>

		init {
			val mapped = delegate.asObservable().map { a: T -> converter.convertNonNull(a) }
			observable = mapped.distinctUntilChanged()
		}

		override fun asObservable(): Observable<R> {
			return observable
		}
	}
}
