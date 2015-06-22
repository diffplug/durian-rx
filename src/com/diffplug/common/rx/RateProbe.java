/*
 * Copyright 2015 DiffPlug
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/** A probe for inspecting rates in reactive systems. */
public class RateProbe {
	private volatile long lastEvent = System.nanoTime();
	private final BehaviorSubject<Long> timestampNano = BehaviorSubject.create();

	/** Fires the rate probe, and returns the elapsed time since the last call in nanoseconds. */
	public long fire() {
		long now = System.nanoTime();
		timestampNano.onNext(now);
		long elapsed = now - lastEvent;
		lastEvent = now;
		return elapsed;
	}

	/** Returns a stream of the average rate in hertz over the last n samples. */
	public Observable<Double> rateHzOverNSamples(int n) {
		return timestampNano.buffer(n).map(RateProbe::toHz);
	}

	/** Returns a stream of the average rate in hertz over the specified time period. */
	public Observable<Double> rateHzOver(long timespan, TimeUnit unit) {
		return timestampNano.buffer(timespan, unit).map(RateProbe::toHz);
	}

	/** Converts a list of System.nanoTime() timestamps into a rate. */
	private static double toHz(List<Long> timestamps) {
		if (timestamps.size() < 2) {
			return 0;
		} else {
			long elapsedNano = timestamps.get(timestamps.size() - 1) - timestamps.get(0);
			return timestamps.size() / (elapsedNano * SECONDS_PER_NANO);
		}
	}

	private static final double SECONDS_PER_NANO = 1.0 / TimeUnit.SECONDS.toNanos(1);
}
