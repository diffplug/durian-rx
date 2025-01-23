/*
 * Copyright (C) 2020-2025 DiffPlug
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

import com.diffplug.common.base.Errors
import com.diffplug.common.util.concurrent.FutureCallback
import java.util.*
import java.util.function.Consumer

class RxListener<T>
internal constructor(
		internal val onValue: Consumer<T>,
		internal val onTerminate: Consumer<Optional<Throwable>>
) : FutureCallback<T?> {
	override fun onSuccess(result: T?) {
		onValue.accept(result as T)
		onTerminate.accept(Optional.empty())
	}

	override fun onFailure(e: Throwable) {
		onTerminate.accept(Optional.of(e))
	}

	fun onErrorDontLog(e: Throwable) {
		if (onTerminate === logErrors) {
			return
		} else {
			val optError = Optional.of(e)
			if (onTerminate is DefaultTerminate) {
				onTerminate.onTerminate.accept(optError)
			} else {
				onTerminate.accept(optError)
			}
		}
	}

	val isLogging: Boolean
		/** Returns true iff the given Rx is a logging Rx. */
		get() = onTerminate === logErrors || onTerminate is DefaultTerminate

	/** An error listener which promises to pass log all errors, without requiring the user to. */
	internal open class DefaultTerminate(onTerminate: Consumer<Optional<Throwable>>) :
			Consumer<Optional<Throwable>> {
		internal val onTerminate: Consumer<Optional<Throwable>> = Objects.requireNonNull(onTerminate)

		override fun accept(t: Optional<Throwable>) {
			onTerminate.accept(t)
			if (t.isPresent) {
				logErrors.accept(t)
			}
		}
	}

	companion object {
		val logErrors: Consumer<Optional<Throwable>> = Consumer { error: Optional<Throwable> ->
			if (error.isPresent) {
				Errors.log().accept(error.get())
			}
		}
	}
}
