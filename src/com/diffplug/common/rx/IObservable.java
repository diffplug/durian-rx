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

import rx.Observable;

/**
 * An object which can be supplied in an {@link rx.Observable} form.
 * <p>
 * Ideally, {@code rx.Observable} would be an interface, which would make this interface unnecessary.  But
 * so long as it isn't, this (combined with {@link Rx}) makes it fairly seamless to fix this.
 */
public interface IObservable<T> {
	Observable<T> asObservable();
}
