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

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.diffplug.common.util.concurrent.ListenableFuture;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

/**
 * GuardedExecutor is an {@link Executor} and {@link RxSubscriber}
 * which promises to cancel its subscriptions and stop executing tasks
 * once its {@link #guard()} has been disposed.
 * 
 * Useful for tying asynchronous tasks to gui elements.
 */
public interface GuardedExecutor extends Executor, RxSubscriber {
	/** The element on which all executions and subscriptions are guarded. */
	DisposableEar guard();

	/** Standard implementation of GuardedExecutor. */
	public abstract class AbstractGuardedExecutor implements GuardedExecutor {
		private final DisposableEar guard;

		public AbstractGuardedExecutor(DisposableEar guard) {
			this.guard = Objects.requireNonNull(guard);
		}

		@Override
		public DisposableEar guard() {
			return guard;
		}

		protected abstract Executor delegateExecutor();

		protected abstract RxSubscriber delegateSubscriber();

		@Override
		public void execute(Runnable command) {
			delegateExecutor().execute(guard().guard(command));
		}

		/** Creates a runnable which runs on this Executor iff the guard widget is not disposed. */
		public Runnable wrap(Runnable delegate) {
			return () -> execute(guard().guard(delegate));
		}

		private Disposable subscribe(Supplier<Disposable> subscriber) {
			if (!guard().isDisposed()) {
				Disposable subscription = subscriber.get();
				guard().runWhenDisposed(() -> subscription.dispose());
				return subscription;
			} else {
				return Disposables.disposed();
			}
		}

		@Override
		public <T> Disposable subscribeDisposable(Observable<? extends T> observable, RxListener<T> listener) {
			return subscribe(() -> delegateSubscriber().subscribeDisposable(observable, listener));
		}

		@Override
		public <T> Disposable subscribeDisposable(ListenableFuture<? extends T> future, RxListener<T> listener) {
			return subscribe(() -> delegateSubscriber().subscribeDisposable(future, listener));
		}

		@Override
		public <T> Disposable subscribeDisposable(CompletionStage<? extends T> future, RxListener<T> listener) {
			return subscribe(() -> delegateSubscriber().subscribeDisposable(future, listener));
		}

		@Override
		public <T> void subscribe(Observable<? extends T> observable, RxListener<T> listener) {
			subscribeDisposable(observable, listener);
		}

		@Override
		public <T> void subscribe(ListenableFuture<? extends T> future, RxListener<T> listener) {
			subscribeDisposable(future, listener);
		}

		@Override
		public <T> void subscribe(CompletionStage<? extends T> future, RxListener<T> listener) {
			subscribeDisposable(future, listener);
		}
	}
}
