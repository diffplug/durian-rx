/*
 * Copyright 2018 DiffPlug
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
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Errors;
import com.diffplug.common.debug.LapTimer;
import com.diffplug.common.debug.ThreadHarness;
import com.diffplug.common.primitives.Ints;

public class LockBoxTest {
	@Test
	public void testLockIdentity() {
		LockBox<Integer> selfBox = LockBox.of(1);
		Assert.assertSame(selfBox, selfBox.lock());

		Object otherLock = new Object();
		LockBox<Integer> otherBox = LockBox.of(1, otherLock);
		Assert.assertSame(otherLock, otherBox.lock());
	}

	@Test
	public void testMappedLockIdentity() {
		LockBox<Integer> intBox = LockBox.of(1);
		LockBox<String> strBox = intBox.map(Ints.stringConverter().reverse());
		Assert.assertSame(intBox.lock(), strBox.lock());

		Object otherLock = new Object();
		Assert.assertSame(otherLock, LockBox.of(1, otherLock).map(Ints.stringConverter().reverse()).lock());
	}

	@Test
	public void testGetSetModify() {
		testLockingBehaviorGetSetModify("LockBox", LockBox::of);
	}

	@Test
	public void testMappedGetSetModify() {
		Function<String, LockBox<String>> constructor = initial -> {
			Integer initialInt = Integer.parseInt(initial);
			LockBox<Integer> box = LockBox.of(initialInt);
			return box.map(Ints.stringConverter().reverse());
		};
		testLockingBehaviorGetSetModify("LockBox mapped", constructor);
	}

	static void testLockingBehaviorGetSetModify(String message, Function<String, LockBox<String>> constructor) {
		testLockingBehavior(message + " get", constructor, box -> box.get(), "12");
		testLockingBehavior(message + " set", constructor, box -> box.set("5"), "5");
		testLockingBehavior(message + " modify", constructor, box -> box.modify(val -> val + "3"), "123");
	}

	static void testLockingBehavior(String message, Function<String, LockBox<String>> constructor, Consumer<LockBox<String>> timed, String expectedResult) {
		LockBox<String> box = constructor.apply("1");
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
		Assert.assertEquals("Wrong delay time for " + message, 0.1, elapsed.get().doubleValue(), 0.05);
		// make sure that the final result was what was expected
		Assert.assertEquals("Wrong result for " + message, expectedResult, box.get());
	}
}
