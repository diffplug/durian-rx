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

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.primitives.Ints;

public class RxLockBoxTest {
	static RxLockBox<String> mappedConstructor(String initial) {
		Integer initialInt = Integer.parseInt(initial);
		RxLockBox<Integer> box = RxLockBox.of(initialInt);
		return box.map(Ints.stringConverter().reverse());
	}

	@Test
	public void testLockIdentity() {
		RxLockBox<Integer> selfBox = RxLockBox.of(1);
		Assert.assertSame(selfBox, selfBox.lock());

		Object otherLock = new Object();
		RxLockBox<Integer> otherBox = RxLockBox.of(1, otherLock);
		Assert.assertSame(otherLock, otherBox.lock());
	}

	@Test
	public void testMappedLockIdentity() {
		RxLockBox<Integer> intBox = RxLockBox.of(1);
		RxLockBox<String> strBox = intBox.map(Ints.stringConverter().reverse());
		Assert.assertSame(intBox.lock(), strBox.lock());

		Object otherLock = new Object();
		Assert.assertSame(otherLock, RxLockBox.of(1, otherLock).map(Ints.stringConverter().reverse()).lock());
	}

	@Test
	public void testGetSetModify() {
		LockBoxTest.testLockingBehaviorGetSetModify("LockBox", RxLockBox::of);
	}

	@Test
	public void testMappedLock() {
		RxLockBox<Integer> intBox = RxLockBox.of(1);
		RxLockBox<String> strBox = intBox.map(Ints.stringConverter().reverse());
		Assert.assertSame(intBox.lock(), strBox.lock());
	}

	@Test
	public void testMappedGetSetModify() {
		LockBoxTest.testLockingBehaviorGetSetModify("LockBox mapped", RxLockBoxTest::mappedConstructor);
	}

	@Test
	public void testObservable() {
		RxBoxTest.assertObservableProperties(RxLockBox::of);
	}

	@Test
	public void testMappedObservable() {
		RxBoxTest.assertObservableProperties(RxLockBoxTest::mappedConstructor);
	}

	@Test
	public void testEnforcedProperties() {
		LockBoxTest.testLockingBehaviorGetSetModify("enforced", RxLockBoxTest::enforcedConstructor);
		LockBoxTest.testLockingBehaviorGetSetModify("mapThenEnforce", RxLockBoxTest::mapThenEnforceConstructor);
		LockBoxTest.testLockingBehaviorGetSetModify("enforceThenMap", RxLockBoxTest::enforceThenMapConstructor);

		RxBoxTest.assertObservableProperties(RxLockBoxTest::mappedConstructor);
		RxBoxTest.assertObservableProperties(RxLockBoxTest::mapThenEnforceConstructor);
		RxBoxTest.assertObservableProperties(RxLockBoxTest::enforceThenMapConstructor);
	}

	static RxLockBox<String> enforcedConstructor(String initial) {
		RxLockBox<String> strBox = RxLockBox.of(initial);
		return strBox.enforce(str -> Integer.toString(Math.abs(Integer.parseInt(str))));
	}

	static RxLockBox<String> mapThenEnforceConstructor(String initial) {
		Integer initialInt = Integer.parseInt(initial);
		RxLockBox<Integer> intBox = RxLockBox.of(initialInt);
		RxLockBox<String> strBox = intBox.map(Ints.stringConverter().reverse());
		return strBox.enforce(str -> Integer.toString(Math.abs(Integer.parseInt(str))));
	}

	static RxLockBox<String> enforceThenMapConstructor(String initial) {
		Integer initialInt = Integer.parseInt(initial);
		RxLockBox<Integer> box = RxLockBox.of(initialInt).enforce(Math::abs);
		return box.map(Ints.stringConverter().reverse());
	}
}
