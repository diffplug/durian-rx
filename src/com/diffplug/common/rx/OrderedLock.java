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
import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import com.diffplug.common.collect.Ordering;

/**
 * All code which takes locks using this code is guaranteed to do so
 * in the same order, guaranteeing there won't be a deadlock.
 *
 * {@code OrderedLock} instances can be reused as many times as
 * you'd like, and they're expensive to create and cheap to keep
 * around, so reuse is highly recommended.
 *
 * Thanks to Brian Goetz and Tim Peierls for their great demo for how to handle
 * the case that they have the same hashcode:
 * https://books.google.com/books?id=mzgFCAAAQBAJ&pg=PA208&dq=System.identityHashCode&hl=en&sa=X&ved=0ahUKEwjUvrLcjufMAhWG4IMKHS4NDVwQ6AEIKzAC#v=onepage&q=System.identityHashCode&f=false
 */
public class OrderedLock {
	/** Monitor object for breaking ties for objects with the same hashCode. */
	private static final Object tieBreaker = new Object();

	/** Creates an OrderedLock for the given collection of locks. */
	public static OrderedLock on(Collection<?> locks) {
		return on(locks.toArray());
	}

	/** Creates an OrderedLock for the given array of locks. */
	public static OrderedLock on(Object... locks) {
		// find any duplicates and create an array of Object
		// that contains only the unique locks.
		//
		// duplicate locks don't affect correctness, but they
		// will require taking the tieBreaker object, which
		// would then effectively become a single global lock
		BitSet isDuplicate = new BitSet(locks.length);
		for (int i = 0; i < locks.length; ++i) {
			Object underTest = locks[i];
			for (int j = i + 1; j < locks.length; ++j) {
				if (underTest == locks[j]) {
					isDuplicate.set(i);
					break;
				}
			}
		}
		int numDuplicates = isDuplicate.cardinality();
		Object[] noDuplicates = new Object[locks.length - numDuplicates];
		if (numDuplicates == 0) {
			System.arraycopy(locks, 0, noDuplicates, 0, locks.length);
		} else {
			int idx = 0;
			int i = 0;
			while (idx < noDuplicates.length) {
				if (!isDuplicate.get(i)) {
					noDuplicates[idx] = locks[i];
					++idx;
				}
				++i;
			}
			for (Object lock : noDuplicates) {
				Objects.requireNonNull(lock);
			}
		}
		return new OrderedLock(noDuplicates);
	}

	private final Object[] locks;
	private final boolean needsTieBreaker;

	private OrderedLock(Object[] locks) {
		Arrays.sort(locks, Ordering.natural().onResultOf(System::identityHashCode));
		boolean needsTieBreaker = false;
		// if any of the locks have the same identity hashCode, then we're going to need a tiebreaker
		for (int i = 0; i < locks.length - 1; ++i) {
			if (System.identityHashCode(locks[i]) == System.identityHashCode(locks[i + 1])) {
				needsTieBreaker = true;
				break;
			}
		}
		this.locks = locks;
		this.needsTieBreaker = needsTieBreaker;
	}

	/** Takes the locks in a globally consistent order, then calls the given supplier and returns its value. */
	public <T> T takeAndGet(Supplier<T> toGet) {
		if (needsTieBreaker) {
			synchronized (tieBreaker) {
				return takeAndGet(toGet, 0);
			}
		} else {
			return takeAndGet(toGet, 0);
		}
	}

	private <T> T takeAndGet(Supplier<T> toGet, int i) {
		if (i == locks.length) {
			return toGet.get();
		} else {
			synchronized (locks[i]) {
				return takeAndGet(toGet, i + 1);
			}
		}
	}

	/** Takes the locks in a globally consistent order, then runs the given Runnable. */
	public void takeAndRun(Runnable toRun) {
		takeAndGet(() -> {
			toRun.run();
			return null;
		});
	}

	/** Returns a Runnable which will get and release the appropriate locks before executing its argument. */
	public Runnable wrap(Runnable toWrap) {
		return () -> takeAndRun(toWrap);
	}

	/** Returns a Supplier which will get and release the appropriate locks before executing its argument. */
	public <T> Supplier<T> wrap(Supplier<T> toWrap) {
		return () -> takeAndGet(toWrap);
	}
}
