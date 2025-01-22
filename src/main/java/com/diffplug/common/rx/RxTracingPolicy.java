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
package com.diffplug.common.rx;

import com.diffplug.common.base.DurianPlugins;
import com.diffplug.common.base.Errors;
import com.diffplug.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.Observable;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Plugin which gets notified of every call to {@link Rx#subscribe Rx.subscribe}, allowing various kinds of tracing.
 * <p>
 * By default, no tracing is done. To enable tracing, do one of the following:
 * <ul>
 * <li>Execute this at the very beginning of your application: `DurianPlugins.set(RxTracingPolicy.class, new MyTracingPolicy());`</li>
 * <li>Set this system property: `durian.plugins.com.diffplug.common.rx.RxTracingPolicy=fully.qualified.name.to.MyTracingPolicy`</li>
 * </ul>
 * {@link LogDisposableTrace} is a useful tracing policy for debugging errors within callbacks.
 * @see DurianPlugins
 */
public interface RxTracingPolicy {
	/**
	 * Given an observable, and an {@link Rx} which is about to be subscribed to this observable,
	 * return a (possibly instrumented) `Rx`.
	 * 
	 * @param observable The {@link IObservable}, {@link Observable}, or {@link ListenableFuture} which is about to be subscribed to.
	 * @param listener The {@link Rx} which is about to be subscribed.
	 * @return An {@link Rx} which may (or may not) be instrumented.  To ensure that the program's behavior
	 * is not changed, implementors should ensure that all method calls are delegated unchanged to the original listener eventually.
	 */
	<T> RxListener<T> hook(Object observable, RxListener<T> listener);

	/** An `RxTracingPolicy` which performs no tracing, and has very low overhead. */
	public static final RxTracingPolicy NONE = new RxTracingPolicy() {
		@Override
		public <T> RxListener<T> hook(Object observable, RxListener<T> listener) {
			return listener;
		}
	};

	/**
	 * An {@link RxTracingPolicy} which logs the stack trace of every subscription, so
	 * that it can decorate any exceptions with the stack trace at the time they were subscribed.
	 * <p>
	 * This logging is fairly expensive, so you might want to set the {@link LogDisposableTrace#shouldLog} field,
	 * which determines whether a subscription is logged or passed along untouched.
	 * <p>
	 * By default every {@link Rx#onValue} listener will be logged, but nothing else.
	 * <p>
	 * To enable this tracing policy, do one of the following:
	 * <ul>
	 * <li>Execute this at the very beginning of your application: `DurianPlugins.set(RxTracingPolicy.class, new LogDisposableTrace());`</li>
	 * <li>Set this system property: `durian.plugins.com.diffplug.common.rx.RxTracingPolicy=com.diffplug.common.rx.RxTracingPolicy$LogDisposableTrace`</li>
	 * </ul>
	 * @see <a href="https://github.com/diffplug/durian-rx/blob/master/src/com/diffplug/common/rx/RxTracingPolicy.java?ts=4">LogSubscriptionTrace source code</a>
	 * @see DurianPlugins
	 */
	public static class LogSubscriptionTrace implements RxTracingPolicy {
		/** The BiPredicate which determines which subscriptions should be logged.  By default, any Rx which is logging will be logged. */
		@SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This is public on purpose, and is only functional in a debug mode.")
		public static BiPredicate<Object, RxListener<?>> shouldLog = (observable, listener) -> listener.isLogging();

		@Override
		public <T> RxListener<T> hook(Object observable, RxListener<T> listener) {
			if (!shouldLog.test(observable, listener)) {
				// we're not logging, so pass the listener unchanged
				return listener;
			} else {
				// capture the stack at the time of the subscription
				List<StackTraceElement> subscriptionTrace = StackDumper.captureStackBelow(LogSubscriptionTrace.class, RxExecutor.class, Rx.class);
				// create a new Rx which passes values unchanged, but instruments exceptions with the subscription stack
				return Rx.onValueOnTerminate(listener::onValue, new KotlinOnTerminateBridge(listener, subscriptionTrace));
			}
		}

		static class KotlinOnTerminateBridge implements Consumer<Optional<Throwable>> {
			final RxListener<?> listener;
			final List<StackTraceElement> subscriptionTrace;

			public KotlinOnTerminateBridge(RxListener<?> listener, List<StackTraceElement> subscriptionTrace) {
				this.listener = listener;
				this.subscriptionTrace = subscriptionTrace;
			}

			@Override
			public void accept(Optional<Throwable> error) {
				if (error.isPresent()) {
					// if there is an error, wrap it in a SubscriptionException and log it
					SubscriptionException subException = new SubscriptionException(error.get(), subscriptionTrace);
					Errors.log().accept(subException);
					// if the original listener was just logging exceptions, there's no need to notify it, as this would be a double-log
					if (!listener.isLogging()) {
						// the listener isn't a simple logger, so we should pass the original exception
						// to ensure that our logging doesn't change the program's behavior
						listener.onTerminate.accept(Optional.of(error.get()));
					}
				} else {
					// pass clean terminations unchanged
					listener.onTerminate.accept(Optional.empty());
				}
			}
		}

		/** An Exception which has the stack trace of the Rx.subscription() call which created the subscription in which the cause was thrown. */
		static class SubscriptionException extends Exception {
			private static final long serialVersionUID = 1L;

			public SubscriptionException(Throwable cause, List<StackTraceElement> stack) {
				super(cause);
				setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
			}
		}
	}
}
