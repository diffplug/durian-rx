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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import com.diffplug.common.base.Errors;

public class OrderedLockTest {
	@Test
	public void takesAbout5Seconds() {
		int numLocks = 5;
		int numThreads = 20;
		int maxSinglePauseMs = 10;
		int maxNumIncrements = 1000;
		Random random = new Random(0);

		ImmutableList<Object> locks = Stream.generate(Object::new)
				.limit(numLocks)
				.collect(Immutables.toList());

		AtomicInteger numIncrements = new AtomicInteger();

		List<Thread> threads = Stream.generate(() -> {
			Thread thread = new Thread(() -> {
				int numIncrementsNow;
				do {
					// take a number of locks, up to (2 * numLocks)
					// possible to take a single lock multiple times
					int locksToTake = random.nextInt(2 * numLocks);
					List<Object> ourLocks = new ArrayList<>(locksToTake);
					for (int i = 0; i < locksToTake; ++i) {
						ourLocks.add(locks.get(random.nextInt(numLocks)));
					}

					// take the locks, and sleep a random amount before incrementing the count
					numIncrementsNow = OrderedLock.on(ourLocks).takeAndGet(() -> {
						Errors.rethrow().run(() -> {
							Thread.sleep(random.nextInt(maxSinglePauseMs));
						});
						return numIncrements.incrementAndGet();
					});
				} while (numIncrementsNow < maxNumIncrements);
			}, "TestThread");
			thread.start();
			return thread;
		})
				.limit(numThreads)
				.collect(Collectors.toList());

		// wait for the threads to finish
		for (Thread thread : threads) {
			Errors.rethrow().run(thread::join);
		}
	}
}
