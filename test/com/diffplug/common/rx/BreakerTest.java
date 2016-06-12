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

import org.junit.Test;

public class BreakerTest {
	@Test
	public void testBreaker() {
		RxBox<Integer> wrapped = RxBox.of(1);
		Breaker<Integer> breaker = Breaker.createClosed(wrapped);

		RxAsserter<Integer> wrappedAssert = RxAsserter.on(wrapped);
		RxAsserter<Integer> breakerAssert = RxAsserter.on(breaker);

		wrappedAssert.assertValues(1);
		breakerAssert.assertValues(1);

		breaker.set(2);
		wrappedAssert.assertValues(1, 2);
		breakerAssert.assertValues(1, 2);

		wrapped.set(3);
		wrappedAssert.assertValues(1, 2, 3);
		breakerAssert.assertValues(1, 2, 3);

		breaker.setClosed(false);
		wrapped.set(4);
		wrappedAssert.assertValues(1, 2, 3, 4);
		breakerAssert.assertValues(1, 2, 3);

		breaker.set(5);
		wrappedAssert.assertValues(1, 2, 3, 4);
		breakerAssert.assertValues(1, 2, 3, 5);

		breaker.setClosed(true);
		wrappedAssert.assertValues(1, 2, 3, 4, 5);
		breakerAssert.assertValues(1, 2, 3, 5);

		wrapped.set(6);
		wrappedAssert.assertValues(1, 2, 3, 4, 5, 6);
		breakerAssert.assertValues(1, 2, 3, 5, 6);

		breaker.set(7);
		wrappedAssert.assertValues(1, 2, 3, 4, 5, 6, 7);
		breakerAssert.assertValues(1, 2, 3, 5, 6, 7);
	}
}
