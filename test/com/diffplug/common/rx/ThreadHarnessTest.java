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

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class ThreadHarnessTest {
	@Test
	public void testHarness() {
		testCase(1);
		testCase(5);
		testCase(10);
		testCase(50);
		testCase(100);
	}

	private void testCase(int numThreads) {
		StringBuilder expected = new StringBuilder();
		StringBuffer buffer = new StringBuffer();
		ThreadHarness harness = new ThreadHarness();
		IntStream.range(0, numThreads).forEach(idx -> {
			String asStr = Integer.toString(idx);
			expected.append(asStr);
			harness.add(() -> buffer.append(asStr));
		});
		harness.run();
		Assert.assertEquals(expected.toString(), buffer.toString());
	}
}
