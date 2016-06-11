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

import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;

import com.diffplug.common.util.concurrent.SettableFuture;

/**
 * This is a simple little test for confirming the behavior of
 * subscribing to stuff.
 */
public class RxAndListenableFutureSemantics {
	@Test
	public void testBehaviorSubjectSubscribe() {
		RxAsserter<String> observer = RxAsserter.create();

		// create an behavior subject, subscribe pre, and pump test through
		BehaviorSubject<String> testSubject = BehaviorSubject.create("initial");
		Rx.subscribe(testSubject, observer);
		// the observer gets the value immediately
		observer.assertValue("initial");

		// call on next, and the observer gets the new value immediately
		testSubject.onNext("value");
		observer.assertValue("value");
	}

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
}
