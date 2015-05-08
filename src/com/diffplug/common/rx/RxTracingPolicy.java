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
import java.util.Objects;
import java.util.function.Predicate;

import com.diffplug.common.base.Predicates;
import com.diffplug.common.base.StackDumper;
import com.diffplug.common.base.StringPrinter;

/**
 * Interface which gets called for every subscription through the Rx mechanism, allowing various kinds of tracing.
 * 
 * To enable maximum tracing, set the following system property as such:
 * durian.plugins.com.diffplug.common.rx.RxTracingPolicy = com.diffplug.common.rx.RxTracingPolicy$DumpSubscriptionTrace
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
	public static class DumpSubscriptionTrace implements RxTracingPolicy {
		private final Predicate<Object> shouldDump;

		/** Dumps the stack trace of all subscriptions. */
		public DumpSubscriptionTrace() {
			this(Predicates.alwaysTrue());
		}

		/**
		 * @param shouldDump A Predicate which operates on the observable (IObservable, Observable, or
		 * ListenableFuture), and returns true iff subscriptions on the observable should be traced.
		 */
		public DumpSubscriptionTrace(Predicate<Object> shouldDump) {
			this.shouldDump = Objects.requireNonNull(shouldDump);
		}

		@Override
		public <T> Rx<T> hook(Object observable, Rx<T> listener) {
			if (shouldDump.test(observable)) {
				List<StackTraceElement> subscriptionTrace = StackDumper.captureStackBelow(DumpSubscriptionTrace.class, Rx.RxExecutor.class, Rx.class);
				return Rx.onValueOrTerminate(listener::onNext, error -> {
					if (error.isPresent()) {
						StackDumper.dump(StringPrinter.buildString(printer -> {
							// dump the stack trace
							error.get().printStackTrace(printer.toPrintWriter());
							// then print where it was subscribed
							printer.println("From a subscription at:");
						}), subscriptionTrace);
						listener.onError(error.get());
					} else {
						listener.onCompleted();
					}
				});
			} else {
				return listener;
			}
		}
	}
}
