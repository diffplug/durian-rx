/*
 * Copyright (C) 2020-2022 DiffPlug
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
import com.diffplug.common.util.concurrent.ListenableFuture
import java.lang.Error
import java.util.*
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RxExecutor
internal constructor(private val executor: Executor, val dispatcher: CoroutineDispatcher) :
		RxSubscriber {

	interface Has : Executor {
		val rxExecutor: RxExecutor
	}

	fun executor() = executor

	override fun <T> subscribe(flow: Flow<T>, listener: RxListener<T>) {
		subscribeDisposable(flow, listener)
	}

	override fun <T> subscribe(deferred: Deferred<T>, listener: RxListener<T>) {
		subscribeDisposable(deferred, listener)
	}

	override fun <T> subscribe(future: ListenableFuture<out T>, untracedListener: RxListener<T>) {
		val listener = Rx.tracingPolicy.hook(future, untracedListener)
		future.addListener(
				{
					try {
						val value =
								try {
									future.get()
								} catch (error: Throwable) {
									listener.onFailure(error)
									return@addListener
								}
						try {
							listener.onSuccess(value)
						} catch (error: Throwable) {
							listener.onFailure(CompletionException(error))
						}
					} catch (t: Throwable) {
						failedInErrorHandler(t)
					}
				},
				executor)
	}

	override fun <T> subscribe(future: CompletionStage<out T>, untracedListener: RxListener<T>) {
		val listener = Rx.tracingPolicy.hook(future, untracedListener)
		future.whenCompleteAsync(
				{ value: T, exception: Throwable? ->
					try {
						if (exception == null) {
							try {
								listener.onSuccess(value)
							} catch (t: Throwable) {
								listener.onFailure(CompletionException(t))
							}
						} else {
							listener.onFailure(exception)
						}
					} catch (t: Throwable) {
						failedInErrorHandler(t)
					}
				},
				executor)
	}

	override fun <T : Any?> subscribeDisposable(flow: Flow<T>, untracedListener: RxListener<T>): Job {
		val listener = Rx.tracingPolicy.hook(flow, untracedListener)
		return flow
				.onEach(listener.onValue::accept)
				.onCompletion {
					if (it != null && it !is CancellationException) {
						listener.onTerminate.accept(Optional.of(it))
					} else listener.onTerminate.accept(Optional.empty())
				}
				.launchIn(CoroutineScope(dispatcher))
	}

	override fun <T : Any?> subscribeDisposable(
			deferred: Deferred<T>,
			untracedListener: RxListener<T>
	): Job {
		val listener = Rx.tracingPolicy.hook(deferred, untracedListener)
		return CoroutineScope(dispatcher).launch {
			try {
				listener.onSuccess(deferred.await())
			} catch (e: Throwable) {
				listener.onFailure(e)
			}
		}
	}

	override fun <T> subscribeDisposable(
			future: ListenableFuture<out T>,
			untracedListener: RxListener<T>
	): Job {
		val listener = Rx.tracingPolicy.hook(future, untracedListener)
		val job = Job()

		future.addListener(
				{
					try {
						if (!job.isCancelled) {
							val value =
									try {
										future.get()
									} catch (error: Throwable) {
										listener.onFailure(error)
										return@addListener
									}
							try {
								if (!job.isCancelled) {
									listener.onSuccess(value)
								}
							} catch (error: Throwable) {
								listener.onFailure(CompletionException(error))
							}
						}
					} catch (t: Throwable) {
						failedInErrorHandler(t)
					}
				},
				executor)
		return job
	}

	override fun <T> subscribeDisposable(
			future: CompletionStage<out T>,
			untracedListener: RxListener<T>
	): Job {
		val listener = Rx.tracingPolicy.hook(future, untracedListener)
		val job = Job()

		future.whenCompleteAsync(
				{ value: T, exception: Throwable? ->
					try {
						if (!job.isCancelled) {
							if (exception == null) {
								try {
									listener.onSuccess(value)
								} catch (t: Throwable) {
									listener.onFailure(CompletionException(t))
								}
							} else {
								listener.onFailure(exception)
							}
						}
					} catch (t: Throwable) {
						failedInErrorHandler(t)
					}
				},
				executor)
		return job
	}

	private fun failedInErrorHandler(t: Throwable) {
		Errors.log().accept(Error("Error handler threw error", t))
	}
}
