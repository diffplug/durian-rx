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
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.diffplug.common.util.concurrent.SettableFuture;

import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;

/**
 * This is a simple little test for confirming the behavior of
 * subscribing to stuff.
 */
public class RxAndListenableFutureSemantics {
	@Test
	public void testBehaviorSubjectSubscribe() {
		// create an behavior subject, subscribe pre, and pump test through
		BehaviorSubject<String> testSubject = BehaviorSubject.createDefault("initial");
		RxAsserter<String> observer = RxAsserter.on(testSubject);
		// the observer gets the value immediately
		observer.assertValues("initial");

		// call on next, and the observer gets the new value immediately
		testSubject.onNext("value");
		observer.assertValues("initial", "value");
	}

	@Test
	public void testAsyncSubjectSubscribeAfterComplete() {
		// create an async subject, subscribe pre, and pump test through
		AsyncSubject<String> testSubject = AsyncSubject.create();
		RxAsserter<String> preObserver = RxAsserter.on(testSubject);
		testSubject.onNext("test");

		// make sure that no one has observed anything yet
		preObserver.assertValues();

		// when the subject completes, pre should observe but not post
		testSubject.onComplete();
		preObserver.assertValues("test");

		// and if we subscribe after the fact, everyone should get it
		RxAsserter<String> postObserver = RxAsserter.on(testSubject);
		preObserver.assertValues("test");
		postObserver.assertValues("test");
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
