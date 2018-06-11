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
 * once its {@link #getGuard()} has been disposed.
 * 
 * Useful for tying asynchronous tasks to gui elements.
 */
public class GuardedExecutor implements Executor, RxSubscriber {
	private final RxExecutor delegate;
	private final DisposableEar guard;

	public GuardedExecutor(RxExecutor delegate, DisposableEar guard) {
		this.delegate = Objects.requireNonNull(delegate);
		this.guard = Objects.requireNonNull(guard);
	}

	/** The underlying executor which is being delegated to. */
	public final RxExecutor getDelegateRxExecutor() {
		return delegate;
	}

	/** The element on which all executions and subscriptions are guarded. */
	public final DisposableEar getGuard() {
		return guard;
	}

	@Override
	public final void execute(Runnable command) {
		delegate.executor().execute(getGuard().guard(command));
	}

	/** Creates a runnable which runs on this Executor iff the guard widget is not disposed. */
	public final Runnable wrap(Runnable delegate) {
		Objects.requireNonNull(delegate);
		return () -> execute(getGuard().guard(delegate));
	}

	private Disposable subscribe(Supplier<Disposable> subscriber) {
		if (!getGuard().isDisposed()) {
			Disposable subscription = subscriber.get();
			getGuard().runWhenDisposed(() -> subscription.dispose());
			return subscription;
		} else {
			return Disposables.disposed();
		}
	}

	@Override
	public final <T> Disposable subscribeDisposable(Observable<? extends T> observable, RxListener<T> listener) {
		return subscribe(() -> delegate.subscribeDisposable(observable, listener));
	}

	@Override
	public final <T> Disposable subscribeDisposable(ListenableFuture<? extends T> future, RxListener<T> listener) {
		return subscribe(() -> delegate.subscribeDisposable(future, listener));
	}

	@Override
	public final <T> Disposable subscribeDisposable(CompletionStage<? extends T> future, RxListener<T> listener) {
		return subscribe(() -> delegate.subscribeDisposable(future, listener));
	}

	@Override
	public final <T> void subscribe(Observable<? extends T> observable, RxListener<T> listener) {
		subscribeDisposable(observable, listener);
	}

	@Override
	public final <T> void subscribe(ListenableFuture<? extends T> future, RxListener<T> listener) {
		subscribeDisposable(future, listener);
	}

	@Override
	public final <T> void subscribe(CompletionStage<? extends T> future, RxListener<T> listener) {
		subscribeDisposable(future, listener);
	}
}
