/*
 * Copyright 2015 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.rx;

import java.util.function.Consumer;

import rx.Observable;
import rx.Subscription;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * An object which can subscribe observables to {@link Rx} listeners.
 */
public interface RxSubscriber {
	/** Subscribes the given listener to the given observable. */
	<T> Subscription subscribe(Observable<? extends T> observable, Rx<T> listener);

	/** Subscribes the given listener to the given future. */
	<T> Subscription subscribe(ListenableFuture<? extends T> future, Rx<T> listener);

	default <T> Subscription subscribe(Observable<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable, Rx.onValue(listener));
	}

	default <T> Subscription subscribe(IObservable<? extends T> observable, Rx<T> listener) {
		return subscribe(observable.asObservable(), listener);
	}

	default <T> Subscription subscribe(IObservable<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable, Rx.onValue(listener));
	}

	default <T> Subscription subscribe(ListenableFuture<? extends T> future, Consumer<T> listener) {
		return subscribe(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future)));
	}
}
