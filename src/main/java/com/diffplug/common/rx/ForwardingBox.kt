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
import kotlinx.coroutines.flow.Flow

/**
 * Utility class for wrapping one kind of box with another.
 *
 * - For wrapping a [CasBox], use [ForwardingBox.Cas].
 * - For wrapping an [RxBox], use [ForwardingBox.Rx].
 * - For wrapping a [LockBox], use [ForwardingBox.Lock].
 * - For wrapping an [RxLockBox], use [ForwardingBox.RxLock].
 *
 * Especially useful for overridding set().
 */
open class ForwardingBox<T, BoxType : Box<T>>
protected constructor(protected val delegate: BoxType) : Box<T> {
	override fun get(): T {
		return delegate.get()
	}

	override fun set(value: T) {
		delegate.set(value)
	}

	class Cas<T> protected constructor(delegate: CasBox<T>) :
			ForwardingBox<T, CasBox<T>>(delegate), CasBox<T> {
		override fun compareAndSet(expect: T, update: T): Boolean {
			return delegate.compareAndSet(expect, update)
		}

		override fun getAndSet(newValue: T): T {
			return delegate.getAndSet(newValue)
		}
	}

	class Lock<T> protected constructor(delegate: LockBox<T>) :
			ForwardingBox<T, LockBox<T>>(delegate), LockBox<T> {
		override fun lock(): Any {
			return delegate.lock()
		}
	}

	open class Rx<T> protected constructor(delegate: RxBox<T>) :
			ForwardingBox<T, RxBox<T>>(delegate), RxBox<T> {
		override fun asObservable(): Flow<T> {
			return delegate.asObservable()
		}
	}

	class RxLock<T> protected constructor(delegate: RxLockBox<T>) :
			ForwardingBox<T, RxLockBox<T>>(delegate), RxLockBox<T> {
		override fun lock(): Any {
			return delegate.lock()
		}

		override fun asObservable(): Flow<T> {
			return delegate.asObservable()
		}
	}
}
