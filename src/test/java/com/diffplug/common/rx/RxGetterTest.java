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


import com.diffplug.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class RxGetterTest {
	@Test
	public void testMap() {
		RxBox<Optional<String>> original = RxBox.of(Optional.empty());
		RxGetter<Boolean> mapped = original.readOnly().map(Optional::isPresent);

		Asserter<Optional<String>> assertOriginal = new Asserter<>(original);
		Asserter<Boolean> mappedOriginal = new Asserter<>(mapped);

		// we shoudl get the initial values 
		assertOriginal.check(Optional.empty());
		mappedOriginal.check(false);

		// nothing should happen because the value is equal to the previoius
		original.set(Optional.empty());
		assertOriginal.check();
		mappedOriginal.check();

		// should see the "A"
		original.set(Optional.of("A"));
		assertOriginal.check(Optional.of("A"));
		mappedOriginal.check(true);

		// should see the "B", but the mapped shoudn't have a change
		original.set(Optional.of("B"));
		assertOriginal.check(Optional.of("B"));
		mappedOriginal.check();

		// if we go back to empty, they should both see it
		original.set(Optional.empty());
		assertOriginal.check(Optional.empty());
		mappedOriginal.check(false);
	}

	private static class Asserter<T> {
		private List<T> newValues = Lists.newArrayList();

		public Asserter(RxGetter<T> getter) {
			Rx.subscribe(getter, newValues::add);
		}

		@SafeVarargs
		public final void check(T... expected) {
			Assert.assertEquals(Arrays.asList(expected), newValues);
			newValues.clear();
		}
	}
}
