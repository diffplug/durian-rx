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

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Converter;
import com.diffplug.common.base.Errors;
import com.diffplug.common.debug.LapTimer;

public class LockBoxTest {
	@Test
	public void testModifyIsLocked() {
		testLockingBehavior(box -> box.modify(val -> val + "3"), "123");
	}

	@Test
	public void testSetIsLocked() {
		testLockingBehavior(box -> box.set("5"), "5");
	}

	@Test
	public void testGetIsLocked() {
		testLockingBehavior(box -> box.get(), "12");
	}

	static void testLockingBehavior(Consumer<LockBox<String>> timed, String expectedResult) {
		testLockingBehavior(LockBox.of("1"), timed, expectedResult);
	}

	@Test
	public void testMappedLock() {
		LockBox<Integer> intBox = LockBox.of(1);
		LockBox<String> strBox = intBox.map(intConverter());
		Assert.assertSame(intBox.lock(), strBox.lock());
	}

	@Test
	public void testMappedModifyIsLocked() {
		testMappedLockingBehavior(box -> box.modify(val -> val + "3"), "123");
	}

	@Test
	public void testMappedSetIsLocked() {
		testMappedLockingBehavior(box -> box.set("5"), "5");
	}

	@Test
	public void testMappedGetIsLocked() {
		testMappedLockingBehavior(box -> box.get(), "12");
	}

	static void testMappedLockingBehavior(Consumer<LockBox<String>> timed, String expectedResult) {
		LockBox<Integer> box = LockBox.of(1);
		testLockingBehavior(box.map(intConverter()), timed, expectedResult);
	}

	static void testLockingBehavior(LockBox<String> box, Consumer<LockBox<String>> timed, String expectedResult) {
		Box.Nullable<Double> elapsed = Box.Nullable.of(null);
		ThreadHarness harness = new ThreadHarness();
		harness.add(() -> {
			box.modify(val -> {
				Errors.rethrow().run(() -> Thread.sleep(100));
				return val + "2";
			});
		});
		harness.add(() -> {
			LapTimer timer = LapTimer.createMs();
			timed.accept(box);
			elapsed.set(timer.lap());
		});
		harness.run();
		// make sure that the action which should have been delayed, was delayed for the proper time
		Assert.assertEquals(0.1, elapsed.get().doubleValue(), 0.05);
		// make sure that the final result was what was expected
		Assert.assertEquals(expectedResult, box.get());
	}

	static Converter<Integer, String> intConverter() {
		return Converter.from(i -> Integer.toString(i), Integer::parseInt);
	}
}
