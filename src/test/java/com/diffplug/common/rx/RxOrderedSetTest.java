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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.collect.ImmutableList;
import com.diffplug.common.rx.RxOrderedSet;
import com.diffplug.common.rx.RxOrderedSet.OnDuplicate;

public class RxOrderedSetTest {
	@Test
	public void testDisallowDuplicates() {
		// first and last
		testCase(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(1, 2, 3, 4, 5, 1), OnDuplicate.TAKE_FIRST, Arrays.asList(1, 2, 3, 4, 5));
		testCase(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(1, 2, 3, 4, 5, 1), OnDuplicate.TAKE_LAST, Arrays.asList(2, 3, 4, 5, 1));
	}

	@Test(expected = Exception.class)
	public void testDisallowDuplicatesError() {
		testCase(Arrays.asList(), Arrays.asList(1, 1), OnDuplicate.ERROR, Arrays.asList());
	}

	private void testCase(List<Integer> before, List<Integer> after, OnDuplicate policy, List<Integer> expected) {
		// create the initial list with its policy
		RxOrderedSet<Integer> list = RxOrderedSet.of(ImmutableList.copyOf(before), policy);
		// set the new value
		list.set(ImmutableList.copyOf(after));
		// test the resulting value
		Assert.assertEquals(expected, list.get());
	}
}
