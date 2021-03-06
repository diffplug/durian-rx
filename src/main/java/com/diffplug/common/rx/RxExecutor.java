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

import static java.util.Objects.requireNonNull;

import com.diffplug.common.base.Errors;
import com.diffplug.common.util.concurrent.ListenableFuture;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * This class holds an instance of Executor (for ListenableFuture) and
 * Scheduler (for Observable).  It has methods which match the signatures of Rx's
 * static methods, which allows users to   
 */
public final class RxExecutor implements RxSubscriber {
	/*** Marker interface which allows an Executor to specify its own Scheduler. */
	public interface Has extends Executor {
		RxExecutor getRxExecutor();
	}

	private final Executor executor;
	private final Scheduler scheduler;

	RxExecutor(Executor executor, Scheduler scheduler) {
		this.executor = requireNonNull(executor);
		this.scheduler = requireNonNull(scheduler);
	}

	public Executor executor() {
		return executor;
	}

	public Scheduler scheduler() {
		return scheduler;
	}

	@Override
	public <T> void subscribe(Observable<? extends T> observable, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = Rx.getTracingPolicy().hook(observable, untracedListener);
		observable.observeOn(scheduler).subscribe(listener);
	}

	@Override
	public <T> void subscribe(ListenableFuture<? extends T> future, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = Rx.getTracingPolicy().hook(future, untracedListener);
		// add a callback that guards on whether it is still subscribed
		future.addListener(() -> {
			try {
				T value;
				try {
					value = future.get();
				} catch (Throwable error) {
					listener.onFailure(error);
					return;
				}
				try {
					listener.onSuccess(value);
				} catch (Throwable error) {
					listener.onFailure(new CompletionException(error));
				}
			} catch (Throwable t) {
				failedInErrorHandler(t);
			}
		}, executor);
	}

	@Override
	public <T> void subscribe(CompletionStage<? extends T> future, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = Rx.getTracingPolicy().hook(future, untracedListener);
		future.whenCompleteAsync((value, exception) -> {
			try {
				if (exception == null) {
					try {
						listener.onSuccess(value);
					} catch (Throwable t) {
						listener.onFailure(new CompletionException(t));
					}
				} else {
					listener.onFailure(exception);
				}
			} catch (Throwable t) {
				failedInErrorHandler(t);
			}
		}, executor);
	}

	@Override
	public <T> Disposable subscribeDisposable(Observable<? extends T> observable, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = Rx.getTracingPolicy().hook(observable, untracedListener);
		return observable.observeOn(scheduler).subscribe(listener::onNext, listener::onError, listener::onComplete);
	}

	@Override
	public <T> Disposable subscribeDisposable(ListenableFuture<? extends T> future, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = Rx.getTracingPolicy().hook(future, untracedListener);
		// when we're unsubscribed, set the flag to false
		Disposable sub = Disposables.empty();
		// add a callback that guards on whether it is still subscribed
		future.addListener(() -> {
			try {
				T value;
				try {
					value = future.get();
				} catch (Throwable error) {
					if (!sub.isDisposed()) {
						listener.onFailure(error);
					}
					return;
				}
				try {
					if (!sub.isDisposed()) {
						listener.onSuccess(value);
					}
				} catch (Throwable error) {
					listener.onFailure(new CompletionException(error));
				}
			} catch (Throwable t) {
				failedInErrorHandler(t);
			}
		}, executor);
		// return the subscription
		return sub;
	}

	@Override
	public <T> Disposable subscribeDisposable(CompletionStage<? extends T> future, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = Rx.getTracingPolicy().hook(future, untracedListener);

		// when we're unsubscribed, set the flag to false
		Disposable sub = Disposables.empty();
		future.whenCompleteAsync((value, exception) -> {
			try {
				if (!sub.isDisposed()) {
					if (exception == null) {
						try {
							listener.onSuccess(value);
						} catch (Throwable t) {
							listener.onFailure(new CompletionException(t));
						}
					} else {
						listener.onFailure(exception);
					}
				}
			} catch (Throwable t) {
				failedInErrorHandler(t);
			}
		}, executor);
		// return the subscription
		return sub;
	}

	private void failedInErrorHandler(Throwable t) {
		Errors.log().accept(new Error("Error handler threw error", t));
	}
}
