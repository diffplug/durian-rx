/*
 * Copyright 2016 DiffPlug
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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;

import com.diffplug.common.util.concurrent.ListenableFuture;

/**
 * This class holds an instance of Executor (for ListenableFuture) and
 * Scheduler (for Observable).  It has methods which match the signatures of Rx's
 * static methods, which allows users to   
 */
public class RxExecutor implements RxSubscriber {
	private final Executor executor;
	private final Scheduler scheduler;
	private final RxTracingPolicy tracingPolicy;

	RxExecutor(Executor executor, Scheduler scheduler) {
		this.executor = requireNonNull(executor);
		this.scheduler = requireNonNull(scheduler);
		this.tracingPolicy = Rx.getTracingPolicy();
	}

	@Override
	public <T> Subscription subscribe(Observable<? extends T> observable, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = tracingPolicy.hook(observable, untracedListener);
		return observable.observeOn(scheduler).subscribe(listener);
	}

	@Override
	public <T> Subscription subscribe(CompletionStage<? extends T> future, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = tracingPolicy.hook(future, untracedListener);

		// when we're unsubscribed, set the flag to false
		BooleanSubscription sub = BooleanSubscription.create();
		future.whenCompleteAsync((value, exception) -> {
			if (!sub.isUnsubscribed()) {
				if (exception == null) {
					listener.onSuccess(value);
				} else {
					listener.onFailure(exception);
				}
			}
		}, executor);
		// return the subscription
		return sub;
	}

	@Override
	public <T> Subscription subscribe(ListenableFuture<? extends T> future, RxListener<T> untracedListener) {
		requireNonNull(untracedListener);
		RxListener<T> listener = tracingPolicy.hook(future, untracedListener);
		// when we're unsubscribed, set the flag to false
		BooleanSubscription sub = BooleanSubscription.create();
		// add a callback that guards on whether it is still subscribed
		future.addListener(() -> {
			try {
				T value = future.get();
				if (!sub.isUnsubscribed()) {
					listener.onSuccess(value);
				}
			} catch (Throwable error) {
				if (!sub.isUnsubscribed()) {
					listener.onFailure(error);
				}
			}
		}, executor);
		// return the subscription
		return sub;
	}

	/*** Marker interface which allows an Executor to specify its own Scheduler. */
	public interface Has extends Executor {
		RxExecutor getRxExecutor();
	}
}
