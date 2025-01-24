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

import com.diffplug.common.rx.Rx.TrackCancelled
import com.diffplug.common.rx.Rx.onValue
import com.diffplug.common.rx.Rx.onValueOnTerminate
import com.diffplug.common.util.concurrent.ListenableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface RxSubscriber {
	fun <T> subscribe(flow: Flow<T>, listener: RxListener<T>)

	fun <T> subscribe(deferred: Deferred<T>, listener: RxListener<T>)

	fun <T> subscribe(future: ListenableFuture<out T>, listener: RxListener<T>)

	fun <T> subscribe(future: CompletionStage<out T>, listener: RxListener<T>)

	fun <T> subscribe(flow: Flow<T>, listener: Consumer<T>) {
		subscribe(flow, onValue(listener))
	}

	fun <T> subscribe(deferred: Deferred<T>, listener: Consumer<T>) {
		subscribe(deferred, onValue(listener))
	}

	fun <T> subscribe(flow: IFlowable<out T>, listener: RxListener<T>) {
		subscribe(flow.asFlow(), listener)
	}

	fun <T> subscribe(flow: IFlowable<out T>, listener: Consumer<T>) {
		subscribe(flow, onValue(listener))
	}

	fun <T> subscribe(future: ListenableFuture<out T>, listener: Consumer<T>) {
		subscribe(future, onValueOnTerminate(listener, TrackCancelled(future)))
	}

	fun <T> subscribe(future: CompletionStage<out T>, listener: Consumer<T>) {
		subscribe(future, onValueOnTerminate(listener, TrackCancelled(future.toCompletableFuture())))
	}

	fun <T> subscribeDisposable(flow: Flow<T>, listener: RxListener<T>): Job

	fun <T> subscribeDisposable(deferred: Deferred<T>, listener: RxListener<T>): Job

	fun <T> subscribeDisposable(future: ListenableFuture<out T>, listener: RxListener<T>): Job

	fun <T> subscribeDisposable(future: CompletionStage<out T>, listener: RxListener<T>): Job

	fun <T> subscribeDisposable(flow: Flow<T>, listener: Consumer<T>): Job {
		return subscribeDisposable(flow, onValue(listener))
	}

	fun <T> subscribeDisposable(deferred: Deferred<T>, listener: Consumer<T>): Job {
		return subscribeDisposable(deferred, onValue(listener))
	}

	fun <T> subscribeDisposable(flow: IFlowable<out T>, listener: RxListener<T>): Job {
		return subscribeDisposable(flow.asFlow(), listener)
	}

	fun <T> subscribeDisposable(flow: IFlowable<out T>, listener: Consumer<T>): Job {
		return subscribeDisposable(flow, onValue(listener))
	}

	fun <T> subscribeDisposable(future: ListenableFuture<out T>, listener: Consumer<T>): Job {
		return subscribeDisposable(future, onValueOnTerminate(listener, TrackCancelled(future)))
	}

	fun <T> subscribeDisposable(future: CompletionStage<out T>, listener: Consumer<T>): Job {
		return subscribeDisposable(
				future, onValueOnTerminate(listener, TrackCancelled(future.toCompletableFuture())))
	}
}
