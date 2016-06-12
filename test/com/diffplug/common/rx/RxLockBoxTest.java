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

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

public class RxLockBoxTest {
	@Test
	public void testGetSetModify() {
		LockBoxTest.testLockingBehaviorGetSetModify("LockBox", RxLockBox::of);
	}

	@Test
	public void testMappedLock() {
		RxLockBox<Integer> intBox = RxLockBox.of(1);
		RxLockBox<String> strBox = intBox.map(LockBoxTest.intConverter());
		Assert.assertSame(intBox.lock(), strBox.lock());
	}

	@Test
	public void testMappedGetSetModify() {
		Function<String, LockBox<String>> constructor = initial -> {
			Integer initialInt = Integer.parseInt(initial);
			RxLockBox<Integer> box = RxLockBox.of(initialInt);
			return box.map(LockBoxTest.intConverter());
		};
		LockBoxTest.testLockingBehaviorGetSetModify("LockBox mapped", constructor);
	}
}
