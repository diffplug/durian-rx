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

import com.diffplug.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import kotlinx.coroutines.flow.Flow;
import org.junit.Assert;

final class RxAsserter<T> {
	private final List<T> values = new ArrayList<>();
	private Optional<Throwable> terminal = null;
	private final RxListener<T> listener = Rx.onValueOnTerminate(value -> {
		synchronized (RxAsserter.this) {
			values.add(value);
		}
	}, terminate -> {
		synchronized (RxAsserter.this) {
			terminal = terminate;
		}
	});

	public static <T> RxAsserter<T> on(Flow<? extends T> observable) {
		RxAsserter<T> asserter = new RxAsserter<>();
		Rx.subscribe(observable, asserter.listener);
		return asserter;
	}

	public static <T> RxAsserter<T> on(IFlowable<? extends T> observable) {
		RxAsserter<T> asserter = new RxAsserter<>();
		Rx.subscribe(observable, asserter.listener);
		return asserter;
	}

	public static <T> RxAsserter<T> on(ListenableFuture<? extends T> observable) {
		RxAsserter<T> asserter = new RxAsserter<>();
		Rx.subscribe(observable, asserter.listener);
		return asserter;
	}

	public static <T> RxAsserter<T> on(CompletionStage<? extends T> observable) {
		RxAsserter<T> asserter = new RxAsserter<>();
		Rx.subscribe(observable, asserter.listener);
		return asserter;
	}

	/** Asserts that the given values were observed. */
	@SafeVarargs
	public final void assertValues(T... expected) {
		synchronized (this) {
			Assert.assertEquals(Arrays.asList(expected), values);
		}
	}

	/** Asserts that the given terminal condition was observed. */
	public void assertTerminal(Optional<Throwable> expected) {
		synchronized (this) {
			Assert.assertEquals(expected, terminal);
		}
	}

	/** Asserts that the given terminal condition was observed. */
	public void assertTerminalExceptionClass(Class<? extends Throwable> clazz) {
		synchronized (this) {
			Assert.assertTrue(terminal.isPresent());
			Assert.assertEquals(clazz, terminal.get().getClass());
		}
	}
}
