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
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.CompletionStage
import java.util.function.Consumer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

/** An object which can subscribe observables to [RxListener] listeners. */
interface RxSubscriber {
	fun <T> subscribe(flow: Flow<T>, listener: RxListener<T>)
	fun <T> subscribe(deferred: Deferred<T>, listener: RxListener<T>)
	fun <T> subscribe(observable: Observable<out T>, listener: RxListener<T>)
	fun <T> subscribe(future: ListenableFuture<out T>, listener: RxListener<T>)
	fun <T> subscribe(future: CompletionStage<out T>, listener: RxListener<T>)
	fun <T> subscribe(observable: Flow<T>, listener: Consumer<T>) {
		subscribe(observable, onValue(listener))
	}

	fun <T> subscribe(deferred: Deferred<T>, listener: Consumer<T>) {
		subscribe(deferred, onValue(listener))
	}

	fun <T> subscribe(observable: Observable<out T>, listener: Consumer<T>) {
		subscribe(observable, onValue(listener))
	}

	fun <T> subscribe(observable: IObservable<out T>, listener: RxListener<T>) {
		subscribe(observable.asObservable(), listener)
	}

	fun <T> subscribe(observable: IObservable<out T>, listener: Consumer<T>) {
		subscribe(observable, onValue(listener))
	}

	fun <T> subscribe(future: ListenableFuture<out T>, listener: Consumer<T>) {
		subscribe(future, onValueOnTerminate(listener, TrackCancelled(future)))
	}

	fun <T> subscribe(future: CompletionStage<out T>, listener: Consumer<T>) {
		subscribe(future, onValueOnTerminate(listener, TrackCancelled(future.toCompletableFuture())))
	}

	fun <T> subscribeDisposable(flow: Flow<T>, listener: RxListener<T>): Disposable
	fun <T> subscribeDisposable(deferred: Deferred<T>, listener: RxListener<T>): Disposable
	fun <T> subscribeDisposable(observable: Observable<out T>, listener: RxListener<T>): Disposable
	fun <T> subscribeDisposable(future: ListenableFuture<out T>, listener: RxListener<T>): Disposable
	fun <T> subscribeDisposable(future: CompletionStage<out T>, listener: RxListener<T>): Disposable
	fun <T> subscribeDisposable(flow: Flow<T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(flow, onValue(listener))
	}

	fun <T> subscribeDisposable(deferred: Deferred<T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(deferred, onValue(listener))
	}

	fun <T> subscribeDisposable(observable: Observable<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(observable, onValue(listener))
	}

	fun <T> subscribeDisposable(observable: IObservable<out T>, listener: RxListener<T>): Disposable {
		return subscribeDisposable(observable.asObservable(), listener)
	}

	fun <T> subscribeDisposable(observable: IObservable<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(observable, onValue(listener))
	}

	fun <T> subscribeDisposable(future: ListenableFuture<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(future, onValueOnTerminate(listener, TrackCancelled(future)))
	}

	fun <T> subscribeDisposable(future: CompletionStage<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(
				future, onValueOnTerminate(listener, TrackCancelled(future.toCompletableFuture())))
	}
}
