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

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;

import com.diffplug.common.base.Consumers;
import com.diffplug.common.base.DurianPlugins;
import com.diffplug.common.base.Errors;
import com.diffplug.common.util.concurrent.ListenableFuture;
import com.diffplug.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unifies the listener models of {@link rx.Observable RxJava's Observable} 
 * with <code><a href="https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained">Guava's ListenableFuture</a></code>
 * , and also adds tracing capabilities.
 * <p>
 * TL;DR
 * <pre>
 * // subscribe to values, termination, or both
 * Rx.subscribe(listenableOrObservable, val -> doSomething(val));                             // errors are passed to Errors.log()
 * Rx.subscribe(listenableOrObservable, Rx.onTerminate(optionalError -> maybeHandleError());  // values are ignored
 * Rx.subscribe(listenableOrObservable, Rx.onValueOrTerminate(val -> doSomething(val), optionalError -> maybeHandleError()));
 * // receive callbacks on a specific executor
 * Rx.on(someExecutor).subscribe(listenableOrObservable, val -> doSomething(val));
 * // call unsubscribe() on the subscription to cancel it 
 * rx.Subscription subscription = Rx.subscribe(listenableOrObservable, val -> doSomething);
 * </pre>
 * Long version: {@code Rx} implements both the {@link rx.Observer} and {@link com.diffplug.common.util.concurrent.FutureCallback}
 * interfaces by mapping them to two {@code Consumer}s:
 * <ul>
 * <li>{@code Consumer<T> onValue}</li>
 * <li>{@code Consumer<Optional<Throwable>> onTerminate}</li>
 * </ul>
 * Which are mapped as follows:
 * <ul>
 * <li>{@code Observable.onNext(T value)          -> onValue.accept(value)}</li>
 * <li>{@code Observable.onCompleted()            -> onTerminate.accept(Optional.empty())}</li>
 * <li>{@code Observable.onError(Throwable error) -> onTerminate.accept(Optional.of(error))}</li>
 * <li>{@code FutureCallback.onSuccess(T value)       -> onValue.accept(value); onTerminate.accept(Optional.empty());}</li>
 * <li>{@code FutureCallback.onError(Throwable error) -> onTerminate.accept(Optional.of(error))}</li>
 * </ul>
 * An instance of Rx is created by calling one of Rx's static creator methods:
 * <ul>
 * <li>{@link #onValue(Consumer)     onValue(Consumer&lt;T&gt;)}</li>
 * <li>{@link #onTerminate(Consumer) onTerminate(Consumer&lt;Optional&lt;Throwable&gt;&gt;)}</li>
 * <li>{@link #onFailure(Consumer)   onFailure(Consumer&lt;Throwable&gt;)}</li>
 * <li>{@link #onValueOnTerminate    onValueOrTerminate(Consumer&lt;T&gt;, Consumer&lt;Optional&lt;Throwable&gt;&gt;)}</li>
 * <li>{@link #onValueOnFailure      onValueOrFailure(Consumer&lt;T&gt;, Consumer&lt;Throwable&gt;)}</li>
 * </ul>
 * Once you have an instance of Rx, you can subscribe it using the normal RxJava or Guava calls:
 * <ul>
 * <li>{@code rxObservable.subscribe(Rx.onValue(val -> doSomething(val));}</li>
 * <li>{@code Futures.addCallback(listenableFuture, Rx.onValue(val -> doSomething(val));}</li>
 * </ul>
 * But the recommended way to subscribe is to use:
 * <ul>
 * <li>{@code Rx.subscribe(listenableOrObservable, Rx.onValue(val -> doSomething(val)));}</li>
 * <li>{@code Rx.subscribe(listenableOrObservable, val -> doSomething(val)); // automatically uses Rx.onValue()}</li>
 * </ul>
 * The advantage of this latter method is that it returns {@link rx.Subscription} instances
 * which allow you to unsubscribe from futures in the same manner as for observables.
 * <ul>
 * <li>{@code subscription = Rx.subscribe( ... )}</li>
 * </ul>
 * If you wish to receive callbacks on a specific thread, you can use:
 * <ul>
 * <li>{@code Rx.on(someExecutor).subscribe( ... )}</li>
 * </ul>
 * Because RxJava's Observables use {@link rx.Scheduler}s rather than {@link java.util.concurrent.Executor}s,
 * a Scheduler is automatically created using {@link rx.Schedulers#from}. If you'd like to specify the Scheduler manually, you can use {@link Rx#on(Executor, Scheduler)}
 * or you can create an executor which implements {@link Rx.HasRxExecutor}.
 * 
 * @see <a href="https://diffplug.github.io/durian-swt/javadoc/snapshot/com/diffplug/common/swt/SwtExec.html">SwtExec</a>
 */
public class Rx {
	private Rx() {}

	/**
	 * Creates an Rx instance which will call the given consumer whenever a value is received.
	 * Any errors are sent to ErrorHandler.log().
	 */
	public static <T> RxListener<T> onValue(Consumer<T> onValue) {
		return new RxListener<T>(onValue, RxListener.logErrors);
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream
	 * or future completes, whether with an error or not.
	 */
	public static <T> RxListener<T> onTerminate(Consumer<Optional<Throwable>> onTerminate) {
		return new RxListener<T>(Consumers.doNothing(), onTerminate);
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream
	 * or future completes, whether with an error or not, and the error (if present) will be logged.
	 */
	public static <T> RxListener<T> onTerminateLogError(Consumer<Optional<Throwable>> onTerminate) {
		return new RxListener<T>(Consumers.doNothing(), new RxListener.DefaultTerminate(onTerminate));
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream
	 * or future completes with an error.
	 */
	public static <T> RxListener<T> onFailure(Consumer<Throwable> onFailure) {
		requireNonNull(onFailure);
		return new RxListener<T>(Consumers.doNothing(), error -> {
			if (error.isPresent()) {
				onFailure.accept(error.get());
			}
		});
	}

	/**
	 * Creates an Rx instance which will call onValue whenever a value is received, 
	 * is received, and onTerminate when the future or observable completes, whether with an error or not.
	 */
	public static <T> RxListener<T> onValueOnTerminate(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminate) {
		return new RxListener<T>(onValue, onTerminate);
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream
	 * or future completes, whether with an error or not, and the error (if present) will automatically be logged.
	 */
	public static <T> RxListener<T> onValueOnTerminateLogError(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminate) {
		return new RxListener<T>(onValue, new RxListener.DefaultTerminate(onTerminate));
	}

	/**
	 * Creates an Rx instance which will call onValue whenever a value is received,
	 * and onFailure if the stream or future completes with an error.
	 */
	public static <T> RxListener<T> onValueOnFailure(Consumer<T> onValue, Consumer<Throwable> onFailure) {
		requireNonNull(onFailure);
		return new RxListener<T>(onValue, error -> {
			if (error.isPresent()) {
				onFailure.accept(error.get());
			}
		});
	}

	// Static versions
	public static <T> Subscription subscribe(Observable<? extends T> observable, RxListener<T> listener) {
		return sameThreadExecutor().subscribe(observable, listener);
	}

	public static <T> Subscription subscribe(Observable<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable, Rx.onValue(listener));
	}

	public static <T> Subscription subscribe(IObservable<? extends T> observable, RxListener<T> listener) {
		return subscribe(observable.asObservable(), listener);
	}

	public static <T> Subscription subscribe(IObservable<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable.asObservable(), listener);
	}

	// Static versions
	public static <T> Subscription subscribe(ListenableFuture<? extends T> future, RxListener<T> listener) {
		return sameThreadExecutor().subscribe(future, listener);
	}

	public static <T> Subscription subscribe(ListenableFuture<? extends T> future, Consumer<T> listener) {
		return subscribe(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future)));
	}

	public static <T> Subscription subscribe(CompletionStage<? extends T> future, RxListener<T> listener) {
		return sameThreadExecutor().subscribe(future, listener);
	}

	public static <T> Subscription subscribe(CompletionStage<? extends T> future, Consumer<T> listener) {
		return subscribe(future, Rx.onValueOnTerminate(listener, new Rx.TrackCancelled(future.toCompletableFuture())));
	}

	/** An error listener which tracks whether a future has been cancelled, so that it doesn't log the errors of cancelled futures. */
	static class TrackCancelled implements Consumer<Optional<Throwable>> {
		private final Future<?> future;

		public TrackCancelled(Future<?> future) {
			this.future = future;
		}

		@Override
		public void accept(Optional<Throwable> errorOpt) {
			if (errorOpt.isPresent() && !future.isCancelled()) {
				Errors.log().accept(errorOpt.get());
			}
		}
	}

	/**
	 * Mechanism for specifying a specific Executor.  A corresponding Scheduler will
	 * be created using Schedulers.from(executor).
	 */
	public static RxExecutor on(Executor executor) {
		if (executor == MoreExecutors.directExecutor()) {
			return sameThreadExecutor();
		} else if (executor instanceof RxExecutor.Has) {
			return ((RxExecutor.Has) executor).getRxExecutor();
		} else {
			return new RxExecutor(executor, Schedulers.from(executor));
		}
	}

	/** Mechanism for specifying a specific Executor (for ListenableFuture) and Scheduler (for Observable). */
	public static RxExecutor on(Executor executor, Scheduler scheduler) {
		return new RxExecutor(executor, scheduler);
	}

	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
	public static RxExecutor sameThreadExecutor() {
		// There is an acceptable race condition here - _sameThread might get set multiple times.
		// This would happen if multiple threads called blocking() at the same time
		// during initialization, and this is likely to actually happen in practice.
		//
		// It is important for this method to be fast, so it's better to accept
		// that getSameThreadExecutor() might return different instances (which each have the
		// same behavior), rather than to incur the cost of some type of synchronization.
		if (_sameThread == null) {
			_sameThread = new RxExecutor(MoreExecutors.directExecutor(), Schedulers.immediate());
		}
		return _sameThread;
	}

	private static RxExecutor _sameThread;

	/** Returns the global tracing policy. */
	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
	static RxTracingPolicy getTracingPolicy() {
		// There is an acceptable race condition here - see getSameThreadExecutor()
		if (_tracingPolicy == null) {
			_tracingPolicy = DurianPlugins.get(RxTracingPolicy.class, RxTracingPolicy.NONE);
		}
		return _tracingPolicy;
	}

	private static RxTracingPolicy _tracingPolicy;

	/** Package-private for testing - resets all of the static member variables. */
	static void resetForTesting() {
		_sameThread = null;
		_tracingPolicy = null;
	}
}
