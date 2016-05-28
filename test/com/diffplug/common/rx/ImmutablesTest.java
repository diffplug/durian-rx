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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.collect.ImmutableList;
import com.diffplug.common.collect.ImmutableMap;
import com.diffplug.common.collect.ImmutableSet;
import com.diffplug.common.collect.Lists;
import com.diffplug.common.collect.Ordering;

public class ImmutablesTest {
	private List<Integer> zeroTo100() {
		return IntStream.range(0, 100).boxed().collect(Collectors.toList());
	}

	@Test
	public void testToList() {
		Assert.assertEquals(ImmutableList.copyOf(zeroTo100()), zeroTo100().parallelStream().collect(Immutables.toList()));
	}

	@Test
	public void testToSet() {
		ImmutableSet<Integer> set = ImmutableSet.copyOf(zeroTo100());
		List<Integer> zeroTo100x3 = Lists.newArrayList();
		for (int i = 0; i < 3; ++i) {
			zeroTo100x3.addAll(zeroTo100());
		}
		Assert.assertEquals(set, zeroTo100x3.parallelStream().collect(Immutables.toSet()));
	}

	@Test
	public void testToSortedSet() {
		List<Integer> values = zeroTo100();
		Collections.shuffle(values, new Random(0));

		List<Integer> zeroTo100x3 = Lists.newArrayList();
		for (int i = 0; i < 3; ++i) {
			zeroTo100x3.addAll(zeroTo100());
		}
		Assert.assertEquals(ImmutableSet.copyOf(zeroTo100()), zeroTo100x3.parallelStream().collect(Immutables.toSortedSet()));
	}

	@Test
	public void testToSortedSetReverse() {
		List<Integer> hundredToZero = zeroTo100();
		Collections.reverse(hundredToZero);
		Assert.assertEquals(hundredToZero, zeroTo100().parallelStream().collect(Immutables.toSortedSet(Ordering.natural().reverse())).asList());
	}

	@Test
	public void testToMap() {
		ImmutableMap<Integer, Integer> expected = ImmutableMap.of(1, 1);
		Assert.assertEquals(expected, expected.entrySet().parallelStream().collect(Immutables.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	@Test
	public void testToSortedMap() {
		ImmutableMap<Integer, Integer> expected = ImmutableMap.of(1, 1, 2, 2);
		Assert.assertEquals(expected.entrySet().asList(), expected.entrySet().parallelStream().collect(Immutables.toSortedMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().asList());
	}

	@Test
	public void testToSortedMapReversed() {
		ImmutableMap<Integer, Integer> expected = ImmutableMap.of(2, 2, 1, 1);
		Assert.assertEquals(expected.entrySet().asList(), expected.entrySet().parallelStream().collect(Immutables.toSortedMap(Ordering.natural().reverse(), Map.Entry::getKey, Map.Entry::getValue)).entrySet().asList());
	}
}
