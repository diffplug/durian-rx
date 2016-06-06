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

import java.util.concurrent.atomic.AtomicReference;

import com.diffplug.common.base.Errors;

/**
 * Logs a descriptive error if check() is ever called
 * from more than one thread.
 *
 * @author ntwigg
 */
public final class ThreadTracer {
	private final String name;
	private final AtomicReference<Thread> firstCall = new AtomicReference<>();

	public ThreadTracer(String name) {
		this.name = name;
	}

	public final void check() {
		Thread current = Thread.currentThread();
		boolean isFirstCall = firstCall.compareAndSet(null, current);
		if (!isFirstCall) {
			Thread first = firstCall.get();
			if (first != current) {
				Errors.log().accept(new InconsistentThreadCallException(name, first, current));
			}
		}
	}

	public static class InconsistentThreadCallException extends RuntimeException {
		private static final long serialVersionUID = -7946660913539412568L;

		public final String name;
		public final String expected, actual;

		public InconsistentThreadCallException(String name, Thread expected, Thread actual) {
			this.name = name;
			this.expected = expected.toString();
			this.actual = actual.toString();
		}

		@Override
		public String getMessage() {
			return name + ": Expected thread " + expected + " but was " + actual;
		}
	}
}
