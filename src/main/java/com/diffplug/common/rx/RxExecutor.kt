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

import com.diffplug.common.base.Errors
import com.diffplug.common.util.concurrent.ListenableFuture
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import java.lang.Error
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
* This class holds an instance of Executor (for ListenableFuture) and Scheduler (for Observable).
* It has methods which match the signatures of Rx's static methods, which allows users to
*/
class RxExecutor
internal constructor(private val executor: Executor, private val scheduler: Scheduler) :
		RxSubscriber {
	/** * Marker interface which allows an Executor to specify its own Scheduler. */
	interface Has : Executor {
		val rxExecutor: RxExecutor
	}

	fun executor() = executor
	fun scheduler() = scheduler

	override fun <T> subscribe(observable: Observable<out T>, untracedListener: RxListener<T>) {
		val listener = Rx.getTracingPolicy().hook(observable, untracedListener)
		observable.observeOn(scheduler).subscribe(listener)
	}

	override fun <T> subscribe(future: ListenableFuture<out T>, untracedListener: RxListener<T>) {
		val listener = Rx.getTracingPolicy().hook(future, untracedListener)
		// add a callback that guards on whether it is still subscribed
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
		val listener = Rx.getTracingPolicy().hook(future, untracedListener)
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

	override fun <T> subscribeDisposable(
			observable: Observable<out T>,
			untracedListener: RxListener<T>
	): Disposable {
		val listener = Rx.getTracingPolicy().hook(observable, untracedListener)
		return observable.observeOn(scheduler).subscribe(
						{ t: T -> listener.onNext(t) }, { e: Throwable -> listener.onError(e) }) {
			listener.onComplete()
		}
	}

	override fun <T> subscribeDisposable(
			future: ListenableFuture<out T>,
			untracedListener: RxListener<T>
	): Disposable {
		val listener = Rx.getTracingPolicy().hook(future, untracedListener)
		// when we're unsubscribed, set the flag to false
		val sub = Disposables.empty()
		// add a callback that guards on whether it is still subscribed
		future.addListener(
				{
					try {
						val value =
								try {
									future.get()
								} catch (error: Throwable) {
									if (!sub.isDisposed) {
										listener.onFailure(error)
									}
									return@addListener
								}
						try {
							if (!sub.isDisposed) {
								listener.onSuccess(value)
							}
						} catch (error: Throwable) {
							listener.onFailure(CompletionException(error))
						}
					} catch (t: Throwable) {
						failedInErrorHandler(t)
					}
				},
				executor)
		// return the subscription
		return sub
	}

	override fun <T> subscribeDisposable(
			future: CompletionStage<out T>,
			untracedListener: RxListener<T>
	): Disposable {
		val listener = Rx.getTracingPolicy().hook(future, untracedListener)

		// when we're unsubscribed, set the flag to false
		val sub = Disposables.empty()
		future.whenCompleteAsync(
				{ value: T, exception: Throwable? ->
					try {
						if (!sub.isDisposed) {
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
		// return the subscription
		return sub
	}

	private fun failedInErrorHandler(t: Throwable) {
		Errors.log().accept(Error("Error handler threw error", t))
	}
}