/*
 * Copyright 2020 DiffPlug
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


import java.util.Objects;

class ChitImpl {
	private ChitImpl() {}

	static final class GuardedRunnable implements Runnable {
		final Chit guard;
		final Runnable delegate;

		public GuardedRunnable(Chit guard, Runnable delegate) {
			this.guard = Objects.requireNonNull(guard);
			this.delegate = Objects.requireNonNull(delegate);
		}

		@Override
		public void run() {
			if (!guard.isDisposed()) {
				delegate.run();
			}
		}
	}

	static final class AlreadyDisposed implements Chit {
		@Override
		public boolean isDisposed() {
			return true;
		}

		@Override
		public void runWhenDisposed(Runnable whenDisposed) {
			whenDisposed.run();
		}
	}
}
