/*
 * Copyright 2020 DiffPlug
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


import io.reactivex.Observable;
import org.junit.Test;

public class RxTest {
	@Test
	public void testMerge() {
		RxBox<Byte> byteBox = RxBox.of((byte) 1);
		RxBox<Short> shortBox = RxBox.of((short) 2);
		RxBox<Integer> intBox = RxBox.of(3);
		RxBox<Long> longBox = RxBox.of(4L);
		RxBox<Float> floatBox = RxBox.of(5.f);
		RxBox<Double> dblBox = RxBox.of(6.);
		Observable<Number> lastBox = Rx.merge(byteBox, shortBox, intBox, longBox, floatBox, dblBox);
		RxAsserter<Number> asserter = RxAsserter.on(lastBox);
		asserter.assertValues((byte) 1, (short) 2, 3, 4L, 5.0f, 6.0);

		shortBox.set((short) -7);
		asserter.assertValues((byte) 1, (short) 2, 3, 4L, 5.0f, 6.0, (short) -7);

		intBox.set(-8);
		asserter.assertValues((byte) 1, (short) 2, 3, 4L, 5.0f, 6.0, (short) -7, -8);
	}
}
