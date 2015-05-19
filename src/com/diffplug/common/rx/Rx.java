/**
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

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.BooleanSubscription;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import com.diffplug.common.base.Consumers;
import com.diffplug.common.base.DurianPlugins;
import com.diffplug.common.base.Errors;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Rx is a class which unifies the listener models of rx.Observable
 * with com.google.common.util.concurrent.ListenableFuture.
 * 
 * TL;DR:
 * 
 * Rx.subscribe(listenableOrObservable, val -> doSomething(val)); // errors are passed to ErrorHandler.log()
 * Rx.subscribe(listenableOrObservable, Rx.onTerminate(optionalError -> maybeHandleError()); // values are ignored
 * Rx.subscribe(listenableOrObservable, Rx.onValueOrTerminate(val -> doSomething(val), optionalError -> maybeHandleError()));
 * 
 * Rx.on(someExecutor).subscribe(listenableOrObservable, val -> doSomething(val));
 * 
 * rx.Subscription subscription = Rx.subscribe(listenableOrObservable, val -> doSomething);
 * 
 * Long version:
 * 
 * Rx implements both the Observer and FutureCallback interfaces by mapping them to two functions,
 * `onValue(T value)` and `onTerminate(Optional<Throwable> error)` as follows:
 * 
 * - Observable.onNext(T value) -> Rx.onValue(T value)
 * - Observable.onCompleted() -> Rx.onTerminate(Optional.empty())
 * - Observable.onError(Throwable error) -> Rx.onTerminate(Optional.of(error))
 * - ListenableFuture.onSuccess(T result) -> Rx.onValue(T value), Rx.onTerminate(Optional.empty())
 * - ListenableFuture.onError(Throwable error -> Rx.onTerminate(Optional.of(error))
 * 
 * An instance of Rx is created by calling one of Rx's 3 static creator methods:
 * - Rx.onValueOrTerminate(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminate))
 * - Rx.onTerminate(Consumer<Optional<Throwable>> onTerminate) // This Rx implementation ignores all values
 * - Rx.onValue(Consumer<T> onValue) // This Rx implementation passes all errors to ErrorHandler.log()
 * 
 * Once you have an instance of Rx, you can subscribe it using the normal RxJava or Guava calls:
 *   Futures.addCallback(listenableFuture, Rx.onValue(val -> doSomething(val));
 *   rxObservable.subscribe(Rx.onValue(val -> doSomething(val));
 * 
 * But the recommended way to subscribe is to use:
 *   Rx.subscribe(listenableOrObservable, Rx.onValue(val -> doSomething(val)));
 *   Rx.subscribe(listenableOrObservable, val -> doSomething(val)); // automatically uses Rx.onValue()
 * 
 * The advantage of this latter method is that it returns rx.Subscription instances
 * which allow you to unsubscribe from futures in the same manner as for observables.
 * 
 *   subscription = Rx.subscribe( ... )
 * 
 * If you wish to receive callbacks on a specific thread, you can use:
 * 
 * Rx.on(someExecutor).subscribe( ... ).
 * 
 * Because RxJava's Observables use Schedulers rather than Executors, a Scheduler is
 * automatically created using Schedulers.from(executor).  If you'd like to specify
 * the Scheduler manually, you can use Rx.on(someExecutor, someScheduler), or you can
 * create an executor which implements Rx.HasRxExecutor.
 */
public class Rx<T> implements Observer<T>, FutureCallback<T> {
	private final Consumer<T> onValue;
	private final Consumer<Optional<Throwable>> onTerminate;

	protected Rx(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminate) {
		this.onValue = onValue;
		this.onTerminate = onTerminate;
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever a value is received.
	 * Any errors are sent to ErrorHandler.log().
	 */
	public static <T> Rx<T> onValue(Consumer<T> onValue) {
		return new Rx<T>(onValue, logErrors);
	}

	private static final Consumer<Optional<Throwable>> logErrors = error -> {
		if (error.isPresent()) {
			Errors.log().handle(error.get());
		}
	};

	/** Returns true iff the given Rx is a logging Rx. */
	boolean isLogging() {
		return onTerminate == logErrors;
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream
	 * or future completes, whether with an error or not.
	 */
	public static <T> Rx<T> onTerminate(Consumer<Optional<Throwable>> onTerminal) {
		return new Rx<T>(Consumers.doNothing(), onTerminal);
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream
	 * or future completes with an error.
	 */
	public static <T> Rx<T> onFailure(Consumer<Throwable> onFailure) {
		return new Rx<T>(Consumers.doNothing(), error -> {
			if (error.isPresent()) {
				onFailure.accept(error.get());
			}
		});
	}

	/**
	 * Creates an Rx instance which will call onValue whenever a value is received, 
	 * is received, or onTerminal when the future or observable completes.
	 */
	public static <T> Rx<T> onValueOrTerminate(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminal) {
		return new Rx<T>(onValue, onTerminal);
	}

	/**
	 * Creates an Rx instance which will call onValue whenever a value is received,
	 * and onFailure whenever an exception is thrown. 
	 */
	public static <T> Rx<T> onValueOrFailure(Consumer<T> onValue, Consumer<Throwable> onFailure) {
		return new Rx<T>(onValue, error -> {
			if (error.isPresent()) {
				onFailure.accept(error.get());
			}
		});
	}

	// ///////////
	// Observer //
	// ///////////
	@Override
	public final void onNext(T t) {
		onValue.accept(t);
	}

	@Override
	public final void onCompleted() {
		onTerminate.accept(Optional.empty());
	}

	@Override
	public final void onError(Throwable e) {
		onTerminate.accept(Optional.of(e));
	}

	// Static versions
	public static <T> Subscription subscribe(Observable<? extends T> observable, Rx<T> listener) {
		return getSameThreadExecutor().subscribe(observable, listener);
	}

	public static <T> Subscription subscribe(Observable<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable, Rx.onValue(listener));
	}

	public static <T> Subscription subscribe(IObservable<? extends T> observable, Rx<T> listener) {
		return subscribe(observable.asObservable(), listener);
	}

	public static <T> Subscription subscribe(IObservable<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable.asObservable(), listener);
	}

	// /////////////////
	// FutureCallback //
	// /////////////////
	@Override
	public final void onSuccess(T result) {
		onValue.accept(result);
		onTerminate.accept(Optional.empty());
	}

	@Override
	public final void onFailure(Throwable e) {
		onTerminate.accept(Optional.of(e));
	}

	// Static versions
	public static <T> Subscription subscribe(ListenableFuture<? extends T> observable, Rx<T> listener) {
		return getSameThreadExecutor().subscribe(observable, listener);
	}

	public static <T> Subscription subscribe(ListenableFuture<? extends T> observable, Consumer<T> listener) {
		return subscribe(observable, Rx.onValue(listener));
	}

	/**
	 * Mechanism for specifying a specific Executor.  A corresponding Scheduler will
	 * be created using Schedulers.from(executor).
	 */
	public static RxExecutor on(Executor executor) {
		if (executor == MoreExecutors.directExecutor()) {
			return getSameThreadExecutor();
		} else if (executor instanceof HasRxExecutor) {
			return ((HasRxExecutor) executor).getRxExecutor();
		} else {
			return new RxExecutor(executor, Schedulers.from(executor));
		}
	}

	/** Mechanism for specifying a specific Executor (for ListenableFuture) and Scheduler (for Observable). */
	public static RxExecutor on(Executor executor, Scheduler scheduler) {
		return new RxExecutor(executor, scheduler);
	}

	/*** Marker interface which allows an Executor to specify its own Scheduler. */
	public interface HasRxExecutor extends Executor {
		RxExecutor getRxExecutor();
	}

	/**
	 * This class holds an instance of Executor (for ListenableFuture) and
	 * Scheduler (for Observable).  It has methods which match the signatures of Rx's
	 * static methods, which allows users to   
	 */
	public static class RxExecutor {
		private final Executor executor;
		private final Scheduler scheduler;
		private final RxTracingPolicy tracingPolicy;

		private RxExecutor(Executor executor, Scheduler scheduler) {
			this.executor = executor;
			this.scheduler = scheduler;
			this.tracingPolicy = getTracingPolicy();
		}

		public <T> Subscription subscribe(Observable<? extends T> observable, Rx<T> untracedListener) {
			Rx<T> listener = tracingPolicy.hook(observable, untracedListener);
			return observable.observeOn(scheduler).subscribe(listener);
		}

		public <T> Subscription subscribe(Observable<? extends T> observable, Consumer<T> listener) {
			return subscribe(observable, Rx.onValue(listener));
		}

		public <T> Subscription subscribe(IObservable<? extends T> observable, Rx<T> listener) {
			return subscribe(observable.asObservable(), listener);
		}

		public <T> Subscription subscribe(IObservable<? extends T> observable, Consumer<T> listener) {
			return subscribe(observable, Rx.onValue(listener));
		}

		public <T> Subscription subscribe(ListenableFuture<? extends T> future, Rx<T> untracedListener) {
			Rx<T> listener = tracingPolicy.hook(future, untracedListener);
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

		public <T> Subscription subscribe(ListenableFuture<? extends T> future, Consumer<T> listener) {
			return subscribe(future, Rx.onValue(listener));
		}
	}

	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
	private static RxExecutor getSameThreadExecutor() {
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
	private static RxTracingPolicy getTracingPolicy() {
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
