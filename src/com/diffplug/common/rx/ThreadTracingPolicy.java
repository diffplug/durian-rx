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

import java.util.function.Predicate;

import rx.Observable;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.DurianPlugins;
import com.diffplug.common.rx.RxTracingPolicy.LogSubscriptionTrace;
import com.diffplug.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Plugin which gets notified of every call to {@link Rx#subscribe Rx.subscribe}, allowing various kinds of tracing.
 * <p>
 * By default, no tracing is done. To enable tracing, do one of the following:
 * <ul>
 * <li>Execute this at the very beginning of your application: {@code DurianPlugins.set(RxTracingPolicy.class, new MyTracingPolicy());}</li>
 * <li>Set this system property: {@code durian.plugins.com.diffplug.common.rx.RxTracingPolicy=fully.qualified.name.to.MyTracingPolicy}</li>
 * </ul>
 * {@link LogSubscriptionTrace} is a useful tracing policy for debugging errors within callbacks.
 * @see DurianPlugins
 */
public interface ThreadTracingPolicy {
	/**
	 * Given an observable, and an {@link Rx} which is about to be subscribed to this observable,
	 * return a (possibly instrumented) {@code Rx}.
	 * 
	 * @param observable The {@link IObservable}, {@link Observable}, or {@link ListenableFuture} which is about to be subscribed to.
	 * @param listener The {@link Rx} which is about to be subscribed.
	 * @return An {@link Rx} which may (or may not) be instrumented.  To ensure that the program's behavior
	 * is not changed, implementors should ensure that all method calls are delegated unchanged to the original listener eventually.
	 */
	boolean trace(Box<?> toTrace);

	/** An {@code RxTracingPolicy} which performs no tracing, and has very low overhead. */
	public static final ThreadTracingPolicy NONE = new ThreadTracingPolicy() {
		@Override
		public boolean trace(Box<?> toTrace) {
			return false;
		}
	};

	/** An {@code RxTracingPolicy} which performs no tracing, and has very low overhead. */
	public static final ThreadTracingPolicy ALL = new ThreadTracingPolicy() {
		@Override
		public boolean trace(Box<?> toTrace) {
			return true;
		}
	};

	public static class Some implements ThreadTracingPolicy {
		/** The Predicate which determines which Boxes will have their `set()` methods logged. */
		@SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This is public on purpose, and is only functional in a debug mode.")
		public static Predicate<Box<?>> shouldTrace = (toLog) -> true;

		@Override
		public boolean trace(Box<?> toTrace) {
			return shouldTrace.test(toTrace);
		}
	}
}
