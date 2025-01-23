/*
 * Copyright (C) 2020-2025 DiffPlug
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

import kotlinx.coroutines.flow.Flow;

/**
 * An object which can be supplied in an {@link io.reactivex.Observable} form.
 *
 * Ideally, `io.reactivex.Observable` would be an interface, which would make this interface unnecessary.  But
 * so long as it isn't, this (combined with {@link Rx}) makes it fairly seamless to fix this.
 */
public interface IFlowable<T> {
	Flow<T> asFlow();

	/** Wraps an actual observable as an IObservable. */
	static <T> IFlowable<T> wrap(Flow<T> observable) {
		return () -> observable;
	}
}
