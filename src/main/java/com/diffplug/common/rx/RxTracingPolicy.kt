/*
 * Copyright (C) 2020-2025 DiffPlug
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

import com.diffplug.common.base.Errors
import com.diffplug.common.rx.Rx.onValueOnTerminate
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import java.util.*
import java.util.function.BiPredicate
import java.util.function.Consumer

/**
 * Plugin which gets notified of every call to [Rx.subscribe], allowing various kinds of tracing.
 *
 * By default, no tracing is done. To enable tracing, do one of the following:
 * * Execute this at the very beginning of your application:
 *   `DurianPlugins.set(RxTracingPolicy.class, new MyTracingPolicy());`
 * * Set this system property:
 *   `durian.plugins.com.diffplug.common.rx.RxTracingPolicy=fully.qualified.name.to.MyTracingPolicy`
 *
 * [LogDisposableTrace] is a useful tracing policy for debugging errors within callbacks.
 *
 * @see DurianPlugins
 */
interface RxTracingPolicy {
	/**
	 * Given an observable, and an [Rx] which is about to be subscribed to this observable, return a
	 * (possibly instrumented) `Rx`.
	 *
	 * @param observable The [IFlowable], [Observable], or [ListenableFuture] which is about to be
	 *   subscribed to.
	 * @param listener The [Rx] which is about to be subscribed.
	 * @return An [Rx] which may (or may not) be instrumented. To ensure that the program's behavior
	 *   is not changed, implementors should ensure that all method calls are delegated unchanged to
	 *   the original listener eventually.
	 */
	fun <T> hook(flow: Any, listener: RxListener<T>): RxListener<T>

	/**
	 * An [RxTracingPolicy] which logs the stack trace of every subscription, so that it can decorate
	 * any exceptions with the stack trace at the time they were subscribed.
	 *
	 * This logging is fairly expensive, so you might want to set the [LogDisposableTrace.shouldLog]
	 * field, which determines whether a subscription is logged or passed along untouched.
	 *
	 * By default every [Rx.onValue] listener will be logged, but nothing else.
	 *
	 * To enable this tracing policy, do one of the following:
	 * * Execute this at the very beginning of your application:
	 *   `DurianPlugins.set(RxTracingPolicy.class, new LogDisposableTrace());`
	 * * Set this system property:
	 *   `durian.plugins.com.diffplug.common.rx.RxTracingPolicy=com.diffplug.common.rx.RxTracingPolicy$LogDisposableTrace`
	 *
	 * @see
	 *   [LogSubscriptionTrace source code](https://github.com/diffplug/durian-rx/blob/master/src/com/diffplug/common/rx/RxTracingPolicy.java?ts=4)
	 * @see DurianPlugins
	 */
	class LogSubscriptionTrace : RxTracingPolicy {
		override fun <T> hook(flow: Any, listener: RxListener<T>): RxListener<T> {
			if (!shouldLog.test(flow, listener)) {
				// we're not logging, so pass the listener unchanged
				return listener
			} else {
				// capture the stack at the time of the subscription
				val subscriptionTrace =
						StackDumper.captureStackBelow(
								LogSubscriptionTrace::class.java, RxExecutor::class.java, Rx::class.java)
				// create a new Rx which passes values unchanged, but instruments exceptions with the
				// subscription stack
				return onValueOnTerminate(
						listener.onValue, KotlinOnTerminateBridge(listener, subscriptionTrace))
			}
		}

		internal class KotlinOnTerminateBridge(
				val listener: RxListener<*>,
				val subscriptionTrace: List<StackTraceElement>
		) : Consumer<Optional<Throwable>> {
			override fun accept(error: Optional<Throwable>) {
				if (error.isPresent) {
					// if there is an error, wrap it in a SubscriptionException and log it
					val subException = SubscriptionException(error.get(), subscriptionTrace)
					Errors.log().accept(subException)
					// if the original listener was just logging exceptions, there's no need to notify it, as
					// this would be a double-log
					if (!listener.isLogging) {
						// the listener isn't a simple logger, so we should pass the original exception
						// to ensure that our logging doesn't change the program's behavior
						listener.onTerminate.accept(Optional.of(error.get()))
					}
				} else {
					// pass clean terminations unchanged
					listener.onTerminate.accept(Optional.empty())
				}
			}
		}

		/**
		 * An Exception which has the stack trace of the Rx.subscription() call which created the
		 * subscription in which the cause was thrown.
		 */
		internal class SubscriptionException(cause: Throwable, stack: List<StackTraceElement>) :
				Exception(cause) {
			init {
				stackTrace = stack.toTypedArray<StackTraceElement>()
			}

			companion object {
				private const val serialVersionUID = 1L
			}
		}

		companion object {
			/**
			 * The BiPredicate which determines which subscriptions should be logged. By default, any Rx
			 * which is logging will be logged.
			 */
			@SuppressFBWarnings(
					value = ["MS_SHOULD_BE_FINAL"],
					justification = "This is public on purpose, and is only functional in a debug mode.")
			var shouldLog: BiPredicate<Any, RxListener<*>> =
					BiPredicate { flow: Any?, listener: RxListener<*> ->
						listener.isLogging
					}
		}
	}

	companion object {
		/** An `RxTracingPolicy` which performs no tracing, and has very low overhead. */
		val NONE: RxTracingPolicy =
				object : RxTracingPolicy {
					override fun <T> hook(flow: Any, listener: RxListener<T>): RxListener<T> = listener
				}
	}
}
