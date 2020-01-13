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
package com.diffplug.common.rx;


import com.diffplug.common.util.concurrent.ListenableFuture;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * An object which can subscribe observables to {@link RxListener} listeners.
 */
public interface RxSubscriber {
	/** Subscribes the given listener to the given observable. */
	<T> void subscribe(Observable<? extends T> observable, RxListener<T> listener);

	/** Subscribes the given listener to the given Guava ListenableFuture. */
	<T> void subscribe(ListenableFuture<? extends T> future, RxListener<T> listener);

	/** Subscribes the given listener to the given Java 8 CompletableFuture. */
	<T> void subscribe(CompletionStage<? extends T> future, RxListener<T> listener);

	default <T> void subscribe(Observable<? extends T> observable, Consumer<T> listener) {
		subscribe(observable, Rx.onValue(listener));
	}

	default <T> void subscribe(IObservable<? extends T> observable, RxListener<T> listener) {
		subscribe(observable.asObservable(), listener);
	}

	default <T> void subscribe(IObservable<? extends T> observable, Consumer<T> listener) {
		subscribe(observable, Rx.onValue(listener));
	}

	default <T> void subscribe(ListenableFuture<? extends T> future, Consumer<T> listener) {
		subscribe(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future)));
	}

	default <T> void subscribe(CompletionStage<? extends T> future, Consumer<T> listener) {
		subscribe(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future.toCompletableFuture())));
	}

	/** Subscribes the given listener to the given observable. */
	<T> Disposable subscribeDisposable(Observable<? extends T> observable, RxListener<T> listener);

	/** Subscribes the given listener to the given Guava ListenableFuture. */
	<T> Disposable subscribeDisposable(ListenableFuture<? extends T> future, RxListener<T> listener);

	/** Subscribes the given listener to the given Java 8 CompletableFuture. */
	<T> Disposable subscribeDisposable(CompletionStage<? extends T> future, RxListener<T> listener);

	default <T> Disposable subscribeDisposable(Observable<? extends T> observable, Consumer<T> listener) {
		return subscribeDisposable(observable, Rx.onValue(listener));
	}

	default <T> Disposable subscribeDisposable(IObservable<? extends T> observable, RxListener<T> listener) {
		return subscribeDisposable(observable.asObservable(), listener);
	}

	default <T> Disposable subscribeDisposable(IObservable<? extends T> observable, Consumer<T> listener) {
		return subscribeDisposable(observable, Rx.onValue(listener));
	}

	default <T> Disposable subscribeDisposable(ListenableFuture<? extends T> future, Consumer<T> listener) {
		return subscribeDisposable(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future)));
	}

	default <T> Disposable subscribeDisposable(CompletionStage<? extends T> future, Consumer<T> listener) {
		return subscribeDisposable(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future.toCompletableFuture())));
	}
}
