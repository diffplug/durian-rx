/**
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

import rx.subjects.BehaviorSubject;

import com.diffplug.common.base.Box;

public class RxUtil {
	/** Returns the underlying state from a BehaviorSubject. */
	public static <T> T getFromBehavior(BehaviorSubject<T> subject) {
		Box<T> holder = Box.empty();
		// subscribe, which gets the value, and then immediately unsubscribe
		// if we don't unsubscribe, then this subscription hangs around forever
		subject.subscribe(value -> holder.set(value)).unsubscribe();
		// return the value we got from the subscription
		return holder.get();
	}
}
