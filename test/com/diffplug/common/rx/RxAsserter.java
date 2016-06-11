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

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Assert;

import com.diffplug.common.base.Box;

class RxAsserter<T> extends Rx<T> {
	static <T> RxAsserter<T> create() {
		Box.Nullable<T> value = Box.Nullable.ofVolatileNull();
		Box.Nullable<Optional<Throwable>> terminal = Box.Nullable.ofVolatileNull();
		return new RxAsserter<T>(val -> value.set(val), ter -> terminal.set(ter), value, terminal);
	}

	private final Box.Nullable<T> value;
	private final Box.Nullable<Optional<Throwable>> terminal;

	private RxAsserter(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminal, Box.Nullable<T> value, Box.Nullable<Optional<Throwable>> terminal) {
		super(onValue, onTerminal);
		this.value = value;
		this.terminal = terminal;
	}

	/** Asserts that the given value was observed. */
	public void assertValue(T expected) {
		Assert.assertEquals(expected, value.get());
	}

	/** Asserts that the given terminal condition was observed. */
	public void assertTerminal(Optional<Throwable> expected) {
		Assert.assertEquals(expected, terminal.get());
	}

	/** Asserts that the given terminal condition was observed. */
	public void assertTerminalExceptionClass(Class<? extends Throwable> clazz) {
		Assert.assertTrue(terminal.get().isPresent());
		Assert.assertEquals(clazz, terminal.get().get().getClass());
	}
}
