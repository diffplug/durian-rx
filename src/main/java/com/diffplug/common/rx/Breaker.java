/*
 * Copyright 2018 DiffPlug
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

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.Disposable;

/**
 * A mechanism for "turning off" an existing `RxBox`, much like a a circuit breaker. 
 *
 * Useful for cases where some {@link RxBox#enforce(java.util.function.Function)} calls are causing
 * a problem during a major change, and you'd like to be able to open the circuit, fix things up,
 * then close it again to resume operation.
 *
 * A `Breaker` is an `RxBox` which wraps another `RxBox`.  So long as the breaker is closed, calling
 * `set` on either the `Breaker` or the wrapped box will set both of them. 
 *
 * Once you call `setClosed(false)`, the two boxes are separate.  You can still modify the `Breaker`
 * and its wrapped box, but they will not affect each other.
 *
 * When you call `setClosed(true)`, the wrapped box will be set to the value of the `Breaker`, and all
 * future changes will again be synchronized.
 */
public class Breaker<T> extends RxBoxImp<T> {
	protected final RxBox<T> delegate;
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private volatile Disposable subscription;

	Breaker(RxBox<T> delegate, boolean closed) {
		super(delegate.get());
		this.delegate = delegate;
		setClosed(closed);
	}

	/**
	 * Sets that this breaker is closed or open.
	 *
	 * - If the breaker is already in the given state, nothing happens.
	 * - If the breaker changes to closed, the wrapped box is to the value of `Breaker`, and future changes to either are synchronized.
	 * - If the breaker changes to open, all future changes to either box are not synchronized.
	 */
	public void setClosed(boolean closed) {
		// check for delta
		if (this.closed.compareAndSet(!closed, closed)) {
			// if we're closing
			if (closed) {
				// set the delegate value to whatever the breaker is holding
				delegate.set(super.get());
				// and start subscribing to the delegate value
				subscription = delegate.asObservable().subscribe(super::set);
			} else {
				subscription.dispose();
			}
		}
	}

	@Override
	public void set(T newValue) {
		super.set(newValue);
		if (closed.get()) {
			delegate.set(newValue);
		}
	}

	@Override
	public T get() {
		return closed.get() ? delegate.get() : super.get();
	}

	/** Creates a `Breaker` which is initially closed. */
	public static <T> Breaker<T> createClosed(RxBox<T> box) {
		return new Breaker<>(box, true);
	}

	/** Creates a `Breaker` which is initially open. */
	public static <T> Breaker<T> createOpen(RxBox<T> box) {
		return new Breaker<>(box, false);
	}
}
