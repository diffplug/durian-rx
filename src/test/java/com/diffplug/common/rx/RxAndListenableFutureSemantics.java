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

import com.diffplug.common.util.concurrent.SettableFuture;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import org.junit.Test;

/**
 * This is a simple little test for confirming the behavior of
 * subscribing to stuff.
 */
public class RxAndListenableFutureSemantics {
	@Test
	public void testBehaviorSubjectSubscribe() {
		// create an behavior subject, subscribe pre, and pump test through
		MutableStateFlow<String> testSubject = StateFlowKt.MutableStateFlow("initial");
		RxAsserter<String> observer = RxAsserter.on(testSubject);
		// the observer gets the value immediately
		observer.assertValues("initial");

		// call on next, and the observer gets the new value immediately
		testSubject.setValue("value");
		observer.assertValues("initial", "value");
	}

	@Test
	public void testListenableFutureCancellationResult() {
		SettableFuture<String> future = SettableFuture.create();

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.on(future);
		future.cancel(true);
		assertDuring.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.on(future);
		assertAfter.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);
	}

	@Test
	public void testListenableFutureCancelAfterSet() {
		SettableFuture<String> future = SettableFuture.create();
		future.set("Some value");

		// cancelling after setting value should not fail
		RxAsserter<String> assertDuring = RxAsserter.on(future);
		future.cancel(true);
		assertDuring.assertTerminal(Optional.empty());

		RxAsserter<String> assertAfter = RxAsserter.on(future);
		assertAfter.assertTerminal(Optional.empty());
	}

	@Test
	public void testCompletableFutureCancellationResult() {
		CompletableFuture<String> future = new CompletableFuture<>();

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.on(future);
		future.cancel(true);
		assertDuring.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.on(future);
		assertAfter.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);
	}

	@Test
	public void testCompletableFutureCancelAfterSet() {
		CompletableFuture<String> future = new CompletableFuture<>();
		future.complete("Some value");

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.on(future);
		future.cancel(true);
		assertDuring.assertTerminal(Optional.empty());

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.on(future);
		assertAfter.assertTerminal(Optional.empty());
	}
}
