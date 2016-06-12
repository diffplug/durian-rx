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

import org.junit.Test;

import com.diffplug.common.primitives.Ints;

public class RxBoxTest {
	@Test
	public void testObservable() {
		assertObservableProperties(RxBox::of);
	}

	@Test
	public void testMappedObservable() {
		Function<String, RxBox<String>> constructor = initial -> {
			Integer initialInt = Integer.parseInt(initial);
			RxBox<Integer> box = RxBox.of(initialInt);
			return box.map(Ints.stringConverter().reverse());
		};
		assertObservableProperties(constructor);
	}

	static void assertObservableProperties(Function<String, RxBox<String>> constructor) {
		RxBox<String> box = constructor.apply("1");
		RxBox<Integer> mappedBox = box.map(Ints.stringConverter());
		
		RxAsserter<String> asserter = RxAsserter.on(box);
		RxAsserter<Integer> mapped = RxAsserter.on(mappedBox);
		asserter.assertValues("1");
		mapped.assertValues(1);
		// double-setting doesn't transmit
		box.set("1");
		asserter.assertValues("1");
		mapped.assertValues(1);
		// but setting to a new value does
		box.set("2");
		asserter.assertValues("1", "2");
		mapped.assertValues(1, 2);
		// and setting back
		box.set("1");
		asserter.assertValues("1", "2", "1");
		mapped.assertValues(1, 2, 1);
		// and modify should work too
		box.modify(val -> val + "9");
		asserter.assertValues("1", "2", "1", "19");
		mapped.assertValues(1, 2, 1, 19);
	}
}
