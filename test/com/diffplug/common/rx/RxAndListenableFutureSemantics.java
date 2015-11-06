/*
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import rx.subjects.AsyncSubject;

import com.google.common.util.concurrent.SettableFuture;

import com.diffplug.common.base.Box.Nullable;

/**
 * This is a simple little test for confirming the behavior of
 * subscribing to a stuff.
 */
public class RxAndListenableFutureSemantics {
	@Test
	public void testAsyncSubjectSubscribeAfterComplete() {
		RxAsserter<String> preObserver = RxAsserter.create();
		RxAsserter<String> postObserver = RxAsserter.create();

		// create an async subject, subscribe pre, and pump test through
		AsyncSubject<String> testSubject = AsyncSubject.create();
		Rx.subscribe(testSubject, preObserver);
		testSubject.subscribe(preObserver);
		testSubject.onNext("test");

		// make sure that no one has observed anything yet
		preObserver.assertValue(null);
		postObserver.assertValue(null);

		// when the subject completes, pre should observe but not post
		testSubject.onCompleted();
		preObserver.assertValue("test");
		postObserver.assertValue(null);

		// and if we subscribe after the fact, everyone should get it
		Rx.subscribe(testSubject, postObserver);
		preObserver.assertValue("test");
		postObserver.assertValue("test");
	}

	@Test
	public void testListenableFutureCancellationResult() {
		SettableFuture<String> future = SettableFuture.create();

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.create();
		Rx.subscribe(future, assertDuring);
		future.cancel(true);
		assertDuring.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.create();
		Rx.subscribe(future, assertAfter);
		assertDuring.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);
	}

	@Test
	public void testListenableFutureCancelAfterSet() {
		SettableFuture<String> future = SettableFuture.create();
		future.set("Some value");

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.create();
		Rx.subscribe(future, assertDuring);
		future.cancel(true);
		assertDuring.assertTerminal(Optional.empty());

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.create();
		Rx.subscribe(future, assertAfter);
		assertDuring.assertTerminal(Optional.empty());
	}

	@Test
	public void testCompletableFutureCancellationResult() {
		CompletableFuture<String> future = new CompletableFuture<>();

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.create();
		Rx.subscribe(future, assertDuring);
		future.cancel(true);
		assertDuring.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.create();
		Rx.subscribe(future, assertAfter);
		assertDuring.assertTerminalExceptionClass(java.util.concurrent.CancellationException.class);
	}

	@Test
	public void testCompletableFutureCancelAfterSet() {
		CompletableFuture<String> future = new CompletableFuture<>();
		future.complete("Some value");

		// subscribe to a future then cancel should terminate with CancellationException
		RxAsserter<String> assertDuring = RxAsserter.create();
		Rx.subscribe(future, assertDuring);
		future.cancel(true);
		assertDuring.assertTerminal(Optional.empty());

		// subscribe to a cancelled future should terminate with CancellationException
		RxAsserter<String> assertAfter = RxAsserter.create();
		Rx.subscribe(future, assertAfter);
		assertDuring.assertTerminal(Optional.empty());
	}

	private static class RxAsserter<T> extends Rx<T> {
		private static <T> RxAsserter<T> create() {
			Nullable<T> value = Nullable.ofNull();
			Nullable<Optional<Throwable>> terminal = Nullable.ofNull();
			return new RxAsserter<T>(val -> value.set(val), ter -> terminal.set(ter), value, terminal);
		}

		private final Nullable<T> value;
		private final Nullable<Optional<Throwable>> terminal;

		private RxAsserter(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminal, Nullable<T> value, Nullable<Optional<Throwable>> terminal) {
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
}
