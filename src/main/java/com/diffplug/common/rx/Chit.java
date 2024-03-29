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
package com.diffplug.common.rx;


import java.util.ArrayList;
import java.util.List;

/**
 * Makes it possible to receive a notification when a resource is disposed.
 * 
 * Oftentimes, a UI resource (such as a dialog) is subject to disposal. There
 * might be a long-running task in a non-UI thread which should be cancelled
 * if the dialog is closed.  This interface provides a clean abstraction for
 * guaranteeing that an action is taken in response to a resource being disposed,
 * with exact semantics on the UI thread, and eventually-consistent semantics
 * on other threads.
 * 
 * @see com.diffplug.common.swt.SwtRx#disposableEar(Widget)
 */
public interface Chit {
	/**
	 * Returns whether the resource is disposed. May be called from any thread,
	 * and must return true as soon as `dispose()` is called, no matter which thread
	 * `dispose()` was called from.
	 */
	boolean isDisposed();

	/**
	 * Adds a listener which will run when the given element is disposed.
	 * The runnable might be executed on any thread.  If the element has
	 * already been disposed (subject to the glitch constraints of
	 * {@link #isDisposed}), the runnable will be executed immediately.
	 */
	void runWhenDisposed(Runnable whenDisposed);

	/**
	 * Wraps the runnable such that it will only run iff the disposable has not been disposed,
	 * according to {@link #isDisposed()}.
	 */
	default Runnable guard(Runnable delegate) {
		return new ChitImpl.GuardedRunnable(this, delegate);
	}

	/** Returns a {@link Chit} which has already been disposed. */
	public static Chit alreadyDisposed() {
		return new ChitImpl.AlreadyDisposed();
	}

	/** Creates a Settable Disposable ear. */
	public static Settable settable() {
		return new Settable();
	}

	/** The standard implementation of DisposableEar. */
	public final class Settable implements Chit {
		ArrayList<Runnable> runWhenDisposed = new ArrayList<>();

		private Settable() {}

		public void dispose() {
			List<Runnable> toDispose;
			synchronized (this) {
				toDispose = runWhenDisposed;
				runWhenDisposed = null;
			}
			if (toDispose != null) {
				toDispose.forEach(Runnable::run);
			}
		}

		public synchronized boolean isDisposed() {
			return runWhenDisposed == null;
		}

		public synchronized void runWhenDisposed(Runnable whenDisposed) {
			if (runWhenDisposed != null) {
				runWhenDisposed.add(whenDisposed);
			} else {
				whenDisposed.run();
			}
		}
	}
}
