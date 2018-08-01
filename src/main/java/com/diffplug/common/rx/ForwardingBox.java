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

import com.diffplug.common.base.Box;
import io.reactivex.Observable;

/**
 * Utility class for wrapping one kind of box with another.
 *
 * - For wrapping a {@link CasBox}, use {@link ForwardingBox.Cas}.
 * - For wrapping an {@link RxBox}, use {@link ForwardingBox.Rx}.
 * - For wrapping a {@link LockBox}, use {@link ForwardingBox.Lock}.
 * - For wrapping an {@link RxLockBox}, use {@link ForwardingBox.RxLock}.
 * 
 * Especially useful for overridding set().
 */
public class ForwardingBox<T, BoxType extends Box<T>> implements Box<T> {
	protected final BoxType delegate;

	protected ForwardingBox(BoxType delegate) {
		this.delegate = delegate;
	}

	@Override
	public T get() {
		return delegate.get();
	}

	@Override
	public void set(T value) {
		delegate.set(value);
	}

	public static class Cas<T> extends ForwardingBox<T, CasBox<T>> implements CasBox<T> {
		protected Cas(CasBox<T> delegate) {
			super(delegate);
		}

		@Override
		public boolean compareAndSet(T expect, T update) {
			return delegate.compareAndSet(expect, update);
		}

		@Override
		public T getAndSet(T newValue) {
			return delegate.getAndSet(newValue);
		}
	}

	public static class Lock<T> extends ForwardingBox<T, LockBox<T>> implements LockBox<T> {
		protected Lock(LockBox<T> delegate) {
			super(delegate);
		}

		@Override
		public Object lock() {
			return delegate.lock();
		}
	}

	public static class Rx<T> extends ForwardingBox<T, RxBox<T>> implements RxBox<T> {
		protected Rx(RxBox<T> delegate) {
			super(delegate);
		}

		@Override
		public Observable<T> asObservable() {
			return delegate.asObservable();
		}
	}

	public static class RxLock<T> extends ForwardingBox<T, RxLockBox<T>> implements RxLockBox<T> {
		protected RxLock(RxLockBox<T> delegate) {
			super(delegate);
		}

		@Override
		public Object lock() {
			return delegate.lock();
		}

		@Override
		public Observable<T> asObservable() {
			return delegate.asObservable();
		}
	}
}
