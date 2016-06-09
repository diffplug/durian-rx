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

import com.diffplug.common.base.Converter;

/** {@link RxBox} and {@link LockBox} in one. */
public interface RxLockBox<T> extends LockBox<T>, RxBox<T> {
	public static <T> RxLockBox<T> of(T value) {
		return new RxLockBoxImp<>(value);
	}

	/** RxLockBox must map to another kind of LockBox. */
	@Override
	default <R> RxLockBox<R> map(Converter<T, R> converter) {
		return new RxLockBoxImp.Mapped<T, R>(this, converter);
	}
}
