/*
 * Copyright (C) 2020-2021 DiffPlug
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

import com.diffplug.common.util.concurrent.ListenableFuture
import java.util.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.Supplier
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

/**
 * GuardedExecutor is an [Executor] and [RxSubscriber] which promises to cancel its subscriptions
 * and stop executing tasks once its [.getGuard] has been disposed.
 *
 * Useful for tying asynchronous tasks to gui elements.
 */
open class GuardedExecutor(val delegate: RxExecutor, val guard: Chit) : Executor, RxSubscriber {
	override fun execute(command: Runnable) {
		delegate.executor().execute(guard.guard(command))
	}

	/** Creates a runnable which runs on this Executor iff the guard widget is not disposed. */
	fun wrap(delegate: Runnable): Runnable {
		Objects.requireNonNull(delegate)
		return Runnable { execute(guard.guard(delegate)) }
	}

	private fun subscribe(subscriber: Supplier<Job>): Job {
		return if (!guard.isDisposed) {
			val job = subscriber.get()
			guard.runWhenDisposed { job.cancel() }
			job
		} else {
			SupervisorJob().apply { cancel() }
		}
	}

	override fun <T> subscribeDisposable(flow: Flow<T>, listener: RxListener<T>): Job {
		return subscribe { delegate.subscribeDisposable(flow, listener) }
	}

	override fun <T> subscribeDisposable(deferred: Deferred<T>, listener: RxListener<T>): Job {
		return subscribe { delegate.subscribeDisposable(deferred, listener) }
	}

	override fun <T> subscribeDisposable(
			future: ListenableFuture<out T>,
			listener: RxListener<T>
	): Job {
		return subscribe { delegate.subscribeDisposable(future, listener) }
	}

	override fun <T> subscribeDisposable(
			future: CompletionStage<out T>,
			listener: RxListener<T>
	): Job {
		return subscribe { delegate.subscribeDisposable(future, listener) }
	}

	override fun <T> subscribe(flow: Flow<T>, listener: RxListener<T>) {
		subscribeDisposable(flow, listener)
	}

	override fun <T> subscribe(deferred: Deferred<T>, listener: RxListener<T>) {
		subscribeDisposable(deferred, listener)
	}

	override fun <T> subscribe(future: ListenableFuture<out T>, listener: RxListener<T>) {
		subscribeDisposable(future, listener)
	}

	override fun <T> subscribe(future: CompletionStage<out T>, listener: RxListener<T>) {
		subscribeDisposable(future, listener)
	}
}
