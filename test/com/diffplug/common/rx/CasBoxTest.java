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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.base.Converter;
import com.diffplug.common.base.Errors;
import com.diffplug.common.debug.ThreadHarness;

public class CasBoxTest {
	@Test
	public void testModifyIsLocked() {
		testRetryingBehavior(box -> box.modify(val -> val + "3"), 2, "132");
	}

	@Test
	public void testSetIsLocked() {
		testRetryingBehavior(box -> box.set("5"), 2, "52");
		testRetryingBehavior(box -> {
			box.set("5");
			Errors.log().run(() -> Thread.sleep(150));
			box.set("4");
		}, 3, "42");
	}

	@Test
	public void testGetIsLocked() {
		testRetryingBehavior(box -> box.get(), 1, "12");
	}

	static void testRetryingBehavior(Consumer<CasBox<String>> timed, int numTimesCalled, String expectedResult) {
		testRetryingBehavior(CasBox.of("1"), timed, numTimesCalled, expectedResult);
	}

	@Test
	public void testMappedModifyIsLocked() {
		testMappedRetryingBehavior(box -> box.modify(val -> val + "3"), 2, "132");
	}

	@Test
	public void testMappedSetIsLocked() {
		testMappedRetryingBehavior(box -> box.set("5"), 2, "52");
		testMappedRetryingBehavior(box -> {
			box.set("5");
			Errors.log().run(() -> Thread.sleep(150));
			box.set("4");
		}, 3, "42");
	}

	@Test
	public void testMappedGetIsLocked() {
		testMappedRetryingBehavior(box -> box.get(), 1, "12");
	}

	static void testMappedRetryingBehavior(Consumer<CasBox<String>> timed, int numTimesCalled, String expectedResult) {
		CasBox<Integer> box = CasBox.of(1);
		testRetryingBehavior(box.map(intConverter()), timed, numTimesCalled, expectedResult);
	}

	static void testRetryingBehavior(CasBox<String> box, Consumer<CasBox<String>> timed, int numTimesCalled, String expectedResult) {
		AtomicInteger timesCalled = new AtomicInteger();
		ThreadHarness harness = new ThreadHarness();
		harness.add(() -> {
			box.modify(val -> {
				timesCalled.incrementAndGet();
				Errors.rethrow().run(() -> Thread.sleep(100));
				return val + "2";
			});
		});
		harness.add(() -> {
			timed.accept(box);
		});
		harness.run();
		// make sure it got called as many times as required
		Assert.assertEquals(numTimesCalled, timesCalled.get());
		// make sure that the final result was what was expected
		Assert.assertEquals(expectedResult, box.get());
	}

	static Converter<Integer, String> intConverter() {
		return Converter.from(i -> Integer.toString(i), Integer::parseInt);
	}
}
