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

import java.util.List;
import java.util.function.BiPredicate;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.StackDumper;

/**
 * Interface which gets called for every subscription through the Rx mechanism, allowing various kinds of tracing.
 * 
 * To enable tracing for subscription calls, set the following system property:
 *   durian.plugins.com.diffplug.common.rx.RxTracingPolicy = com.diffplug.common.rx.RxTracingPolicy$LogSubscriptionTrace
 * Or call
 *   DurianPlugins.set(RxTracingPolicy.class, new LogSubscriptionTrace()).
 */
public interface RxTracingPolicy {
	/**
	 * Given an observable, and an Rx which is about to be subscribed to this observable,
	 * return an observable which might have various kinds of instrumentation.
	 * 
	 * @param observable The IObservable, Observable, or ListenableFuture which is about to be subscribed to.
	 * @param listener The Rx<T> which is about to be subscribed.
	 * @return An Rx<T> which may (or may not) be instrumented.  In order to ensure that the program's behavior
	 * is not changed, implementors should ensure that all method calls are delegated to the listener Rx eventually.
	 */
	<T> Rx<T> hook(Object observable, Rx<T> listener);

	/** An RxTracingPolicy which performs no tracing, and has very low overhead. */
	public static final RxTracingPolicy NONE = new RxTracingPolicy() {
		@Override
		public <T> Rx<T> hook(Object observable, Rx<T> listener) {
			return listener;
		}
	};

	/**
	 * An RxTracingPolicy which records the stack trace of every subscription which matches its
	 * predicate.  Anytime a subscription throws an error, that error's stacktrace gets dumped, along
	 * with the stacktrace at the time of the subscription.
	 */
	public static class LogSubscriptionTrace implements RxTracingPolicy {
		/** The BiPredicate which determines which subscriptions should be logged.  By default, any Rx which is logging will be logged. */
		public static BiPredicate<Object, Rx<?>> shouldLog = (observable, listener) -> listener.isLogging();

		@Override
		public <T> Rx<T> hook(Object observable, Rx<T> listener) {
			if (shouldLog.test(observable, listener)) {
				// capture the stack at the time of the subscription
				List<StackTraceElement> subscriptionTrace = StackDumper.captureStackBelow(LogSubscriptionTrace.class, Rx.RxExecutor.class, Rx.class);
				return Rx.onValueOrTerminate(listener::onNext, error -> {
					if (error.isPresent()) {
						// if there is an error, wrap it in a SubscriptionException and log it
						SubscriptionException subException = new SubscriptionException(error.get(), subscriptionTrace);
						Errors.log().handle(subException);
						// prevent double-logging
						if (!listener.isLogging()) {
							listener.onError(subException);
						}
					} else {
						listener.onCompleted();
					}
				});
			} else {
				return listener;
			}
		}

		/** An Exception which has the stack trace of the Rx.subscription() call which created the subscription in which the cause was thrown. */
		static class SubscriptionException extends Exception {
			private static final long serialVersionUID = -265762944158637711L;
			
			public SubscriptionException(Throwable cause, List<StackTraceElement> stack) {
				super(cause);
				setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
			}
		}
	}
}
