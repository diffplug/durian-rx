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


import com.diffplug.common.util.concurrent.FutureCallback;
import com.diffplug.common.util.concurrent.Futures;
import com.diffplug.common.util.concurrent.ListenableFuture;
import com.diffplug.common.util.concurrent.MoreExecutors;
import com.diffplug.common.util.concurrent.SettableFuture;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executor;
import org.junit.Test;

/** The point of this test is to demonstrate why the Rx API should be what it is. */
@SuppressWarnings("null")
public class RxApiJustification {
	@Test(expected = NullPointerException.class)
	public void testApiAlternatives() {
		// create an Observable, Future, and DpRx
		Observable<Integer> observable = null;
		SettableFuture<String> future = null;
		RxListener<Object> listener = null;

		// observing with static methods isn't awful
		Executor executor = null;
		observable.observeOn(Schedulers.from(executor)).subscribe(listener);
		RxApiJustification.addObserver(observable, listener, executor);
		Futures.addCallback(future, listener, executor);

		// but it's even better with instance methods, except that it inverts the data flow
		// listener.subscribe(observable, executor);
		// listener.subscribe(future, executor);
		// which is why we only have these static methods
		Rx.callbackOn(executor).subscribe(observable, listener);
		Rx.callbackOn(executor).subscribe(future, listener);
	}

	/** Hypothetical API: Subscribes the observer to the observable on the executor. */
	private static <T> Disposable addObserver(Observable<T> observable, Observer<? super T> observer, Executor executor) {
		return observable.observeOn(Schedulers.from(executor)).subscribe(observer::onNext, observer::onError, observer::onComplete);
	}

	/** Explains why Futures(static).addCallback is better than future(instance).addListener */
	@Test(expected = NullPointerException.class)
	public void staticBetterThanInstanceForListenableFuture() {
		SettableFuture<String> future = SettableFuture.create();
		DpRxWithFutureRunnable<Object> listener = null;
		// static (clearly better)
		Futures.addCallback(future, listener);
		// instance (clearly worse)
		future.addListener(listener.toFutureRunnable(future), MoreExecutors.directExecutor());
	}

	/** A theoretical DpRx with support for creating Runnables to add as listeners to ListenableFutures. */
	private static class DpRxWithFutureRunnable<T> implements FutureCallback<T> {
		/**
		 * Returns a Runnable appropriate for a FutureListener callback, e.g.
		 * 
		 * future.addListener(listener.toFutureRunnable(future), executor);
		 */
		public Runnable toFutureRunnable(ListenableFuture<? extends T> future) {
			return () -> {
				try {
					T value = future.get();
					onSuccess(value);
				} catch (Throwable t) {
					onFailure(t);
				}
			};
		}

		@Override
		public void onSuccess(T result) {}

		@Override
		public void onFailure(Throwable t) {}
	}
}
