/*
 * Copyright (C) 2020-2022 DiffPlug
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

import com.diffplug.common.base.Box
import com.diffplug.common.base.Consumers
import com.diffplug.common.base.DurianPlugins
import com.diffplug.common.base.Either
import com.diffplug.common.base.Errors
import com.diffplug.common.rx.RxListener.DefaultTerminate
import com.diffplug.common.util.concurrent.ListenableFuture
import com.diffplug.common.util.concurrent.MoreExecutors
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalStateException
import java.lang.SafeVarargs
import java.util.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.function.Consumer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge

/**
 * Unifies the listener models of [RxJava&#39;s Observable][io.reactivex.Observable] with `
 * [Guava's ListenableFuture](https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained)
 * ` , and also adds tracing capabilities.
 *
 * TL;DR <pre> // subscribe to values, termination, or both Rx.subscribe(listenableOrObservable, val
 * -> doSomething(val)); // errors are passed to Errors.log() Rx.subscribe(listenableOrObservable,
 * Rx.onTerminate(optionalError -> maybeHandleError()); // values are ignored
 * Rx.subscribe(listenableOrObservable, Rx.onValueOrTerminate(val -> doSomething(val), optionalError
 * -> maybeHandleError())); // receive callbacks on a specific executor
 * Rx.on(someExecutor).subscribe(listenableOrObservable, val -> doSomething(val)); // call
 * unsubscribe() on the subscription to cancel it io.reactivex.disposables.Disposable subscription =
 * Rx.subscribe(listenableOrObservable, val -> doSomething); </pre> * Long version: `Rx` implements
 * both the [io.reactivex.Observer] and [com.diffplug.common.util.concurrent.FutureCallback]
 * interfaces by mapping them to two `Consumer`s:
 * * `Consumer<T> onValue`</T>
 * * `Consumer<Optional></Optional><Throwable>> onTerminate`</Throwable>
 *
 * Which are mapped as follows:
 * * `Observable.onNext(T value) -> onValue.accept(value)`
 * * `Observable.onCompleted() -> onTerminate.accept(Optional.empty())`
 * * `Observable.onError(Throwable error) -> onTerminate.accept(Optional.of(error))`
 * * `FutureCallback.onSuccess(T value) -> onValue.accept(value);
 *   onTerminate.accept(Optional.empty());`
 * * `FutureCallback.onError(Throwable error) -> onTerminate.accept(Optional.of(error))`
 *
 * An instance of Rx is created by calling one of Rx's static creator methods:
 * * [onValue(Consumer&amp;lt;T&amp;gt;)][.onValue]
 * * [onTerminate(Consumer&amp;lt;Optional&amp;lt;Throwable&amp;gt;&amp;gt;)][.onTerminate]
 * * [onFailure(Consumer&amp;lt;Throwable&amp;gt;)][.onFailure]
 * * [onValueOrTerminate(Consumer&amp;lt;T&amp;gt;,
 *   Consumer&amp;lt;Optional&amp;lt;Throwable&amp;gt;&amp;gt;)][.onValueOnTerminate]
 * * [onValueOrFailure(Consumer&amp;lt;T&amp;gt;,
 *   Consumer&amp;lt;Throwable&amp;gt;)][.onValueOnFailure]
 *
 * Once you have an instance of Rx, you can subscribe it using the normal RxJava or Guava calls:
 * * `rxObservable.subscribe(Rx.onValue(val -> doSomething(val));`
 * * `Futures.addCallback(listenableFuture, Rx.onValue(val -> doSomething(val));`
 *
 * But the recommended way to subscribe is to use:
 * * `Rx.subscribe(listenableOrObservable, Rx.onValue(val -> doSomething(val)));`
 * * `Rx.subscribe(listenableOrObservable, val -> doSomething(val)); // automatically uses
 *   Rx.onValue()`
 *
 * The advantage of this latter method is that it returns [io.reactivex.disposables.Disposable]
 * instances which allow you to unsubscribe from futures in the same manner as for observables.
 * * `subscription = Rx.subscribe( ... )`
 *
 * If you wish to receive callbacks on a specific thread, you can use:
 * * `Rx.on(someExecutor).subscribe( ... )`
 *
 * Because RxJava's Observables use [io.reactivex.Scheduler]s rather than
 * [java.util.concurrent.Executor]s, a Scheduler is automatically created using [Schedulers.from].
 * If you'd like to specify the Scheduler manually, you can use [Rx.callbackOn] or you can create an
 * executor which implements [RxExecutor.Has].
 *
 * @see [SwtExec]
 *   (https://diffplug.github.io/durian-swt/javadoc/snapshot/com/diffplug/common/swt/SwtExec.html)
 */
object Rx {
	fun <T> createEmitFlow() =
			MutableSharedFlow<T>(replay = 0, extraBufferCapacity = 1, BufferOverflow.SUSPEND)

	@JvmStatic
	fun <T> emit(flow: MutableSharedFlow<T>, value: T) {
		if (!flow.tryEmit(value)) {
			throw IllegalStateException("Failed to emit $value on $flow")
		}
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever a value is received. Any
	 * errors are sent to ErrorHandler.log().
	 */
	@JvmStatic
	fun <T> onValue(onValue: Consumer<T>): RxListener<T> {
		return RxListener(onValue, RxListener.logErrors)
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream or
	 * future completes, whether with an error or not.
	 */
	@JvmStatic
	fun <T> onTerminate(onTerminate: Consumer<Optional<Throwable>>): RxListener<T> {
		return RxListener(Consumers.doNothing(), onTerminate)
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream or
	 * future completes, whether with an error or not, and the error (if present) will be logged.
	 */
	fun <T> onTerminateLogError(onTerminate: Consumer<Optional<Throwable>>): RxListener<T> {
		return RxListener(Consumers.doNothing(), DefaultTerminate(onTerminate))
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream or
	 * future completes with an error.
	 */
	@JvmStatic
	fun <T> onFailure(onFailure: Consumer<Throwable>): RxListener<T> {
		Objects.requireNonNull(onFailure)
		return RxListener(Consumers.doNothing()) { error: Optional<Throwable> ->
			if (error.isPresent) {
				onFailure.accept(error.get())
			}
		}
	}

	/**
	 * Creates an Rx instance which will call onValue whenever a value is received, is received, and
	 * onTerminate when the future or observable completes, whether with an error or not.
	 */
	@JvmStatic
	fun <T> onValueOnTerminate(
			onValue: Consumer<T>,
			onTerminate: Consumer<Optional<Throwable>>
	): RxListener<T> {
		return RxListener(onValue, onTerminate)
	}

	/**
	 * Creates an Rx instance which will call the given consumer whenever the followed stream or
	 * future completes, whether with an error or not, and the error (if present) will automatically
	 * be logged.
	 */
	@JvmStatic
	fun <T> onValueOnTerminateLogError(
			onValue: Consumer<T>,
			onTerminate: Consumer<Optional<Throwable>>
	): RxListener<T> {
		return RxListener(onValue, DefaultTerminate(onTerminate))
	}

	/**
	 * Creates an Rx instance which will call onValue whenever a value is received, and onFailure if
	 * the stream or future completes with an error.
	 */
	@JvmStatic
	fun <T> onValueOnFailure(onValue: Consumer<T>, onFailure: Consumer<Throwable>): RxListener<T> {
		Objects.requireNonNull(onFailure)
		return RxListener(onValue) { error: Optional<Throwable> ->
			if (error.isPresent) {
				onFailure.accept(error.get())
			}
		}
	}

	// Static versions
	@JvmStatic
	fun <T> subscribe(flow: Flow<T>, listener: RxListener<T>) {
		sameThreadExecutor().subscribe(flow, listener)
	}

	@JvmStatic
	fun <T> subscribe(flow: Flow<T>, listener: Consumer<T>) {
		subscribe(flow, onValue(listener))
	}

	@JvmStatic
	fun <T> subscribe(deferred: Deferred<T>, listener: RxListener<T>) {
		sameThreadExecutor().subscribe(deferred, listener)
	}

	@JvmStatic
	fun <T> subscribe(deferred: Deferred<T>, listener: Consumer<T>) {
		subscribe(deferred, onValue(listener))
	}

	@JvmStatic
	fun <T> subscribe(observable: IObservable<out T>, listener: RxListener<T>) {
		subscribe(observable.asObservable(), listener)
	}

	@JvmStatic
	fun <T> subscribe(observable: IObservable<out T>, listener: Consumer<T>) {
		subscribe(observable.asObservable(), listener)
	}

	@JvmStatic
	fun <T> subscribe(future: ListenableFuture<out T>, listener: RxListener<T>) {
		sameThreadExecutor().subscribe(future, listener)
	}

	@JvmStatic
	fun <T> subscribe(future: ListenableFuture<out T>, listener: Consumer<T>) {
		subscribe(future, onValueOnTerminate(listener, TrackCancelled(future)))
	}

	@JvmStatic
	fun <T> subscribe(future: CompletionStage<out T>, listener: RxListener<T>) {
		sameThreadExecutor().subscribe(future, listener)
	}

	@JvmStatic
	fun <T> subscribe(future: CompletionStage<out T>, listener: Consumer<T>) {
		subscribe(future, onValueOnTerminate(listener, TrackCancelled(future.toCompletableFuture())))
	}

	// Static versions
	@JvmStatic
	fun <T> subscribeDisposable(flow: Flow<T>, listener: RxListener<T>): Disposable {
		return sameThreadExecutor().subscribeDisposable(flow, listener)
	}

	@JvmStatic
	fun <T> subscribeDisposable(flow: Flow<T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(flow, onValue(listener))
	}

	@JvmStatic
	fun <T> subscribeDisposable(deferred: Deferred<T>, listener: RxListener<T>): Disposable {
		return sameThreadExecutor().subscribeDisposable(deferred, listener)
	}

	@JvmStatic
	fun <T> subscribeDisposable(deferred: Deferred<T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(deferred, onValue(listener))
	}

	@JvmStatic
	fun <T> subscribeDisposable(observable: IObservable<out T>, listener: RxListener<T>): Disposable {
		return subscribeDisposable(observable.asObservable(), listener)
	}

	@JvmStatic
	fun <T> subscribeDisposable(observable: IObservable<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(observable.asObservable(), listener)
	}

	@JvmStatic
	fun <T> subscribeDisposable(
			future: ListenableFuture<out T>,
			listener: RxListener<T>
	): Disposable {
		return sameThreadExecutor().subscribeDisposable(future, listener)
	}

	@JvmStatic
	fun <T> subscribeDisposable(future: ListenableFuture<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(future, onValueOnTerminate(listener, TrackCancelled(future)))
	}

	@JvmStatic
	fun <T> subscribeDisposable(future: CompletionStage<out T>, listener: RxListener<T>): Disposable {
		return sameThreadExecutor().subscribeDisposable(future, listener)
	}

	@JvmStatic
	fun <T> subscribeDisposable(future: CompletionStage<out T>, listener: Consumer<T>): Disposable {
		return subscribeDisposable(
				future, onValueOnTerminate(listener, TrackCancelled(future.toCompletableFuture())))
	}

	/**
	 * Mechanism for specifying a specific Executor. A corresponding Scheduler will be created using
	 * Schedulers.from(executor).
	 */
	@JvmStatic
	fun callbackOn(executor: Executor): RxExecutor {
		return if (executor === MoreExecutors.directExecutor()) {
			sameThreadExecutor()
		} else if (executor is RxExecutor.Has) {
			executor.rxExecutor
		} else {
			RxExecutor(executor, Schedulers.from(executor), executor.asCoroutineDispatcher())
		}
	}

	/**
	 * Mechanism for specifying a specific Executor (for ListenableFuture) and Scheduler (for
	 * Observable).
	 */
	@JvmStatic
	fun callbackOn(executor: Executor, scheduler: Scheduler): RxExecutor {
		return callbackOn(executor, scheduler, executor.asCoroutineDispatcher())
	}

	@JvmStatic
	fun callbackOn(
			executor: Executor,
			scheduler: Scheduler,
			dispatcher: CoroutineDispatcher
	): RxExecutor {
		return RxExecutor(executor, scheduler, dispatcher)
	}

	@JvmStatic
	@SuppressFBWarnings(
			value = ["LI_LAZY_INIT_STATIC"],
			justification = "This race condition is fine, as explained in the comment below.")
	fun sameThreadExecutor(): RxExecutor {
		// There is an acceptable race condition here - _sameThread might get set multiple times.
		// This would happen if multiple threads called blocking() at the same time
		// during initialization, and this is likely to actually happen in practice.
		//
		// It is important for this method to be fast, so it's better to accept
		// that getSameThreadExecutor() might return different instances (which each have the
		// same behavior), rather than to incur the cost of some type of synchronization.
		if (_sameThread == null) {
			_sameThread =
					RxExecutor(
							MoreExecutors.directExecutor(),
							Schedulers.trampoline(),
							MoreExecutors.directExecutor().asCoroutineDispatcher())
		}
		return _sameThread!!
	}

	private var _sameThread: RxExecutor? =
			null // if it isn't an RxListener, then we'll apply _tracing policy// if it's an RxListener,
	// then _tracingPolicy handled it already// There is an acceptable race condition here -
	// see getSameThreadExecutor()

	/** Returns the global tracing policy. */
	@get:SuppressFBWarnings(
			value = ["LI_LAZY_INIT_STATIC", "LI_LAZY_INIT_UPDATE_STATIC"],
			justification = "This race condition is fine, as explained in the comment below.")
	val tracingPolicy: RxTracingPolicy
		get() {
			// There is an acceptable race condition here - see getSameThreadExecutor()
			if (_tracingPolicy == null) {
				_tracingPolicy = DurianPlugins.get(RxTracingPolicy::class.java, RxTracingPolicy.NONE)
				if (_tracingPolicy !== RxTracingPolicy.NONE) {
					// TODO: setup tracing
				}
			}
			return _tracingPolicy!!
		}

	private var _tracingPolicy: RxTracingPolicy? = null

	/** Package-private for testing - resets all of the static member variables. */
	fun resetForTesting() {
		_sameThread = null
		_tracingPolicy = null
	}

	/**
	 * Merges a bunch of [IObservable]s into a single [Observable] containing the most-recent value.
	 */
	@JvmStatic
	@SafeVarargs
	fun <T> merge(vararg toMerge: IObservable<out T>): Flow<T> {
		return toMerge.map { it.asObservable() }.merge()
	}

	/** Reliable way to sync two RxBox to each other. */
	@JvmStatic
	fun <T> sync(left: RxBox<T>, right: RxBox<T>) {
		sync(sameThreadExecutor(), left, right)
	}

	/**
	 * Reliable way to sync two RxBox to each other, using the given RxSubscriber to listen for
	 * changes
	 */
	@JvmStatic
	fun <T> sync(subscriber: RxSubscriber?, left: RxBox<T>, right: RxBox<T>) {
		val firstChange = Box.Nullable.ofNull<Either<T, T>?>()
		subscriber!!.subscribe(left) { leftVal: T ->
			// the left changed before we could acknowledge it
			if (leftVal != left.get()) {
				return@subscribe
			}
			var doSet: Boolean
			synchronized(firstChange) {
				val change = firstChange.get()
				doSet = change == null || change.isLeft
				if (!doSet) {
					// this means the right set something before we did
					if (leftVal == change!!.right) {
						// if we're setting to the value that the right
						// requested, then we're just responding to them,
						// and there's no need to fire another event
						firstChange.set(null)
					} else {
						// otherwise, we'll record that we set it first
						firstChange.set(Either.createLeft(leftVal))
					}
				}
			}
			if (doSet) {
				right.set(leftVal)
			}
		}
		subscriber.subscribe(right) { rightVal: T ->
			// the right changed before we could acknowledge it
			if (rightVal != right.get()) {
				return@subscribe
			}
			var doSet: Boolean
			synchronized(firstChange) {
				val change = firstChange.get()
				doSet = change == null || change.isRight
				if (!doSet) {
					// this means the left set something before we did
					if (rightVal == change!!.left) {
						// if we're setting to the value that the left
						// requested, then we're just responding to them,
						// and there's no need to fire another event
						firstChange.set(null)
					} else {
						// otherwise, we'll record that we set it first
						firstChange.set(Either.createRight(rightVal))
					}
				}
			}
			if (doSet) {
				left.set(rightVal)
			}
		}
	}

	/**
	 * An error listener which tracks whether a future has been cancelled, so that it doesn't log the
	 * errors of cancelled futures.
	 */
	internal class TrackCancelled(private val future: Future<*>) :
			DefaultTerminate(Consumers.doNothing()) {
		override fun accept(errorOpt: Optional<Throwable>) {
			if (errorOpt.isPresent && !future.isCancelled) {
				Errors.log().accept(errorOpt.get())
			}
		}
	}
}
