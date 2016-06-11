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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.diffplug.common.base.Errors;
import com.diffplug.common.base.Preconditions;
import com.diffplug.common.base.Throwing;

/**
 * Creates N threads, and promises that each thread will start
 * ~10 ms after the previous one. 
 */
public class ThreadHarness {
	/**
	 * Runs each of the given runnables in their own thread,
	 * each starting about 10ms after the previous, and blocks
	 * until all threads have completed.
	 */
	public static void createAndRun(Throwing.Runnable... allToRun) {
		ThreadHarness harness = new ThreadHarness();
		Arrays.asList(allToRun).forEach(harness::add);
		harness.run();
	}

	List<Throwing.Runnable> allToRun = new ArrayList<>();

	/** Adds a thread to run. */
	public void add(Throwing.Runnable toRun) {
		Objects.requireNonNull(toRun);
		allToRun.add(toRun);
	}

	/** Runs each thread about 10ms after the previous, and blocks until all of them have completed. */
	public void run() {
		List<TestThread> testThreads = IntStream.range(0, allToRun.size())
				.mapToObj(TestThread::new)
				.collect(Collectors.toList());
		for (TestThread testThread : testThreads) {
			testThread.blocker.release();
		}
		for (TestThread testThread : testThreads) {
			Errors.log().run(testThread::join);
		}
	}

	private class TestThread extends Thread {
		final Throwing.Runnable toRun;
		final BlockUntilReleased blocker = new BlockUntilReleased();

		TestThread(int i) {
			toRun = allToRun.get(i);
			start();
		}

		@Override
		public void run() {
			blocker.block();
			Errors.log().run(toRun);
		}
	}

	static class BlockUntilReleased {
		boolean isBlocked = true;
		volatile boolean unblockAcknowledged = false;

		/** Blocks until release is called, and acknowledges the release after the thread has begun to progress. */
		void block() {
			synchronized (this) {
				while (isBlocked) {
					Errors.rethrow().run(this::wait);
				}
			}
			unblockAcknowledged = true;
		}

		/** Releases the thread, and blocks until the blocked thread has begun forward progress. */
		void release() {
			synchronized (this) {
				Preconditions.checkArgument(isBlocked);
				isBlocked = false;
				notifyAll();
			}
			while (!unblockAcknowledged) {
				Errors.rethrow().run(() -> Thread.sleep(WAIT_BETWEEN_UNBLOCK_MS));
			}
		}
	}

	static final int WAIT_BETWEEN_UNBLOCK_MS = 10;
}
